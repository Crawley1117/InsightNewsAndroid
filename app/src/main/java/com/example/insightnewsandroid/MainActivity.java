
package com.example.insightnewsandroid;

import android.content.Intent;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.auth.WelcomeActivity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.insightnewsandroid.ui.home.HomeFragment;
import com.example.insightnewsandroid.ui.explore.ExploreFragment;
import com.example.insightnewsandroid.ui.profile.ProfileFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthRepository authRepo = new AuthRepository(this);
        if (!authRepo.isLoggedIn()) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);


        // 初始化 Fragment
        homeFragment = new HomeFragment();
        exploreFragment = new ExploreFragment();
        profileFragment = new ProfileFragment();

        // 默认显示 Home
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, homeFragment)
                .commit();

        // ✅ 关键：ID 必须和 XML 一致
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.navigation_home) {
            selectedFragment = homeFragment;
        } else if (item.getItemId() == R.id.navigation_explore) {
            selectedFragment = exploreFragment;
        } else if (item.getItemId() == R.id.navigation_profile) {
            selectedFragment = profileFragment;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, selectedFragment)
                    .commit();
            return true;
        }
        return false;
    }
}