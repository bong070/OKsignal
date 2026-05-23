# OKSignal API Reference

Last updated: 2026-05-22

## Overview

OKSignal provides REST APIs for:
- user onboarding
- invite workflows
- guardian/member relationships
- heartbeat submission
- inactivity alert retrieval
- family group management

Backend platform:
- Cloudflare Workers
- Cloudflare D1

---

# Current Core Routes

## Health
- `GET /health`

## Authentication / Registration
- `POST /register`

## Direct Relationship Invites
- `POST /invites/create`
- `POST /invites/accept`

## Guardian APIs
- `GET /guardians/members`
- `GET /guardians/alerts`
- `POST /guardians/primary-member`

## Heartbeat & Activity
- `POST /heartbeat`

## Subscription APIs
- `POST /subscriptions/update`

## Family Group APIs
- `POST /groups/create`
- `POST /groups/invites/create`
- `POST /groups/invites/accept`
- `GET /groups/members`

---

# Design Notes

## Relationship-Based Ownership

Users are not permanently assigned a guardian/member role.

Direct care responsibility is stored in:
- `guardian_member_links`

Family visibility is stored separately through:
- `groups`
- `group_members`

---

# Heartbeat Model

The Android client periodically submits heartbeat payloads.

Example payload:

```json
{
  "device_id": "android-device-001",
  "last_activity_at": "2026-05-22T17:30:00Z",
  "battery_level": 78,
  "approximate_location": {
    "lat": 43.6532,
    "lng": -79.3832
  }
}
```

The backend uses:
- `last_activity_at`
- inactivity threshold
- grace period rules

to determine whether alerts should be generated.

Heartbeat arrival itself does not imply user activity.

---

# Alert Model

Alerts include:
- active state
- resolved state
- created timestamp
- resolved timestamp
- guardian owner
- monitored member

Duplicate active alerts are prevented.

---

# Security & Privacy Notes

The MVP intentionally minimizes stored personal data.

Current design goals:
- no continuous location history
- latest approximate location only
- minimal activity retention
- relationship-scoped visibility

---

# Development Status

The API is currently under active MVP development.

Endpoints and payload structures may evolve during ongoing product iteration.
