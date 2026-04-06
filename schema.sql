CREATE TABLE users (
  id TEXT PRIMARY KEY,
  role TEXT NOT NULL, -- guardian / member
  display_name TEXT,
  status TEXT NOT NULL DEFAULT 'active',
  created_at TEXT NOT NULL
);