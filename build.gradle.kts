plugins {
    id("java")
}

group = "org.login"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Adding MariaDB JDBC driver as an implementation dependency
    implementation(files("libs/mariadb-java-client-3.3.2.jar"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}