package com.example.mosque.ui.home;

import static com.example.mosque.NavigationActivity.isAdmin;

import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.icu.util.ULocale;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView tvhijriDate;
    private TextView tvNextPrayerDate;
    private TextView tvFajrTime, tvDhuhrTime, tvAsrTime, tvMaghribTime, tvIshaTime;
    private TextView tvFajr, tvDhuhr, tvAsr, tvMaghrib, tvIsha;
    private DBHelper dbHelper;
    String url = "http://192.168.178.29:5000/api/prayer_times"; //http://192.168.178.29:5000/api/prayer_times
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    ImageButton btnEditFajrTime, btnEditDhuhrTime, btnEditAsrTime, btnEditMaghribTime, btnEditIshaTime;
    Date now = new Date();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tvhijriDate = root.findViewById(R.id.tvHijriDate);
        tvNextPrayerDate = root.findViewById(R.id.tvNextPrayerDate);
        setTvhijriDate();
        updateDate();

        tvFajrTime = root.findViewById(R.id.tvFajrTime);
        tvDhuhrTime   = root.findViewById(R.id.tvDhuhrTime);
        tvAsrTime     = root.findViewById(R.id.tvAsrTime);
        tvMaghribTime = root.findViewById(R.id.tvMaghribTime);
        tvIshaTime    = root.findViewById(R.id.tvIshaTime);

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

        if (isAdmin(getActivity())) {
            btnEditFajrTime.setVisibility(View.VISIBLE);
            btnEditDhuhrTime.setVisibility(View.VISIBLE);
            btnEditAsrTime.setVisibility(View.VISIBLE);
            btnEditMaghribTime.setVisibility(View.VISIBLE);
            btnEditIshaTime.setVisibility(View.VISIBLE);
        }



        getPrayerTimes_and_upadet_bolf();
        // 2) find the ImageView *on that root view*…
       // ImageView contactImage = root.findViewById(R.id.contactImage);
       // contactImage.setImageResource(R.drawable.first);

        //final TextView textView = binding.textHome;
       // homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void setTvhijriDate() {
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
                                tvFajr.setTypeface(null, Typeface.BOLD);
                                tvFajrTime.setTypeface(null, Typeface.BOLD);
                            }
                            else if (currentTime.after(dhuhrTime) && currentTime.before(asrTime)) {
                                tvDhuhr.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvDhuhrTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvDhuhr.setTypeface(null, Typeface.BOLD);
                                tvDhuhrTime.setTypeface(null, Typeface.BOLD);
                            } else if (currentTime.after(asrTime) && currentTime.before(magTime)) {
                                tvAsr.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvAsrTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvAsr.setTypeface(null, Typeface.BOLD);
                                tvAsrTime.setTypeface(null, Typeface.BOLD);
                            } else if (currentTime.after(magTime) && currentTime.before(ishaTime)) {
                                tvMaghrib.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvMaghribTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvMaghrib.setTypeface(null, Typeface.BOLD);
                                tvMaghribTime.setTypeface(null, Typeface.BOLD);
                            } else {
                                tvIsha.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvIshaTime.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
                                tvIsha.setTypeface(null, Typeface.BOLD);
                                tvIshaTime.setTypeface(null, Typeface.BOLD);
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
}