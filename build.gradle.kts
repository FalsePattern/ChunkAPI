plugins {
    id("com.falsepattern.fpgradle-mc") version("0.17.1")
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
        changelog = "https://github.com/LegacyModdingMC/ChunkAPI/releases/tag/$version"
        maven {
            repoUrl = "https://mvn.falsepattern.com/releases/"
            repoName = "mavenpattern"
        }
        curseforge {
            projectId = "844484"
        }
        modrinth {
            projectId = "y0vBUOla"
        }
    }
}

repositories {
    cursemavenEX()
}

dependencies {
    //LookingGlass 0.2.0.01
    compileOnly(deobfCurse("lookingglass-230541:2321557"))
}