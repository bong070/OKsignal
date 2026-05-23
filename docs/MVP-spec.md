# OKSignal MVP Specification

Last updated: 2026-05-22

## Product Vision

OKSignal is a privacy-focused family safety platform designed to notify trusted guardians when a family member appears inactive for an extended period of time.

The product intentionally avoids invasive continuous tracking.
Instead, the system stores only minimal state required for inactivity detection and alert routing.

---

# Core Product Principles

1. Privacy-first architecture
2. Minimal retained user data
3. Relationship-based visibility
4. Lightweight mobile background activity
5. AI-assisted rapid iteration and development

---

# User Roles

Users do not have permanent fixed roles.

A single user may:
- monitor another user
- be monitored by another user
- belong to a family group
- later become a family group owner

Relationships are modeled independently from account identity.

---

# Subscription Model

## Free
- 1 direct monitored member
- Fixed inactivity threshold

## Premium
- Unlimited direct monitored members
- Configurable inactivity threshold

## Family
- Shared family visibility group
- Group-wide member visibility
- Group owner manages invites

---

# Inactivity Model

The Android client locally tracks:
- latest activity timestamp
- latest heartbeat timestamp

Examples of activity:
- device unlock
- meaningful foreground usage
- manual "I'm OK" confirmation

The backend evaluates inactivity using:
- `last_activity_at`
- configured inactivity threshold
- grace period window

Heartbeat arrival alone does not automatically imply user activity.

---

# Alert Lifecycle

## Flow

1. Device becomes inactive
2. Scheduled backend job detects threshold violation
3. Alert created
4. Guardian notified
5. Alert resolved after activity resumes

## Rules

- duplicate active alerts prevented
- alerts maintain active/resolved state
- alert ownership tied to direct guardian relationship

---

# Backend Architecture

## Platform

- Cloudflare Workers
- Cloudflare D1
- Scheduled cron jobs
- REST APIs

## Main Entities

- users
- subscriptions
- devices
- guardian_member_links
- direct_invites
- groups
- group_members
- alerts

---

# Mobile Architecture

## Android Stack

- Kotlin
- Jetpack Compose
- WorkManager
- DataStore
- Firebase Cloud Messaging

## Responsibilities

- heartbeat submission
- local activity tracking
- guardian/member UI
- alert handling
- onboarding flows

---

# Current MVP Scope

## Included

- Guardian/member invite flow
- Family visibility groups
- Heartbeat tracking
- Inactivity detection
- Alert creation and resolution
- Android client MVP

## Deferred

- iOS client
- advanced analytics
- historical reporting
- wearable integrations
- cloud backup
