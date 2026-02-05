package com.sonicflow.app.core.domain.model

data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int
)