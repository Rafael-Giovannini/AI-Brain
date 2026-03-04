package app.ghostfit.data.model

data class FeedbackRecord(
    val sessionId: String,
    val thumbsUp: Boolean,
    val modelUsed: String,
    val garmentCategory: String,
    val timestamp: Long = System.currentTimeMillis()
)
