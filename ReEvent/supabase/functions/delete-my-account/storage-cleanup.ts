export const STORAGE_LIST_PAGE_SIZE = 1000;

export type StorageListEntry = { name: string; id?: string | null };
export type StoragePageLoader = (
  bucket: string,
  prefix: string,
  limit: number,
  offset: number,
) => Promise<StorageListEntry[]>;

/** Collects every object below a private prefix without assuming that one list page is complete. */
export async function collectStoragePaths(
  loadPage: StoragePageLoader,
  bucket: string,
  prefix: string,
): Promise<string[]> {
  const paths: string[] = [];
  let offset = 0;
  while (true) {
    const page = await loadPage(bucket, prefix, STORAGE_LIST_PAGE_SIZE, offset);
    for (const object of page) {
      const childPath = `${prefix}/${object.name}`;
      if (object.id == null) paths.push(...await collectStoragePaths(loadPage, bucket, childPath));
      else paths.push(childPath);
    }
    if (page.length < STORAGE_LIST_PAGE_SIZE) break;
    offset += page.length;
  }
  return paths;
}
