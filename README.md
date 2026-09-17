# CampusOS — Everything Campus. One Place.

Cross-platform Smart College ERP (Java 21 + JavaFX + SQLite).

> Full scope: see master prompt in project chat. Built with vibe-coding phases.
> Live demo (teachers/friends): GitHub Pages site in `web/` — full JavaFX app is in this repo with downloadable releases.

## Quick start (Phase 1)

Requirements: JDK 21, Maven 3.9+.

```bash
cd campusos
mvn test
mvn javafx:run
```

Maven portable (this machine):
`C:\Users\bezal\AppData\Local\Temp\opencode\maven\apache-maven-3.9.9\bin\mvn.cmd`

DB file: `~/.campusos/campusos.db` (auto-created from `schema.sql` + `seed.sql`).

Demo users (Phase 2 auth will activate): `admin` / `faculty1` / `student1` — password `password123`.

## Architecture

```
UI (JavaFX) → Controller → Service → Repository → JDBC → SQLite
```

Business logic is UI-independent so it can later be exposed via REST for web/mobile.
See `docs/architecture.md`.

## Phases

- [x] Phase 1: Maven + JavaFX shell + SQLite + tests
- [ ] Phase 2: Auth + roles
- [ ] Phase 3: Student dashboard
- [ ] … through Phase 14 (packaging + docs)

Progress log: `docs/development-log.md`.

## Live / sharing

- Source: this GitHub repo (public) — friends can clone + PR.
- Live site: `web/index.html` deployed to GitHub Pages (static demo + screenshots + download links).
- Releases: GitHub Releases with portable JAR (Phase 14).
