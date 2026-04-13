# OKSignal API Reference
_Last updated: 2026-04-13_

This reference is based on the current `index.js` implementation and local D1 schema used in development.

## Overview

### Current core concepts
- **Users** are not fixed to a guardian/member role.
- **Direct links** are stored in `guardian_member_links`.
- **Direct invite tokens** are stored in `direct_invites`.
- **Family groups** are stored across `groups`, `group_members`, and `group_invites`.
- **Alerts** are still routed by direct ownership (`guardian_member_links`), not by group membership.
- **Subscription entitlement** is stored in `subscriptions`.

### Current route list
- `GET /health`
- `POST /register`
- `POST /invites/create`
- `POST /invites/accept`
- `GET /guardians/members`
- `GET /guardians/alerts`
- `POST /guardians/primary-member`
- `POST /heartbeat`
- `POST /subscriptions/update`
- `POST /groups/create`
- `POST /groups/invites/create`
- `POST /groups/invites/accept`
- `GET /groups/members`

---

## 1) GET /health

Returns a simple health response and the current SQLite table list.

### Request
No body.

### Example
```http
GET /health
```

### Success response
```json
{
  "success": true,
  "tables": [
    "alerts",
    "devices",
    "direct_invites",
    "group_invites",
    "group_members",
    "groups",
    "guardian_member_links",
    "subscriptions",
    "users"
  ]
}
```

---

## 2) POST /register

Registers a device and user, or updates an existing device-bound user.

### Purpose
- First install: create `users`, `devices`, and a default `free` row in `subscriptions`
- Reinstall / repeat register on same device: update device token and profile fields

### Request body
```json
{
  "device_id": "android-device-001",
  "fcm_token": "optional-fcm-token",
  "display_name": "Brandon",
  "email": "u1@test.com",
  "phone_number": null,
  "device_name": "Pixel 8"
}
```

### Required fields
- `device_id`

### Success response
```json
{
  "success": true,
  "token": "generated-session-token",
  "user": {
    "id": "user-uuid",
    "display_name": "Brandon",
    "email": "u1@test.com",
    "phone_number": null,
    "status": "active",
    "plan_type": "free",
    "subscription_status": "active"
  },
  "is_new_user": true
}
```

### Notes
- The current implementation assumes one `device_id` maps to one user.
- New users automatically receive a `free` subscription row.

---

## 3) POST /invites/create

Creates a **direct invite** token for direct guardian-member style linking.

### Purpose
Used when a user wants to invite another person into their direct monitored list.

### Request body
```json
{
  "guardian_user_id": "u1",
  "expires_in_hours": 168
}
```

You may also send:
```json
{
  "inviter_user_id": "u1",
  "expires_in_hours": 168
}
```

### Required fields
- `guardian_user_id` or `inviter_user_id`

### Plan rules
- `free`: max 1 active direct link and max 1 active direct invite
- `premium` / `family`: no such free-tier limit in the current implementation

### Success response
```json
{
  "success": true,
  "invite_token_id": "invite-uuid",
  "invite_token": "opaque-token",
  "invite_link": "oksignal://invite?token=opaque-token",
  "expires_at": "2026-04-20T03:04:19.055Z"
}
```

### Common errors
- `400`: missing inviter field
- `403`: free plan limit reached
- `404`: inviter user not found
- `400`: inviter user inactive

---

## 4) POST /invites/accept

Accepts a direct invite token and creates a row in `guardian_member_links`.

### Purpose
Turns a direct invite into a direct guardian-member link.

### Request body
```json
{
  "token": "opaque-token",
  "device_id": "android-device-002",
  "fcm_token": "optional-fcm-token",
  "display_name": "Mom",
  "email": "u2@test.com",
  "phone_number": null,
  "device_name": "Galaxy S24"
}
```

### Required fields
- `token`
- `device_id`

### Behavior
- Expires old direct invites first
- Looks up the token in `direct_invites`
- Finds or creates the accepting user
- Finds or creates the accepting device
- Creates `guardian_member_links`
- Marks the token as `used`

### Success response
```json
{
  "success": true,
  "guardian": {
    "id": "u1",
    "display_name": "Brandon",
    "plan_type": "family",
    "subscription_status": "active"
  },
  "member": {
    "id": "u2",
    "display_name": "Mom",
    "email": "u2@test.com",
    "phone_number": null,
    "status": "active",
    "plan_type": "free",
    "subscription_status": "active"
  },
  "link_created": true,
  "member_created": true
}
```

