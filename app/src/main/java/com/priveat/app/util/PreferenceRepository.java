package com.priveat.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PreferenceRepository {
    public static final String PREFS_NAME = "priveat_preferences";
    public static final String KEY_AUTH = "priveat_auth";
    public static final String KEY_PRIVACY_LOCK = "priveat_privacy_lock";
    public static final String KEY_PRIVACY_BLUR = "priveat_privacy_blur";
    public static final String KEY_DIET = "priveat_prefs_diet";
    public static final String KEY_CONDITIONS = "priveat_prefs_conditions";
    public static final String KEY_WATER = "priveat_water_count";
    public static final String KEY_PIN = "priveat_local_pin";

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public PreferenceRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAuthenticated() {
        return sharedPreferences.getBoolean(KEY_AUTH, false);
    }

    public void setAuthenticated(boolean authenticated) {
        sharedPreferences.edit().putBoolean(KEY_AUTH, authenticated).apply();
    }

    public boolean isPrivacyLockEnabled() {
        return sharedPreferences.getBoolean(KEY_PRIVACY_LOCK, true);
    }

    public void setPrivacyLockEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_PRIVACY_LOCK, enabled).apply();
    }

    public boolean isPrivacyBlurEnabled() {
        return sharedPreferences.getBoolean(KEY_PRIVACY_BLUR, false);
    }

    public void setPrivacyBlurEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_PRIVACY_BLUR, enabled).apply();
    }

    public String getDietPreference() {
        return sharedPreferences.getString(KEY_DIET, "veg");
    }

    public void setDietPreference(String diet) {
        sharedPreferences.edit().putString(KEY_DIET, diet).apply();
    }

    public List<String> getConditions() {
        String raw = sharedPreferences.getString(KEY_CONDITIONS, "[]");
        Type type = new TypeToken<ArrayList<String>>() { }.getType();
        List<String> conditions = gson.fromJson(raw, type);
        return conditions == null ? new ArrayList<>() : conditions;
    }

    public void addCondition(String condition) {
        List<String> conditions = getConditions();
        if (!conditions.contains(condition)) {
            conditions.add(condition);
            sharedPreferences.edit().putString(KEY_CONDITIONS, gson.toJson(conditions)).apply();
        }
    }

    public int getWaterCount() {
        return sharedPreferences.getInt(KEY_WATER, 0);
    }

    public void setWaterCount(int count) {
        sharedPreferences.edit().putInt(KEY_WATER, count).apply();
    }

    public String getPin() {
        return sharedPreferences.getString(KEY_PIN, "1234");
    }
}
