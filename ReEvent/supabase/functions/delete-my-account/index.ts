// Deploy with JWT verification enabled. The Android app sends only its user JWT; this function
// alone reads SUPABASE_SERVICE_ROLE_KEY and performs privileged Storage/Auth work.
import { createClient } from "npm:@supabase/supabase-js@2";
import { collectStoragePaths } from "./storage-cleanup.ts";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY");
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
const PRIVATE_BUCKETS = ["resource-photos", "event-photos", "partner-logos", "profile-avatars"];

const createServerClient = (url: string, key: string) => createClient(url, key, {
  auth: { autoRefreshToken: false, persistSession: false },
});
type ServerClient = ReturnType<typeof createServerClient>;

type DeletionStatus =
  | "DELETED"
  | "READY_FOR_AUTH_DELETION"
  | "FINALIZATION_PENDING"
  | "FRESH_REAUTHENTICATION_REQUIRED"
  | "PASSWORD_REAUTHENTICATION_UNAVAILABLE"
  | "BLOCKED_ACTIVE_TRANSACTIONS"
  | "BLOCKED_ACTIVE_RESOURCES"
  | "BLOCKED_ACTIVE_EVENTS"
  | "BLOCKED_OPEN_LISTINGS"
  | "BLOCKED_ACTIVE_PROGRAMMES"
  | "BLOCKED_UNSETTLED_COINS";

const json = (body: Record<string, string>, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
  });

async function storagePathsUnderPrefix(
  admin: ServerClient,
  bucket: string,
  prefix: string,
): Promise<string[]> {
  return collectStoragePaths(async (pageBucket, pagePrefix, limit, offset) => {
    const { data, error } = await admin.storage.from(pageBucket).list(pagePrefix, {
      limit,
      offset,
      sortBy: { column: "name", order: "asc" },
    });
    if (error) throw new Error(`Unable to list private objects in ${pageBucket}`);
    return data ?? [];
  }, bucket, prefix);
}

async function removePrivateStorage(admin: ServerClient, userId: string) {
  for (const bucket of PRIVATE_BUCKETS) {
    const paths = await storagePathsUnderPrefix(admin, bucket, userId);
    for (let start = 0; start < paths.length; start += 100) {
      const { error } = await admin.storage.from(bucket).remove(paths.slice(start, start + 100));
      if (error) throw new Error(`Unable to remove private objects in ${bucket}`);
    }
  }
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response(null, { status: 204 });
  if (request.method !== "POST") return json({ status: "METHOD_NOT_ALLOWED" }, 405);
  if (!SUPABASE_URL || !SUPABASE_ANON_KEY || !SUPABASE_SERVICE_ROLE_KEY) {
    console.error("delete-my-account is missing server configuration");
    return json({ status: "SERVICE_NOT_CONFIGURED" }, 500);
  }

  const authorization = request.headers.get("Authorization");
  if (!authorization) return json({ status: "AUTH_REQUIRED" }, 401);
  let currentPassword: string | null = null;
  try {
    const body = await request.json() as { currentPassword?: unknown };
    currentPassword = typeof body.currentPassword === "string" ? body.currentPassword : null;
  } catch {
    return json({ status: "CURRENT_PASSWORD_REQUIRED" }, 400);
  }
  if (!currentPassword || currentPassword.trim().length === 0) return json({ status: "CURRENT_PASSWORD_REQUIRED" }, 400);

  // verify_jwt is enabled at deployment, and getUser verifies identity again before a privileged
  // client is created. The target id is never taken from the request body.
  const caller = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: authorization } },
    auth: { autoRefreshToken: false, persistSession: false },
  });
  const { data: userData, error: userError } = await caller.auth.getUser();
  if (userError || !userData.user) return json({ status: "AUTH_REQUIRED" }, 401);

  // A token issue time does not prove password re-entry because refresh tokens can issue a newer
  // JWT. Verify the submitted password server-side, then require that it authenticates the same
  // account identified by the already-verified caller JWT. Never log the password or request body.
  const providers = new Set<string>([
    typeof userData.user.app_metadata?.provider === "string" ? userData.user.app_metadata.provider : "",
    ...(userData.user.identities ?? []).map((identity) => identity.provider),
  ]);
  if (!userData.user.email || !providers.has("email")) {
    return json({ status: "PASSWORD_REAUTHENTICATION_UNAVAILABLE" });
  }
  const passwordVerifier = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    auth: { autoRefreshToken: false, persistSession: false },
  });
  const { data: reauthenticated, error: reauthenticationError } = await passwordVerifier.auth.signInWithPassword({
    email: userData.user.email,
    password: currentPassword,
  });
  if (reauthenticationError || reauthenticated.user?.id !== userData.user.id) {
    return json({ status: "FRESH_REAUTHENTICATION_REQUIRED" });
  }

  const admin = createServerClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);
  const { data: preparation, error: preparationError } = await admin.rpc("prepare_account_deletion", {
    p_user_id: userData.user.id,
  });
  if (preparationError) {
    console.error("account deletion preparation failed", preparationError.message);
    return json({ status: "DELETION_REQUIRES_RETRY" }, 500);
  }

  const preparationStatus = (preparation as { status?: string } | null)?.status as DeletionStatus | undefined;
  if (!preparationStatus) return json({ status: "DELETION_REQUIRES_RETRY" }, 500);
  if (preparationStatus.startsWith("BLOCKED_")) return json({ status: preparationStatus });
  if (preparationStatus !== "READY_FOR_AUTH_DELETION") return json({ status: "DELETION_REQUIRES_RETRY" }, 500);

  try {
    await removePrivateStorage(admin, userData.user.id);
    const { error: deleteError } = await admin.auth.admin.deleteUser(userData.user.id, false);
    if (deleteError) throw deleteError;
    return json({ status: "DELETED" });
  } catch (error) {
    console.error("account deletion finalisation failed", error instanceof Error ? error.message : "unknown error");
    // The prepared profile remains unable to perform lifecycle actions. A fresh sign-in and retry
    // can safely continue the Storage/Auth finalisation without repeating any visible work.
    return json({ status: "FINALIZATION_PENDING" });
  }
});
