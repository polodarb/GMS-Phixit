# GMS Phixit
---

> **⚠️ NOTE: This app was built from scratch in just 12 hours. The codebase is a quick-and-dirty solution and is NOT a reference for clean architecture or best practices. Use at your own risk and do not use this project as an example of production-quality code!**

---

## What is GMS Phixit?

**GMS Phixit** is a continuation of the [GMS Flags project](https://github.com/polodarb/GMS-Flags), designed to work with the new Google Play Services (GMS) database schema. The app allows you to view and modify feature flags in GMS and other Google apps, even after the original GMS Flags stopped working due to database changes.

- **Supports new DB schema** (Phixit-compatible, DB version 1033+)
- Root access required
- Fast flag search, add, and edit

## ⚠️ Why This App Is Currently Unusable
### <b style="color:red;">ALL FLAGS RESET AFTER 24 HOURS</b>

Any changes made to flags are automatically reverted by Google Play Services within 24 hours. This makes it impossible to use the app for persistent flag modifications. The app is released in hope that the community will find a workaround for this limitation.

## Why this project?

Google changed the internal structure of the GMS database, breaking compatibility with previous tools. GMS Phixit was created to restore this functionality for power users and developers.

The current progress is published in the hope that it will help other developers fully reverse-engineer the Phixit schema.

From our side, reverse engineering will most likely be paused — whether temporarily or permanently, we do not know yet.

## Authors
- [polodarb](https://github.com/polodarb) — Lead Developer
- [transaero21](https://github.com/transaero21) — Reverse Engineering

## License
```
MIT License

Copyright (c) 2025 Danyil Kobzar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
