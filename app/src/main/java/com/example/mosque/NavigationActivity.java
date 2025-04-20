package com.example.mosque;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.mosque.ui.gallery.ContactFragment;
import com.example.mosque.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class NavigationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sahabah Mosque");

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
}
