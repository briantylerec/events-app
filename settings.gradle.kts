pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }

val sharedUiPackageRepository = providers.gradleProperty("gpr.sharedUiRepository")
    .orElse("briantylerec/shared-ui-library")
    .get()
val sharedSdkPackageRepository = providers.gradleProperty("gpr.sharedSdkRepository")
    .orElse("briantylerec/shared-sdk")
    .get()
val packageUsername = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
val packageToken = System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.key").orNull

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
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
