# Events app

Compose + MVVM consumer app with `data`, `domain`, `presentation` and Hilt wiring. Its domain selects up to six highlights from the catalog provided by the shared SDK. The list is demo business logic over DummyJSON products, not a real events backend.

## Configure shared packages

The app directly consumes `com.redbull.lab:shared-ui:0.1.6` and `com.redbull.lab:android-sdk:0.2.2`. It declares only these two direct dependencies. `android-sdk` exposes the `RedBullSdk` facade; its `android-data` and `android-domain` modules resolve transitively from `https://maven.pkg.github.com/briantylerec/shared-sdk`.

The Maven endpoints are already configured in `settings.gradle.kts`: `briantylerec/shared-ui-library` and `briantylerec/shared-sdk`. Grant this app repository `Read` access to the UI package and the SDK package(s) in GitHub Packages. CI uses `GITHUB_TOKEN` with `packages: read`.

Normal builds use the published packages. To test local changes, run `./gradlew publishToMavenLocal` from both library project roots, then use:

```bash
./gradlew -PuseMavenLocal=true :app:testDebugUnitTest :app:assembleDebug
```

That flag adds Maven Local ahead of GitHub Packages for this build. Omit it to use the published packages. For local GitHub Packages authentication, store your GitHub username and a classic PAT with `read:packages` in `~/.gradle/gradle.properties`, outside the repository:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_CLASSIC_PAT
```

Never commit credentials. The project `gradle.properties` contains only non-secret Android/Gradle build settings and is versioned so CI receives them. The Maven URL is a Gradle registry endpoint, not a browser page. `.github/workflows/android-ci.yml` builds the app on pushes and pull requests.

## Kotlin, Hilt y KAPT

La app usa Kotlin 2.3.21 y Hilt 2.58. El procesador de Hilt debe leer el metadata emitido por Kotlin: por eso `gradle/libs.versions.toml` declara `kotlin-metadata-jvm` y `app/build.gradle.kts` lo añade a `kapt(...)`. Dagger reporta la incompatibilidad del lector anterior con Kotlin 2.3 y recomienda actualizar esa dependencia por la configuración del procesador. Se conserva Hilt 2.58 para esta build con AGP 8.13; la línea 2.59+ del plugin Hilt requiere AGP 9, que implica una migración separada. [Dagger issue #5001](https://github.com/google/dagger/issues/5001), [Dagger releases](https://github.com/google/dagger/releases).
