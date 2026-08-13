#!/usr/bin/env node

import { readFile, readdir, stat } from "node:fs/promises";
import { extname, join, relative, resolve } from "node:path";

const workspace = process.cwd();
const requestedRoots = process.argv.slice(2);
const roots = (requestedRoots.length
  ? requestedRoots
  : ["AGENTS.md", "docs", "scripts", "ReEvent", "ReEventWebsite"])
  .map((entry) => resolve(workspace, entry));

const excludedDirectories = new Set([
  ".git",
  ".gradle",
  ".next",
  ".vinext",
  ".wrangler",
  "archive",
  "build",
  "dist",
  "node_modules",
  "out",
  "outputs",
  "work",
]);

const textExtensions = new Set([
  ".css",
  ".gradle",
  ".js",
  ".json",
  ".kt",
  ".kts",
  ".md",
  ".mjs",
  ".properties",
  ".sql",
  ".toml",
  ".ts",
  ".tsx",
  ".xml",
]);

const windows1252Continuation =
  "[\\u0080-\\u00BF\\u0152\\u0153\\u0160\\u0161\\u0178\\u017D\\u017E" +
  "\\u0192\\u02C6\\u02DC\\u2013\\u2014\\u2018-\\u201A\\u201C-\\u201E" +
  "\\u2020-\\u2022\\u2026\\u2030\\u2039\\u203A\\u20AC\\u2122]";
const suspicious = new RegExp(
  [
    "\\uFFFD",
    "[\\u0080-\\u009F]",
    `\\u00C2${windows1252Continuation}`,
    `\\u00C3${windows1252Continuation}`,
    `\\u00E2${windows1252Continuation}`,
    `\\u00F0${windows1252Continuation}`,
    "\\u00EF\\u00BB\\u00BF",
  ].join("|"),
  "u",
);
const failures = [];

async function collect(path) {
  let details;
  try {
    details = await stat(path);
  } catch {
    return [];
  }
  if (details.isFile()) return [path];
  if (!details.isDirectory()) return [];

  const entries = await readdir(path, { withFileTypes: true });
  const children = [];
  for (const entry of entries) {
    if (entry.isDirectory() && excludedDirectories.has(entry.name)) continue;
    children.push(...await collect(join(path, entry.name)));
  }
  return children;
}

for (const file of (await Promise.all(roots.map(collect))).flat()) {
  if (!textExtensions.has(extname(file).toLowerCase())) continue;
  const contents = await readFile(file, "utf8");
  contents.split(/\r?\n/u).forEach((line, index) => {
    if (suspicious.test(line)) {
      failures.push(`${relative(workspace, file)}:${index + 1}: ${line.trim()}`);
    }
  });
}

if (failures.length) {
  console.error("Possible UTF-8/mojibake problems found:");
  failures.forEach((failure) => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log("Encoding check passed: no mojibake markers found in active text files.");
}
