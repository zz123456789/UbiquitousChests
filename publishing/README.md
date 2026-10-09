# Publishing

Release: **1.0.2**, Minecraft 1.20.1 Forge, Java 17, client and server.

- `DESCRIPTION.en.md` and `DESCRIPTION.curseforge.en.md`: platform descriptions,
  structured after the author's Sealed Word Orb listing.
- `project.json`: public Modrinth metadata.
- `CURSEFORGE-PROJECT.md`: author-dashboard project creation fields.
- `CHANGELOG-1.0.2.en.md`: release notes.
- `icon.png`: generated promotional artwork; prompt and provenance in `ARTWORK.md`.
- `Publish-GitHub.ps1`: creates the public repository, then publishes a verified
  release after the source has been pushed (`-Release`).
- `Publish-Modrinth.ps1 -SubmitForReview`: uploads a hash-verified release and
  submits the draft project for platform review.
- `Publish-CurseForge.ps1 -ProjectId <ID>`: uploads the release to the existing
  CurseForge project; acceptance does not imply moderation approval.

Modrinth and CurseForge publishing scripts read the existing Windows DPAPI-encrypted
credentials from `%LOCALAPPDATA%/SealedWordOrbPublishing/credentials.json`.
GitHub uses the Windows Git credential manager. Credentials are never stored in
this repository, request URLs, or release archives. Receipt files are ignored.

CurseForge's upload API requires an existing project ID; project creation and
website description/license fields are managed in the author dashboard.

Do not automatically retry a creation/upload whose response was interrupted:
first inspect the remote project or file list to avoid duplicates.
