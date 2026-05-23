# OKSignal
- Direct guardian-member relationships
- Family group visibility support
- Subscription-aware relationship limits

## Inactivity Monitoring
- Device heartbeat tracking
- Local activity timestamp updates
- Configurable inactivity thresholds
- Grace-period based alerting

## Alert System
- Active / resolved alert lifecycle
- Duplicate alert prevention
- Guardian-specific routing
- Push notification integration

## Privacy-First Design
- Latest approximate location only
- No historical location tracking
- Minimal retained activity data
- Relationship-based access control

---

# Architecture Notes

The current MVP architecture separates:
- identity/account management
- guardian/member ownership
- family visibility
- subscription entitlement
- alert routing

The backend uses scheduled Workers jobs to evaluate inactivity windows while Android clients periodically submit lightweight heartbeat updates.

---

# AI-Assisted Development Workflow

OKSignal is also an experiment in AI-assisted software engineering workflows.

AI tools are actively used for:
- architecture exploration
- implementation planning
- debugging workflows
- API design
- documentation
- testing strategy
- rapid prototyping

The goal is not fully automated development, but accelerating iteration and reducing friction during product development.

---

# Project Status

Current status:
- Backend MVP functional
- Android client in active development
- Guardian/member flows implemented
- Inactivity alert pipeline implemented
- Ongoing UX, deployment, and notification improvements

---

## Development Status

Android client development is currently active in frontend feature branches while APIs and backend architecture continue to evolve.

---

# Repository Structure

```text
/docs              Product specs and API references
/frontend          Android client
/src               Cloudflare Workers backend
