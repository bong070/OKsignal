PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  role TEXT NOT NULL CHECK (role IN ('guardian', 'member')),
  display_name TEXT,
  email TEXT,
  phone_number TEXT,
  is_premium INTEGER NOT NULL DEFAULT 0 CHECK (is_premium IN (0, 1)),
  status TEXT NOT NULL DEFAULT 'active',
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS devices (
  id TEXT PRIMARY KEY,                -- deviceId from Android
  user_id TEXT NOT NULL,
  platform TEXT NOT NULL DEFAULT 'android',
  device_name TEXT,
  fcm_token TEXT,
  last_seen_at TEXT,
  status TEXT NOT NULL DEFAULT 'active',
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS invite_tokens (
  token TEXT PRIMARY KEY,
  guardian_user_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'used', 'expired', 'revoked')),
  expires_at TEXT,
  used_at TEXT,
  used_by_user_id TEXT,
  created_at TEXT NOT NULL,
  FOREIGN KEY (guardian_user_id) REFERENCES users(id),
  FOREIGN KEY (used_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS guardian_member_links (
  id TEXT PRIMARY KEY,
  guardian_user_id TEXT NOT NULL,
  member_user_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active',
  created_at TEXT NOT NULL,
  FOREIGN KEY (guardian_user_id) REFERENCES users(id),
  FOREIGN KEY (member_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS activity_status (
  user_id TEXT PRIMARY KEY,
  last_active_at TEXT,
  inactivity_detected_at TEXT,
  current_state TEXT NOT NULL DEFAULT 'active' CHECK (current_state IN ('active', 'inactive', 'alerted')),
  updated_at TEXT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS alerts (
  id TEXT PRIMARY KEY,
  member_user_id TEXT NOT NULL,
  alert_type TEXT NOT NULL DEFAULT 'inactivity',
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open', 'acknowledged', 'resolved')),
  triggered_at TEXT NOT NULL,
  resolved_at TEXT,
  metadata_json TEXT,
  FOREIGN KEY (member_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_devices_user_id
  ON devices(user_id);

CREATE INDEX IF NOT EXISTS idx_devices_last_seen_at
  ON devices(last_seen_at);

CREATE INDEX IF NOT EXISTS idx_invite_tokens_guardian_user_id
  ON invite_tokens(guardian_user_id);

CREATE INDEX IF NOT EXISTS idx_invite_tokens_status
  ON invite_tokens(status);

CREATE INDEX IF NOT EXISTS idx_guardian_member_links_guardian_user_id
  ON guardian_member_links(guardian_user_id);

CREATE INDEX IF NOT EXISTS idx_guardian_member_links_member_user_id
  ON guardian_member_links(member_user_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_guardian_member_active_link
  ON guardian_member_links(guardian_user_id, member_user_id);

CREATE INDEX IF NOT EXISTS idx_alerts_member_user_id
  ON alerts(member_user_id);

CREATE INDEX IF NOT EXISTS idx_alerts_status
  ON alerts(status);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email
  ON users(email);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone_number
  ON users(phone_number);