package wonton.utils

import androidx.collection.SparseArrayCompat
import androidx.core.util.ObjectsCompat

inline fun <reified T, reified R> sparseArrayOf(vararg triples: Triple<Int, T, R>): SparseArrayCompat<Pair<T, R>> {
    val result = SparseArrayCompat<Pair<T, R>>()
    triples.forEach {
        result.append(it.first, it.second to it.third)
    }
    return result
}

inline fun <reified T> sparseArrayOf(vararg pairs: Pair<Int, T>): SparseArrayCompat<T> {
    val result = SparseArrayCompat<T>()
    pairs.forEach {
        result.append(it.first, it.second)
    }
    return result
}

inline fun <reified N, reified T, reified R> tripleOf(first: N, second: T, third: R): Triple<N, T, R> = Triple(first, second, third)

fun Any.toStringCompat() = ObjectsCompat.toString(this, "no toString implementation")

fun Any?.hashCodeCompat() = ObjectsCompat.hashCode(this)