pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(url = "https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")
        maven(url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/"))
        mavenCentral()
    }
}

rootProject.name = "TPCafeLog"
include(":app")
