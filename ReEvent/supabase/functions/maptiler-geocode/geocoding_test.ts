import { assert, assertEquals } from "jsr:@std/assert@1";
import { mapTilerUrl, normalizeFeatures, validateRequest } from "./geocoding.ts";
import { createGeocodingHandler } from "./handler.ts";

Deno.test("validates forward and reverse requests without accepting partial coordinates", () => {
  assertEquals(validateRequest({ operation: "forward", query: "KL", latitude: 3, longitude: 101 }), null);
  assertEquals(validateRequest({ operation: "reverse", latitude: 3.14 }), null);
  assertEquals(validateRequest({ operation: "forward", query: " Kuala Lumpur " }), {
    operation: "forward",
    query: "Kuala Lumpur",
  });
});

Deno.test("builds Malaysia-scoped provider requests", () => {
  const url = mapTilerUrl({ operation: "forward", query: "Subang", latitude: 3.1, longitude: 101.5 }, "secret");
  assertEquals(url.searchParams.get("country"), "MY");
  assertEquals(url.searchParams.get("limit"), "5");
  assertEquals(url.searchParams.get("proximity"), "101.5,3.1");
});

Deno.test("normalizes only coordinate-bearing features", () => {
  assertEquals(normalizeFeatures({ features: [
    { id: "place.1", place_name: "Kuala Lumpur, Malaysia", center: [101.6869, 3.139] },
    { id: "bad", place_name: "Missing coordinate" },
  ] }), [{
    id: "place.1",
    label: "Kuala Lumpur, Malaysia",
    latitude: 3.139,
    longitude: 101.6869,
  }]);
});

const request = (body: unknown, authorization = "Bearer valid") => new Request("http://localhost/maptiler-geocode", {
  method: "POST",
  headers: { "Content-Type": "application/json", ...(authorization ? { Authorization: authorization } : {}) },
  body: JSON.stringify(body),
});

Deno.test("rejects missing or invalid JWT before quota and provider calls", async () => {
  let quotaCalls = 0;
  let providerCalls = 0;
  const handler = createGeocodingHandler({
    apiKey: "secret",
    authenticate: async () => null,
    consumeQuota: async () => { quotaCalls += 1; return true; },
    fetchProvider: async () => { providerCalls += 1; return Response.json({ features: [] }); },
  });

  assertEquals((await handler(request({ operation: "forward", query: "Kuala Lumpur" }, ""))).status, 401);
  assertEquals((await handler(request({ operation: "forward", query: "Kuala Lumpur" }))).status, 401);
  assertEquals(quotaCalls, 0);
  assertEquals(providerCalls, 0);
});

Deno.test("rate limits before forwarding and returns normalized empty results", async () => {
  let providerCalls = 0;
  const limited = createGeocodingHandler({
    apiKey: "secret",
    authenticate: async () => "user",
    consumeQuota: async () => false,
    fetchProvider: async () => { providerCalls += 1; return Response.json({ features: [] }); },
  });
  assertEquals((await limited(request({ operation: "forward", query: "Kuala Lumpur" }))).status, 429);
  assertEquals(providerCalls, 0);

  const allowed = createGeocodingHandler({
    apiKey: "secret",
    authenticate: async () => "user",
    consumeQuota: async () => true,
    fetchProvider: async () => Response.json({ features: [] }),
  });
  const response = await allowed(request({ operation: "forward", query: "Kuala Lumpur" }));
  assertEquals(response.status, 200);
  assertEquals(await response.json(), { suggestions: [] });
});

Deno.test("provider failures use secret-safe logs", async () => {
  const logs: string[] = [];
  const handler = createGeocodingHandler({
    apiKey: "super-secret-key",
    authenticate: async () => "user",
    consumeQuota: async () => true,
    fetchProvider: async () => new Response("provider body", { status: 503 }),
    logError: (message) => logs.push(message),
  });
  const response = await handler(request({ operation: "reverse", latitude: 3.139, longitude: 101.6869 }));
  assertEquals(response.status, 502);
  assert(logs.length === 1);
  assert(!logs[0].includes("super-secret-key"));
  assert(!logs[0].includes("3.139"));
  assert(!logs[0].includes("101.6869"));
});
