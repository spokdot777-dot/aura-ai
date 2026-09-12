$ErrorActionPreference = 'Stop'

$distUrl = 'https://services.gradle.org/distributions/gradle-8.7-bin.zip'
$tmp = Join-Path $env:TEMP ("gradle-8.7-" + [System.Guid]::NewGuid().ToString())
$zip = Join-Path $tmp 'gradle-8.7-bin.zip'

New-Item -ItemType Directory -Path $tmp -Force | Out-Null
Write-Host "Downloading Gradle 8.7..."
Invoke-WebRequest -Uri $distUrl -OutFile $zip -UseBasicParsing

Write-Host "Extracting distribution..."
Expand-Archive -Path $zip -DestinationPath $tmp -Force

Write-Host "Locating gradle-wrapper.jar in the extracted distribution..."
$found = Get-ChildItem -Path $tmp -Recurse -Filter 'gradle-wrapper.jar' -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $found) {
    Write-Error "Could not find gradle-wrapper.jar in the distribution"
    Exit 1
}

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$destDir = Join-Path $repoRoot 'gradle\wrapper'
if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
$destJar = Join-Path $destDir 'gradle-wrapper.jar'

Write-Host "Copying gradle-wrapper.jar to $destJar"
Copy-Item -Path $found.FullName -Destination $destJar -Force

# Ensure gradle-wrapper.properties points to Gradle 8.7
$props = Join-Path $destDir 'gradle-wrapper.properties'
if (Test-Path $props) {
    (Get-Content $props) | ForEach-Object {
        if ($_ -match '^distributionUrl=') {
            'distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip'
        } else { $_ }
    } | Set-Content $props -Force
} else {
    @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@ | Set-Content $props -Force
}

# Clean up temporary files
Remove-Item -Path $tmp -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Running wrapper build: .\gradlew.bat assembleDebug"
& "$repoRoot\gradlew.bat" assembleDebug
