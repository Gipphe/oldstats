import { openDatabase } from "./db/index.js";

/** Fresh isolated in-memory database for a single test (file). */
export function freshDb() {
  return openDatabase(":memory:");
}
