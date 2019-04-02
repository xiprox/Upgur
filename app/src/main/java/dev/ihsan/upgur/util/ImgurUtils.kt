package dev.ihsan.upgur.util

object ImgurUtils {
    /**
     * Takes in cover image id and returns imgur url for the image
     */
    fun coverImageUrlFromId(id: String) = "https://i.imgur.com/$id.jpg"
}