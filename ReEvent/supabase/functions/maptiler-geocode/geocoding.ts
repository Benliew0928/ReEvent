export type GeocodingOperation = "forward" | "reverse";

export type GeocodingRequest = {
  operation: GeocodingOperation;
  query?: string;
  latitude?: number;
  longitude?: number;
};

export type GeocodingSuggestion = {
  id: string;
  label: string;
  latitude: number;
  longitude: number;
};

export function validateRequest(value: unknown): GeocodingRequest | null {
  if (!value || typeof value !== "object") return null;
  const input = value as Record<string, unknown>;
  if (input.operation === "forward") {
    const query = typeof input.query === "string" ? input.query.trim() : "";
    if (query.length < 3 || query.length > 200) return null;
    const request: GeocodingRequest = { operation: "forward", query };
    if (validCoordinates(input.latitude, input.longitude)) {
      request.latitude = input.latitude as number;
      request.longitude = input.longitude as number;
    }
    return request;
  }
  if (input.operation === "reverse" && validCoordinates(input.latitude, input.longitude)) {
    return {
      operation: "reverse",
      latitude: input.latitude as number,
      longitude: input.longitude as number,
    };
  }
  return null;
}

export function mapTilerUrl(request: GeocodingRequest, apiKey: string): URL {
  const lookup = request.operation === "forward"
    ? encodeURIComponent(request.query ?? "")
    : `${request.longitude},${request.latitude}`;
  const url = new URL(`https://api.maptiler.com/geocoding/${lookup}.json`);
  url.searchParams.set("key", apiKey);
  url.searchParams.set("country", "MY");
  url.searchParams.set("language", "en");
  url.searchParams.set("limit", request.operation === "forward" ? "5" : "1");
  if (request.operation === "forward") {
    url.searchParams.set("autocomplete", "true");
    if (request.latitude !== undefined && request.longitude !== undefined) {
      url.searchParams.set("proximity", `${request.longitude},${request.latitude}`);
    }
  }
  return url;
}

export function normalizeFeatures(value: unknown): GeocodingSuggestion[] {
  if (!value || typeof value !== "object") return [];
  const features = (value as { features?: unknown }).features;
  if (!Array.isArray(features)) return [];
  return features.slice(0, 5).flatMap((feature, index) => {
    if (!feature || typeof feature !== "object") return [];
    const record = feature as Record<string, unknown>;
    const center = record.center;
    if (!Array.isArray(center) || center.length < 2) return [];
    const longitude = Number(center[0]);
    const latitude = Number(center[1]);
    const label = typeof record.place_name === "string"
      ? record.place_name.trim()
      : typeof record.text === "string" ? record.text.trim() : "";
    if (!label || !validCoordinates(latitude, longitude)) return [];
    return [{
      id: typeof record.id === "string" ? record.id : `result-${index}`,
      label,
      latitude,
      longitude,
    }];
  });
}

function validCoordinates(latitude: unknown, longitude: unknown): boolean {
  return typeof latitude === "number" && Number.isFinite(latitude) && latitude >= -90 && latitude <= 90 &&
    typeof longitude === "number" && Number.isFinite(longitude) && longitude >= -180 && longitude <= 180;
}
