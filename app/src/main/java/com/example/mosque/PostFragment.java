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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imagePreview;
    FrameLayout previwe_image_container;
    private ImageButton buttonRemoveImage;
    LinearLayout photoButton, postButton;
    private Uri selectedImageUri;
    private EditText postTextInput;
    private static final String BASE_URL = "http://192.168.178.29:5000";
    private static final String UPLOAD_URL = BASE_URL + "/upload";
    private static final String POSTS_URL = BASE_URL + "/api/getposts";
    private int editingPostId = -1;

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private LinearLayout cardContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_posts, container, false);
        postTextInput = root.findViewById(R.id.post_text_input);
        imagePreview = root.findViewById(R.id.imagePreview);
        buttonRemoveImage = root.findViewById(R.id.buttonRemoveImage);
        previwe_image_container = root.findViewById(R.id.previwe_image_container);
        cardContainer = root.findViewById(R.id.card_container);

        photoButton = root.findViewById(R.id.photo_button);
        photoButton.setOnClickListener(v -> openGallery());

        buttonRemoveImage.setOnClickListener(view -> clearSelectedImage());

        postButton = root.findViewById(R.id.post_button);
        postButton.setOnClickListener(v -> {
            String text = postTextInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter post text", Toast.LENGTH_SHORT).show();
                return;
            }
            if (editingPostId != -1) {
                updatePostOnServer(editingPostId, text);
                editingPostId = -1;
            } else {
                uploadPost(text);
            }
        });

        fetchPosts();
        return root;
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
                    ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), selectedImageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
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
        builder.addFormDataPart("text", text);
        if (selectedImageUri != null) {
            String filePath = getRealPathFromUri(selectedImageUri);
            if (filePath == null) return;
            File file = new File(filePath);
            MediaType mediaType = MediaType.parse(getMimeType(filePath));
            builder.addFormDataPart("picture", file.getName(), RequestBody.create(file, mediaType));
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(UPLOAD_URL).post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                        clearSelectedImage();
                        postTextInput.setText("");
                        fetchPosts();
                    }
                });
            }
        });
    }

    private void updatePostOnServer(int postId, String updatedText) {
        String url = BASE_URL + "/api/edit_post/" + postId;
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("text", updatedText);
        if (selectedImageUri != null) {
            String filePath = getRealPathFromUri(selectedImageUri);
            if (filePath != null) {
                File file = new File(filePath);
                MediaType mediaType = MediaType.parse(getMimeType(filePath));
                builder.addFormDataPart("picture", file.getName(), RequestBody.create(file, mediaType));
            }
        }
        if (selectedImageUri == null) {
            builder.addFormDataPart("remove_image", "true");
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).put(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Post updated", Toast.LENGTH_SHORT).show();
                        clearSelectedImage();
                        postTextInput.setText("");
                        fetchPosts();
                    }
                });
            }
        });
    }

    private void fetchPosts() {
        cardContainer.removeAllViews();
        Request request = new Request.Builder().url(POSTS_URL).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Failed to fetch posts", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    JSONArray posts = root.getJSONArray("posts");
                    requireActivity().runOnUiThread(() -> displayPosts(posts));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayPosts(JSONArray posts) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        cardContainer.removeAllViews();
        for (int i = 0; i < posts.length(); i++) {
            try {
                JSONObject p = posts.getJSONObject(i);
                String text = p.optString("text", "");
                String rawPath = p.optString("filepath", null);
                int postId = p.optInt("id", -1);

                View item = inflater.inflate(R.layout.post_item, cardContainer, false);
                TextView tvBody = item.findViewById(R.id.tvPostBody);
                ImageView ivPostImg = item.findViewById(R.id.ivPostImage);
                ImageView ivMore = item.findViewById(R.id.ivMore);

                tvBody.setText(text);
                ivMore.setOnClickListener(v -> customPopup(v, postId, p));

                if (rawPath != null && !rawPath.equals("null") && !rawPath.isEmpty()) {
                    String filename = rawPath.substring(rawPath.lastIndexOf("\\") + 1);
                    String imageUrl = BASE_URL + "/uploads/" + filename;
                    ivPostImg.setVisibility(View.VISIBLE);
                    Picasso.get().load(imageUrl).fit().centerCrop().into(ivPostImg);
                } else {
                    ivPostImg.setVisibility(View.GONE);
                }
                cardContainer.addView(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void customPopup(View anchorView, int postId, JSONObject postObj) {
        View menuView = LayoutInflater.from(getContext()).inflate(R.layout.custom_popup_menu, null);
        PopupWindow popupWindow = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(10);
        menuView.findViewById(R.id.menu_edit).setOnClickListener(v -> {
            prefillPostFields(postId, postObj);
            popupWindow.dismiss();
        });
        menuView.findViewById(R.id.menu_delete).setOnClickListener(v -> {
            deletePostFromServer(postId);
            popupWindow.dismiss();
        });
        popupWindow.showAsDropDown(anchorView);
    }

    private void prefillPostFields(int postId, JSONObject postObj) {
        String text = postObj.optString("text", "");
        String rawPath = postObj.optString("filepath", null);
        postTextInput.setText(text);
        editingPostId = postId;
        if (rawPath != null && !rawPath.equals("null") && !rawPath.isEmpty()) {
            String filename = rawPath.substring(rawPath.lastIndexOf("\\") + 1);
            String imageUrl = BASE_URL + "/uploads/" + filename;
            Picasso.get().load(imageUrl).into(imagePreview);
            imagePreview.setVisibility(View.VISIBLE);
            buttonRemoveImage.setVisibility(View.VISIBLE);
            previwe_image_container.setVisibility(View.VISIBLE);
            selectedImageUri = null;
        } else {
            clearSelectedImage();
        }
    }

    private void deletePostFromServer(int postId) {
        String deleteUrl = BASE_URL + "/api/delete_post/" + postId;
        Request request = new Request.Builder().url(deleteUrl).delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Delete failed", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Post deleted", Toast.LENGTH_SHORT).show();
                        fetchPosts();
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
}
