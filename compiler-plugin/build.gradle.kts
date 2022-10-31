import tanvd.kosogor.proxy.publishJar

group = rootProject.group
version = rootProject.version

dependencies {
    val kotlinVersion by System.getProperties()
    val junitPlatformVersion = "1.9.0"

    kotlin("compiler")
        .let {
        compileOnly(it)
        testImplementation(it)
    }

    testRuntimeOnly("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-annotations-jvm:$kotlinVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-internal-test-framework:$kotlinVersion")
    testImplementation("junit:junit:4.12")

    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.junit.platform:junit-platform-commons:$junitPlatformVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    testImplementation("org.junit.platform:junit-platform-suite-api:$junitPlatformVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
    workingDir = project.rootDir

    doFirst {
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
        setLibraryProperty("org.jetbrains.kotlin.compiler", "kotlin-compiler")
    }
}

publishJar {}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path =
        project.configurations.testRuntimeClasspath
            .get()
            .files
            .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
            ?.absolutePath
            ?: return
    println("$propName: $path")
    systemProperty(propName, path)
}

tasks.create<JavaExec>("generateTests") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("org.example.compiler.plugin.GenerateTestsKt")
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main"))
        resources.setSrcDirs(listOf("src/main/resources"))
    }
    test {
        java.setSrcDirs(listOf("src/test/java", "src/test/kotlin"))
        resources.setSrcDirs(listOf("src/test/resources"))
    }
}