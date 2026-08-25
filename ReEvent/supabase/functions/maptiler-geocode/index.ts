// Deploy with JWT verification enabled. Search text and coordinates are never logged.
import { createClient } from "npm:@supabase/supabase-js@2";
import { createGeocodingHandler } from "./handler.ts";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const MAPTILER_API_KEY = Deno.env.get("MAPTILER_API_KEY") ?? "";

if (!SUPABASE_URL || !SUPABASE_ANON_KEY || !SUPABASE_SERVICE_ROLE_KEY || !MAPTILER_API_KEY) {
  console.error("maptiler-geocode is missing server configuration");
}

const handler = createGeocodingHandler({
  apiKey: MAPTILER_API_KEY,
  authenticate: async (authorization) => {
    if (!SUPABASE_URL || !SUPABASE_ANON_KEY) return null;
    const caller = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      global: { headers: { Authorization: authorization } },
      auth: { autoRefreshToken: false, persistSession: false },
    });
    const { data, error } = await caller.auth.getUser();
    return error ? null : data.user?.id ?? null;
  },
  consumeQuota: async (userId) => {
    if (!SUPABASE_URL || !SUPABASE_SERVICE_ROLE_KEY) throw new Error("not configured");
    const admin = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
      auth: { autoRefreshToken: false, persistSession: false },
    });
    const { data, error } = await admin.rpc("consume_geocoding_quota", {
      p_user_id: userId,
      p_max_requests: 30,
    });
    if (error) throw error;
    return data === true;
  },
  logError: (message) => console.error(message),
});

Deno.serve(handler);
