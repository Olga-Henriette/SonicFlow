package com.sonicflow.app.core.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val year: Int = 0,
    val artworkUri: String? = null
)