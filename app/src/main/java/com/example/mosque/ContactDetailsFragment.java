package com.example.mosque;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContactDetailsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1) inflate your fragment layout
        View root = inflater.inflate(R.layout.fragment_contact_details, container, false);

        // 2) find the ImageView *on that root view*…
        ImageView contactImage = root.findViewById(R.id.contactImage);
        contactImage.setImageResource(R.drawable.first);



        // 3) return it
        return root;
    }
}
