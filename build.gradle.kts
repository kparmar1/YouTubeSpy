plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.apis:google-api-services-youtube:v3-rev20230816-2.0.0")
    implementation("com.google.api-client:google-api-client:2.9.0")
    implementation("org.apache.velocity:velocity-engine-core:2.4.1")
    implementation("commons-cli:commons-cli:1.11.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.self.youtube.spy.YouTubeSpy")
}

tasks.test {
    useJUnitPlatform()
}

val runtimeClasspath = configurations.named<Configuration>("runtimeClasspath")

tasks.register<Jar>("fatJar") {
    archiveFileName.set("YouTubeSpy.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "org.self.youtube.spy.YouTubeSpy"
        )
    }
    from(runtimeClasspath.map { it.map { zipTree(it) } })
    from(sourceSets.main.get().output)
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}