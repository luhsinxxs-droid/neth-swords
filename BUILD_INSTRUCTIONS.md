# DiaSwords — Build Instructions

This is a complete Maven project for a Paper 1.21.11 plugin. I wasn't able to compile
it in my sandbox (no internet access there to fetch Maven or the Paper API), but a
GitHub Actions workflow is included so GitHub will build it for you automatically —
no local installs needed.

## Option A: GitHub Actions (recommended, no installs)

1. Create a new repository on GitHub (can be private).
2. Push this entire `DiaSwords` folder to it (the `.github/workflows/build.yml` file
   must end up at the repo root level, i.e. `your-repo/.github/workflows/build.yml`,
   not nested inside another folder).
   - Easiest way if you don't use git: go to your new repo on github.com, click
     "Add file" → "Upload files", drag in everything from inside this `DiaSwords`
     folder (including the hidden `.github` folder — if your file browser hides it,
     use `git` instead, see below).
   - Via git:
     ```
     cd DiaSwords
     git init
     git add .
     git commit -m "Initial commit"
     git branch -M main
     git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
     git push -u origin main
     ```
3. Go to the "Actions" tab on your GitHub repo. A workflow run will already be running
   (or click "Run workflow" if not).
4. When it finishes (green checkmark), click into the run, scroll to "Artifacts" at the
   bottom, and download **DiaSwords** — that's a zip containing `DiaSwords.jar`.
5. Unzip it, drop `DiaSwords.jar` into your server's `plugins/` folder, restart the server.

Every time you push a change (e.g. after editing config.yml defaults or any .java file),
GitHub rebuilds it automatically — just grab the new artifact from the latest run.

## Option B: Build locally instead
Requires JDK 21 + Maven installed.
```
cd DiaSwords
mvn package
```
Output lands at `target/DiaSwords.jar`.

## Sword Types & Commands
Give swords with: `/diaswords give <player> <sword>`
Sword names: `dash`, `lightning`, `vampire`, `frost`, `vortex`, `explosive`

Other commands:
- `/diaswords list` — lists all sword types
- `/diaswords reload` — reloads config.yml without restarting

## What each sword does

| Sword | Trigger | Effect |
|---|---|---|
| Dash | Right-click | Dashes forward in your look direction. Brief fall-damage immunity. |
| Lightning | Passive (on hit) | 10% chance per hit to strike lightning + bonus damage. |
| Vampire | Passive (on hit) | Heals you for a % of damage dealt, capped per hit. |
| Frost | Right-click | Roots/slows all enemies in a radius (Slowness + Mining Fatigue). |
| Vortex | Right-click | Pulls all enemies in a radius toward you. |
| Explosive | Right-click | Fires a real fireball that explodes on impact. Block destruction off by default; shooter is immune to their own blast by default. |

## Configuration
Everything is tunable in `src/main/resources/config.yml` — cooldowns, damage values,
proc chances, radii, durations, etc. Edit the file in your server's `plugins/DiaSwords/`
folder after first run, then `/diaswords reload`.

Notable safety defaults (all configurable):
- `explosive-sword.break-blocks: false` — prevents map griefing
- `explosive-sword.shooter-immune: true` — you won't blow yourself up
- All abilities have permission node `diaswords.use` (default: true for everyone)
- Admin commands require `diaswords.admin` (default: op)

## Notes on the build environment
I wrote and carefully reviewed every line by hand, including verifying exact Paper API
method signatures (e.g. `PlayerDeathEvent.getItemsToKeep()`) against the official 1.21.11
javadocs. But I could not run an actual `mvn package` here since this sandbox has no
network access to download Maven or the Paper API jar — so please do a test run on your
server before going live, and let me know if you hit any compile errors and I'll fix them
immediately.
