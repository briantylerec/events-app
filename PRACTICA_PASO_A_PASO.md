# Guía paso a paso: app Events consumidora

**Objetivo.** Crear una app Compose con MVVM, Hilt y capas data/domain/presentation que consume versiones publicadas de las bibliotecas UI y SDK.

## Cómo usar esta guía

El proyecto de esta guía es `03-events-app`. Abre una terminal en la raíz de esa carpeta. En cada paso crea las carpetas y archivos indicados (o reemplaza su contenido), pega el bloque completo y guarda. La explicación está junto al archivo. Ejecuta el comando al final de cada paso antes de continuar. Los bloques reproducen los archivos del proyecto actual.

Requisitos: JDK 17, Android Studio y Android SDK Platform 36. Usa el Gradle Wrapper que ya está en este proyecto: `./gradlew` en macOS/Linux o `gradlew.bat` en Windows. Conserva `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar`; no los reemplaces con bloques de texto.

## Pasos

### 1. Configurar Gradle y versiones consumidas

settings.gradle.kts usa GitHub Packages como fuente normal con los repositorios reales. Maven Local se activa solo con `-PuseMavenLocal=true` para probar artefactos publicados localmente. `libs.versions.toml` fija UI 0.1.6 y SDK 0.2.2.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 1 — Crea o actualiza `settings.gradle.kts`

**En el proyecto de la aplicación Events, en la ruta `settings.gradle.kts`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Declara el nombre de esta build, los módulos y los repositorios desde donde Gradle descarga plugins y dependencias. `maven.pkg.github.com` es el endpoint Maven para Gradle; no es una página de navegador. GitHub Packages requiere un PAT classic con `read:packages` para la descarga local, incluso si el paquete es público.

````kotlin
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }

val sharedUiPackageRepository = providers.gradleProperty("gpr.sharedUiRepository")
    .orElse("briantylerec/shared-ui-library")
    .get()
val sharedSdkPackageRepository = providers.gradleProperty("gpr.sharedSdkRepository")
    .orElse("briantylerec/shared-sdk")
    .get()
val packageUsername = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
val packageToken = System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.key").orNull
val useMavenLocal = providers.gradleProperty("useMavenLocal").orNull?.toBoolean() ?: false

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (useMavenLocal) mavenLocal()
        maven {
            name = "SharedUiGitHubPackages"
            url = uri("https://maven.pkg.github.com/$sharedUiPackageRepository")
            credentials {
                username = packageUsername
                password = packageToken
            }
        }
        maven {
            name = "SharedSdkGitHubPackages"
            url = uri("https://maven.pkg.github.com/$sharedSdkPackageRepository")
            credentials {
                username = packageUsername
                password = packageToken
            }
        }
    }
}
rootProject.name = "events-app"

include(":app")
````

#### Subpaso 2 — Crea o actualiza `build.gradle.kts`

**En el proyecto de la aplicación Events, en la ruta `build.gradle.kts`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Declara plugins y configuración comunes del build independiente.

````kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
}
````

#### Subpaso 3 — Crea o actualiza `gradle.properties`

**En el proyecto de la aplicación Events, en la ruta `gradle.properties`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Fija opciones de Gradle para que la compilación sea estable.

````properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
````

#### Subpaso 4 — Crea o actualiza `gradle/libs.versions.toml`

