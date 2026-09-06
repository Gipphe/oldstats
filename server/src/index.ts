import { db } from "./db/index.js";
import { createApp } from "./app.js";

const app = createApp(db);

const port = Number(process.env.PORT ?? 4000);
app.listen(port, () => {
  console.log(`oldstats-server listening on http://localhost:${port}`);
});
