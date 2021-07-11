package com.datartproject.dataart.client

inline fun <T: Any> ifLet(vararg elements: T?, onNotNull: (List<T>) -> Unit) {
    if (elements.all { it != null }) {
        onNotNull(elements.filterNotNull())
    }
}