**En el proyecto de la aplicación Events, en la ruta `gradle/libs.versions.toml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Centraliza versiones y aliases para dependencias y plugins en formato TOML.

````toml
[versions]
agp="8.13.2"
kotlin="2.3.21"
composeBom="2024.10.00"
hilt="2.58"
activityCompose="1.9.3"
lifecycle="2.8.7"
sharedUi="0.1.6"
sharedSdk="0.2.2"

[plugins]
android-application={id="com.android.application",version.ref="agp"}
kotlin-android={id="org.jetbrains.kotlin.android",version.ref="kotlin"}
kotlin-compose={id="org.jetbrains.kotlin.plugin.compose",version.ref="kotlin"}
kotlin-kapt={id="org.jetbrains.kotlin.kapt",version.ref="kotlin"}
hilt={id="com.google.dagger.hilt.android",version.ref="hilt"}

[libraries]
compose-bom={module="androidx.compose:compose-bom",version.ref="composeBom"}
compose-ui={module="androidx.compose.ui:ui"}
compose-material3={module="androidx.compose.material3:material3"}
compose-activity={module="androidx.activity:activity-compose",version.ref="activityCompose"}
compose-preview={module="androidx.compose.ui:ui-tooling-preview"}
lifecycle-viewmodel={module="androidx.lifecycle:lifecycle-viewmodel-compose",version.ref="lifecycle"}
lifecycle-viewmodel-ktx={module="androidx.lifecycle:lifecycle-viewmodel-ktx",version.ref="lifecycle"}
lifecycle-runtime={module="androidx.lifecycle:lifecycle-runtime-compose",version.ref="lifecycle"}
hilt-android={module="com.google.dagger:hilt-android",version.ref="hilt"}
hilt-compiler={module="com.google.dagger:hilt-compiler",version.ref="hilt"}
hilt-navigation={module="androidx.hilt:hilt-navigation-compose",version="1.2.0"}

shared-ui = { module = "com.redbull.lab:shared-ui", version.ref = "sharedUi" }
shared-sdk = { module = "com.redbull.lab:android-sdk", version.ref = "sharedSdk" }
kotlin-metadata = { module = "org.jetbrains.kotlin:kotlin-metadata-jvm", version.ref = "kotlin" }
````

#### Subpaso 5 — Crea o actualiza `app/build.gradle.kts`

**En el proyecto de la aplicación Events, en la ruta `app/build.gradle.kts`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Configura la app Android, Compose, Hilt, sus dependencias y las versiones publicadas de UI y SDK.

````kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android { namespace = "com.redbull.lab.events"; compileSdk = 36
    defaultConfig { applicationId = "com.redbull.lab.events"; minSdk = 24; targetSdk = 35; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

kapt { correctErrorTypes = true }
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui); implementation(libs.compose.material3); implementation(libs.compose.activity); implementation(libs.compose.preview)
    implementation(libs.lifecycle.viewmodel); implementation(libs.lifecycle.viewmodel.ktx); implementation(libs.lifecycle.runtime)
    implementation(libs.hilt.android); kapt(libs.hilt.compiler); kapt(libs.kotlin.metadata); implementation(libs.hilt.navigation)
    implementation(libs.shared.ui)
    implementation(libs.shared.sdk)
}
````

#### Subpaso 6 — Crea o actualiza `app/src/main/AndroidManifest.xml`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/AndroidManifest.xml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Declara permiso de internet, clase Application y Activity inicial de esta aplicación.

````xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application android:name=".EventsApplication" android:label="Events" android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity android:name=".EventsActivity" android:exported="true"><intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter></activity>
    </application>
</manifest>
````

**Al terminar este paso:**

```bash
./gradlew tasks
```

### 2. Definir reglas y contrato de datos de Events

El contrato del repositorio queda en domain. GetEventHighlightsUseCase filtra y ordena la selección. La implementación de data adapta el SDK compartido al contrato de Events, sin llevar Retrofit a la app.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 7 — Crea o actualiza `app/src/main/java/com/redbull/lab/events/domain/ProductCatalogRepository.kt`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/java/com/redbull/lab/events/domain/ProductCatalogRepository.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Define el contrato de datos y las reglas/casos de uso de la app consumidora.

````kotlin
package com.redbull.lab.events.domain

import com.redbull.lab.domain.Product
import javax.inject.Inject

/** App-facing contract for the catalog data used by the event highlights screen. */
interface ProductCatalogRepository {
    suspend fun loadProducts(): List<Product>
}

/** Applies the event screen's presentation-independent curation rule. */
class GetEventHighlightsUseCase @Inject constructor(private val repository: ProductCatalogRepository) {
    suspend operator fun invoke(): List<Product> = repository.loadProducts()
        .filter { it.price >= 0.0 }
        .sortedByDescending { it.price }
        .take(HIGHLIGHT_LIMIT)

    private companion object {
        const val HIGHLIGHT_LIMIT = 6
    }
}
````

#### Subpaso 8 — Crea o actualiza `app/src/main/java/com/redbull/lab/events/data/SdkProductCatalogRepository.kt`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/java/com/redbull/lab/events/data/SdkProductCatalogRepository.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Adapta la fachada pública del SDK al contrato de dominio propio de la app.

````kotlin
package com.redbull.lab.events.data

import com.redbull.lab.domain.Product
import com.redbull.lab.events.domain.ProductCatalogRepository
import com.redbull.lab.sdk.RedBullSdk
import javax.inject.Inject

/** Adapts the shared SDK to the Events app's domain contract. */
class SdkProductCatalogRepository @Inject constructor(
    private val sdk: RedBullSdk,
) : ProductCatalogRepository {
    override suspend fun loadProducts(): List<Product> = sdk.getProducts()
}
````

#### Subpaso 9 — Crea o actualiza `app/src/main/java/com/redbull/lab/events/di/PlatformModule.kt`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/java/com/redbull/lab/events/di/PlatformModule.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Conecta interfaces con implementaciones mediante inyección de dependencias Hilt.

````kotlin
package com.redbull.lab.events.di

import com.redbull.lab.events.data.SdkProductCatalogRepository
import com.redbull.lab.events.domain.ProductCatalogRepository
import com.redbull.lab.sdk.RedBullSdk
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds @Singleton
    abstract fun bindProductCatalogRepository(implementation: SdkProductCatalogRepository): ProductCatalogRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {
    @Provides @Singleton
    fun provideSdk(): RedBullSdk = RedBullSdk.create()
}
````

**Al terminar este paso:**

```bash
./gradlew :app:assembleDebug
```

### 3. Crear estado y ViewModel

El ViewModel ejecuta el caso de uso en viewModelScope y expone StateFlow<EventsUiState>. Maneja carga, error, vacío y éxito. Compose recoge el estado con collectAsStateWithLifecycle() y dibuja componentes de UI compartida.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 10 — Crea o actualiza `app/src/main/java/com/redbull/lab/events/presentation/EventsViewModel.kt`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/java/com/redbull/lab/events/presentation/EventsViewModel.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Coordina el caso de uso y expone el estado observable que consume la pantalla Events.

````kotlin
package com.redbull.lab.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redbull.lab.domain.Product
import com.redbull.lab.events.domain.GetEventHighlightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventsUiState(
    val isLoading: Boolean = true,
    val highlights: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val hasError: Boolean = false,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventHighlights: GetEventHighlightsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = EventsUiState(isLoading = true)
            try {
                _uiState.value = EventsUiState(isLoading = false, highlights = getEventHighlights())
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.value = EventsUiState(
                    isLoading = false,
                    errorMessage = error.message,
                    hasError = true,
                )
            }
        }
    }
}
````

#### Subpaso 11 — Crea o actualiza `app/src/main/java/com/redbull/lab/events/EventsApplication.kt`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/java/com/redbull/lab/events/EventsApplication.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Inicializa Hilt para el proceso de la app Events.

````kotlin
package com.redbull.lab.events

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EventsApplication : Application()
````

#### Subpaso 11-A — Crea `app/src/main/res/values/strings.xml`

**En el proyecto Events, crea el archivo de recursos Android en la ruta indicada.** Android guarda aquí los textos de interfaz para que puedan localizarse y cargarse con `stringResource(R.string...)` desde Compose.

````xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="events_title">Eventos destacados</string>
    <string name="events_subtitle">Selección de muestra ordenada por precio</string>
    <string name="empty_highlights">No hay elementos para mostrar.</string>
    <string name="load_error">No se pudo cargar el contenido.</string>
    <string name="product_details">$%1$s • %2$s</string>
</resources>
````

#### Subpaso 11-B — Crea `app/src/main/java/com/redbull/lab/events/presentation/resources/Dimensions.kt`

**En el proyecto Events, crea este archivo Kotlin.** Centraliza las dimensiones Compose siguiendo el patrón usado por la librería UI.

````kotlin
package com.redbull.lab.events.presentation.resources

import androidx.compose.ui.unit.dp

/** Spacing and size tokens used by the Events presentation layer. */
object Dimensions {
    val Dim16dp = 16.dp
}
````

#### Subpaso 12 — Crea o actualiza `app/src/main/java/com/redbull/lab/events/EventsActivity.kt`

**En el proyecto de la aplicación Events, en la ruta `app/src/main/java/com/redbull/lab/events/EventsActivity.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Monta la interfaz Compose de Events y representa estados y acciones del usuario.

