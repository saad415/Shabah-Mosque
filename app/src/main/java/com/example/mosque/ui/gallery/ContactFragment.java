package com.example.mosque.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosque.databinding.FragmentContactBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ContactFragment extends Fragment {

    private FragmentContactBinding binding;

    private TextView linkgooglemaps;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentContactBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        linkgooglemaps = binding.linkgooglemaps;

        linkgooglemaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openGoogleMaps(view);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void openGoogleMaps(View view) {
        // Your shop address - replace with your actual address
        String shopAddress = "Finkenstraße 11, 90439 Nürnberg";

        // Encode the address for URL
        String encodedAddress;
        try {
            encodedAddress = URLEncoder.encode(shopAddress, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error encoding address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Google Maps URI
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + encodedAddress);

        // Create an Intent to open Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if Google Maps is installed
        if (mapIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // If Google Maps isn't installed, open in browser instead
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + encodedAddress);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }
}