package com.datartproject.dataart.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class Action(
    @SerialName("key")
    val key: String,
    @SerialName("user_key")
    val userKey: String,
    @SerialName("is_anonymous_user")
    val isAnonymousUser: Boolean,
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Date,
    @SerialName("metadata")
    val metadata: Map<String, String>,
)
fun Action.toJsonString(): String = Json.encodeToString(this)

@Serializable
data class ActionsContainer(
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Date,
    @SerialName("actions")
    val actions: List<Action>
)
fun ActionsContainer.toJsonString(): String = Json.encodeToString(this)


@Serializable
data class Identity(
    @SerialName("user_key")
    val userKey: String,
    @SerialName("metadata")
    val metadata: Map<String, String>,
)
fun Identity.toJsonString(): String = Json.encodeToString(this)