````kotlin
package com.redbull.lab.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.redbull.lab.events.presentation.EventsViewModel
import com.redbull.lab.events.presentation.resources.Dimensions
import com.redbull.lab.ui.PlatformEmptyState
import com.redbull.lab.ui.PlatformErrorState
import com.redbull.lab.ui.PlatformLoadingIndicator
import com.redbull.lab.ui.ProductCard
import com.redbull.lab.ui.RedBullTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsActivity : ComponentActivity() {
    private val viewModel: EventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedBullTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                Column(Modifier.fillMaxSize().padding(Dimensions.Dim16dp)) {
                    Text(stringResource(R.string.events_title), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.events_subtitle), style = MaterialTheme.typography.bodyMedium)
                    when {
                        state.isLoading -> PlatformLoadingIndicator()
                        state.hasError -> PlatformErrorState(state.errorMessage.orEmpty().ifBlank { stringResource(R.string.load_error) }, viewModel::refresh)
                        state.highlights.isEmpty() -> PlatformEmptyState(stringResource(R.string.empty_highlights))
                        else -> LazyColumn {
                            items(state.highlights, key = { it.id }) { product ->
                                ProductCard(product.title, stringResource(R.string.product_details, product.price.toString(), product.description))
                            }
                        }
                    }
                }
            }
        }
    }
}
````

