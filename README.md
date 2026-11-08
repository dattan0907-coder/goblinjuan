# Dead Traveler Skeleton (NeoForge 1.21.1 + GeckoLib)

- Minecraft 1.21.1, NeoForge 21.1.220, GeckoLib 4.8.4, Java 21
- Mod id: `traveler`, package: `com.traveler`
- Entity: `traveler:dead_skeleton` (`/summon traveler:dead_skeleton`), has a spawn egg

## Assets
- `assets/traveler/geo/entity/dead_skeleton.geo.json`
- `assets/traveler/animations/entity/dead_skeleton.animation.json`
- `assets/traveler/textures/entity/dead_skeleton.png`  <- PLACEHOLDER, replace with your real 128x128 texture

## Build
GitHub Actions builds on every push (see `.github/workflows/build.yml`). The jar is in the run's artifacts.
Release: `git tag v1.0.0 && git push --tags`

Local build: install Gradle 8.14+ and JDK 21, then `gradle build` (or run `gradle wrapper` once to create gradlew).

## Animations
- idle / walk / charge: automatic (moving, aggressive)
- slash: when it hits a target
- begin_block + block: AI raises the buckler when a target is within 6 blocks
- block_stop: when it lowers the buckler
- block_destroy: after 5 blocked hits (shield breaks, mob is stunned ~1.75s)

## Goblin Champion
- Entity: `traveler:goblin_champion` (`/summon traveler:goblin_champion`), has a spawn egg
- Assets: `geo/entity/goblin_champion.geo.json`, `animations/entity/goblin_champion.animation.json`, `textures/entity/goblin_champion.png`
- Animations used: idle, walking, running, 2h_swing1, 2h_swing2.
  Not wired: walk, walk_loop, charge_forward, charge_sweep, hostile, hostile_no_sway, snap
- geo + animation exported from Blockbench (goblin-champion)
