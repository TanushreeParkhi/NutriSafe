package com.priveat.app;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.priveat.app.databinding.ActivityMainBinding;
import com.priveat.app.ui.security.AuthLockFragment;
import com.priveat.app.util.PreferenceRepository;

public class MainActivity extends AppCompatActivity implements AuthLockFragment.AuthSuccessListener {
    private ActivityMainBinding binding;
    private PreferenceRepository preferenceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceRepository = new PreferenceRepository(this);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = binding.bottomNav;
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        maybeShowLockScreen();
    }

    private void maybeShowLockScreen() {
        if (preferenceRepository.isPrivacyLockEnabled() && !preferenceRepository.isAuthenticated()) {
            binding.authOverlayContainer.setVisibility(View.VISIBLE);
            Fragment fragment = AuthLockFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_overlay_container, fragment)
                    .commitNowAllowingStateLoss();
        } else {
            binding.authOverlayContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAuthSuccess() {
        preferenceRepository.setAuthenticated(true);
        binding.authOverlayContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferenceRepository.setAuthenticated(false);
    }
}
