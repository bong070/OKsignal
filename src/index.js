function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}

function badRequest(message) {
  return json({ error: message }, 400);
}

function notFound(message = "Not Found") {
  return json({ error: message }, 404);
}

function methodNotAllowed() {
  return json({ error: "Method Not Allowed" }, 405);
}

function nowIso() {
  return new Date().toISOString();
}

function generateId() {
  return crypto.randomUUID();
}

function generateInviteToken(length = 24) {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789abcdefghijkmnopqrstuvwxyz";
  const bytes = new Uint8Array(length);
  crypto.getRandomValues(bytes);

  let token = "";
  for (let i = 0; i < length; i++) {
    token += chars[bytes[i] % chars.length];
  }
  return token;
}

async function readJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

async function getUserById(db, userId) {
  const result = await db
    .prepare("SELECT * FROM users WHERE id = ?")
    .bind(userId)
    .first();
  return result;
}

async function getInviteByToken(db, token) {
  const result = await db
    .prepare("SELECT * FROM invite_tokens WHERE token = ?")
    .bind(token)
    .first();
  return result;
}

async function handleHealth(env) {
  const result = await env.DB
    .prepare("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
    .all();

  return json({
    ok: true,
    tables: result.results?.map((x) => x.name) ?? [],
  });
}

async function handleRegister(request, env) {
  const body = await readJson(request);
  if (!body) return badRequest("Invalid JSON body");

  const { role, displayName, deviceId, fcmToken } = body;

  if (!role || !["guardian", "member"].includes(role)) {
    return badRequest("role must be 'guardian' or 'member'");
  }

  if (!deviceId || typeof deviceId !== "string") {
    return badRequest("deviceId is required");
  }

  const existingDevice = await env.DB
    .prepare("SELECT * FROM devices WHERE id = ?")
    .bind(deviceId)
    .first();

  const timestamp = nowIso();

  // Device already linked to a user: update device info and return that user
  if (existingDevice) {
    await env.DB
      .prepare(`
        UPDATE devices
        SET fcm_token = ?, updated_at = ?, status = 'active'
        WHERE id = ?
      `)
      .bind(fcmToken ?? null, timestamp, deviceId)
      .run();

    if (displayName && typeof displayName === "string") {
      await env.DB
        .prepare(`
          UPDATE users
          SET display_name = ?, updated_at = ?
          WHERE id = ?
        `)
        .bind(displayName, timestamp, existingDevice.user_id)
        .run();
    }

    const user = await getUserById(env.DB, existingDevice.user_id);

    return json({
      success: true,
      user,
      deviceId,
      isNewUser: false,
    });
  }

  const userId = generateId();

  const statements = [
    env.DB
      .prepare(`
        INSERT INTO users (id, role, display_name, status, created_at, updated_at)
        VALUES (?, ?, ?, 'active', ?, ?)
      `)
      .bind(userId, role, displayName ?? null, timestamp, timestamp),

    env.DB
      .prepare(`
        INSERT INTO devices (id, user_id, platform, fcm_token, status, created_at, updated_at)
        VALUES (?, ?, 'android', ?, 'active', ?, ?)
      `)
      .bind(deviceId, userId, fcmToken ?? null, timestamp, timestamp),

    env.DB
      .prepare(`
        INSERT INTO activity_status (user_id, last_active_at, inactivity_detected_at, current_state, updated_at)
        VALUES (?, NULL, NULL, 'active', ?)
      `)
      .bind(userId, timestamp),
  ];

  await env.DB.batch(statements);

  const user = await getUserById(env.DB, userId);

  return json(
    {
      success: true,
      user,
      deviceId,
      isNewUser: true,
    },
    201
  );
}

async function handleCreateInvite(request, env) {
  const body = await readJson(request);
  if (!body) return badRequest("Invalid JSON body");

  const { guardianUserId, expiresInHours } = body;

  if (!guardianUserId || typeof guardianUserId !== "string") {
    return badRequest("guardianUserId is required");
  }

  const guardian = await getUserById(env.DB, guardianUserId);
  if (!guardian) return notFound("Guardian user not found");
  if (guardian.role !== "guardian") {
    return badRequest("Only guardian users can create invite tokens");
  }

  const token = generateInviteToken(24);
  const createdAt = nowIso();

  let expiresAt = null;
  if (Number.isInteger(expiresInHours) && expiresInHours > 0) {
    expiresAt = new Date(Date.now() + expiresInHours * 60 * 60 * 1000).toISOString();
  }

  await env.DB
    .prepare(`
      INSERT INTO invite_tokens (token, guardian_user_id, status, expires_at, used_at, used_by_user_id, created_at)
      VALUES (?, ?, 'active', ?, NULL, NULL, ?)
    `)
    .bind(token, guardianUserId, expiresAt, createdAt)
    .run();

  return json(
    {
      success: true,
      inviteToken: token,
      inviteLink: `oksignal://invite?token=${encodeURIComponent(token)}`,
      guardianUserId,
      expiresAt,
    },
    201
  );
}

async function handleAcceptInvite(request, env) {
  const body = await readJson(request);
  if (!body) return badRequest("Invalid JSON body");

  const { token, displayName, deviceId, fcmToken } = body;

  if (!token || typeof token !== "string") {
    return badRequest("token is required");
  }

  if (!deviceId || typeof deviceId !== "string") {
    return badRequest("deviceId is required");
  }

  const invite = await getInviteByToken(env.DB, token);
  if (!invite) return notFound("Invite token not found");

  if (invite.status !== "active") {
    return badRequest(`Invite token is not active (status: ${invite.status})`);
  }

  if (invite.expires_at && new Date(invite.expires_at).getTime() < Date.now()) {
    await env.DB
      .prepare("UPDATE invite_tokens SET status = 'expired' WHERE token = ?")
      .bind(token)
      .run();

    return badRequest("Invite token has expired");
  }

  const guardian = await getUserById(env.DB, invite.guardian_user_id);
  if (!guardian) return notFound("Guardian user not found");

  let memberUserId;
  const timestamp = nowIso();

  const existingDevice = await env.DB
    .prepare("SELECT * FROM devices WHERE id = ?")
    .bind(deviceId)
    .first();

  if (existingDevice) {
    const existingUser = await getUserById(env.DB, existingDevice.user_id);
    if (!existingUser) {
      return json({ error: "Device exists but linked user was not found" }, 500);
    }

    if (existingUser.role !== "member") {
      return badRequest("This device is already linked to a non-member user");
    }

    memberUserId = existingUser.id;

    await env.DB
      .prepare(`
        UPDATE devices
        SET fcm_token = ?, updated_at = ?, status = 'active'
        WHERE id = ?
      `)
      .bind(fcmToken ?? null, timestamp, deviceId)
      .run();

    if (displayName && typeof displayName === "string") {
      await env.DB
        .prepare(`
          UPDATE users
          SET display_name = ?, updated_at = ?
          WHERE id = ?
        `)
        .bind(displayName, timestamp, memberUserId)
        .run();
    }
  } else {
    memberUserId = generateId();

    const createStatements = [
      env.DB
        .prepare(`
          INSERT INTO users (id, role, display_name, status, created_at, updated_at)
          VALUES (?, 'member', ?, 'active', ?, ?)
        `)
        .bind(memberUserId, displayName ?? null, timestamp, timestamp),

      env.DB
        .prepare(`
          INSERT INTO devices (id, user_id, platform, fcm_token, status, created_at, updated_at)
          VALUES (?, ?, 'android', ?, 'active', ?, ?)
        `)
        .bind(deviceId, memberUserId, fcmToken ?? null, timestamp, timestamp),

      env.DB
        .prepare(`
          INSERT INTO activity_status (user_id, last_active_at, inactivity_detected_at, current_state, updated_at)
          VALUES (?, NULL, NULL, 'active', ?)
        `)
        .bind(memberUserId, timestamp),
    ];

    await env.DB.batch(createStatements);
  }

  const existingLink = await env.DB
    .prepare(`
      SELECT * FROM guardian_member_links
      WHERE guardian_user_id = ? AND member_user_id = ?
    `)
    .bind(invite.guardian_user_id, memberUserId)
    .first();

  if (!existingLink) {
    await env.DB
      .prepare(`
        INSERT INTO guardian_member_links (id, guardian_user_id, member_user_id, status, created_at)
        VALUES (?, ?, ?, 'active', ?)
      `)
      .bind(generateId(), invite.guardian_user_id, memberUserId, timestamp)
      .run();
  }

  await env.DB
    .prepare(`
      UPDATE invite_tokens
      SET status = 'used',
          used_at = ?,
          used_by_user_id = ?
      WHERE token = ?
    `)
    .bind(timestamp, memberUserId, token)
    .run();

  const member = await getUserById(env.DB, memberUserId);

  return json({
    success: true,
    guardian: {
      id: guardian.id,
      display_name: guardian.display_name,
    },
    member,
    linkCreated: !existingLink,
  });
}

async function handleGetGuardianMembers(request, env) {
  const url = new URL(request.url);
  const guardianUserId = url.searchParams.get("guardianUserId");

  if (!guardianUserId) {
    return badRequest("guardianUserId query parameter is required");
  }

  const guardian = await getUserById(env.DB, guardianUserId);
  if (!guardian) return notFound("Guardian user not found");
  if (guardian.role !== "guardian") {
    return badRequest("User is not a guardian");
  }

  const result = await env.DB
    .prepare(`
      SELECT
        l.id AS link_id,
        l.status AS link_status,
        l.created_at AS linked_at,
        u.id AS member_user_id,
        u.display_name AS member_display_name,
        u.status AS member_status,
        a.current_state AS activity_state,
        a.last_active_at,
        a.inactivity_detected_at
      FROM guardian_member_links l
      JOIN users u
        ON u.id = l.member_user_id
      LEFT JOIN activity_status a
        ON a.user_id = u.id
      WHERE l.guardian_user_id = ?
      ORDER BY l.created_at DESC
    `)
    .bind(guardianUserId)
    .all();

  return json({
    success: true,
    members: result.results ?? [],
  });
}

export default {
  async fetch(request, env) {
    try {
      const url = new URL(request.url);
      const { pathname } = url;

      if (request.method === "GET" && pathname === "/health") {
        return handleHealth(env);
      }

      if (request.method === "POST" && pathname === "/users/register") {
        return handleRegister(request, env);
      }

      if (request.method === "POST" && pathname === "/invites/create") {
        return handleCreateInvite(request, env);
      }

      if (request.method === "POST" && pathname === "/invites/accept") {
        return handleAcceptInvite(request, env);
      }

      if (request.method === "GET" && pathname === "/guardians/members") {
        return handleGetGuardianMembers(request, env);
      }

      const knownPaths = [
        "/health",
        "/users/register",
        "/invites/create",
        "/invites/accept",
        "/guardians/members",
      ];

      if (knownPaths.includes(pathname)) {
        return methodNotAllowed();
      }

      return notFound();
    } catch (err) {
      return json(
        {
          error: "Internal Server Error",
          detail: String(err),
        },
        500
      );
    }
  },
};