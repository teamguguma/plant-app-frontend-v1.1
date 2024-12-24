package com.guguma.guguma_application.dto

data class PlantDto (
    val id: Long, // 추가된 필드
    val createDate: String,
    val name: String,
    val checkdate: Int,
    val nickname: String,
    val remedy: String,
    val imageUrl: String,
)