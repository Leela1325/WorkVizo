data class LoginResponse(
    val status: String,
    val message: String,
    val user: UserData?
)

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val dob: String
)
