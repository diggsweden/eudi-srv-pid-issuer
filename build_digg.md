# Build e

Preconditions:
Have a working, up-to-date run of wsl-dev-base

### settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://registry.digg.se/repository/gradle-plugins/")
        }
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
    }
}
```

### build.gradle.kts

```kotlin
repositories {

    maven {
        url = uri("https://registry.digg.se/repository/maven-public/")
    }
}
```

