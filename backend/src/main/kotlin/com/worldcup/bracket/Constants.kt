object Constants {
    const val BASE_API = "https://v3.football.api-sports.io/"
    const val FIXTURES_API = BASE_API + "fixtures?season=2022&league=1"
    const val X_RAPID_API_HOST = "v3.football.api-sports.io"
    @JvmField // https://stackoverflow.com/questions/46482576/java-static-final-in-kotlin-const-val-initializer-should-be-a-constant-value
    val FOOTBALL_API_KEY = System.getenv("FOOTBALL_API_KEY")
}