package com.example.mosque;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

import java.io.IOException;

public class PostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imagePreview;

    private ImageView moreVctor;

    FrameLayout previwe_image_container;
    private ImageButton buttonRemoveImage;
    LinearLayout photoButton;
    private Uri selectedImageUri;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1) inflate your fragment layout
        View root = inflater.inflate(R.layout.fragment_posts, container, false);

        moreVctor = root.findViewById(R.id.imageView);
        moreVctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPopupMenu(view);
                customPopup(view);
            }
        });
        imagePreview = root.findViewById(R.id.imagePreview);
        buttonRemoveImage = root.findViewById(R.id.buttonRemoveImage); // initialize
        previwe_image_container = root.findViewById(R.id.previwe_image_container);

        photoButton = root.findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle photo button click
                Toast.makeText(getActivity(), "Photo clicked", Toast.LENGTH_SHORT).show();
                openGallery();
            }
        });

        buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePreview.setImageDrawable(null);
                imagePreview.setVisibility(View.GONE);
                buttonRemoveImage.setVisibility(View.GONE);
                previwe_image_container.setVisibility(View.GONE);
                selectedImageUri = null;
            }
        });

        // 2) find the ImageView *on that root view*…
       // ImageView contactImage = root.findViewById(R.id.contactImage);
       // contactImage.setImageResource(R.drawable.first);



        // 3) return it
        return root;
    }
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.posts_edit, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.item1) { // Assuming 'item1' is the 'Edit' button
                    Toast.makeText(getActivity(), "Edit option selected", Toast.LENGTH_SHORT).show();
                    // Add your edit functionality here
                    return true;
                } else if (id == R.id.item3) { // Assuming 'item3' is the 'Delete' button
                    Toast.makeText(getActivity(), "Delete option selected", Toast.LENGTH_SHORT).show();
                    // Add your delete functionality here
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }
    private void customPopup(View anchorView) {
        View menuView = LayoutInflater.from(getContext()).inflate(R.layout.custom_popup_menu, null);
        PopupWindow popupWindow = new PopupWindow(menuView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Set elevation (optional for shadow)
        popupWindow.setElevation(10);

        // Click listeners
        menuView.findViewById(R.id.menu_edit).setOnClickListener(v -> {
            // Handle edit
            popupWindow.dismiss();
        });

        menuView.findViewById(R.id.menu_delete).setOnClickListener(v -> {
            // Handle delete
            popupWindow.dismiss();
        });

        int offsetInDp = -12;
        float density = getContext().getResources().getDisplayMetrics().density;
        int offsetInPx = (int) (offsetInDp * density + 0.5f);

        // Calculate horizontal offset to align with the more icon
        int xOffset = -popupWindow.getContentView().getMeasuredWidth() + anchorView.getWidth();
        // Show the popup below the anchor view
        popupWindow.showAsDropDown(anchorView, xOffset, 0);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            try {
                Bitmap bitmap;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Use ImageDecoder for API 28+
                    ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), selectedImageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    // Fallback for older versions
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                }


                imagePreview.setImageBitmap(bitmap);
                imagePreview.setVisibility(View.VISIBLE);
                buttonRemoveImage.setVisibility(View.VISIBLE);
                previwe_image_container.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
