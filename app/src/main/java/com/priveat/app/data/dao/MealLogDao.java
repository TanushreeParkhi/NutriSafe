package com.priveat.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.priveat.app.data.model.MealLog;

import java.util.List;

@Dao
public interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MealLog mealLog);

    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    List<MealLog> getAll();

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_logs WHERE timestamp >= :dayStart")
    int getCaloriesSince(long dayStart);
}
