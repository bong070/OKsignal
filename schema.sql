PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  display_name TEXT,
  email TEXT,
  phone_number TEXT,
  password_hash TEXT,
  password_salt TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive')),
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS devices (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  platform TEXT NOT NULL DEFAULT 'android',
  device_name TEXT,
  push_token TEXT,
  last_ping_at TEXT,
  last_activity_at TEXT,
  last_known_lat REAL,
  last_known_lng REAL,
  last_known_location_at TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS direct_invites (
  id TEXT PRIMARY KEY,
  token TEXT NOT NULL UNIQUE,
  inviter_user_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'used', 'expired', 'revoked')),
  expires_at TEXT,
  used_at TEXT,
  used_by_user_id TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (inviter_user_id) REFERENCES users(id),
  FOREIGN KEY (used_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS guardian_member_links (
  id TEXT PRIMARY KEY,
  guardian_user_id TEXT NOT NULL,
  member_user_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'revoked')),
  is_primary_visible INTEGER NOT NULL DEFAULT 0 CHECK (is_primary_visible IN (0, 1)),
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  revoked_at TEXT,
  FOREIGN KEY (guardian_user_id) REFERENCES users(id),
  FOREIGN KEY (member_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS alerts (
  id TEXT PRIMARY KEY,
  guardian_user_id TEXT NOT NULL,
  member_user_id TEXT NOT NULL,
  reason TEXT NOT NULL DEFAULT 'inactivity',
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'resolved')),
  triggered_at TEXT NOT NULL,
  resolved_at TEXT,
  metadata_json TEXT,
  FOREIGN KEY (guardian_user_id) REFERENCES users(id),
  FOREIGN KEY (member_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS subscriptions (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  plan_type TEXT NOT NULL CHECK (plan_type IN ('free', 'premium', 'family')),
  status TEXT NOT NULL CHECK (status IN ('active', 'canceled', 'expired', 'grace_period')),
  started_at TEXT NOT NULL,
  expires_at TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS groups (
  id TEXT PRIMARY KEY,
  leader_user_id TEXT NOT NULL,
  name TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'archived')),
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (leader_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_members (
  id TEXT PRIMARY KEY,
  group_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'left', 'removed')),
  joined_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (group_id) REFERENCES groups(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_invites (
  id TEXT PRIMARY KEY,
  token TEXT NOT NULL UNIQUE,
  group_id TEXT NOT NULL,
  invited_by_user_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'used', 'expired', 'revoked')),
  expires_at TEXT,
  used_at TEXT,
  used_by_user_id TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (group_id) REFERENCES groups(id),
  FOREIGN KEY (invited_by_user_id) REFERENCES users(id),
  FOREIGN KEY (used_by_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_devices_user_id
  ON devices(user_id);

CREATE INDEX IF NOT EXISTS idx_devices_last_ping_at
  ON devices(last_ping_at);

CREATE INDEX IF NOT EXISTS idx_devices_last_activity_at
  ON devices(last_activity_at);

CREATE INDEX IF NOT EXISTS idx_guardian_member_links_guardian_user_id
  ON guardian_member_links(guardian_user_id);

CREATE INDEX IF NOT EXISTS idx_guardian_member_links_member_user_id
  ON guardian_member_links(member_user_id);

CREATE UNIQUE INDEX ux_guardian_member_pair_active
  ON guardian_member_links(guardian_user_id, member_user_id)
  WHERE status = 'active';
  
CREATE UNIQUE INDEX ux_group_members_active
  ON group_members(group_id, user_id)
  WHERE status = 'active';

CREATE INDEX IF NOT EXISTS idx_alerts_guardian_user_id
  ON alerts(guardian_user_id);

CREATE INDEX IF NOT EXISTS idx_alerts_member_user_id
  ON alerts(member_user_id);

CREATE INDEX IF NOT EXISTS idx_alerts_status
  ON alerts(status);
  
CREATE INDEX IF NOT EXISTS idx_group_invites_invited_by_user_id
  ON group_invites(invited_by_user_id);
  
CREATE INDEX IF NOT EXISTS idx_direct_invites_inviter_user_id
  ON direct_invites(inviter_user_id);

CREATE INDEX IF NOT EXISTS idx_direct_invites_status
  ON direct_invites(status);

CREATE INDEX IF NOT EXISTS idx_group_members_group_id
  ON group_members(group_id);

CREATE INDEX IF NOT EXISTS idx_group_invites_group_id
  ON group_invites(group_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email
  ON users(email)
  WHERE email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone_number
  ON users(phone_number)
  WHERE phone_number IS NOT NULL;
  
CREATE UNIQUE INDEX IF NOT EXISTS ux_subscriptions_active_user
  ON subscriptions(user_id)
  WHERE status IN ('active', 'grace_period');

CREATE UNIQUE INDEX IF NOT EXISTS ux_groups_active_leader
  ON groups(leader_user_id)
  WHERE status = 'active';
