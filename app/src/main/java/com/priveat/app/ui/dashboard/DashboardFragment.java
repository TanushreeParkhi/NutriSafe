package com.priveat.app.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.priveat.app.databinding.FragmentDashboardBinding;
import com.priveat.app.service.MealRepository;
import com.priveat.app.util.PreferenceRepository;

import java.util.Calendar;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private PreferenceRepository preferenceRepository;
    private MealRepository mealRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        preferenceRepository = new PreferenceRepository(requireContext());
        mealRepository = new MealRepository(requireContext());
        binding.buttonWaterPlus.setOnClickListener(v -> incrementWater());
        binding.blurOverlay.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                binding.blurOverlay.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
        loadStats();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        int water = preferenceRepository.getWaterCount();
        binding.textWaterCount.setText(String.valueOf(water));
        binding.blurOverlay.setVisibility(preferenceRepository.isPrivacyBlurEnabled() ? View.VISIBLE : View.GONE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        mealRepository.loadTodayCalories(calendar.getTimeInMillis(), calories -> mainHandler.post(() -> {
            binding.textCalories.setText(calories + " kcal");
            binding.calorieRingView.setProgress(calories / 2200f);
        }));
    }

    private void incrementWater() {
        int next = preferenceRepository.getWaterCount() + 1;
        preferenceRepository.setWaterCount(next);
        binding.textWaterCount.setText(String.valueOf(next));
    }
}