### Important note
The current implementation allows a user to be:
- someone else's linked member, and
- a guardian/inviter for someone else

That is intentional and matches the new schema direction.

---

## 5) GET /guardians/members

Returns direct members for a guardian-style user.

### Purpose
Fetches members from `guardian_member_links`, not from family groups.

### Query params
- `guardian_user_id`

### Example
```http
GET /guardians/members?guardian_user_id=u1
```

### Behavior
- `free`: returns only the primary visible direct member
- non-`free` (`premium` or `family`): returns all active direct members

### Success response
```json
{
  "success": true,
  "guardian": {
    "id": "u1",
    "display_name": "Brandon",
    "plan_type": "family",
    "subscription_status": "active"
  },
  "members": [
    {
      "link_id": "gml1",
      "link_status": "active",
      "is_primary_visible": 1,
      "linked_at": "2026-04-13T...",
      "member_user_id": "u2",
      "member_display_name": "Mom",
      "member_email": "u2@test.com",
      "member_phone_number": null,
      "member_status": "active",
      "device_id": "device-uuid",
      "device_name": "Galaxy S24",
      "last_ping_at": null,
      "last_activity_at": null,
      "last_known_lat": null,
      "last_known_lng": null,
      "last_known_location_at": null
    }
  ]
}
```

### Important note
This endpoint is **direct-link only**.  
It does **not** return group members.

---

## 6) GET /guardians/alerts

Returns alerts for a guardian-style user.

### Query params
- `guardian_user_id`
- `status` = `active` | `resolved` | `all` (default: `active`)

### Example
```http
GET /guardians/alerts?guardian_user_id=u1&status=all
```

### Success response
```json
{
  "success": true,
  "guardian_user_id": "u1",
  "status_filter": "all",
  "alerts": [
    {
      "id": "a1",
      "guardian_user_id": "u1",
      "member_user_id": "u3",
      "member_display_name": "Grandma",
      "member_email": "u3@test.com",
      "member_phone_number": null,
      "reason": "inactivity",
      "status": "active",
      "triggered_at": "2026-04-13T...",
      "metadata": {
        "device_id": "device-uuid",
        "last_activity_at": "2026-04-13T...",
        "last_ping_at": "2026-04-13T..."
      }
    }
  ]
}
```

### Important note
Alerts are driven by `guardian_member_links`, not by `group_members`.

---

## 7) POST /guardians/primary-member

Sets the primary visible direct member for a free-plan guardian-style user.

### Request body
```json
{
  "guardian_user_id": "u1",
  "member_user_id": "u2"
}
```

### Rules
- Only meaningful on `free`
- Returns error for non-free plans

### Success response
```json
{
  "success": true,
  "guardian_user_id": "u1",
  "member_user_id": "u2"
}
```

---

## 8) POST /heartbeat

Stores latest device heartbeat and resolves active inactivity alerts for that member user.

### Request body
```json
{
  "device_id": "android-device-002",
  "last_activity_at": "2026-04-13T03:00:00.000Z",
  "last_known_lat": 43.7615,
  "last_known_lng": -79.4111,
  "last_known_location_at": "2026-04-13T03:00:00.000Z"
}
```

### Required fields
- `device_id`
- `last_activity_at`

### Success response
```json
{
  "success": true,
  "device_id": "android-device-002",
  "member_user_id": "u2",
  "last_ping_at": "2026-04-13T03:10:00.000Z",
  "last_activity_at": "2026-04-13T03:00:00.000Z"
}
```

### Current behavior
- Updates `devices.last_ping_at`
- Updates `devices.last_activity_at`
- Updates latest approximate location fields
- Resolves active inactivity alerts for that member user

---

## 9) POST /subscriptions/update

Temporary testing helper for plan changes. Intended to be replaced later by billing integration.

### Request body
```json
{
  "guardian_user_id": "u1",
  "plan_type": "family"
}
```

You may also send:
```json
{
  "user_id": "u1",
  "plan_type": "premium"
}
```

### Allowed `plan_type`
- `free`
- `premium`
- `family`

### Success response
```json
{
  "success": true,
  "guardian": {
    "id": "u1",
    "display_name": "Brandon",
    "plan_type": "family",
    "subscription_status": "active"
  }
}
```

### Current downgrade behavior
If downgraded to `free`:
- active `direct_invites` are revoked
- first active direct member may be set as primary visible if none is set

---

## 10) POST /groups/create

