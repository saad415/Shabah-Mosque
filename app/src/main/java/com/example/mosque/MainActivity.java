package com.example.mosque;

import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.icu.util.IslamicCalendar;
import android.icu.util.ULocale;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mosque.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mosque.R;

import com.example.mosque.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DBHelper dbHelper;
    private DrawerLayout drawer;
    private BottomNavigationView bottomNav;
    private TextView tvFajrTime, tvDhuhrTime, tvAsrTime, tvMaghribTime, tvIshaTime;
    private TextView tvNextPrayerDate;

    private TextView tvhijriDate;
    private TextView tvFajr, tvDhuhr, tvAsr, tvMaghrib, tvIsha;

    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    String url = "http://192.168.178.29:5000/api/prayer_times"; //http://192.168.178.29:5000/api/prayer_times
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        tvhijriDate = findViewById(R.id.tvHijriDate);

        tvFajrTime = findViewById(R.id.tvFajrTime);
        tvDhuhrTime   = findViewById(R.id.tvDhuhrTime);
        tvAsrTime     = findViewById(R.id.tvAsrTime);
        tvMaghribTime = findViewById(R.id.tvMaghribTime);
        tvIshaTime    = findViewById(R.id.tvIshaTime);

        tvNextPrayerDate = findViewById(R.id.tvNextPrayerDate);
        tvFajr = findViewById(R.id.tvFajr);
        tvDhuhr = findViewById(R.id.tvDhuhr);
        tvAsr = findViewById(R.id.tvAsr);
        tvMaghrib = findViewById(R.id.tvMaghrib);
        tvIsha = findViewById(R.id.tvIsha);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Build a ULocale that uses the Umm‑al‑Qura calendar
            ULocale uLocale = ULocale.forLocale(Locale.getDefault())
                    .setKeywordValue("calendar","islamic-umalqura");

            // Get an ICU DateFormat in that locale
            DateFormat fmt = DateFormat.getDateInstance(
                    DateFormat.LONG,    // e.g. “22 Shawwal 1446 AH”
                    uLocale
            );

            // Format *now*:
            String hijri = fmt.format(new Date());
            tvhijriDate.setText(hijri);
        } else {
            tvhijriDate.setText("Requires API 24+");
        }

        updateDate();





        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        String gh = String.valueOf(R.id.item_one);
        Toast.makeText(MainActivity.this, gh, Toast.LENGTH_SHORT).show();

        //my code
        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.item_one) {
                    Toast.makeText(MainActivity.this, "First Button Clicked", Toast.LENGTH_SHORT).show();
                    // openFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.item_two) {
                    Toast.makeText(MainActivity.this, "Second Button Clicked", Toast.LENGTH_SHORT).show();
                    // openFragment(new SearchFragment());
                    return true;
                }

                return false;
            }
        });


        // 5) Load default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.item_one);
        }

        //testing locally get  getting data from sqlite
        dbHelper = new DBHelper(this);
        PrayerTimes times = dbHelper.getPrayerTimes();

        //getting data from sqlite localhost
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        StringBuilder builder = new StringBuilder();
                        try {
                            if (response.length() == 0) {
                                Toast.makeText(MainActivity.this,
                                        "No data received",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Grab the first object
                            JSONObject obj = response.getJSONObject(0);

                            // Populate your TextViews
                            tvFajrTime.setText(obj.getString("fajr"));
                            tvDhuhrTime.setText(obj.getString("dhuhr"));
                            tvAsrTime.setText(obj.getString("asr"));
                            tvMaghribTime.setText(obj.getString("magrib"));
                            tvIshaTime.setText(obj.getString("isha"));


                            Date fajrTime = sdf.parse(tvFajrTime.getText().toString());
                            Date dhuhrTime = sdf.parse(tvDhuhrTime.getText().toString());
                            Date asrTime = sdf.parse(tvAsrTime.getText().toString());
                            Date magTime = sdf.parse(tvMaghribTime.getText().toString());
                            Date ishaTime = sdf.parse(tvIshaTime.getText().toString());
                            String currentTimeStr = sdf.format(now);
                            Date currentTime = sdf.parse(currentTimeStr);

                            if (currentTime.after(fajrTime) && currentTime.before(dhuhrTime)) {
                                tvFajr.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvFajrTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvFajr.setTypeface(null, Typeface.BOLD);
                                tvFajrTime.setTypeface(null, Typeface.BOLD);
                            }
                            else if (currentTime.after(dhuhrTime) && currentTime.before(asrTime)) {
                                tvDhuhr.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvDhuhrTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvDhuhr.setTypeface(null, Typeface.BOLD);
                                tvDhuhrTime.setTypeface(null, Typeface.BOLD);
                            } else if (currentTime.after(asrTime) && currentTime.before(magTime)) {
                                tvAsr.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvAsrTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvAsr.setTypeface(null, Typeface.BOLD);
                                tvAsrTime.setTypeface(null, Typeface.BOLD);
                            } else if (currentTime.after(magTime) && currentTime.before(ishaTime)) {
                                tvMaghrib.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvMaghribTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvMaghrib.setTypeface(null, Typeface.BOLD);
                                tvMaghribTime.setTypeface(null, Typeface.BOLD);
                            } else {
                                tvIsha.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvIshaTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                                tvIsha.setTypeface(null, Typeface.BOLD);
                                tvIshaTime.setTypeface(null, Typeface.BOLD);
                            }

                        } catch (Exception e) {
                            Log.e("MainActivity", "JSON parse / view‑binding failed", e);
                            Toast.makeText(
                                    MainActivity.this,
                                    "Error: " + e.getClass().getSimpleName() + " – " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "Network error",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(jsonArrayRequest);

        try {

            Date asrTime = sdf.parse("17:00");  // drop the “AM”
            Date dhuhrTime = sdf.parse("05:44");  // drop the “AM”
            Date fajrTime = sdf.parse(tvFajrTime.getText().toString());

            if (now.after(dhuhrTime) && now.before(asrTime)) {
                Toast.makeText(this, "Fajr: " + times.getFajr(), Toast.LENGTH_SHORT).show();

                tvFajr.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
                tvFajrTime.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    private void updateDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(cal.getTime());
        tvNextPrayerDate.setText("Date: " + formattedDate);
    }
}