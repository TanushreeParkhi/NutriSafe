package com.priveat.app.ui.meal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.priveat.app.databinding.FragmentMealLogBinding;
import com.priveat.app.service.GeminiService;
import com.priveat.app.service.MealRepository;
import com.priveat.app.ui.adapter.MealLogAdapter;
import com.priveat.app.util.PreferenceRepository;

public class MealLogFragment extends Fragment {
    private FragmentMealLogBinding binding;
    private MealLogAdapter adapter;
    private GeminiService geminiService;
    private MealRepository mealRepository;
    private PreferenceRepository preferenceRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap bitmap = extras == null ? null : (Bitmap) extras.get("data");
                    if (bitmap != null) {
                        analyze(bitmap);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMealLogBinding.inflate(inflater, container, false);
        geminiService = new GeminiService();
        mealRepository = new MealRepository(requireContext());
        preferenceRepository = new PreferenceRepository(requireContext());
        adapter = new MealLogAdapter();
        binding.recyclerMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMeals.setAdapter(adapter);
        binding.buttonCaptureMeal.setOnClickListener(v -> launchCamera());
        loadMeals();
        return binding.getRoot();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void analyze(Bitmap bitmap) {
        binding.progressAnalyze.setVisibility(View.VISIBLE);
        geminiService.analyzeMeal(bitmap, preferenceRepository.getConditions(), new GeminiService.MealAnalysisCallback() {
            @Override
            public void onSuccess(com.priveat.app.data.model.MealLog mealLog) {
                mealRepository.insert(mealLog);
                mainHandler.post(() -> {
                    binding.progressAnalyze.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Meal saved", Toast.LENGTH_SHORT).show();
                    loadMeals();
                });
            }

            @Override
            public void onError(Throwable throwable) {
                mainHandler.post(() -> {
                    binding.progressAnalyze.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadMeals() {
        mealRepository.loadMeals(mealLogs -> mainHandler.post(() -> adapter.submitList(mealLogs)));
    }
}
