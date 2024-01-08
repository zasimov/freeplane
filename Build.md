# Build

Current branch is `1.11.x-dev`.

1. Install Java 11:
   - Fedora: `sudo dnf install -y java-11-openjdk`
2. Setup `gradle 8.4` - https://docs.gradle.org/current/userguide/installation.html to `/opt/gradle/8.4`
3. Build:
```
export PATH="$PATH:/opt/gradle/8.4/bin"
gradle wrapper
gradle build
```
4. Copy `BIN` to `/usr/local/freeplane`.
