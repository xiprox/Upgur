package dev.ihsan.upgur.data.remote

/**
 * Exception thrown when the Imgur API returns an error response.
 */
class ImgurException(
    val statusCode: Int?,
    val msg: String?
) : Throwable() {

    override val message: String?
        get() = "$statusCode - ${msg ?: "Unknown cause"}"
}