export default {
  async fetch(request, env) {
    try {
      const url = new URL(request.url);
      const { pathname } = url;

      if (request.method === "GET" && pathname === "/health") {
        return handleHealth(env);
      }
	  
	  if (request.method === "POST" && pathname === "/auth/signup") {
	    return handleAuthSignup(request, env);
	  }

	  if (request.method === "POST" && pathname === "/auth/login") {
	    return handleAuthLogin(request, env);
	  }

      if (request.method === "POST" && pathname === "/register") {
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
	  
	  if (request.method === "GET" && pathname === "/guardians/alerts") {
        return handleGetGuardianAlerts(request, env);
      }

      if (request.method === "POST" && pathname === "/guardians/primary-member") {
        return handleSetGuardianPrimaryMember(request, env);
      }

      if (request.method === "POST" && pathname === "/heartbeat") {
        return handleHeartbeat(request, env);
      }
	  
	  if (request.method === "POST" && pathname === "/groups/create") {
        return handleCreateGroup(request, env);
      }
	  
	  if (request.method === "POST" && pathname === "/groups/invites/create") {
        return handleCreateGroupInvite(request, env);
      }
	  
	  if (request.method === "POST" && pathname === "/groups/invites/accept") {
        return handleAcceptGroupInvite(request, env);
      }
	  
	  if (request.method === "GET" && pathname === "/groups/members") {
        return handleGetGroupMembers(request, env);
      }

      // Temporary helper for local MVP testing. Replace with billing webhook later.
      if (request.method === "POST" && pathname === "/subscriptions/update") {
        return handleSubscriptionUpdate(request, env);
      }

      return json({ success: false, error: "Not Found" }, 404);
    } catch (e) {
      return json(
        {
          success: false,
          error: "Internal Server Error",
          detail: String(e?.message || e),
        },
        500
      );
    }
  },

  async scheduled(_event, env) {
    // MVP placeholder: alert generation will be added next.
	await expireInviteTokens(env);
	await checkInactivityAlerts(env);
  },
};

async function handleHealth(env) {
  const result = await env.DB.prepare(
    "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
  ).all();

  return json({
    success: true,
    tables: result.results?.map((x) => x.name) ?? [],
  });
}

async function handleRegister(request, env) {
  const body = await request.json();
  const {
    device_id,
    fcm_token,
    display_name,
    email,
    phone_number,
    device_name,
  } = body;

  if (!device_id) {
    return json({ success: false, error: "device_id is required" }, 400);
  }

  const now = isoNow();
  const existingDevice = await env.DB
    .prepare(`SELECT * FROM devices WHERE id = ?`)
    .bind(device_id)
    .first();

  if (existingDevice) {
    const existingUser = await getUserById(env, existingDevice.user_id);

    if (!existingUser) {
      return json({ success: false, error: "Existing device has no user" }, 500);
    }

    await env.DB.batch([
      env.DB
        .prepare(`
          UPDATE devices
          SET push_token = ?, device_name = ?, updated_at = ?
          WHERE id = ?
        `)
        .bind(fcm_token ?? null, device_name ?? null, now, device_id),
      env.DB
        .prepare(`
          UPDATE users
          SET display_name = ?, email = ?, phone_number = ?, updated_at = ?
          WHERE id = ?
        `)
        .bind(
          display_name ?? existingUser.display_name ?? null,
          email ?? existingUser.email ?? null,
          phone_number ?? existingUser.phone_number ?? null,
          now,
          existingUser.id
        ),
    ]);

    const refreshedUser = await getUserWithPlan(env, existingUser.id);
    return json({
      success: true,
      token: generateSessionToken(),
      user: serializeUser(refreshedUser),
      is_new_user: false,
    });
  }

  const userId = crypto.randomUUID();

  await env.DB.batch([
    env.DB
      .prepare(`
        INSERT INTO users (
          id, display_name, email, phone_number, status, created_at, updated_at
        )
        VALUES (?, ?, ?, ?, 'active', ?, ?)
      `)
      .bind(
        userId,
        display_name ?? null,
        email ?? null,
        phone_number ?? null,
        now,
        now
      ),
    env.DB
      .prepare(`
        INSERT INTO devices (
          id, user_id, platform, device_name, push_token, created_at, updated_at
        )
        VALUES (?, ?, 'android', ?, ?, ?, ?)
      `)
      .bind(device_id, userId, device_name ?? null, fcm_token ?? null, now, now),
    env.DB
      .prepare(`
        INSERT INTO subscriptions (
          id, user_id, plan_type, status, started_at, created_at, updated_at
        )
        VALUES (?, ?, 'free', 'active', ?, ?, ?)
      `)
      .bind(crypto.randomUUID(), userId, now, now, now),
  ]);

  const user = await getUserWithPlan(env, userId);
  return json({
    success: true,
    token: generateSessionToken(),
    user: serializeUser(user),
    is_new_user: true,
  });
}

async function handleCreateInvite(request, env) {
  const body = await request.json();
  const { guardian_user_id, inviter_user_id, expires_in_hours = 168 } = body;

  const inviterUserId = guardian_user_id ?? inviter_user_id;
  if (!inviterUserId) {
    return json(
      { success: false, error: "guardian_user_id or inviter_user_id is required" },
      400
    );
  }

  const guardian = await requireGuardian(env, inviterUserId);
  if (!guardian.ok) {
    return guardian.response;
  }

  const activeLinkCount = await getActiveLinkCount(env, inviterUserId);

  if (guardian.user.plan_type === "free" && activeLinkCount >= 1) {
    return json(
      {
        success: false,
        error: "Free plan supports only 1 linked member. Upgrade to premium to add another member.",
      },
      403
    );
  }

  const now = isoNow();

  const existingInvite = await env.DB
    .prepare(`
      SELECT id, token, expires_at, created_at, updated_at
      FROM direct_invites
      WHERE inviter_user_id = ?
        AND status = 'active'
        AND expires_at > ?
      ORDER BY created_at DESC
      LIMIT 1
    `)
    .bind(inviterUserId, now)
    .first();

  if (existingInvite) {
    return json({
      success: true,
      reused: true,
      invite_token_id: existingInvite.id,
      invite_token: existingInvite.token,
      invite_link: `oksignal://invite?token=${encodeURIComponent(existingInvite.token)}`,
      expires_at: existingInvite.expires_at,
      created_at: existingInvite.created_at,
    });
  }

  const hours =
    Number.isInteger(expires_in_hours) && expires_in_hours > 0
      ? expires_in_hours
      : 168;

  const expiresAt = new Date(Date.now() + hours * 60 * 60 * 1000).toISOString();
  const tokenId = crypto.randomUUID();
  const token = generateInviteToken();

  await env.DB
    .prepare(`
      INSERT INTO direct_invites (
        id, token, inviter_user_id, status, expires_at, created_at, updated_at
      )
      VALUES (?, ?, ?, 'active', ?, ?, ?)
    `)
    .bind(tokenId, token, inviterUserId, expiresAt, now, now)
    .run();

  return json({
    success: true,
    reused: false,
    invite_token_id: tokenId,
    invite_token: token,
    invite_link: `oksignal://invite?token=${encodeURIComponent(token)}`,
    expires_at: expiresAt,
    created_at: now,
  });
}

async function handleAcceptInvite(request, env) {
  const body = await request.json();
  const {
    token,
    device_id,
    fcm_token,
    display_name,
    email,
    phone_number,
    device_name,
  } = body;

  if (!token || !device_id) {
    return json({ success: false, error: "token and device_id are required" }, 400);
  }

  await expireInviteTokens(env);

  const invite = await env.DB
    .prepare(`SELECT * FROM direct_invites WHERE token = ?`)
    .bind(token)
    .first();

  if (!invite) {
    return json({ success: false, error: "Invite token not found" }, 404);
  }

  if (invite.status !== "active") {
    return json({ success: false, error: `Invite token is ${invite.status}` }, 400);
  }

  const guardianResult = await requireGuardian(env, invite.inviter_user_id);
  if (!guardianResult.ok) {
    return guardianResult.response;
  }
  const guardian = guardianResult.user;

  const currentActiveLinkCount = await getActiveLinkCount(env, guardian.id);
  if (guardian.plan_type === "free" && currentActiveLinkCount >= 1) {
    return json(
      {
        success: false,
        error: "Guardian is on the free plan and cannot link another member.",
      },
      403
    );
  }

  const now = isoNow();
  const existingDevice = await env.DB
    .prepare(`SELECT * FROM devices WHERE id = ?`)
    .bind(device_id)
    .first();

  let memberUserId;
  let memberCreated = false;

  if (existingDevice) {
    memberUserId = existingDevice.user_id;
    const existingMember = await getUserById(env, memberUserId);

    if (!existingMember) {
      return json({ success: false, error: "Existing device has no user" }, 500);
    }

    await env.DB.batch([
      env.DB
        .prepare(`
          UPDATE devices
          SET push_token = ?, device_name = ?, updated_at = ?
          WHERE id = ?
        `)
        .bind(fcm_token ?? null, device_name ?? null, now, device_id),
      env.DB
        .prepare(`
          UPDATE users
          SET display_name = ?, email = ?, phone_number = ?, updated_at = ?
          WHERE id = ?
        `)
        .bind(
          display_name ?? existingMember.display_name ?? null,
          email ?? existingMember.email ?? null,
          phone_number ?? existingMember.phone_number ?? null,
          now,
          memberUserId
        ),
    ]);
  } else {
    memberUserId = crypto.randomUUID();
    memberCreated = true;

    await env.DB.batch([
      env.DB
        .prepare(`
          INSERT INTO users (
            id, display_name, email, phone_number, status, created_at, updated_at
          )
          VALUES (?, ?, ?, ?, 'active', ?, ?)
        `)
        .bind(
          memberUserId,
          display_name ?? null,
          email ?? null,
          phone_number ?? null,
          now,
          now
        ),
      env.DB
        .prepare(`
          INSERT INTO devices (
            id, user_id, platform, device_name, push_token, created_at, updated_at
          )
          VALUES (?, ?, 'android', ?, ?, ?, ?)
        `)
        .bind(device_id, memberUserId, device_name ?? null, fcm_token ?? null, now, now),
      env.DB
        .prepare(`
          INSERT INTO subscriptions (
            id, user_id, plan_type, status, started_at, created_at, updated_at
          )
          VALUES (?, ?, 'free', 'active', ?, ?, ?)
        `)
        .bind(crypto.randomUUID(), memberUserId, now, now, now),
    ]);
  }

  const existingActiveLink = await env.DB
    .prepare(`
      SELECT *
      FROM guardian_member_links
      WHERE guardian_user_id = ?
        AND member_user_id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(guardian.id, memberUserId)
    .first();

  if (existingActiveLink) {
    await markInviteUsed(env, token, memberUserId, now);
    const member = await getUserWithPlan(env, memberUserId);
    return json({
      success: true,
      guardian: summarizeGuardian(guardian),
      member: serializeUser(member),
      link_created: false,
      member_created: false,
    });
  }

  const isPrimaryVisible = currentActiveLinkCount === 0 ? 1 : 0;

  await env.DB.batch([
    env.DB
      .prepare(`
        INSERT INTO guardian_member_links (
          id, guardian_user_id, member_user_id, status, is_primary_visible, created_at, updated_at
        )
        VALUES (?, ?, ?, 'active', ?, ?, ?)
      `)
      .bind(crypto.randomUUID(), guardian.id, memberUserId, isPrimaryVisible, now, now),
    env.DB
      .prepare(`
        UPDATE direct_invites
        SET status = 'used', used_at = ?, used_by_user_id = ?, updated_at = ?
        WHERE token = ?
      `)
      .bind(now, memberUserId, now, token),
  ]);

  const member = await getUserWithPlan(env, memberUserId);
  return json({
    success: true,
    guardian: summarizeGuardian(guardian),
    member: serializeUser(member),
    link_created: true,
    member_created: memberCreated,
  });
}

async function handleGetGuardianMembers(request, env) {
  const url = new URL(request.url);
  const guardianUserId = url.searchParams.get("guardian_user_id");

  if (!guardianUserId) {
    return json({ success: false, error: "guardian_user_id is required" }, 400);
  }

  const guardianResult = await requireGuardian(env, guardianUserId);
  if (!guardianResult.ok) {
    return guardianResult.response;
  }
  const guardian = guardianResult.user;

  const sql = guardian.plan_type !== "free"
    ? `
      SELECT
        l.id AS link_id,
        l.status AS link_status,
        l.is_primary_visible,
        l.created_at AS linked_at,
        u.id AS member_user_id,
        u.display_name AS member_display_name,
        u.email AS member_email,
        u.phone_number AS member_phone_number,
        u.status AS member_status,
        d.id AS device_id,
        d.device_name,
        d.last_ping_at,
        d.last_activity_at,
        d.last_known_lat,
        d.last_known_lng,
        d.last_known_location_at
      FROM guardian_member_links l
      JOIN users u ON u.id = l.member_user_id
      LEFT JOIN devices d ON d.user_id = u.id
      WHERE l.guardian_user_id = ?
        AND l.status = 'active'
      ORDER BY l.is_primary_visible DESC, l.created_at ASC
    `
    : `
      SELECT
        l.id AS link_id,
        l.status AS link_status,
        l.is_primary_visible,
        l.created_at AS linked_at,
        u.id AS member_user_id,
        u.display_name AS member_display_name,
        u.email AS member_email,
        u.phone_number AS member_phone_number,
        u.status AS member_status,
        d.id AS device_id,
        d.device_name,
        d.last_ping_at,
        d.last_activity_at,
        d.last_known_lat,
        d.last_known_lng,
        d.last_known_location_at
      FROM guardian_member_links l
      JOIN users u ON u.id = l.member_user_id
      LEFT JOIN devices d ON d.user_id = u.id
      WHERE l.guardian_user_id = ?
        AND l.status = 'active'
        AND l.is_primary_visible = 1
      ORDER BY l.created_at ASC
      LIMIT 1
    `;

  const result = await env.DB.prepare(sql).bind(guardianUserId).all();

  return json({
    success: true,
    guardian: summarizeGuardian(guardian),
    members: result.results ?? [],
  });
}

async function handleGetGuardianAlerts(request, env) {
  const url = new URL(request.url);
  const guardianUserId = url.searchParams.get("guardian_user_id");
  const status = url.searchParams.get("status") ?? "active";

  if (!guardianUserId) {
    return json({ success: false, error: "guardian_user_id is required" }, 400);
  }

  if (!["active", "resolved", "all"].includes(status)) {
    return json({ success: false, error: "status must be active, resolved, or all" }, 400);
  }

  const guardianResult = await requireGuardian(env, guardianUserId);
  if (!guardianResult.ok) {
    return guardianResult.response;
  }

  let sql = `
    SELECT
      a.id,
      a.guardian_user_id,
      a.member_user_id,
      a.reason,
      a.status,
      a.triggered_at,
      a.metadata_json,
      u.display_name AS member_display_name,
      u.email AS member_email,
      u.phone_number AS member_phone_number
    FROM alerts a
    JOIN users u ON u.id = a.member_user_id
    WHERE a.guardian_user_id = ?
  `;

  if (status !== "all") {
    sql += ` AND a.status = ?`;
  }

  sql += ` ORDER BY a.triggered_at DESC`;

  const stmt = env.DB.prepare(sql);
  const result =
    status === "all"
      ? await stmt.bind(guardianUserId).all()
      : await stmt.bind(guardianUserId, status).all();

  const alerts = (result.results ?? []).map((row) => ({
    id: row.id,
    guardian_user_id: row.guardian_user_id,
    member_user_id: row.member_user_id,
    member_display_name: row.member_display_name,
    member_email: row.member_email,
    member_phone_number: row.member_phone_number,
    reason: row.reason,
    status: row.status,
    triggered_at: normalizeTimestamp(row.triggered_at),
    metadata: normalizeAlertMetadata(safeParseJson(row.metadata_json)),
  }));

  return json({
    success: true,
    guardian_user_id: guardianUserId,
    status_filter: status,
    alerts,
  });
}

async function handleSetGuardianPrimaryMember(request, env) {
  const body = await request.json();
  const { guardian_user_id, member_user_id } = body;

  if (!guardian_user_id || !member_user_id) {
    return json({ success: false, error: "guardian_user_id and member_user_id are required" }, 400);
  }

  const guardianResult = await requireGuardian(env, guardian_user_id);
  if (!guardianResult.ok) {
    return guardianResult.response;
  }
  const guardian = guardianResult.user;

  if (guardian.plan_type !== "free") {
    return json(
      { success: false, error: "Primary member selection is only needed on the free plan" },
      400
    );
  }

  const link = await env.DB
    .prepare(`
      SELECT * FROM guardian_member_links
      WHERE guardian_user_id = ?
        AND member_user_id = ?
        AND status = 'active'
    `)
    .bind(guardian_user_id, member_user_id)
    .first();

  if (!link) {
    return json({ success: false, error: "Active guardian-member link not found" }, 404);
  }

  const now = isoNow();
  await env.DB.batch([
    env.DB
      .prepare(`
        UPDATE guardian_member_links
        SET is_primary_visible = 0, updated_at = ?
        WHERE guardian_user_id = ?
          AND status = 'active'
      `)
      .bind(now, guardian_user_id),
    env.DB
      .prepare(`
        UPDATE guardian_member_links
        SET is_primary_visible = 1, updated_at = ?
        WHERE guardian_user_id = ?
          AND member_user_id = ?
          AND status = 'active'
      `)
      .bind(now, guardian_user_id, member_user_id),
  ]);

  return json({ success: true, guardian_user_id, member_user_id });
}

async function handleHeartbeat(request, env) {
  const body = await request.json();
  const {
    device_id,
    last_activity_at,
    last_known_lat,
    last_known_lng,
    last_known_location_at,
  } = body;

  if (!device_id || !last_activity_at) {
    return json({ success: false, error: "device_id and last_activity_at are required" }, 400);
  }

  const parsedActivityAt = parseIsoDate(last_activity_at);
  if (!parsedActivityAt) {
    return json({ success: false, error: "last_activity_at must be a valid ISO timestamp" }, 400);
  }

  const device = await env.DB
    .prepare(`SELECT * FROM devices WHERE id = ?`)
    .bind(device_id)
    .first();

  if (!device) {
    return json({ success: false, error: "Device not found" }, 404);
  }

  const user = await getUserById(env, device.user_id);
  if (!user) {
    return json({ success: false, error: "User not found for device" }, 404);
  }

  const now = isoNow();
  const locationTimestamp = last_known_location_at ? parseIsoDate(last_known_location_at) : null;

  await env.DB.batch([
    env.DB
      .prepare(`
        UPDATE devices
        SET last_ping_at = ?,
            last_activity_at = ?,
            last_known_lat = ?,
            last_known_lng = ?,
            last_known_location_at = ?,
            updated_at = ?
        WHERE id = ?
      `)
      .bind(
        now,
        parsedActivityAt,
        isFiniteNumber(last_known_lat) ? last_known_lat : null,
        isFiniteNumber(last_known_lng) ? last_known_lng : null,
        locationTimestamp,
        now,
        device_id
      ),

    env.DB
      .prepare(`
        UPDATE alerts
        SET status = 'resolved',
		  resolved_at = ?
        WHERE member_user_id = ?
          AND reason = 'inactivity'
          AND status = 'active'
      `)
      .bind(now, user.id),
  ]);

  return json({
    success: true,
    device_id,
    member_user_id: user.id,
    last_ping_at: now,
    last_activity_at: parsedActivityAt,
  });
}

async function handleSubscriptionUpdate(request, env) {
  const body = await request.json();
  const { guardian_user_id, user_id, plan_type } = body;

  const targetUserId = guardian_user_id ?? user_id;
  if (!targetUserId || !["free", "premium", "family"].includes(plan_type)) {
    return json(
      { success: false, error: "guardian_user_id or user_id and valid plan_type are required" },
      400
    );
  }

  const guardianResult = await requireGuardian(env, targetUserId);
  if (!guardianResult.ok) {
    return guardianResult.response;
  }

  const now = isoNow();
  const currentSubscription = await getActiveSubscription(env, targetUserId);

  if (currentSubscription) {
    await env.DB
      .prepare(`
        UPDATE subscriptions
        SET plan_type = ?, status = 'active', updated_at = ?
        WHERE id = ?
      `)
      .bind(plan_type, now, currentSubscription.id)
      .run();
  } else {
    await env.DB
      .prepare(`
        INSERT INTO subscriptions (
          id, user_id, plan_type, status, started_at, created_at, updated_at
        )
        VALUES (?, ?, ?, 'active', ?, ?, ?)
      `)
      .bind(crypto.randomUUID(), targetUserId, plan_type, now, now, now)
      .run();
  }

  await handleSubscriptionStateChange(env, targetUserId, plan_type, now);

  const refreshed = await getUserWithPlan(env, targetUserId);
  return json({ success: true, guardian: summarizeGuardian(refreshed) });
}

async function handleSubscriptionStateChange(env, guardianUserId, planType, now) {
  if (planType !== "free") {
    return;
  }

  await env.DB
    .prepare(`
      UPDATE direct_invites
      SET status = 'revoked', updated_at = ?
      WHERE inviter_user_id = ?
        AND status = 'active'
    `)
    .bind(now, guardianUserId)
    .run();

  const currentPrimary = await env.DB
    .prepare(`
      SELECT id
      FROM guardian_member_links
      WHERE guardian_user_id = ?
        AND status = 'active'
        AND is_primary_visible = 1
      LIMIT 1
    `)
    .bind(guardianUserId)
    .first();

  if (currentPrimary) {
    return;
  }

  const firstActiveLink = await env.DB
    .prepare(`
      SELECT id
      FROM guardian_member_links
      WHERE guardian_user_id = ?
        AND status = 'active'
      ORDER BY created_at ASC
      LIMIT 1
    `)
    .bind(guardianUserId)
    .first();

  if (!firstActiveLink) {
    return;
  }

  await env.DB.batch([
    env.DB
      .prepare(`
        UPDATE guardian_member_links
        SET is_primary_visible = 0, updated_at = ?
        WHERE guardian_user_id = ?
          AND status = 'active'
      `)
      .bind(now, guardianUserId),
    env.DB
      .prepare(`
        UPDATE guardian_member_links
        SET is_primary_visible = 1, updated_at = ?
        WHERE id = ?
      `)
      .bind(now, firstActiveLink.id),
  ]);
}

async function expireInviteTokens(env) {
  const now = isoNow();
  await env.DB
    .prepare(`
      UPDATE direct_invites
      SET status = 'expired', updated_at = ?
      WHERE status = 'active'
        AND expires_at IS NOT NULL
        AND expires_at <= ?
    `)
    .bind(now, now)
    .run();
}

async function getActiveLinkCount(env, guardianUserId) {
  const row = await env.DB
    .prepare(`
      SELECT COUNT(*) AS count
      FROM guardian_member_links
      WHERE guardian_user_id = ?
        AND status = 'active'
    `)
    .bind(guardianUserId)
    .first();

  return Number(row?.count ?? 0);
}

async function getActiveInviteCount(env, guardianUserId) {
  const row = await env.DB
    .prepare(`
      SELECT COUNT(*) AS count
      FROM direct_invites
      WHERE inviter_user_id = ?
        AND status = 'active'
        AND (expires_at IS NULL OR expires_at > ?)
    `)
    .bind(guardianUserId, isoNow())
    .first();

  return Number(row?.count ?? 0);
}

async function requireGuardian(env, guardianUserId) {
  const guardian = await getUserWithPlan(env, guardianUserId);

  if (!guardian) {
    return { ok: false, response: json({ success: false, error: "User not found" }, 404) };
  }

  if (guardian.status !== "active") {
    return { ok: false, response: json({ success: false, error: "User is not active" }, 400) };
  }

  return { ok: true, user: guardian };
}

async function getUserById(env, userId) {
  return env.DB.prepare(`SELECT * FROM users WHERE id = ?`).bind(userId).first();
}

async function getActiveSubscription(env, userId) {
  return env.DB
    .prepare(`
      SELECT *
      FROM subscriptions
      WHERE user_id = ?
        AND status IN ('active', 'grace_period')
      ORDER BY created_at DESC
      LIMIT 1
    `)
    .bind(userId)
    .first();
}

async function getUserWithPlan(env, userId) {
  const user = await getUserById(env, userId);
  if (!user) return null;

  const subscription = await getActiveSubscription(env, userId);
  return {
    ...user,
    plan_type: subscription?.plan_type ?? "free",
    subscription_status: subscription?.status ?? "active",
  };
}

async function markInviteUsed(env, token, memberUserId, now) {
  await env.DB
    .prepare(`
      UPDATE direct_invites
      SET status = 'used', used_at = ?, used_by_user_id = ?, updated_at = ?
      WHERE token = ?
    `)
    .bind(now, memberUserId, now, token)
    .run();
}

async function checkInactivityAlerts(env) {
  const result = await env.DB.prepare(`
    SELECT
      gml.guardian_user_id,
      gml.member_user_id,
      d.id AS device_id,
      d.last_activity_at,
      d.last_ping_at,
      d.last_known_lat,
      d.last_known_lng,
      d.last_known_location_at
    FROM guardian_member_links gml
    JOIN devices d
      ON d.user_id = gml.member_user_id
    LEFT JOIN alerts a
      ON a.guardian_user_id = gml.guardian_user_id
      AND a.member_user_id = gml.member_user_id
      AND a.reason = 'inactivity'
      AND a.status = 'active'
    WHERE gml.status = 'active'
      AND d.last_activity_at IS NOT NULL
      AND d.last_ping_at IS NOT NULL
      AND (strftime('%s','now') - strftime('%s', d.last_activity_at)) > 4 * 3600
      AND (strftime('%s','now') - strftime('%s', d.last_ping_at)) > 2 * 3600
      AND a.id IS NULL
  `).all();

  for (const row of result.results ?? []) {
    const alertId = crypto.randomUUID();

    const metadata = JSON.stringify({
      device_id: row.device_id,
      last_activity_at: row.last_activity_at,
      last_ping_at: row.last_ping_at,
      last_known_lat: row.last_known_lat,
      last_known_lng: row.last_known_lng,
      last_known_location_at: row.last_known_location_at,
    });

    await env.DB.prepare(`
      INSERT INTO alerts (
        id,
        guardian_user_id,
        member_user_id,
        reason,
        status,
        triggered_at,
        metadata_json
      )
      VALUES (?, ?, ?, 'inactivity', 'active', ?, ?)
    `)
      .bind(
        alertId,
        row.guardian_user_id,
        row.member_user_id,
        isoNow(),
        metadata
      )
      .run();

    console.log(
      `Created inactivity alert: guardian=${row.guardian_user_id}, member=${row.member_user_id}`
    );
  }
}

async function handleCreateGroup(request, env) {
  const body = await request.json();
  const { leader_user_id, name } = body;

  if (!leader_user_id) {
    return json({ success: false, error: "leader_user_id is required" }, 400);
  }

  const leaderResult = await requireGuardian(env, leader_user_id);
  if (!leaderResult.ok) {
    return leaderResult.response;
  }

  const leader = leaderResult.user;

  if (leader.plan_type !== "family") {
    return json(
      { success: false, error: "Only family plan users can create a group" },
      403
    );
  }

  const existingGroup = await env.DB
    .prepare(`
      SELECT *
      FROM groups
      WHERE leader_user_id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(leader_user_id)
    .first();

  if (existingGroup) {
    return json(
      {
        success: false,
        error: "Active group already exists for this leader",
        group_id: existingGroup.id,
      },
      409
    );
  }

  const now = isoNow();
  const groupId = crypto.randomUUID();
  const groupMemberId = crypto.randomUUID();
  const groupName =
    typeof name === "string" && name.trim().length > 0
      ? name.trim()
      : `${leader.display_name ?? "Family"} Group`;

  await env.DB.batch([
    env.DB
      .prepare(`
        INSERT INTO groups (
          id, leader_user_id, name, status, created_at, updated_at
        )
        VALUES (?, ?, ?, 'active', ?, ?)
      `)
      .bind(groupId, leader_user_id, groupName, now, now),

    env.DB
      .prepare(`
        INSERT INTO group_members (
          id, group_id, user_id, status, joined_at, updated_at
        )
        VALUES (?, ?, ?, 'active', ?, ?)
      `)
      .bind(groupMemberId, groupId, leader_user_id, now, now),
  ]);

  return json({
    success: true,
    group: {
      id: groupId,
      leader_user_id,
      name: groupName,
      status: "active",
      created_at: now,
    },
    leader_member_added: true,
  });
}

async function handleCreateGroupInvite(request, env) {
  const body = await request.json();
  const { leader_user_id, invited_by_user_id, group_id, expires_in_hours = 168 } = body;

  const inviterUserId = invited_by_user_id ?? leader_user_id;

  if (!group_id || !inviterUserId) {
    return json(
      { success: false, error: "group_id and leader_user_id or invited_by_user_id are required" },
      400
    );
  }

  const inviterResult = await requireGuardian(env, inviterUserId);
  if (!inviterResult.ok) {
    return inviterResult.response;
  }

  const inviter = inviterResult.user;

  const group = await env.DB
    .prepare(`
      SELECT *
      FROM groups
      WHERE id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(group_id)
    .first();

  if (!group) {
    return json({ success: false, error: "Active group not found" }, 404);
  }

  if (group.leader_user_id !== inviter.id) {
    return json(
      { success: false, error: "Only the group leader can create group invites" },
      403
    );
  }

  if (inviter.plan_type !== "family") {
    return json(
      { success: false, error: "Only family plan users can create group invites" },
      403
    );
  }

  const activeInviteCountRow = await env.DB
    .prepare(`
      SELECT COUNT(*) AS count
      FROM group_invites
      WHERE group_id = ?
        AND status = 'active'
        AND (expires_at IS NULL OR expires_at > ?)
    `)
    .bind(group_id, isoNow())
    .first();

  const activeInviteCount = Number(activeInviteCountRow?.count ?? 0);

  const now = isoNow();
  const hours = Number.isInteger(expires_in_hours) && expires_in_hours > 0 ? expires_in_hours : 168;
  const expiresAt = new Date(Date.now() + hours * 60 * 60 * 1000).toISOString();
  const inviteId = crypto.randomUUID();
  const token = generateInviteToken();

  await env.DB
    .prepare(`
      INSERT INTO group_invites (
        id, token, group_id, invited_by_user_id, status, expires_at, created_at, updated_at
      )
      VALUES (?, ?, ?, ?, 'active', ?, ?, ?)
    `)
    .bind(inviteId, token, group_id, inviter.id, expiresAt, now, now)
    .run();

  return json({
    success: true,
    group_invite_id: inviteId,
    invite_token: token,
    invite_link: `oksignal://group-invite?token=${encodeURIComponent(token)}`,
    expires_at: expiresAt,
    active_invite_count_for_group: activeInviteCount + 1,
  });
}

async function handleAcceptGroupInvite(request, env) {
  const body = await request.json();
  const {
    token,
    user_id,
    device_id,
    fcm_token,
    display_name,
    email,
    phone_number,
    device_name,
  } = body;

  if (!token) {
    return json({ success: false, error: "token is required" }, 400);
  }

  const now = isoNow();

  await expireGroupInviteTokens(env);

  const invite = await env.DB
    .prepare(`
      SELECT *
      FROM group_invites
      WHERE token = ?
      LIMIT 1
    `)
    .bind(token)
    .first();

  if (!invite) {
    return json({ success: false, error: "Group invite token not found" }, 404);
  }

  if (invite.status !== "active") {
    return json({ success: false, error: `Group invite token is ${invite.status}` }, 400);
  }

  const group = await env.DB
    .prepare(`
      SELECT *
      FROM groups
      WHERE id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(invite.group_id)
    .first();

  if (!group) {
    return json({ success: false, error: "Active group not found" }, 404);
  }

  let targetUserId = user_id ?? null;
  let userCreated = false;

  if (targetUserId) {
    const existingUser = await getUserById(env, targetUserId);
    if (!existingUser) {
      return json({ success: false, error: "Provided user_id not found" }, 404);
    }

    if (device_id) {
      const existingDevice = await env.DB
        .prepare(`SELECT * FROM devices WHERE id = ?`)
        .bind(device_id)
        .first();

      if (existingDevice && existingDevice.user_id !== targetUserId) {
        return json(
          { success: false, error: "device_id is already linked to another user" },
          409
        );
      }

      if (existingDevice) {
        await env.DB.batch([
          env.DB
            .prepare(`
              UPDATE devices
              SET push_token = ?, device_name = ?, updated_at = ?
              WHERE id = ?
            `)
            .bind(fcm_token ?? null, device_name ?? null, now, device_id),
          env.DB
            .prepare(`
              UPDATE users
              SET display_name = ?, email = ?, phone_number = ?, updated_at = ?
              WHERE id = ?
            `)
            .bind(
              display_name ?? existingUser.display_name ?? null,
              email ?? existingUser.email ?? null,
              phone_number ?? existingUser.phone_number ?? null,
              now,
              targetUserId
            ),
        ]);
      } else {
        await env.DB.batch([
          env.DB
            .prepare(`
              INSERT INTO devices (
                id, user_id, platform, device_name, push_token, created_at, updated_at
              )
              VALUES (?, ?, 'android', ?, ?, ?, ?)
            `)
            .bind(device_id, targetUserId, device_name ?? null, fcm_token ?? null, now, now),
          env.DB
            .prepare(`
              UPDATE users
              SET display_name = ?, email = ?, phone_number = ?, updated_at = ?
              WHERE id = ?
            `)
            .bind(
              display_name ?? existingUser.display_name ?? null,
              email ?? existingUser.email ?? null,
              phone_number ?? existingUser.phone_number ?? null,
              now,
              targetUserId
            ),
        ]);
      }
    }
  } else {
    if (!device_id) {
      return json({ success: false, error: "device_id is required when user_id is not provided" }, 400);
    }

    const existingDevice = await env.DB
      .prepare(`SELECT * FROM devices WHERE id = ?`)
      .bind(device_id)
      .first();

    if (existingDevice) {
      targetUserId = existingDevice.user_id;
      const existingUser = await getUserById(env, targetUserId);

      if (!existingUser) {
        return json({ success: false, error: "Existing device has no user" }, 500);
      }

      await env.DB.batch([
        env.DB
          .prepare(`
            UPDATE devices
            SET push_token = ?, device_name = ?, updated_at = ?
            WHERE id = ?
          `)
          .bind(fcm_token ?? null, device_name ?? null, now, device_id),
        env.DB
          .prepare(`
            UPDATE users
            SET display_name = ?, email = ?, phone_number = ?, updated_at = ?
            WHERE id = ?
          `)
          .bind(
            display_name ?? existingUser.display_name ?? null,
            email ?? existingUser.email ?? null,
            phone_number ?? existingUser.phone_number ?? null,
            now,
            targetUserId
          ),
      ]);
    } else {
      targetUserId = crypto.randomUUID();
      userCreated = true;

      await env.DB.batch([
        env.DB
          .prepare(`
            INSERT INTO users (
              id, display_name, email, phone_number, status, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, 'active', ?, ?)
          `)
          .bind(
            targetUserId,
            display_name ?? null,
            email ?? null,
            phone_number ?? null,
            now,
            now
          ),
        env.DB
          .prepare(`
            INSERT INTO devices (
              id, user_id, platform, device_name, push_token, created_at, updated_at
            )
            VALUES (?, ?, 'android', ?, ?, ?, ?)
          `)
          .bind(device_id, targetUserId, device_name ?? null, fcm_token ?? null, now, now),
        env.DB
          .prepare(`
            INSERT INTO subscriptions (
              id, user_id, plan_type, status, started_at, created_at, updated_at
            )
            VALUES (?, ?, 'free', 'active', ?, ?, ?)
          `)
          .bind(crypto.randomUUID(), targetUserId, now, now, now),
      ]);
    }
  }

  const existingMembership = await env.DB
    .prepare(`
      SELECT *
      FROM group_members
      WHERE group_id = ?
        AND user_id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(group.id, targetUserId)
    .first();

  if (existingMembership) {
    await env.DB
      .prepare(`
        UPDATE group_invites
        SET status = 'used', used_at = ?, used_by_user_id = ?, updated_at = ?
        WHERE id = ?
      `)
      .bind(now, targetUserId, now, invite.id)
      .run();

    const user = await getUserWithPlan(env, targetUserId);
    return json({
      success: true,
      group: {
        id: group.id,
        leader_user_id: group.leader_user_id,
        name: group.name,
      },
      user: serializeUser(user),
      membership_created: false,
      user_created: false,
    });
  }

  await env.DB.batch([
    env.DB
      .prepare(`
        INSERT INTO group_members (
          id, group_id, user_id, status, joined_at, updated_at
        )
        VALUES (?, ?, ?, 'active', ?, ?)
      `)
      .bind(crypto.randomUUID(), group.id, targetUserId, now, now),
    env.DB
      .prepare(`
        UPDATE group_invites
        SET status = 'used', used_at = ?, used_by_user_id = ?, updated_at = ?
        WHERE id = ?
      `)
      .bind(now, targetUserId, now, invite.id),
  ]);

  const user = await getUserWithPlan(env, targetUserId);

  return json({
    success: true,
    group: {
      id: group.id,
      leader_user_id: group.leader_user_id,
      name: group.name,
    },
    user: serializeUser(user),
    membership_created: true,
    user_created: userCreated,
  });
}

async function expireGroupInviteTokens(env) {
  const now = isoNow();
  await env.DB
    .prepare(`
      UPDATE group_invites
      SET status = 'expired', updated_at = ?
      WHERE status = 'active'
        AND expires_at IS NOT NULL
        AND expires_at <= ?
    `)
    .bind(now, now)
    .run();
}

async function handleGetGroupMembers(request, env) {
  const url = new URL(request.url);
  const groupId = url.searchParams.get("group_id");
  const requesterUserId = url.searchParams.get("requester_user_id");

  if (!groupId) {
    return json({ success: false, error: "group_id is required" }, 400);
  }

  if (!requesterUserId) {
    return json({ success: false, error: "requester_user_id is required" }, 400);
  }

  const requester = await getUserWithPlan(env, requesterUserId);
  if (!requester) {
    return json({ success: false, error: "Requester not found" }, 404);
  }

  const group = await env.DB
    .prepare(`
      SELECT *
      FROM groups
      WHERE id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(groupId)
    .first();

  if (!group) {
    return json({ success: false, error: "Active group not found" }, 404);
  }

  const requesterMembership = await env.DB
    .prepare(`
      SELECT *
      FROM group_members
      WHERE group_id = ?
        AND user_id = ?
        AND status = 'active'
      LIMIT 1
    `)
    .bind(groupId, requesterUserId)
    .first();

  if (!requesterMembership) {
    return json(
      { success: false, error: "Requester is not an active member of this group" },
      403
    );
  }

  const result = await env.DB
    .prepare(`
      SELECT
        gm.group_id,
        gm.user_id,
        gm.status AS membership_status,
        gm.joined_at,
        u.display_name,
        u.email,
        u.phone_number,
        u.status AS user_status,
        d.id AS device_id,
        d.device_name,
        d.last_ping_at,
        d.last_activity_at,
        d.last_known_lat,
        d.last_known_lng,
        d.last_known_location_at
      FROM group_members gm
      JOIN users u
        ON u.id = gm.user_id
      LEFT JOIN devices d
        ON d.user_id = u.id
      WHERE gm.group_id = ?
        AND gm.status = 'active'
      ORDER BY
        CASE WHEN gm.user_id = ? THEN 0 ELSE 1 END,
        u.display_name ASC,
        gm.joined_at ASC
    `)
    .bind(groupId, group.leader_user_id)
    .all();

  const members = (result.results ?? []).map((row) => ({
    group_id: row.group_id,
    user_id: row.user_id,
    display_name: row.display_name,
    email: row.email,
    phone_number: row.phone_number,
    user_status: row.user_status,
    membership_status: row.membership_status,
    joined_at: normalizeTimestamp(row.joined_at),
    is_group_leader: row.user_id === group.leader_user_id,
    device: row.device_id
      ? {
          id: row.device_id,
          name: row.device_name,
          last_ping_at: normalizeTimestamp(row.last_ping_at),
          last_activity_at: normalizeTimestamp(row.last_activity_at),
          last_known_lat: row.last_known_lat,
          last_known_lng: row.last_known_lng,
          last_known_location_at: normalizeTimestamp(row.last_known_location_at),
        }
      : null,
  }));

  return json({
    success: true,
    group: {
      id: group.id,
      leader_user_id: group.leader_user_id,
      name: group.name,
      status: group.status,
      created_at: normalizeTimestamp(group.created_at),
    },
    requester_user_id: requesterUserId,
    members,
  });
}

async function handleAuthSignup(request, env) {
  const body = await request.json();
  const {
    email,
    password,
    display_name,
    device_id,
    fcm_token,
    device_name,
  } = body;

  const normalizedEmail = normalizeEmail(email);

  if (!normalizedEmail) {
    return json({ success: false, error: "email is required" }, 400);
  }

  if (!validatePassword(password)) {
    return json({ success: false, error: "Password must be at least 8 characters" }, 400);
  }

  if (!device_id) {
    return json({ success: false, error: "device_id is required" }, 400);
  }

  const existingUser = await env.DB
    .prepare(`SELECT id FROM users WHERE lower(email) = ?`)
    .bind(normalizedEmail)
    .first();

  if (existingUser) {
    return json({ success: false, error: "Email already exists" }, 409);
  }

  const now = isoNow();
  const userId = crypto.randomUUID();
  const passwordResult = await hashPassword(password);

  await env.DB.batch([
    env.DB
      .prepare(`
        INSERT INTO users (
          id, display_name, email, phone_number, password_hash, password_salt, status, created_at, updated_at
        )
        VALUES (?, ?, ?, NULL, ?, ?, 'active', ?, ?)
      `)
      .bind(
        userId,
        display_name ?? null,
        normalizedEmail,
        passwordResult.hash,
        passwordResult.salt,
        now,
        now
      ),

    env.DB
      .prepare(`
        INSERT OR REPLACE INTO devices (
          id, user_id, platform, device_name, push_token, created_at, updated_at
        )
        VALUES (?, ?, 'android', ?, ?, ?, ?)
      `)
      .bind(device_id, userId, device_name ?? null, fcm_token ?? null, now, now),

    env.DB
      .prepare(`
        INSERT INTO subscriptions (
          id, user_id, plan_type, status, started_at, created_at, updated_at
        )
        VALUES (?, ?, 'free', 'active', ?, ?, ?)
      `)
      .bind(crypto.randomUUID(), userId, now, now, now),
  ]);

  const user = await getUserWithPlan(env, userId);

  return json({
    success: true,
    token: generateSessionToken(),
    user: serializeUser(user),
  });
}

async function handleAuthLogin(request, env) {
  const body = await request.json();
  const { email, password, device_id, fcm_token, device_name } = body;

  const normalizedEmail = normalizeEmail(email);

  if (!normalizedEmail || !password) {
    return json({ success: false, error: "email and password are required" }, 400);
  }

  const user = await env.DB
    .prepare(`
      SELECT u.*, s.plan_type, s.status AS subscription_status
      FROM users u
      LEFT JOIN subscriptions s
        ON s.user_id = u.id
       AND s.status IN ('active', 'grace_period')
      WHERE lower(u.email) = ?
      LIMIT 1
    `)
    .bind(normalizedEmail)
    .first();

  if (!user) {
    return json({ success: false, error: "Invalid email or password" }, 401);
  }

  const passwordOk = await verifyPassword(password, user.password_salt, user.password_hash);

  if (!passwordOk) {
    return json({ success: false, error: "Invalid email or password" }, 401);
  }

  const now = isoNow();

  if (device_id) {
    await env.DB
      .prepare(`
        INSERT OR REPLACE INTO devices (
          id, user_id, platform, device_name, push_token, created_at, updated_at
        )
        VALUES (?, ?, 'android', ?, ?, COALESCE((SELECT created_at FROM devices WHERE id = ?), ?), ?)
      `)
      .bind(
        device_id,
        user.id,
        device_name ?? null,
        fcm_token ?? null,
        device_id,
        now,
        now
      )
      .run();
  }

  return json({
    success: true,
    token: generateSessionToken(),
    user: serializeUser(user),
  });
}

function serializeUser(user) {
  return {
    id: user.id,
    display_name: user.display_name,
    email: user.email,
    phone_number: user.phone_number,
    status: user.status,
    plan_type: user.plan_type ?? "free",
    subscription_status: user.subscription_status ?? "active",
  };
}

function summarizeGuardian(user) {
  return {
    id: user.id,
    display_name: user.display_name,
    plan_type: user.plan_type ?? "free",
    subscription_status: user.subscription_status ?? "active",
  };
}

function generateSessionToken() {
  return crypto.randomUUID().replace(/-/g, "");
}

function generateInviteToken() {
  return crypto.randomUUID().replace(/-/g, "");
}

function parseIsoDate(value) {
  if (typeof value !== "string") return null;
  const timestamp = Date.parse(value);
  if (Number.isNaN(timestamp)) return null;
  return new Date(timestamp).toISOString();
}

function isoNow() {
  return new Date().toISOString();
}

function isFiniteNumber(value) {
  return typeof value === "number" && Number.isFinite(value);
}

function safeParseJson(value) {
  if (typeof value !== "string") return null;

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function normalizeTimestamp(value) {
  if (typeof value !== "string") return value;

  const parsed = Date.parse(value);
  if (!Number.isNaN(parsed)) {
    return new Date(parsed).toISOString();
  }

  const sqliteLike = value.match(
    /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/
  );

  if (sqliteLike) {
    const [, y, mo, d, h, mi, s] = sqliteLike;
    return new Date(`${y}-${mo}-${d}T${h}:${mi}:${s}Z`).toISOString();
  }

  return value;
}

function normalizeAlertMetadata(metadata) {
  if (!metadata || typeof metadata !== "object") return metadata;

  return {
    ...metadata,
    last_activity_at: normalizeTimestamp(metadata.last_activity_at),
    last_ping_at: normalizeTimestamp(metadata.last_ping_at),
    last_known_location_at: normalizeTimestamp(metadata.last_known_location_at),
  };
}

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function validatePassword(password) {
  return typeof password === "string" && password.length >= 8;
}

function bytesToHex(bytes) {
  return [...new Uint8Array(bytes)]
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

function hexToBytes(hex) {
  const bytes = new Uint8Array(hex.length / 2);
  for (let i = 0; i < bytes.length; i++) {
    bytes[i] = parseInt(hex.substr(i * 2, 2), 16);
  }
  return bytes;
}

async function hashPassword(password, saltHex) {
  const encoder = new TextEncoder();
  const salt = saltHex ? hexToBytes(saltHex) : crypto.getRandomValues(new Uint8Array(16));

  const keyMaterial = await crypto.subtle.importKey(
    "raw",
    encoder.encode(password),
    "PBKDF2",
    false,
    ["deriveBits"]
  );

  const derivedBits = await crypto.subtle.deriveBits(
    {
      name: "PBKDF2",
      salt,
      iterations: 100000,
      hash: "SHA-256",
    },
    keyMaterial,
    256
  );

  return {
    salt: bytesToHex(salt),
    hash: bytesToHex(derivedBits),
  };
}

async function verifyPassword(password, saltHex, expectedHash) {
  if (!saltHex || !expectedHash) return false;
  const result = await hashPassword(password, saltHex);
  return result.hash === expectedHash;
}

function json(data, status = 200) {
  return new Response(JSON.stringify(data, null, 2), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}
