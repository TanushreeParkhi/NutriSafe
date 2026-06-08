package com.priveat.app.service;

import android.content.Context;

import com.priveat.app.data.db.AppDatabase;
import com.priveat.app.data.model.MealLog;
import com.priveat.app.util.AppExecutors;

import java.util.List;

public class MealRepository {
    public interface LoadMealsCallback {
        void onLoaded(List<MealLog> mealLogs);
    }

    public interface CaloriesCallback {
        void onLoaded(int calories);
    }

    private final AppDatabase appDatabase;

    public MealRepository(Context context) {
        appDatabase = AppDatabase.getInstance(context);
    }

    public void insert(MealLog mealLog) {
        AppExecutors.io().execute(() -> appDatabase.mealLogDao().insert(mealLog));
    }

    public void loadMeals(LoadMealsCallback callback) {
        AppExecutors.io().execute(() -> callback.onLoaded(appDatabase.mealLogDao().getAll()));
    }

    public void loadTodayCalories(long dayStart, CaloriesCallback callback) {
        AppExecutors.io().execute(() -> callback.onLoaded(appDatabase.mealLogDao().getCaloriesSince(dayStart)));
    }
}
