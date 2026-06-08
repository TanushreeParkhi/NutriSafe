package com.priveat.app.ui.security;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.priveat.app.databinding.FragmentAuthLockBinding;
import com.priveat.app.util.PreferenceRepository;

import java.util.concurrent.Executor;

public class AuthLockFragment extends Fragment implements View.OnClickListener {
    public interface AuthSuccessListener {
        void onAuthSuccess();
    }

    private FragmentAuthLockBinding binding;
    private PreferenceRepository preferenceRepository;
    private AuthSuccessListener authSuccessListener;
    private final StringBuilder pinBuilder = new StringBuilder();

    public static AuthLockFragment newInstance() {
        return new AuthLockFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AuthSuccessListener) {
            authSuccessListener = (AuthSuccessListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthLockBinding.inflate(inflater, container, false);
        preferenceRepository = new PreferenceRepository(requireContext());
        setupPad();
        binding.buttonBiometric.setOnClickListener(v -> promptBiometric());
        return binding.getRoot();
    }

    private void setupPad() {
        for (int i = 0; i < binding.pinGrid.getChildCount(); i++) {
            View child = binding.pinGrid.getChildAt(i);
            if (child instanceof MaterialButton) {
                child.setOnClickListener(this);
            }
        }
        binding.buttonDelete.setOnClickListener(v -> {
            if (pinBuilder.length() > 0) {
                pinBuilder.deleteCharAt(pinBuilder.length() - 1);
                renderPin();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            pinBuilder.append(((TextView) v).getText());
            renderPin();
            if (pinBuilder.length() == 4) {
                validatePin();
            }
        }
    }

    private void renderPin() {
        String[] dots = {"", "•", "••", "•••", "••••"};
        binding.pinIndicator.setText(dots[pinBuilder.length()]);
    }

    private void validatePin() {
        if (TextUtils.equals(pinBuilder.toString(), preferenceRepository.getPin())) {
            if (authSuccessListener != null) {
                authSuccessListener.onAuthSuccess();
            }
        } else {
            Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
            pinBuilder.setLength(0);
            renderPin();
        }
    }

    private void promptBiometric() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (authSuccessListener != null) {
                            authSuccessListener.onAuthSuccess();
                        }
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock PrivEat")
                .setSubtitle("Authenticate to open your local health vault")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}