Creates a family group for a family-plan user and automatically inserts the leader into `group_members`.

### Request body
```json
{
  "leader_user_id": "u1",
  "name": "Song Family"
}
```

### Rules
- Only `family` plan users can create a group
- Current implementation allows only one active group per leader

### Success response
```json
{
  "success": true,
  "group": {
    "id": "group-uuid",
    "leader_user_id": "u1",
    "name": "Song Family",
    "status": "active",
    "created_at": "2026-04-13T..."
  },
  "leader_member_added": true
}
```

### Conflict response
```json
{
  "success": false,
  "error": "Active group already exists for this leader",
  "group_id": "existing-group-id"
}
```

---

## 11) POST /groups/invites/create

Creates a family group invite token.

### Request body
```json
{
  "leader_user_id": "u1",
  "group_id": "grp1",
  "expires_in_hours": 168
}
```

You may also send:
```json
{
  "invited_by_user_id": "u1",
  "group_id": "grp1",
  "expires_in_hours": 168
}
```

### Rules
- Only the group leader can create group invites
- Only family-plan leaders can create them

### Success response
```json
{
  "success": true,
  "group_invite_id": "invite-uuid",
  "invite_token": "opaque-token",
  "invite_link": "oksignal://group-invite?token=opaque-token",
  "expires_at": "2026-04-20T03:04:19.055Z",
  "active_invite_count_for_group": 1
}
```

---

## 12) POST /groups/invites/accept

Accepts a group invite token and adds the user to `group_members`.

### Supported modes
1. **Existing user joins** by passing `user_id`
2. **New user is created during join** when `user_id` is omitted and a new `device_id` is provided

### Request body (new user mode)
```json
{
  "token": "opaque-token",
  "device_id": "dev-u4-new",
  "display_name": "Sibling 2",
  "email": "u5@test.com",
  "device_name": "Pixel Test"
}
```

### Request body (existing user mode)
```json
{
  "token": "opaque-token",
  "user_id": "u2",
  "device_id": "optional-existing-or-new-device-id"
}
```

### Success response
```json
{
  "success": true,
  "group": {
    "id": "grp1",
    "leader_user_id": "u1",
    "name": "Song Family"
  },
  "user": {
    "id": "user-uuid",
    "display_name": "Sibling 2",
    "email": "u5@test.com",
    "phone_number": null,
    "status": "active",
    "plan_type": "free",
    "subscription_status": "active"
  },
  "membership_created": true,
  "user_created": true
}
```

### Current behavior
- Marks the invite as `used`
- Creates a membership if missing
- Creates a new user/device/subscription row when needed
- Returns `membership_created: false` if already in the group

---

## 13) GET /groups/members

Returns the family group member list for the family tab.

### Query params
- `group_id`
- `requester_user_id`

### Example
```http
GET /groups/members?group_id=grp1&requester_user_id=u1
```

### Access rule
Requester must be an active member of that group.

### Success response
```json
{
  "success": true,
  "group": {
    "id": "grp1",
    "leader_user_id": "u1",
    "name": "Song Family",
    "status": "active",
    "created_at": "2026-04-13T05:51:45.000Z"
  },
  "requester_user_id": "u1",
  "members": [
    {
      "group_id": "grp1",
      "user_id": "u1",
      "display_name": "Brandon",
      "email": "u1@test.com",
      "phone_number": null,
      "user_status": "active",
      "membership_status": "active",
      "joined_at": "2026-04-13T05:52:06.000Z",
      "is_group_leader": true,
      "device": null
    }
  ]
}
```

### Error cases
- `400`: missing `group_id` or `requester_user_id`
- `404`: requester not found
- `404`: group not found
- `403`: requester is not an active member of the group

---

## Current behavior summary

### Direct layer
- Invite/create relationship: `direct_invites`
- Ownership and alert routing: `guardian_member_links`

### Group layer
- Family visibility: `groups`, `group_members`, `group_invites`

### Alert layer
- Alerts are created and resolved by direct ownership, not by group membership

### Subscription layer
- `subscriptions` is the current source of truth for plan entitlement
- `free`, `premium`, `family` are the currently supported plan types

---

## Recommended next backend steps
1. Add `GET /groups/me` or `GET /groups/by-user`
2. Add endpoint for leaving/removing group members
3. Add endpoint for listing direct invites and group invites
4. Add stricter transaction safety around multi-step accept flows
5. Replace `/subscriptions/update` with real billing webhook / restore handling
