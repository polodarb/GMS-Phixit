# GMS Phixit
---

> **⚠️ NOTE: This app was built from scratch in just 12 hours. The codebase is a quick-and-dirty solution and is NOT a reference for clean architecture or best practices. Use at your own risk and do not use this project as an example of production-quality code!**

---

## What is GMS Phixit?

**GMS Phixit** is a continuation of the GMS Flags project, designed to work with the new Google Play Services (GMS) database schema. The app allows you to view and modify feature flags in GMS and other Google apps, even after the original GMS Flags stopped working due to database changes.

- **Supports new DB schema** (Phixit-compatible, DB version 1033+)
- Root access required
- Fast flag search, add, and edit

## Why this project?

Google changed the internal structure of the GMS database, breaking compatibility with previous tools. GMS Phixit was created to restore this functionality for power users and developers.

The current progress is published in the hope that it will help other developers fully reverse-engineer the Phixit schema.

From our side, reverse engineering will most likely be paused — whether temporarily or permanently, we do not know yet.

## Authors
- [polodarb](https://github.com/polodarb) — Lead Developer
- [transaero21](https://github.com/transaero21) — Reverse Engineering

## License
MIT (see LICENSE) 