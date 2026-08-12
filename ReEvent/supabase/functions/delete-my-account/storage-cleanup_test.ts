import { assertEquals } from "jsr:@std/assert@1";
import { collectStoragePaths, type StoragePageLoader } from "./storage-cleanup.ts";

Deno.test("collectStoragePaths paginates past 1000 objects", async () => {
  const entries = Array.from({ length: 1001 }, (_, index) => ({
    id: `id-${index}`,
    name: `photo-${index}.jpg`,
  }));
  const offsets: number[] = [];
  const loadPage: StoragePageLoader = (_bucket, _prefix, limit, offset) => {
    offsets.push(offset);
    return Promise.resolve(entries.slice(offset, offset + limit));
  };

  const paths = await collectStoragePaths(loadPage, "resource-photos", "user-id");

  assertEquals(paths.length, 1001);
  assertEquals(paths[0], "user-id/photo-0.jpg");
  assertEquals(paths[1000], "user-id/photo-1000.jpg");
  assertEquals(offsets, [0, 1000]);
});

Deno.test("collectStoragePaths recursively paginates folder prefixes", async () => {
  const folderEntries = Array.from({ length: 1001 }, (_, index) => ({
    id: `nested-${index}`,
    name: `nested-${index}.webp`,
  }));
  const calls: Array<{ prefix: string; offset: number }> = [];
  const loadPage: StoragePageLoader = (_bucket, prefix, limit, offset) => {
    calls.push({ prefix, offset });
    if (prefix === "user-id") {
      return Promise.resolve(offset === 0 ? [{ id: null, name: "resources" }] : []);
    }
    return Promise.resolve(folderEntries.slice(offset, offset + limit));
  };

  const paths = await collectStoragePaths(loadPage, "resource-photos", "user-id");

  assertEquals(paths.length, 1001);
  assertEquals(paths[1000], "user-id/resources/nested-1000.webp");
  assertEquals(calls, [
    { prefix: "user-id", offset: 0 },
    { prefix: "user-id/resources", offset: 0 },
    { prefix: "user-id/resources", offset: 1000 },
  ]);
});
