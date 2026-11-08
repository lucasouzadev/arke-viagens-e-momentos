# OWNER_DECISIONS — Arke

Dated log of the owner's standing instructions (format: `docs/agents/standard/OWNER-DECISIONS-TEMPLATE.md`). Newest first; never delete, mark `SUPERSEDED`. The current one-line rule lives in `AGENTS.md`.
Entries up to 2026-11-02 were moved verbatim in meaning from the top of the old `AGENTS.md` (2026-11-08); links point to the canonical contracts/ADRs.

## 2026-11-08 — Canary per session, early compaction, security/performance skills (agent-kit 1.1.0)
Rule: the owner gives the form of address at the start of each session; agents use it in every reply, keep it only in the conversation, never store it. Auto-compaction at a 200k-token window (`autoCompactWindow`), statusline shows context use; handoff from 70 %. Security/performance skills: `security-core`, `identity-session`, `data-privacy`, `supply-chain`, `performance-core`, `platform-security` (CI repos: the security subset). Kit source: `lucasouzadev/agent-kit-skill`.

## 2026-11-08 — Agent docs standard
Rule: agent-kit 1.0.0. Agent entry docs in English; the existing Portuguese docs under `docs/agents/` and `docs/standards/` stay authoritative as project overlays. Replies to the owner in Portuguese, caveman style. Canary unchanged: private form of address in every chat reply, never stored in files.

## 2026-11-02 — Beta QA fix authorized (M5-QA-FIX-01)
Rule: implement the beta QA on both platforms. The current request replaces the external handle / fixed CTA and authorizes personal location **only in the foreground, requested by tap, never stored or shared**. No merge, no automatic Google Cloud setup, no new beta without an approved dispatch.

## 2026-11-02 — First TestFlight
Rule: explicit chat authorization to sign and upload the first beta after the owner privately configured the six secrets in the `ios-testflight` environment. [M5-TESTFLIGHT-01](../engenharia/M5-TESTFLIGHT-01.md) overrides the historical signing/upload restrictions **only for that slice**. No merge, no App Store/Play Store publication, no automatic invites. **Every future execution needs an explicit dispatch.**

## 2026-11-02 — Device test APK (M5-DEVICE-01)
Rule: debug APK may be produced by Actions as a temporary artifact for device testing; replaces the "PNG only" restriction for that slice. No store publication, no production signing, no automatic TestFlight upload.

## 2026-11-01 — Platform parity (DESIGN-006)
Rule: [DESIGN-006](../design/DESIGN-006.md) guides both platforms: visible handle in sheets, Cancel top-left, same action/field hierarchy and base text/spacing metrics. APIs and materials stay native; platform differences never change flow organization. Review screenshot pairs before advancing.

## 2026-11-01 — Idea details (M5-DETAILS-01)
Rule: [M5-DETAILS-01](../engenharia/M5-DETAILS-01.md) scopes local idea details, finite navigation and return home. No data editing, Pin/geocoding or new persistence; capture examples only in memory and Debug.

## 2026-11-01 — Flow continuity (M5-PLACES-01, ADR-011)
Rule: [M5-PLACES-01](../engenharia/M5-PLACES-01.md) and [ADR-011](../decisoes/ADR-011.md) scope editor/review/cancel, personal written ideas on device and confirmed deletion. No geographic Pin, geocoding, GPS, account, group, sync or new network. Local persistence does not approve the M3 contracts. Keep existing data files on reversions; no automatic merge/release.

## 2026-10-31 — Next slice (ADR-010, M5-PREVIEW-01)
Rule: [ADR-010](../decisoes/ADR-010.md) and [M5-PREVIEW-01](../engenharia/M5-PREVIEW-01.md) authorize Apple Maps/Google Maps without GPS and real simulator captures. Only evidence PNGs may be uploaded by Actions; no distribution binaries. Google Cloud / Android key unavailable; never create accounts or billing automatically.

## 2026-10-31 — Phase exception (ADR-009, M5-BOOTSTRAP-01)
Rule: the owner authorized starting both native codebases and compiling on GitHub Actions. [ADR-009](../decisoes/ADR-009.md) and [M5-BOOTSTRAP-01](../engenharia/M5-BOOTSTRAP-01.md) authorize the foundation, tests and verification builds only. Do not block this again on historical documentation-first instructions. No merge, distribution signing or automatic release.

## Before 2026-10-31 — Documentation-first phase
Rule (partly SUPERSEDED by the entries above): no app code, backend, executable pipelines, signing, builds or distribution until a new milestone is explicitly approved. A draft spec is not authorization to implement. Still valid: no merge, branch promotion, secrets enablement, release, migration or file deletion without authorization for that action.
