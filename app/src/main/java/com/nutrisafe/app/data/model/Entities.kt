package com.nutrisafe.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val imageUri: String? = null,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val riskScore: Int,
    val safetyScore: Int,
    val finalAction: String,
    val freshnessStatus: String,
    val freshnessNotes: String,
    val additives: String,
    val shelfLife: String,
    val safetyExplanation: String,
    val processedClassification: String,
    val microbialRisk: String,
    val allergenWarnings: String,
    val diseaseWarnings: String,
    val sourceType: String = "home",
    val storageNotes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val label: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderType: String,
    val message: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "leftover_timers")
data class LeftoverTimerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val mealName: String,
    val startedAt: Long,
    val expiresAt: Long,
    val action: String,
    val active: Boolean = true
)

