package com.example.mosque.ui.home;

import static androidx.core.app.ActivityCompat.recreate;
import static com.example.mosque.NavigationActivity.isAdmin;

import com.android.volley.toolbox.JsonObjectRequest;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.icu.util.ULocale;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mosque.DBHelper;
import com.example.mosque.PrayerTimes;
import com.example.mosque.R;
import com.example.mosque.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.android.material.card.MaterialCardView;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Intent;
import android.widget.ImageView;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView tvhijriDate;
    private TextView tvNextPrayerDate,  current_time_textview;
    private TextView tvFajrTime, tvDhuhrTime, tvAsrTime, tvMaghribTime, tvIshaTime;
    private TextView tvFajr, tvDhuhr, tvAsr, tvMaghrib, tvIsha;
    private DBHelper dbHelper;
    ProgressBar progressBar;

    String url = "http://192.168.178.29:5000/api/prayer_times"; //http://192.168.178.29:5000/api/prayer_times
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    ImageButton btnEditFajrTime, btnEditDhuhrTime, btnEditAsrTime, btnEditMaghribTime, btnEditIshaTime;

    private TextView tvFajrIqama, tvDhuhrIqama, tvAsrIqama, tvMaghribIqama, tvIshaIqama;

    private MaterialCardView fajr_Cardview, dhuhr_Cardview, asr_Cardview, maghrib_Cardview, isha_Cardview;
    Date now = new Date();
    private Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable updateTimeRunnable;
    private AlertDialog noInternetDialog;
    private PopupWindow tooltipWindow;
    private View tooltipView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = root.findViewById(R.id.progressBar);
        tvhijriDate = root.findViewById(R.id.tvHijriDate);
        tvNextPrayerDate = root.findViewById(R.id.tvNextPrayerDate);
        setTvhijriDate();
        updateDate();

        //MaterialCardView fajr_Cardview = (MaterialCardView) root.findViewById(R.id.fajr_Cardview);
        fajr_Cardview = (MaterialCardView) root.findViewById(R.id.fajr_Cardview);
        dhuhr_Cardview = (MaterialCardView)  root.findViewById(R.id.dhuhr_Cardview);
        asr_Cardview = (MaterialCardView) root.findViewById(R.id.asr_Cardview);
        maghrib_Cardview = root.findViewById(R.id.margrib_Cardview);
        isha_Cardview = root.findViewById(R.id.isha_Cardview);


        tvFajrTime = root.findViewById(R.id.tvFajrTime);
        tvDhuhrTime   = root.findViewById(R.id.tvDhuhrTime);
        tvAsrTime     = root.findViewById(R.id.tvAsrTime);
        tvMaghribTime = root.findViewById(R.id.tvMaghribTime);
        tvIshaTime    = root.findViewById(R.id.tvIshaTime);
        tvFajrIqama = root.findViewById(R.id.tvFajrIqama);
        tvDhuhrIqama = root.findViewById(R.id.tvDhuhrIqama);
        tvAsrIqama = root.findViewById(R.id.tvAsrIqama);
        tvMaghribIqama = root.findViewById(R.id.tvMaghribIqama);
        tvIshaIqama = root.findViewById(R.id.tvIshaIqama);

        current_time_textview = root.findViewById(R.id.current_time_textview);
        tvNextPrayerDate = root.findViewById(R.id.tvNextPrayerDate);
        tvFajr = root.findViewById(R.id.tvFajr);
        tvDhuhr = root.findViewById(R.id.tvDhuhr);
        tvAsr = root.findViewById(R.id.tvAsr);
        tvMaghrib = root.findViewById(R.id.tvMaghrib);
        tvIsha = root.findViewById(R.id.tvIsha);

        btnEditFajrTime = root.findViewById(R.id.btnEditFajrTime);
        btnEditDhuhrTime = root.findViewById(R.id.btnEditDhuhrTime);
        btnEditAsrTime = root.findViewById(R.id.btnEditAsrTime);
        btnEditMaghribTime = root.findViewById(R.id.btnEditMaghribTime);
        btnEditIshaTime = root.findViewById(R.id.btnEditIshaTime);

        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                current_time_textview.setText(currentTime);
                timeHandler.postDelayed(this, 60000); // updates every minute
            }
        };
        timeHandler.post(updateTimeRunnable);

        if (isAdmin(getActivity())) {
            btnEditFajrTime.setVisibility(View.VISIBLE);
            btnEditDhuhrTime.setVisibility(View.VISIBLE);
            btnEditAsrTime.setVisibility(View.VISIBLE);
            btnEditMaghribTime.setVisibility(View.VISIBLE);
            btnEditIshaTime.setVisibility(View.VISIBLE);
        }


        btnEditFajrTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker1("fajr");
            }
        });
        btnEditDhuhrTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker1("dhuhr");
            }
        });
        btnEditAsrTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker1("asr");
            }
        });
        btnEditMaghribTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker1("magrib");
            }
        });
        btnEditIshaTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker1("isha");
            }
        });

        // Check internet connection when fragment is created
        if (!isInternetAvailable()) {
            showNoInternetDialog();
        } else {
            getPrayerTimes_and_upadet_bolf();
            setPrayerTimes();
        }

        // Initialize tooltip
        tooltipView = inflater.inflate(R.layout.tooltip_layout, null);
        tooltipWindow = new PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        );
        tooltipWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        tooltipWindow.setOutsideTouchable(true);

        // Find all azan icons and set click listeners
        ImageView azanFajr = root.findViewById(R.id.azan_icon_fajr);
        ImageView azanDhuhr = root.findViewById(R.id.azan_icon_dhuhr);
        ImageView azanAsr = root.findViewById(R.id.azan_icon_asr);
        ImageView azanMaghrib = root.findViewById(R.id.azan_icon_maghrib);
        ImageView azanIsha = root.findViewById(R.id.azan_icon_isha);

        // Find all mosque icons
        ImageView mosqueFajr = root.findViewById(R.id.mosque_icon_fajr);
        ImageView mosqueDhuhr = root.findViewById(R.id.mosque_icon_dhuhr);
        ImageView mosqueAsr = root.findViewById(R.id.mosque_icon_asr);
        ImageView mosqueMaghrib = root.findViewById(R.id.mosque_icon_maghrib);
        ImageView mosqueIsha = root.findViewById(R.id.mosque_icon_isha);

        // Set click listeners for azan icons
        View.OnClickListener azanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, "Adhan Time");
            }
        };

        // Set click listeners for mosque icons
        View.OnClickListener mosqueClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTooltip(v, "Namaz Time");
            }
        };

        azanFajr.setOnClickListener(azanClickListener);
        azanDhuhr.setOnClickListener(azanClickListener);
        azanAsr.setOnClickListener(azanClickListener);
        azanMaghrib.setOnClickListener(azanClickListener);
        azanIsha.setOnClickListener(azanClickListener);

        mosqueFajr.setOnClickListener(mosqueClickListener);
        mosqueDhuhr.setOnClickListener(mosqueClickListener);
        mosqueAsr.setOnClickListener(mosqueClickListener);
        mosqueMaghrib.setOnClickListener(mosqueClickListener);
        mosqueIsha.setOnClickListener(mosqueClickListener);

        // 2) find the ImageView *on that root view*…
       // ImageView contactImage = root.findViewById(R.id.contactImage);
       // contactImage.setImageResource(R.drawable.first);

        //final TextView textView = binding.textHome;
       // homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
       // fajr_Cardview.setStrokeColor(Color.RED);
       // fajr_Cardview.setStrokeWidth(10);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check internet connection when fragment resumes
        if (!isInternetAvailable()) {
            showNoInternetDialog();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
        }
        binding = null;
        timeHandler.removeCallbacks(updateTimeRunnable);
        if (tooltipWindow != null && tooltipWindow.isShowing()) {
            tooltipWindow.dismiss();
        }
        tooltipWindow = null;
        tooltipView = null;
    }

    private void setTvhijriDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Build a ULocale that uses the Umm‑al‑Qura calendar
            ULocale uLocale = ULocale.forLocale(Locale.getDefault())
                    .setKeywordValue("calendar","islamic-umalqura");

            // Get an ICU DateFormat in that locale
            DateFormat fmt = DateFormat.getDateInstance(
                    DateFormat.LONG,    // e.g. "22 Shawwal 1446 AH"
                    uLocale
            );

            // Format *now*:
            String hijri = fmt.format(new Date());
            tvhijriDate.setText(hijri);
        } else {
            tvhijriDate.setText("Requires API 24+");
        }
    }
    private void updateDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(cal.getTime());
        tvNextPrayerDate.setText( formattedDate);
    }
    private void getPrayerTimes_and_upadet_bolf() {
        dbHelper = new DBHelper(getActivity());
        PrayerTimes times = dbHelper.getPrayerTimes();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        //getting data from sqlite localhost
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        StringBuilder builder = new StringBuilder();
                        try {
                            if (response.length() == 0) {
                                Toast.makeText(getActivity(),
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
                                tvFajr.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvFajrTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvFajrIqama.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvFajr.setTypeface(null, Typeface.BOLD);
                                tvFajrTime.setTypeface(null, Typeface.BOLD);
                                tvFajrIqama.setTypeface(null, Typeface.BOLD);
                                fajr_Cardview.setStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                fajr_Cardview.setStrokeWidth(10);
                                // in pixels

                            }
                            else if (currentTime.after(dhuhrTime) && currentTime.before(asrTime)) {
                                tvDhuhr.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvDhuhrTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvDhuhrIqama.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvDhuhr.setTypeface(null, Typeface.BOLD);
                                tvDhuhrTime.setTypeface(null, Typeface.BOLD);
                                tvDhuhrIqama.setTypeface(null, Typeface.BOLD);
                                dhuhr_Cardview.setStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                dhuhr_Cardview.setStrokeWidth(10);
                                // in pixels

                            } else if (currentTime.after(asrTime) && currentTime.before(magTime)) {
                                tvAsr.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvAsrTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvAsrIqama.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvAsr.setTypeface(null, Typeface.BOLD);
                                tvAsrTime.setTypeface(null, Typeface.BOLD);
                                tvAsrIqama.setTypeface(null, Typeface.BOLD);
                                asr_Cardview.setStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                asr_Cardview.setStrokeWidth(10);
                                // in pixels

                            } else if (currentTime.after(magTime) && currentTime.before(ishaTime)) {
                                tvMaghrib.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvMaghribTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvMaghribIqama.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvMaghrib.setTypeface(null, Typeface.BOLD);
                                tvMaghribTime.setTypeface(null, Typeface.BOLD);
                                tvMaghribIqama.setTypeface(null, Typeface.BOLD);
                                maghrib_Cardview.setStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                maghrib_Cardview.setStrokeWidth(10);
                                // in pixels

                            } else {
                                tvIsha.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvIshaTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvIshaIqama.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvIsha.setTypeface(null, Typeface.BOLD);
                                tvIshaTime.setTypeface(null, Typeface.BOLD);
                                tvIshaIqama.setTypeface(null, Typeface.BOLD);
                                isha_Cardview.setStrokeColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                isha_Cardview.setStrokeWidth(10);
                                // in pixels

                            }

                        } catch (Exception e) {
                            Log.e("MainActivity", "JSON parse / view‑binding failed", e);
                            Toast.makeText(
                                    getActivity(),
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
                        Toast.makeText(getActivity(),
                                "Network error",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(jsonArrayRequest);

    }

    private void showTimePicker1(String prayer) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                (view, hourOfDay, minute, second) -> {
                    // Handle the selected time
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    updatePrayerTime(prayer, time);
                    //updatePrayerTime("fajr", time);
                    Toast.makeText(getActivity(), "Selected Time: " + time, Toast.LENGTH_LONG).show();

                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true // for 24 hour time format
        );

        // Customize TimePickerDialog
        tpd.setAccentColor(getResources().getColor(R.color.purple_700));
        tpd.setThemeDark(false); // Set true for dark mode
        tpd.vibrate(true);
        tpd.dismissOnPause(true);
        tpd.enableSeconds(false); // Set true to enable seconds
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.show(getParentFragmentManager(), "Timepickerdialog"); // Correct method to use in a Fragment

    }

    private void updatePrayerTime(String prayer, String time) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.178.29:5000/api/prayer_times/" + prayer);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"time\":\"" + time + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        Toast.makeText(getActivity(), "Time updated to " + time, Toast.LENGTH_SHORT).show();
                        recreate(getActivity());
                    } else {
                        Toast.makeText(getActivity(), "Update failed: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void setPrayerTimes() {
        String url = "https://api.aladhan.com/v1/timingsByCity/{date}?city=Nuremberg&country=Germany&method=3";

        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject timings = response.getJSONObject("data").getJSONObject("timings");
                        

                        tvFajrIqama.setText(timings.getString("Fajr"));
                        tvDhuhrIqama.setText(timings.getString("Dhuhr"));
                        tvAsrIqama.setText(timings.getString("Asr"));
                        tvMaghribIqama.setText(timings.getString("Maghrib"));
                        tvIshaIqama.setText(timings.getString("Isha"));

                        // Show a success message
                        Toast.makeText(getContext(), "Prayer times updated successfully", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing prayer times", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error fetching prayer times", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showNoInternetDialog() {
        if (getActivity() == null) return;

        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            return; // Don't show dialog if it's already showing
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("No Internet Connection")
               .setMessage("Please connect to the internet to get prayer times.")
               .setCancelable(false)
               .setPositiveButton("Retry", (dialog, which) -> {
                   if (isInternetAvailable()) {
                       getPrayerTimes_and_upadet_bolf();
                       setPrayerTimes();
                   } else {
                       showNoInternetDialog(); // Show dialog again if still no internet
                   }
               })
               .setNegativeButton("Settings", (dialog, which) -> {
                   startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
               });

        noInternetDialog = builder.create();
        noInternetDialog.show();
    }

    private void showTooltip(View anchorView, String message) {
        if (tooltipWindow != null && tooltipView != null) {
            // Set the tooltip message
            TextView tooltipText = tooltipView.findViewById(R.id.tooltip_text);
            tooltipText.setText(message);
            
            // Calculate position for tooltip
            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);
            
            // Show tooltip above the icon
            tooltipWindow.showAtLocation(
                anchorView,
                Gravity.NO_GRAVITY,
                location[0] + anchorView.getWidth() / 2,
                location[1] - tooltipView.getHeight()
            );

            // Auto-dismiss after 2 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tooltipWindow != null && tooltipWindow.isShowing()) {
                        tooltipWindow.dismiss();
                    }
                }
            }, 2000);
        }
    }

}