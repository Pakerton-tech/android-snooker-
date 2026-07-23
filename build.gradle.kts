plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

// Keystore path for release signing
// Load signing properties from a file that is NOT committed to git
ext {
    val keystorePath: String by extra(rootProject.file("release/snooker-release.keystore").absolutePath)
    val keystorePass: String by extra("Sn00kerRel")
    val keyAlias: String by extra("snooker")
    val keyPass: String by extra("Sn00kerRel")
}
