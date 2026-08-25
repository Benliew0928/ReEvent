import { mapTilerUrl, normalizeFeatures, validateRequest } from "./geocoding.ts";

export type GeocodingHandlerDependencies = {
  apiKey: string;
  authenticate: (authorization: string) => Promise<string | null>;
  consumeQuota: (userId: string) => Promise<boolean>;
  fetchProvider?: typeof fetch;
  logError?: (message: string) => void;
};

const json = (body: Record<string, unknown>, status = 200) => new Response(JSON.stringify(body), {
  status,
  headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
});

export function createGeocodingHandler(dependencies: GeocodingHandlerDependencies) {
  const fetchProvider = dependencies.fetchProvider ?? fetch;
  const logError = dependencies.logError ?? (() => undefined);
  return async (request: Request): Promise<Response> => {
    if (request.method === "OPTIONS") return new Response(null, { status: 204 });
    if (request.method !== "POST") return json({ error: "METHOD_NOT_ALLOWED" }, 405);
    if (!dependencies.apiKey) return json({ error: "SERVICE_NOT_CONFIGURED" }, 500);

    const authorization = request.headers.get("Authorization");
    if (!authorization) return json({ error: "AUTH_REQUIRED" }, 401);
    const userId = await dependencies.authenticate(authorization);
    if (!userId) return json({ error: "AUTH_REQUIRED" }, 401);

    let input: unknown;
    try {
      input = await request.json();
    } catch {
      return json({ error: "INVALID_REQUEST" }, 400);
    }
    const geocodingRequest = validateRequest(input);
    if (!geocodingRequest) return json({ error: "INVALID_REQUEST" }, 400);

    let allowed: boolean;
    try {
      allowed = await dependencies.consumeQuota(userId);
    } catch {
      logError("maptiler-geocode quota check failed");
      return json({ error: "SERVICE_UNAVAILABLE" }, 503);
    }
    if (!allowed) return json({ error: "RATE_LIMITED" }, 429);

    try {
      const response = await fetchProvider(mapTilerUrl(geocodingRequest, dependencies.apiKey), {
        headers: { "Accept": "application/json", "User-Agent": "ReEvent/1.0" },
      });
      if (!response.ok) {
        logError(`maptiler-geocode provider failed with status ${response.status}`);
        return json({ error: "PROVIDER_UNAVAILABLE" }, 502);
      }
      return json({ suggestions: normalizeFeatures(await response.json()) });
    } catch {
      logError("maptiler-geocode provider request failed");
      return json({ error: "PROVIDER_UNAVAILABLE" }, 502);
    }
  };
}
