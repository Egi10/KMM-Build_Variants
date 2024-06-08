package id.buaja.kmm_buildvariants

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform