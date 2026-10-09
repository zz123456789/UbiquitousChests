param([switch]$Release)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$env:GCM_INTERACTIVE = 'Never'
$env:GIT_TERMINAL_PROMPT = '0'
$lines = "protocol=https`nhost=github.com`n`n" | git -C $root credential fill 2>$null
$credential = @{}
foreach ($line in $lines) { if ($line -match '^([^=]+)=(.*)$') { $credential[$matches[1]] = $matches[2] } }
if (!$credential.password) { throw 'No saved GitHub credential.' }
$headers = @{Authorization="Bearer $($credential.password)"; 'User-Agent'='UbiquitousChests-Publisher'; 'X-GitHub-Api-Version'='2022-11-28'}
$credential.Clear(); $lines = $null
$api = 'https://api.github.com'
try {
    $account = Invoke-RestMethod "$api/user" -Headers $headers
    if ($account.login -ne 'zz123456789') { throw 'Unexpected GitHub account; refusing to publish.' }
    $repo = $null
    try { $repo = Invoke-RestMethod "$api/repos/zz123456789/UbiquitousChests" -Headers $headers }
    catch { if ([int]$_.Exception.Response.StatusCode -ne 404) { throw } }
    if (!$repo) {
        $body = @{name='UbiquitousChests';description='Configurable underground single-chest structures for Minecraft 1.20.1 Forge, with JEI/JER loot integration. MIT licensed.';private=$false;auto_init=$false;has_issues=$true}
        $repo = Invoke-RestMethod "$api/user/repos" -Method Post -Headers $headers -ContentType 'application/json' -Body ($body | ConvertTo-Json)
    }
    if ($repo.private -or !$repo.permissions.push) { throw 'Repository is not public or write permission is unavailable.' }
    Write-Output "Public repository: $($repo.html_url)"
    if (!$Release) { return }
    $sha = (git -C $root rev-parse HEAD).Trim()
    $remote = Invoke-RestMethod "$api/repos/zz123456789/UbiquitousChests/commits/main" -Headers $headers
    if ($remote.sha -ne $sha) { throw 'Remote main does not match the local release commit.' }
    $version = '1.0.2'
    $ghRelease = $null
    try { $ghRelease = Invoke-RestMethod "$api/repos/zz123456789/UbiquitousChests/releases/tags/v$version" -Headers $headers }
    catch { if ([int]$_.Exception.Response.StatusCode -ne 404) { throw } }
    if (!$ghRelease) {
        $body = @{tag_name="v$version";target_commitish=$sha;name="Ubiquitous Chests $version";body=(Get-Content "$PSScriptRoot/CHANGELOG-$version.en.md" -Raw);draft=$true;prerelease=$false}
        $ghRelease = Invoke-RestMethod "$api/repos/zz123456789/UbiquitousChests/releases" -Method Post -Headers $headers -ContentType 'application/json' -Body ($body | ConvertTo-Json)
    }
    $jar = Get-Item "$root/build/libs/ubiquitous_chests-$version.jar"
    $hash = (Get-FileHash $jar.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    $asset = $ghRelease.assets | Where-Object {$_.name -eq $jar.Name} | Select-Object -First 1
    if (!$asset) {
        $upload = ($ghRelease.upload_url -split '\{')[0] + '?name=' + [Uri]::EscapeDataString($jar.Name)
        if (!$upload.StartsWith('https://uploads.github.com/')) { throw 'Unexpected upload host.' }
        $asset = Invoke-RestMethod $upload -Method Post -Headers $headers -ContentType 'application/java-archive' -InFile $jar.FullName
    }
    if ($asset.digest -ne "sha256:$hash") { throw 'GitHub asset digest mismatch.' }
    $ghRelease = Invoke-RestMethod "$api/repos/zz123456789/UbiquitousChests/releases/$($ghRelease.id)" -Method Patch -Headers $headers -ContentType 'application/json' -Body '{"draft":false,"make_latest":"true"}'
    @{url=$ghRelease.html_url;commit=$sha;version=$version;sha256=$hash} | ConvertTo-Json | Set-Content "$PSScriptRoot/github-result.json" -Encoding utf8
    Write-Output "Published and hash-verified release: $($ghRelease.html_url)"
} finally { $headers.Clear() }