**Al terminar este paso:**

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

### 4. Probar localmente y consumir la versión publicada

Las dependencias configuradas son UI `0.1.6` y SDK `0.2.2`. Sin opciones adicionales, la app resuelve ambas desde GitHub Packages. `android-sdk` es la única dependencia directa del SDK; Gradle también descarga `android-data` y `android-domain` declarados por su POM.

Para verificar cambios locales antes de publicar, desde cada proyecto de biblioteca ejecuta:

```bash
(cd ../01-shared-ui-library && ./gradlew publishToMavenLocal)
(cd ../02-shared-android-sdk && ./gradlew publishToMavenLocal)
```

Vuelve a la raíz de esta app y compila usando el flag que activa `mavenLocal()` antes de GitHub Packages:

```bash
./gradlew -PuseMavenLocal=true :app:testDebugUnitTest :app:assembleDebug
```

Para instalarla en un emulador con internet, usa `./gradlew -PuseMavenLocal=true :app:installDebug`. Las carpetas de las cuatro partes del laboratorio deben ser hermanas dentro de `redbull` para que las rutas relativas funcionen.

Para regresar al paquete publicado, ejecuta los mismos comandos sin `-PuseMavenLocal=true`; las versiones `0.1.6` y `0.2.2` del catálogo ya apuntan a los paquetes publicados. Si pruebas una versión candidata distinta, asegúrate de que la versión del catálogo coincida con la que publicaste en Maven Local, y luego actualízala a la versión publicada en GitHub Packages.

### 5. Configurar el repo separado y su CI

Concede a este repositorio acceso `Read` a los paquetes UI y SDK (y a los módulos transitivos del SDK si GitHub los muestra como paquetes separados). Los repositorios Maven predeterminados apuntan a `briantylerec/shared-ui-library` y `briantylerec/shared-sdk`; el workflow usa `GITHUB_TOKEN` con `packages: read` y compila en cada PR y push.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 13 — Crea o actualiza `.github/workflows/android-ci.yml`

