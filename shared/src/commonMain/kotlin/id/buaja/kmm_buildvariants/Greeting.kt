package id.buaja.kmm_buildvariants

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}! - ${BuildKonfig.name}"
    }
}