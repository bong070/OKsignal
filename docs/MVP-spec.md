# OKSignal MVP Spec Update (2026-04-12)

## Why this update exists
The original schema used a fixed `users.role` (`guardian` or `member`) and enforced one active guardian per member through a unique index on `guardian_member_links(member_user_id)`. That is too restrictive for the product direction.

A realistic family flow is:
- User A directly monitors User B
- User B later purchases their own plan and directly monitors User C

That means one account may be a member in one relationship and a guardian in another. Roles must therefore be relationship-based, not user-based.

## Final product direction

### Free
- A user can create 1 direct care link
- Only the direct guardian sees that member's status and receives alerts

### Premium
- A user can create unlimited direct care links
- Only the direct guardian sees that member's status and receives alerts

### Family
- A user can create 1 family group
- Only the group leader can invite members into the group
- Group members can view the status of all users in the group from a Group tab
- Alerts still go only to the group leader through direct guardian-member links
- Group membership adds visibility, not alert ownership

## Core design rules
1. `users` should not store a fixed guardian/member role
2. Direct care responsibility stays in `guardian_member_links`
3. Subscription entitlement is separate from relationship data
4. Family visibility is separate from alert ownership
5. Group invites and direct-care invites should be separate token tables

## Required schema changes

### 1) Remove fixed role from users
Old:
- `users.role`
- `users.subscription_tier`

New:
- `users` only stores identity/contact/account status
- plan moves to `subscriptions`

### 2) Allow one user to participate in multiple relationships
Replace the current one-active-guardian-per-member constraint with one-active-pair-per-link:
- remove unique index on `guardian_member_links(member_user_id) WHERE status='active'`
- add unique index on `(guardian_user_id, member_user_id) WHERE status='active'`

This allows:
- User A -> User B
- User C -> User B
if you ever want it later, while still preventing duplicate active rows for the same pair.

### 3) Split invites by purpose
Use:
- `direct_invites` for direct guardian-member linking
- `group_invites` for joining a family group

This keeps the flows simpler and avoids overloading a single invite table.

### 4) Add family group tables
Add:
- `groups`
- `group_members`
- `group_invites`

Family group is a visibility container.
It does not replace direct guardian-member ownership.

### 5) Keep alert pipeline mostly unchanged
`alerts.guardian_user_id` and `alerts.member_user_id` remain valid.
For Family, the leader must also have direct guardian-member links to the monitored members whose alerts they should receive.

## Recommended table meanings

### users
Identity and account shell only.

### subscriptions
Tracks entitlement:
- free
- premium
- family

### guardian_member_links
Direct ownership / alert routing.

### groups
Family group owned by one leader.

### group_members
Users visible inside the family group.

### devices
Last ping, last activity, and latest approximate location.

### alerts
Resolved / active inactivity alerts.

## API-level behavior

### Direct invite acceptance
1. Inviter creates direct invite
2. Recipient accepts
3. Create `guardian_member_links` row

### Group invite acceptance
1. Family leader creates group invite
2. Recipient accepts
3. Create `group_members` row

### Family alert rule
- Group tab can show everyone's status
- Alert recipient is still the direct guardian
- For Family, that direct guardian should be the group leader

## Migration notes from current schema
From the uploaded schema:
- remove `users.role`
- remove `users.subscription_tier`
- rename `invite_tokens` or replace it with `direct_invites`
- drop unique index `ux_guardian_member_active_member`
- create `ux_guardian_member_active_pair`
- add `subscriptions`, `groups`, `group_members`, `group_invites`

## Suggested rollout order
1. Update DB schema
2. Update invite APIs
3. Update direct-link creation checks by plan
4. Add Family group APIs
5. Add Group tab in UI
6. Keep inactivity engine unchanged
