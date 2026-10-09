# CurseForge project setup

The CurseForge upload API needs an existing project ID. Use the author dashboard
to create a **Minecraft → Mods** project with these fields:

| Field | Value |
| --- | --- |
| Name | Ubiquitous Chests |
| Slug | ubiquitous-chests |
| Summary | Discover single-chest underground structures with configurable generation, loot, and optional JEI/JER integration. |
| Main category | World Gen |
| Additional category | Structures (if available) |
| License | MIT License |
| Source | https://github.com/zz123456789/UbiquitousChests |
| Issues | https://github.com/zz123456789/UbiquitousChests/issues |
| Icon | `publishing/icon.png` |
| Description | Full text of `publishing/DESCRIPTION.curseforge.en.md` |

The generated artwork is a stylized project icon, not an in-game screenshot.

Once the project exists, its project ID is sufficient to upload the prepared
1.0.2 JAR through `publishing/Publish-CurseForge.ps1`.
