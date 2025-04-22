package com.example.mosque;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.squareup.picasso.Picasso;   // ← add Picasso dependency

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imagePreview;

    private ImageView moreVctor;

    FrameLayout previwe_image_container;
    private ImageButton buttonRemoveImage;
    LinearLayout photoButton, postButton;
    private Uri selectedImageUri;
    private EditText postTextInput;
    // Replace with your server URL
   // private static final String UPLOAD_URL = "http://192.168.178.29:5000/upload";
    private static final String BASE_URL = "http://192.168.178.29:5000";
    private static final String UPLOAD_URL = BASE_URL + "/upload";

    private static final String POSTS_URL = BASE_URL + "/api/getposts";

    //private OkHttpClient client = new OkHttpClient();


    // Extended timeout for slower connections
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private LinearLayout cardContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1) inflate your fragment layout
        View root = inflater.inflate(R.layout.fragment_posts, container, false);
        postTextInput = root.findViewById(R.id.post_text_input);
        //moreVctor = root.findViewById(R.id.imageView);
        //moreVctor.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
                //showPopupMenu(view);
         //       customPopup(view);
         //   }
        // });
        imagePreview = root.findViewById(R.id.imagePreview);
        buttonRemoveImage = root.findViewById(R.id.buttonRemoveImage); // initialize
        previwe_image_container = root.findViewById(R.id.previwe_image_container);


        cardContainer = root.findViewById(R.id.card_container);



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

        postButton = root.findViewById(R.id.post_button);


        postButton.setOnClickListener(v -> {
            String text = postTextInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter post text", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPost(text);
        });

        fetchPosts();

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
    private void uploadPost(String text) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // Add text to the request
        builder.addFormDataPart("text", text);

        // Add image if selected
        if (selectedImageUri != null) {
            String filePath = getRealPathFromUri(selectedImageUri);
            if (filePath == null) {
                Toast.makeText(getActivity(), "Unable to get file path", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(filePath);
            MediaType mediaType = MediaType.parse(getMimeType(filePath));
            RequestBody fileBody = RequestBody.create(file, mediaType);
            builder.addFormDataPart("picture", file.getName(), fileBody);
        }
        // No else needed - we simply don't add a picture field if no image is selected

        // Build the request body
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(UPLOAD_URL).post(requestBody).build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    // Show success message
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Post uploaded successfully", Toast.LENGTH_LONG).show();
                        // Reset UI for a new post
                        postTextInput.setText("");
                        clearSelectedImage();
                    } else {
                        Toast.makeText(getActivity(), "Error: " + response.code() + " - " + body, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(idx);
            cursor.close();
            return path;
        }
        return null;
    }

    private String getMimeType(String path) {
        String extension = path.substring(path.lastIndexOf('.') + 1);
        switch (extension.toLowerCase()) {
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            default: return "application/octet-stream";
        }
    }
    private void clearSelectedImage() {
        selectedImageUri = null;
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
        buttonRemoveImage.setVisibility(View.GONE);
        previwe_image_container.setVisibility(View.GONE);

    }
    private void fetchPosts() {
        // clear any old views
        cardContainer.removeAllViews();

        Request request = new Request.Builder()
                .url(POSTS_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(),
                                "Failed to fetch posts: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(),
                                    "Error fetching posts: " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    JSONArray posts = root.getJSONArray("posts");

                    requireActivity().runOnUiThread(() ->
                            displayPosts(posts)
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(),
                                    "Parse error",
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void displayPosts(JSONArray posts) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        cardContainer.removeAllViews();

        for (int i = 0; i < posts.length(); i++) {
            try {
                JSONObject p       = posts.getJSONObject(i);
                String text        = p.optString("text", "");
                String rawPath     = p.optString("filepath", null);

                // 1) inflate the item
                View   item        = inflater.inflate(R.layout.post_item, cardContainer, false);
                TextView tvBody    = item.findViewById(R.id.tvPostBody);
                ImageView ivPostImg= item.findViewById(R.id.ivPostImage);

                tvBody.setText(text);

                // 2) if filepath isn't null, extract the filename...
                if (rawPath != null && !rawPath.equals("null") && !rawPath.isEmpty()) {
                    // everything after the last backslash
                    String filename = rawPath.substring(rawPath.lastIndexOf("\\") + 1);

                    // 3) build your real URL:
                    String imageUrl = BASE_URL + "/uploads/" + filename;
                    Log.d("ImageURL", imageUrl);

                    ivPostImg.setVisibility(View.VISIBLE);
                    Picasso.get()
                            .load(imageUrl)
                            //.placeholder(R.drawable.placeholder)  // optional
                           // .error(R.drawable.error)             // optional
                            .fit()
                            .centerCrop()
                            .into(ivPostImg);

                } else {
                    ivPostImg.setVisibility(View.GONE);
                }

                cardContainer.addView(item);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
