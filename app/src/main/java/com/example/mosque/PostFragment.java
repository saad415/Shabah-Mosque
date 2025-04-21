package com.example.mosque;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

public class PostFragment extends Fragment {

    private ImageView moreVctor;

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

}
