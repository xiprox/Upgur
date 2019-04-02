package dev.ihsan.upgur.data.remote

// A generic class that contains data and status about loading this data.
class Resource<T>(
    val status: Status,
    var data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(message: String, data: T?): Resource<T> {
            return Resource(
                Status.ERROR,
                data,
                message
            )
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> empty(): Resource<T> {
            return Resource(Status.EMPTY, null, null)
        }
    }

    enum class Status { SUCCESS, ERROR, LOADING, EMPTY }
}