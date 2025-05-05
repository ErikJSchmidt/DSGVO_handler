plugins {
    application
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // recommended by Felix, not used yet
    implementation(group="de.bwaldvogel", name="liblinear", version="2.44" )

    // Used for tokenizer, could maybe be removed as long as I stay with simple tokenization and no further preprocessing
    implementation(group="org.apache.opennlp", name="opennlp-tools", version="2.1.1")

    // tyr for SVM
    implementation("com.github.haifengl:smile-kotlin:3.0.1")
    // Weka seems to be widely used for NLP with java, it also has a wrapper for LIBSVM
    //implementation(group="nz.ac.waikato.cms.weka", name="weka-stable", version="3.8.0")
    //implementation(group = "nz.ac.waikato.cms.weka", name="LibSVM", version="1.0.4" )

    // raw LIBSVM
    implementation(group="tw.edu.ntu.csie", name="libsvm", version="3.31")

    // For json serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0-RC")

    // Jsoup used for HTML DOM tree traversal
    implementation("org.jsoup:jsoup:1.14.3")

    // Nd4j used for vector calculations
    implementation(group="org.nd4j", name="nd4j", version="0.9.1")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}