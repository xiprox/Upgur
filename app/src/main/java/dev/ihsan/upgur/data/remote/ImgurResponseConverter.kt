package dev.ihsan.upgur.data.remote

import dev.ihsan.upgur.data.model.ImgurResponse
import okhttp3.ResponseBody
import retrofit2.Converter

/**
 * Converts [ImgurResponse] wrapped responses of type [T] into objects of type [T].
 *
 * Takes care of unwrapping at low level, making our higher level code much more pleasant.
 */
class ImgurResponseConverter<T>(private val converter: Converter<ResponseBody, ImgurResponse<T>>) :
    Converter<ResponseBody, T> {

    override fun convert(responseBody: ResponseBody): T {
        val response = converter.convert(responseBody)
        if (response?.status == 200) {
            return response.data
        } else {
            throw ImgurException(response?.status, response?.errorMessage)
        }
    }
}