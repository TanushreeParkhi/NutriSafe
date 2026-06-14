package com.priveat.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.priveat.app.data.model.ChatMessageEntity
import com.priveat.app.data.model.HealthRecordEntity
import com.priveat.app.data.model.LeftoverTimerEntity
import com.priveat.app.data.model.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY createdAt DESC")
    fun observeMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE createdAt >= :since ORDER BY createdAt DESC")
    suspend fun getMealsSince(since: Long): List<MealEntity>

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealEntity): Long

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meals")
    suspend fun clear()
}

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_records ORDER BY createdAt DESC")
    fun observeRecords(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE type = :type ORDER BY label")
    suspend fun recordsByType(type: String): List<HealthRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecordEntity): Long

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM health_records")
    suspend fun clear()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC")
    fun observeMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clear()
}

@Dao
interface LeftoverDao {
    @Query("SELECT * FROM leftover_timers WHERE active = 1 ORDER BY expiresAt ASC")
    fun observeTimers(): Flow<List<LeftoverTimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: LeftoverTimerEntity): Long

    @Query("DELETE FROM leftover_timers")
    suspend fun clear()
}

@Database(
    entities = [MealEntity::class, HealthRecordEntity::class, ChatMessageEntity::class, LeftoverTimerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PrivEatDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun healthDao(): HealthDao
    abstract fun chatDao(): ChatDao
    abstract fun leftoverDao(): LeftoverDao
}
