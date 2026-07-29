# Synchronization policy

Every completed implementation in this workspace must be validated and then
synchronized to both repositories:

1. **Complete source repository** — `hadryphern/ChaoticDimensionsMod-1.20.1`.
   It receives all versionable mod source, resources, authored assets,
   Gradle files and technical documentation from this workspace.
2. **Organized release repository** — `hadryphern/ChaoticDimensions`, under
   `Fabric/1.20.1/`. It receives the curated Fabric 1.20.1 source snapshot,
   without local references, build outputs, caches, logs or test worlds.

Before pushing either repository, run the relevant Gradle verification. Never
commit generated `build/`, `.gradle/`, `run/`, `logs/`, IDE state, or personal
world saves. Preserve local reference assets; do not publish third-party
Minecraft vanilla assets without the owner's explicit licensing decision.

See `docs/REPOSITORY_SYNC.md` for the concrete paths and scope.
