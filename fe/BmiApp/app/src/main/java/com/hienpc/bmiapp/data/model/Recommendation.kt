package com.hienpc.bmiapp.data.model

/**
 * Recommendation response from AI
 */
data class RecommendationResponse(
    val items: List<RecommendationItem>,
    val explanation: String,
    val totalSimilarUsers: Int
)

data class RecommendationItem(
    val id: Int,
    val name: String,
    val reason: String,
    val score: Double,
    val popularityCount: Int,
    val unit: String,
    val calories: Int
)

