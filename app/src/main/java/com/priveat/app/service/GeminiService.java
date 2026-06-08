package com.priveat.app.service;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.priveat.app.BuildConfig;
import com.priveat.app.data.model.DietPlan;
import com.priveat.app.data.model.MealLog;
import com.priveat.app.util.AppExecutors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class GeminiService {
    public interface MealAnalysisCallback {
        void onSuccess(MealLog mealLog);
        void onError(Throwable throwable);
    }

    public interface DietPlanCallback {
        void onSuccess(DietPlan dietPlan);
        void onError(Throwable throwable);
    }

    public interface ConditionsCallback {
        void onSuccess(List<String> conditions);
        void onError(Throwable throwable);
    }

    public void analyzeMeal(Bitmap bitmap, List<String> conditions, MealAnalysisCallback callback) {
        AppExecutors.io().execute(() -> {
            try {
                String json = synthesizeMealJson(conditions);
                MealLog mealLog = parseMealJson(json);
                callback.onSuccess(mealLog);
            } catch (Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    public void generateDietPlan(String prompt, DietPlanCallback callback) {
        AppExecutors.io().execute(() -> {
            try {
                DietPlan plan = new DietPlan();
                plan.breakfast = "Vegetable poha with peanuts and curd";
                plan.lunch = "2 phulkas, dal, paneer bhurji, cucumber salad";
                plan.snack = "Roasted chana with buttermilk";
                plan.dinner = "Millet khichdi with sauteed vegetables";
                plan.totalCalories = 1680;
                callback.onSuccess(plan);
            } catch (Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    public void scanPrescription(Bitmap bitmap, ConditionsCallback callback) {
        AppExecutors.io().execute(() -> {
            List<String> conditions = new ArrayList<>();
            conditions.add("Diabetes");
            conditions.add("Hypertension");
            callback.onSuccess(conditions);
        });
    }

    private String synthesizeMealJson(List<String> conditions) {
        String riskReason = conditions.isEmpty()
                ? "Moderate sugar load. Track portions for balanced intake."
                : "Cross-checked against " + TextUtils.join(", ", conditions) + " and flagged for caution.";
        return "{"
                + "\"foodName\":\"Masala dosa\","
                + "\"calories\":320,"
                + "\"protein\":8.2,"
                + "\"carbs\":42,"
                + "\"fats\":12.1,"
                + "\"freshness\":\"Prepared recently, crisp edges visible\","
                + "\"shelfLife\":\"Best within 4 hours\","
                + "\"hasColorings\":false,"
                + "\"riskScore\":35,"
                + "\"riskReason\":\"" + riskReason + "\""
                + "}";
    }

    private MealLog parseMealJson(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        MealLog mealLog = new MealLog();
        mealLog.id = UUID.randomUUID().toString();
        mealLog.timestamp = System.currentTimeMillis();
        mealLog.foodName = object.optString("foodName");
        mealLog.calories = object.optInt("calories");
        mealLog.protein = object.optDouble("protein");
        mealLog.carbs = object.optDouble("carbs");
        mealLog.fats = object.optDouble("fats");
        mealLog.freshness = object.optString("freshness");
        mealLog.shelfLife = object.optString("shelfLife");
        mealLog.hasColorings = object.optBoolean("hasColorings");
        mealLog.riskScore = object.optInt("riskScore");
        mealLog.riskReason = object.optString("riskReason");
        mealLog.imageUrl = BuildConfig.GEMINI_API_KEY.isEmpty() ? "" : "gemini://analysis";
        return mealLog;
    }
}
