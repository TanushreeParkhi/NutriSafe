package com.priveat.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_logs")
public class MealLog {
    @PrimaryKey
    @NonNull
    public String id;
    public long timestamp;
    public String imageUrl;
    public String foodName;
    public int calories;
    public double protein;
    public double carbs;
    public double fats;
    public String freshness;
    public String shelfLife;
    public boolean hasColorings;
    public int riskScore;
    public String riskReason;
}
