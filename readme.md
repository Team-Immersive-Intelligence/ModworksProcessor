<p align="center">
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Team-Immersive-Intelligence/ModworksProcessor/main/.github/logo_dark.png">
  <img alt="ModWorks Logo" src="https://raw.githubusercontent.com/Team-Immersive-Intelligence/ModworksProcessor/main/.github/logo.png">
</picture>
</p>

[![Discord](https://img.shields.io/discord/610912351142674434?logo=discord&logoColor=ffffff&label=Discord&color=7289da)](https://discord.gg/teMfm3R)
[![Jitpack](https://jitpack.io/v/Team-Immersive-Intelligence/ModworksProcessor.svg)](https://jitpack.io/#Team-Immersive-Intelligence/ModworksProcessor)
[![Support me on Patreon](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.vercel.app%2Fapi%3Fusername%3Dpabilo8%26type%3Dpatrons&style=flat&logoColor=ffffff&label=Patreon)](https://www.patreon.com/bePatron?u=34304036)
[![Tests Passing](https://github.com/Team-Immersive-Intelligence/ModworksProcessor/actions/workflows/tests.yml/badge.svg)](#)

### Description

**ModWorks** is an annotation processor and resource/code generator designed for minecraft mods, in particular our 1.12.2 addon to Immersive Engineering called Immersive Intelligence.
In its current state the processor offers:
- [x] `sounds.json` file generation based on annotated SoundEvent variables
- [x] `mcmod.info` file generation based on data passed by gradle build script
- [x] `json` file generation for simple item models
- [ ] `json` file generation for simple blockstates

### Usage

**ModWorks** can be used as a part of Team II's Mod Setup Script Mk.4/5 or standalone.

To use **ModWorks** in a generic Minecraft Mod environment add
```groovy
sourceSets.main.java.srcDirs+=['src/main/generated/java']
sourceSets.main.resources.srcDirs+=['src/main/generated/resources']

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    annotationProcessor 'com.github.Team-Immersive-Intelligence:ModworksProcessor:1.1.2'
    compileOnly 'com.github.Team-Immersive-Intelligence:ModworksProcessor:1.1.2'
}

compileJava {
    options.annotationProcessorGeneratedSourcesDirectory=project.file("src/main/generated")
    options.compilerArgs.addAll([
            '-Amodworks.modid=MODNAME', //MOD ID HERE
            '-Amodworks.resourcedir=resources', //RESOURCE FOLDER NAME
            '-Amodworks.javadir=java' //JAVA FOLDER NAME
    ])
}
```

to your `build.gradle` script.

To activate **ModWorks** in a **Mod Setup Script Mk.4/5** environment change the `ii_useModworksPreprocessor` option to `true`.

<hr>

#### In case you want to use it, please contact @Pabilo8 or another Team II member to get proper support, we're happy when someone wants to use our technology and will give support in integrating it.
#### Feel free to join the mod's [Discord Server](https://discord.gg/teMfm3R) to have a chat with the devs and community.
#### Please report any bugs you find in the 'issues' tab.
