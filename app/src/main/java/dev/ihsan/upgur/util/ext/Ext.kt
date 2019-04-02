package dev.ihsan.upgur.util.ext

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Various extension functions for various purposes that don't really belong in a specific file.
 */

/**
 * Makes it possible to use functions with return types in places where [Unit] is expected.
 */
fun Any.unitify() {
    /* do nothing */
}

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
