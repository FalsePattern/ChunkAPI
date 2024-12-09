plugins {
    id("fpgradle-minecraft") version("0.8.3")
}

group = "com.falsepattern"

minecraft_fp {
    mod {
        modid = "chunkapi"
        name = "ChunkAPI"
        rootPkg = "$group.chunk"
    }

    api {
        packages = listOf("api")
    }

    mixin {
        pkg = "internal.mixin.mixins"
        pluginClass = "internal.mixin.plugin.MixinPlugin"
    }

    core {
        coreModClass = "internal.core.CoreLoadingPlugin"
        accessTransformerFile = "chunkapi_at.cfg"
    }

    tokens {
        tokenClass = "internal.Tags"
    }

    publish {
        changelog = "https://github.com/FalsePattern/ChunkAPI/releases/tag/$version"
        maven {
            repoUrl = "https://mvn.falsepattern.com/releases/"
            repoName = "mavenpattern"
        }
        curseforge {
            projectId = "844484"
            dependencies {
                required("fplib")
            }
        }
        modrinth {
            projectId = "y0vBUOla"
            dependencies {
                required("fplib")
            }
        }
    }
}

repositories {
    exclusive(maven("mavenpattern", "https://mvn.falsepattern.com/releases/"), "com.falsepattern")
}

dependencies {
    apiSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.4.4")
}