package com.example.mosque;

import static com.example.mosque.NavigationActivity.isAdmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.mosque.ui.gallery.ContactFragment;
import com.example.mosque.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class NavigationActivity extends AppCompatActivity {
    private static final int ADMIN_TRIGGER_TAPS = 5;
    private int contactTapCount = 0;

    ImageView logout_icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sahabah Mosque");


        logout_icon = findViewById(R.id.logout_icon);
        if (isAdmin(NavigationActivity.this)) {
            logout_icon.setVisibility(View.VISIBLE);
        }

        logout_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAdmin();
                Intent intent = getIntent();
                finish();
                startActivity(intent);            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.item_one) {
                    Toast.makeText(NavigationActivity.this, "First Button Clicked", Toast.LENGTH_SHORT).show();
                    //openFragment(new HomeFragment());    // ← add this line
                    //getSupportFragmentManager().popBackStack();  // ← goes back to HomeFragment
                    openFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.item_two) {
                    Toast.makeText(NavigationActivity.this, "Second Button Clicked", Toast.LENGTH_SHORT).show();
                    //openFragment(new HomeFragment());
                    //main.setVisibility(View.GONE);
                    openFragment(new PrayerFragment());

                    return true;
                } else if (itemId == R.id.item_three) {
                    openFragment(new ContactFragment());
                    contactTapCount++;
                    if (contactTapCount >= ADMIN_TRIGGER_TAPS) {
                        contactTapCount = 0;
                        showAdminLoginDialog();
                    }
                    return true;
                }

                return false;
            }
        });

        // Set initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)   // <-- use the activity’s container
                .addToBackStack(null)
                .commit();
    }
    private void showAdminLoginDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_admin_login, null);
        EditText etUser = v.findViewById(R.id.et_username);
        EditText etPass = v.findViewById(R.id.et_password);

        new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("OK", (dlg, which) -> {
                    String u = etUser.getText().toString().trim();
                    String p = etPass.getText().toString().trim();
                    if (u.equals(BuildConfig.ADMIN_USER)
                            && p.equals(BuildConfig.ADMIN_PASS)) {
                        Toast.makeText(this, "Admin logged in", Toast.LENGTH_SHORT).show();
                        grantAdmin();
                    } else {
                        logoutAdmin();
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void grantAdmin() {
        // Option A) Static flag on your Application subclass:
        // ((MyApp)getApplication()).isAdmin = true;

        // Option B) SharedPreferences (persists across restarts):
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isAdmin", true)
                .apply();
        logout_icon.setVisibility(View.VISIBLE);
    }

    private void logoutAdmin() {
        // Option A) Static flag on your Application subclass:
        // ((MyApp)getApplication()).isAdmin = false;

        // Option B) SharedPreferences (persists across restarts):
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isAdmin", false)
                .apply();
        logout_icon.setVisibility(View.INVISIBLE);
    }

    public static boolean isAdmin(Context ctx) {
        return ctx.getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("isAdmin", false);
    }
}
