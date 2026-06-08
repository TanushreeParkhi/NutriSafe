package com.priveat.app.ui.privacy;

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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.priveat.app.databinding.FragmentPrivacyVaultBinding;
import com.priveat.app.service.GeminiService;
import com.priveat.app.util.PreferenceRepository;

public class PrivacyVaultFragment extends Fragment {
    private FragmentPrivacyVaultBinding binding;
    private PreferenceRepository preferenceRepository;
    private GeminiService geminiService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> prescriptionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) {
                        binding.progressPrescription.setVisibility(View.VISIBLE);
                        geminiService.scanPrescription(bitmap, new GeminiService.ConditionsCallback() {
                            @Override
                            public void onSuccess(java.util.List<String> conditions) {
                                mainHandler.post(() -> {
                                    for (String condition : conditions) {
                                        preferenceRepository.addCondition(condition);
                                    }
                                    binding.progressPrescription.setVisibility(View.GONE);
                                    refreshConditions();
                                });
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                mainHandler.post(() -> {
                                    binding.progressPrescription.setVisibility(View.GONE);
                                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrivacyVaultBinding.inflate(inflater, container, false);
        preferenceRepository = new PreferenceRepository(requireContext());
        geminiService = new GeminiService();
        setupViews();
        return binding.getRoot();
    }

    private void setupViews() {
        binding.switchPrivacyLock.setChecked(preferenceRepository.isPrivacyLockEnabled());
        binding.switchPrivacyBlur.setChecked(preferenceRepository.isPrivacyBlurEnabled());
        binding.switchPrivacyLock.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferenceRepository.setPrivacyLockEnabled(isChecked));
        binding.switchPrivacyBlur.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferenceRepository.setPrivacyBlurEnabled(isChecked));

        String[] diets = {"veg", "non-veg", "veg+egg", "jain"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, diets);
        binding.inputDiet.setAdapter(adapter);
        binding.inputDiet.setText(preferenceRepository.getDietPreference(), false);
        binding.inputDiet.setOnItemClickListener((parent, view, position, id) ->
                preferenceRepository.setDietPreference(diets[position]));

        binding.buttonAddCondition.setOnClickListener(v -> {
            String condition = binding.inputCondition.getText() == null
                    ? ""
                    : binding.inputCondition.getText().toString().trim();
            if (!condition.isEmpty()) {
                preferenceRepository.addCondition(condition);
                binding.inputCondition.setText(null);
                refreshConditions();
            }
        });
        binding.buttonScanPrescription.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            prescriptionLauncher.launch(intent);
        });
        refreshConditions();
    }

    private void refreshConditions() {
        binding.textConditions.setText(android.text.TextUtils.join("\n", preferenceRepository.getConditions()));
    }
}
