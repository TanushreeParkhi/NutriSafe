package com.priveat.app.data.repository

import com.priveat.app.data.local.HealthDao
import com.priveat.app.data.model.HealthRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HealthRepository(private val healthDao: HealthDao) {
    fun observeRecords(): Flow<List<HealthRecordEntity>> = healthDao.observeRecords()

    fun observeAllergies(): Flow<List<String>> = healthDao.observeRecords().map { records ->
        records.filter { it.type == TYPE_ALLERGY }.map { it.label }
    }

    fun observeConditions(): Flow<List<String>> = healthDao.observeRecords().map { records ->
        records.filter { it.type == TYPE_CONDITION }.map { it.label }
    }

    suspend fun addCondition(label: String, notes: String = "") {
        if (label.isNotBlank()) healthDao.insert(HealthRecordEntity(type = TYPE_CONDITION, label = label.trim(), notes = notes))
    }

    suspend fun addAllergy(label: String, notes: String = "") {
        if (label.isNotBlank()) healthDao.insert(HealthRecordEntity(type = TYPE_ALLERGY, label = label.trim(), notes = notes))
    }

    suspend fun deleteRecord(id: Long) = healthDao.deleteById(id)

    suspend fun allergies(): List<String> = healthDao.recordsByType(TYPE_ALLERGY).map { it.label }
    suspend fun conditions(): List<String> = healthDao.recordsByType(TYPE_CONDITION).map { it.label }

    suspend fun wipe() = healthDao.clear()

    companion object {
        const val TYPE_CONDITION = "condition"
        const val TYPE_ALLERGY = "allergy"
    }
}
