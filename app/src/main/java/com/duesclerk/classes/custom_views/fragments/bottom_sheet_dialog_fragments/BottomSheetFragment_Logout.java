package com.duesclerk.classes.custom_views.fragments.bottom_sheet_dialog_fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.duesclerk.R;
import com.duesclerk.activities.SignInSignupActivity;
import com.duesclerk.classes.custom_utilities.user_data.DataUtils;
import com.duesclerk.classes.storage_adapters.SessionManager;
import com.duesclerk.classes.storage_adapters.UserDatabase;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
@SuppressLint("ValidFragment")
public class BottomSheetFragment_Logout extends BottomSheetDialogFragment {

    private final Context mContext;
    private final UserDatabase database;
    private final SessionManager sessionManager;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;

    public BottomSheetFragment_Logout(Context mContext) {
        this.mContext = mContext; // Get context
        this.sessionManager = new SessionManager(mContext); // Initialize SessionManager object
        this.database = new UserDatabase(mContext); // Initialize database
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog)
                super.onCreateDialog(savedInstanceState);

        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_logout, null);
        TextView tvCancel = contentView.findViewById(R.id.textBSLogout_Cancel);
        TextView tvLogout = contentView.findViewById(R.id.textBSLogout_Logout);
        TextView tvLogoutMessage = contentView.findViewById(R.id.textBSLogout_Message);

        // Check for user information
        if (!DataUtils.isEmptyArrayList(database.getUserAccountInfo(null))) {
            // Set logout message
            tvLogoutMessage.setText(DataUtils.getStringResource(mContext,
                    R.string.msg_logout,
                    database.getUserAccountInfo(null).get(0).getEmailAddress()));
        }

        // Dismiss dialog
        tvCancel.setOnClickListener(v -> dialog.dismiss());

        tvLogout.setOnClickListener(v -> {

            sessionManager.setSignedIn(false); // Falsify session

            // Launch SignInSignUp activity
            startActivity(new Intent(getActivity(), SignInSignupActivity.class));

            // Delete user details from SQLite database
            database.deleteUserAccountInfoByUserId(
                    database.getUserAccountInfo(null).get(0)
                            .getUserId());

            // Close activity
            requireActivity().finish();

            dialog.dismiss(); // Dismiss dialog
        });

        // Set BottomSheet callback
        this.bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) bottomSheetBehavior
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        };

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE); // Remove window title
        // Set transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set Custom View To Dialog
        dialog.setContentView(contentView);

        // Set BottomSheet behaviour
        this.bottomSheetBehavior = BottomSheetBehavior.from((View) contentView.getParent());

        // Set dialog transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        this.bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
    }
}