**En el proyecto de la aplicación Events, en la ruta `.github/workflows/android-ci.yml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Da acceso de lectura a GitHub Packages y compila la app para cada push y pull request usando las versiones publicadas configuradas.

````yaml
name: Events app CI

on:
  push:
  pull_request:

permissions:
  contents: read
  packages: read

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: android-actions/setup-android@v4
      - name: Install Android API 36
        run: sdkmanager "platforms;android-36" "build-tools;36.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - name: Resolve shared packages and build app
        env:
          GITHUB_ACTOR: ${{ github.actor }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: ./gradlew :app:testDebugUnitTest :app:assembleDebug
````

#### Subpaso 14 — Crea o actualiza `.gitignore`

**En el proyecto de la aplicación Events, en la ruta `.gitignore`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Ignora salidas de build, cachés, configuración local y archivos del IDE. `gradle.properties` se versiona porque contiene solo opciones no secretas del build.

````text
**/.gradle/
**/build/
local.properties
*.iml
.idea/
.DS_Store
````

#### Subpaso 15 — Crea o actualiza `README.md`

**En el proyecto de la aplicación Events, en la ruta `README.md`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Describe el propósito, uso, coordenadas y publicación de este repositorio.

````text
# Events app

Compose + MVVM consumer app with `data`, `domain`, `presentation` and Hilt wiring. Its domain selects up to six highlights from the catalog provided by the shared SDK. The list is demo business logic over DummyJSON products, not a real events backend.

## Configure shared packages

Las dependencias directas de esta app son `com.redbull.lab:shared-ui:0.1.6` y `com.redbull.lab:android-sdk:0.2.2`. `android-sdk` es la fachada pública (`RedBullSdk`); `android-data` y `android-domain` se resuelven como dependencias transitivas desde `briantylerec/shared-sdk`.

Los endpoints Maven predeterminados son `briantylerec/shared-ui-library` y `briantylerec/shared-sdk`. Concede a este repositorio acceso `Read` a ambos packages y a los módulos SDK si GitHub los lista por separado. CI usa `GITHUB_TOKEN` con `packages: read`.

Para probar bibliotecas locales, ejecuta `./gradlew publishToMavenLocal` en ambos proyectos y compila con `./gradlew -PuseMavenLocal=true :app:testDebugUnitTest :app:assembleDebug`. Al omitir el flag, Gradle usa las versiones publicadas.

Para descargar paquetes de GitHub desde tu equipo, guarda tu usuario y PAT classic con `read:packages` en `~/.gradle/gradle.properties` (fuera del repositorio):

```properties
gpr.user=TU_USUARIO_GITHUB
gpr.key=TU_PAT_CLASSIC
```

Nunca guardes el token en el proyecto ni en Git.

**Al terminar este paso:**

```bash
git status --short
```

## Publicación y consumo de paquetes

Este repositorio de app es independiente de `shared-ui-library` y `shared-sdk`; no incluye sus fuentes ni los compila como proyectos Gradle. Las versiones directas actuales son `shared-ui:0.1.6` y `android-sdk:0.2.2`. Los endpoints Maven en `settings.gradle.kts` apuntan a `briantylerec/shared-ui-library` y `briantylerec/shared-sdk`.

Concede a este repositorio acceso **Read** en los paquetes UI y SDK. El SDK declara `android-data` y `android-domain` como dependencias transitivas; si GitHub los muestra como paquetes separados, concede también acceso a esos paquetes. El workflow descarga con `GITHUB_TOKEN` y `packages: read`.

Para cambiar una dependencia a otra release, actualiza su versión en `gradle/libs.versions.toml` después de que se publique. Para probar fuentes locales, publica UI y SDK con `publishToMavenLocal` y ejecuta Gradle con `-PuseMavenLocal=true`. En uso normal no pases ese flag; así se consumen las versiones de GitHub Packages. Guarda el PAT local solo en `~/.gradle/gradle.properties`, nunca en el repositorio.

## Comandos útiles desde este proyecto

```bash
./gradlew --version
./gradlew clean
```

Si estás en Windows sustituye `./gradlew` por `gradlew.bat`. Si Gradle no encuentra una biblioteca local, publícala primero desde su propio repo y confirma que la versión de la app coincide con la publicada.


## Compatibilidad de Hilt con Kotlin 2.3

El catálogo fija Hilt 2.58 y Kotlin 2.3.21. `kotlin-metadata-jvm` debe estar en la configuración `kapt` para que el procesador pueda leer los metadatos de Kotlin. En este laboratorio queda como `kapt(libs.kotlin.metadata)`. Dagger describe este error y su arreglo en [issue #5001](https://github.com/google/dagger/issues/5001). Se mantiene Hilt 2.58 porque la versión 2.59+ de su Gradle plugin requiere AGP 9; este proyecto usa AGP 8.13.2 para una actualización contenida.
