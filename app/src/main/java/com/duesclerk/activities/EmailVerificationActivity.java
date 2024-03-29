package com.duesclerk.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.duesclerk.R;
import com.duesclerk.classes.custom_utilities.application.ApplicationClass;
import com.duesclerk.classes.custom_utilities.application.ViewsUtils;
import com.duesclerk.classes.custom_utilities.application.VolleyUtils;
import com.duesclerk.classes.custom_utilities.user_data.DataUtils;
import com.duesclerk.classes.custom_utilities.user_data.UserAccountUtils;
import com.duesclerk.classes.custom_views.toast.CustomToast;
import com.duesclerk.classes.network.InternetConnectivity;
import com.duesclerk.classes.network.NetworkTags;
import com.duesclerk.classes.network.NetworkUrls;
import com.duesclerk.classes.storage_adapters.UserDatabase;
import com.jkb.vcedittext.VerificationCodeEditText;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String strLastLayout = "lastLayout";
    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private ProgressDialog progressDialog;
    private LinearLayout llLogoLayout;
    private LinearLayout llVerificationLayout;
    private LinearLayout llCannotConnect;
    private LinearLayout llSendVerificationLayout;
    private UserDatabase database;
    private TextView textVerifyCodeMessage;
    private TextView textResendCodeEnabled;
    private TextView textResendCodeDisabled;
    private ImageView imageLogo;
    private VerificationCodeEditText editVerificationCode;
    private int lastActiveLayout;
    private boolean isEmailSent, isResendEnabledHidden = false, isResendCountFinished = true;
    private CountDownTimer countDownResendCode;
    private ScrollView scrollView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        // Initialize context
        mContext = getApplicationContext();

        // Get intent
        Intent intent = new Intent();

        // Get business name or first name
        String name = intent.getStringExtra(UserAccountUtils.KEY_NAME);

        ImageView ivExit = findViewById(R.id.imageEmailActivation_Exit);
        imageLogo = findViewById(R.id.imageEmailVerification_Logo);

        // TextViews
        TextView textSendVerificationCodeMessage =
                findViewById(R.id.textEmailVerification_SendCode_Message);
        textResendCodeEnabled = findViewById(R.id.textEmailVerification_ResendCode_Enabled);
        textResendCodeDisabled = findViewById(R.id.textEmailVerification_ResendCode_Disabled);
        TextView textAlreadyHaveCode = findViewById(R.id.textEmailVerification_AlreadyHaveCode);
        textVerifyCodeMessage = findViewById(R.id.textEmailVerification_VerifyCode_Message);
        TextView textCounterVerificationCode = findViewById(
                R.id.textEmailVerification_CounterVerificationCode);

        // EditText
        editVerificationCode = findViewById(R.id.editEmailVerification_Code);

        // Linear layouts
        llLogoLayout = findViewById(R.id.llEmailVerification_LogoLayout);
        llVerificationLayout = findViewById(R.id.llEmailVerification_VerificationLayout);
        llCannotConnect = findViewById(R.id.llEmailVerification_CannotConnect);
        LinearLayout llSendVerificationCode =
                findViewById(R.id.llEmailVerification_SendVerificationCode);
        llSendVerificationLayout =
                findViewById(R.id.llEmailVerification_SendVerificationCode_layout);
        LinearLayout llVerifyCode = findViewById(R.id.llEmailVerification_VerifyCode);
        LinearLayout llCannotConnect_Retry = findViewById(R.id.llNoConnection_TryAgain);

        scrollView = findViewById(R.id.scrollViewEmailVerificationActivity); // ScrollView

        database = new UserDatabase(mContext); // Initialize database object

        // Initialize ProgressDialog
        progressDialog = ViewsUtils.initProgressDialog(
                EmailVerificationActivity.this, false);

        // CountDown timer
        countDownResendCode = new CountDownTimer(300000, 1000) {
            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {

                if (!isResendEnabledHidden) {

                    // Hide resend code Enabled TextView
                    textResendCodeEnabled.setVisibility(View.GONE);

                    isResendEnabledHidden = true; // Set isResendHidden Hidden To True
                }

                String resendCode, timeString;

                // Set message
                resendCode = getResources().getString(R.string.action_resend_code);

                timeString = "\nafter " + String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                                - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
                );

                try {

                    // Strip texts
                    timeString = timeString.replace(", 0 sec", "");
                    timeString = timeString.replace("0 min,", "");
                    timeString = timeString.replace("after 0 min", "");

                } catch (Exception ignored) {
                }

                // Set counter thread text with time
                String resendAfterLabel = resendCode + timeString;
                textResendCodeDisabled.setText(resendAfterLabel);

                // Update resend count finish status
                isResendCountFinished = false;
            }

            public void onFinish() {

                // Set resend code message
                textResendCodeDisabled.setText(getResources()
                        .getString(R.string.action_resend_code));

                // Hide Disabled layout
                textResendCodeDisabled.setVisibility(View.GONE);

                // Show Enabled layout
                textResendCodeEnabled.setVisibility(View.VISIBLE);

                // Update resend Count Finish Status
                isResendCountFinished = true;

                // Set isResendHidden Hidden To False
                isResendEnabledHidden = false;
            }

        };

        // Check for countdown completion
        if (isResendCountFinished) {

            textResendCodeEnabled.setVisibility(View.VISIBLE);
            textResendCodeDisabled.setVisibility(View.GONE);

        } else {

            textResendCodeDisabled.setVisibility(View.VISIBLE);
            textResendCodeEnabled.setVisibility(View.GONE);
        }

        // Set verification message
        String sendVerificationCodeMessage = DataUtils.getStringResource(
                mContext,
                R.string.msg_send_verification_code_message
                , name
        );

        textSendVerificationCodeMessage.setText(sendVerificationCodeMessage);

        // Underline Links
        textAlreadyHaveCode.setPaintFlags(textAlreadyHaveCode.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textResendCodeEnabled.setPaintFlags(textResendCodeEnabled.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Exit activity
        ivExit.setOnClickListener(v -> finish());

        editVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int codeLength = Objects.requireNonNull(editVerificationCode.getText()).length();

                // Set verification code length to counter
                textCounterVerificationCode.setText(String.valueOf(codeLength));

                if (codeLength == 6) {
                    ViewsUtils.hideKeyboard(EmailVerificationActivity.this); // Hide keyboard
                }
            }
        });

        // Resend code onClick listener
        textResendCodeEnabled.setOnClickListener(v -> {

            editVerificationCode.setText(null); // Clear current Input

            ViewsUtils.hideKeyboard(EmailVerificationActivity.this); // Hide keyboard

            // Resend verification code
            sendEmailVerificationCode(
                    // Pass UserId
                    database.getUserAccountInfo(null).get(0).getUserId()
            );
        });

        // Already have code onClick listener
        textAlreadyHaveCode.setOnClickListener(v -> {

            showSendCodeLayout(false); // Hide send verification code layout
            showVerificationLayout(true); // Show verification layout
        });

        // Send verification code LinearLayout onClick
        llSendVerificationCode.setOnClickListener(v -> {

            // Resend verification code
            sendEmailVerificationCode(
                    // Pass userId
                    database.getUserAccountInfo(null).get(0).getUserId());
        });

        // Verification code onTouchListener
        editVerificationCode.setOnTouchListener((v, event) -> {

            // Set Error On EditText
            editVerificationCode.setError(null);
            return false;
        });

        // ScrollView onClick
        scrollView.setOnClickListener(v -> {

            editVerificationCode.clearFocus(); // Clear EditText focus

            ViewsUtils.hideKeyboard(EmailVerificationActivity.this); // Hide keyboard
        });

        // Verify code onClick
        llVerifyCode.setOnClickListener(v -> {

            // Check code length
            if (Objects.requireNonNull(editVerificationCode.getText()).toString().length() < 6) {

                // Set EditText error
                editVerificationCode.setError(DataUtils.getStringResource(mContext,
                        R.string.error_enter_code));

                CustomToast.errorMessage(mContext,
                        DataUtils.getStringResource(
                                mContext,
                                R.string.error_email_verification_code_length),
                        R.drawable.ic_baseline_email_24_white);
            } else {

                // Hide keyboard
                ViewsUtils.hideKeyboard(EmailVerificationActivity.this);

                // Trim verification code
                String strVerificationCode = editVerificationCode.getText().toString().trim();

                // Verify EmailAddress
                verifyEmailAddress(strVerificationCode);
            }
        });

        // Connection retry onClick
        llCannotConnect_Retry.setOnClickListener(v -> {

            if (InternetConnectivity.isConnectedToAnyNetwork(mContext)) {
                    // Connected

                    // Hide no connection layout
                    showNoConnectionLayout(false);

                    // Check last visible layout
                    switch (lastActiveLayout) {

                        case 0:
                            // Show send verification code layout
                            showSendCodeLayout(true);
                            break;

                        case 1:
                            // Show send verification code layout
                            showVerificationLayout(true);
                            break;

                        case 2:
                            // Check If email was sent
                            if (isEmailSent) {
                                // Last visible layout before Network error was verification layout

                                // Show send verification code layout
                                showVerificationLayout(true);

                            } else {
                                // Last visible layout before network error was send code layout

                                // Show send verification code layout
                                showSendCodeLayout(true);
                            }

                        default:
                            break;
                    }
            } else {
                // No connection

                CustomToast.errorMessage(mContext,
                        DataUtils.getStringResource(
                                mContext,
                                R.string.error_network_connection_error_message_short),
                        R.drawable.ic_sad_cloud_100px_white);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Switch layouts
        switchLayouts();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Hide keyboard
        ViewsUtils.hideKeyboard(EmailVerificationActivity.this);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save Last visible layout
        savedInstanceState.putInt(strLastLayout, lastActiveLayout);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Set email Address
        lastActiveLayout = savedInstanceState.getInt(strLastLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Switch layouts
        switchLayouts();
    }

    /**
     * Function to switch between required layouts
     */
    private void switchLayouts() {
        if (InternetConnectivity.isConnectedToAnyNetwork(mContext)) {
            // Network Connected

            ViewsUtils.scrollUpScrollView(scrollView); // Scroll up ScrollView

            switch (lastActiveLayout) {

                case 0:
                    // send code layout

                    showSendCodeLayout(true); // Show send code layout
                    showVerificationLayout(false); // Hide verification layout
                    showNoConnectionLayout(false); // Hide no connection layout
                    break;

                case 1:
                    // Show verification layout

                    showVerificationLayout(true); // Show verification layout
                    showSendCodeLayout(false); // Hide send code layout
                    showNoConnectionLayout(false); // Hide no connection layout
                    break;

                default:
                    break;
            }
        } else {

            showNoConnectionLayout(true); // Show no connection layout
            showSendCodeLayout(false); // Hide send code layout
        }

    }

    /**
     * Function to send email verification code
     *
     * @param userId - Users UserId
     */
    private void sendEmailVerificationCode(final String userId) {

        // Check Internet connection
        if (InternetConnectivity.isConnectedToAnyNetwork(mContext)) {
                // Connected

                showLogoLayout(false); // Hide logo layout
                showVerificationLayout(false); // Hide verification layout
                showSendCodeLayout(false); // Hide send code layout
                showNoConnectionLayout(false); // Hide no connection layout

                // Show ProgressDialog
                ViewsUtils.showProgressDialog(progressDialog,
                        DataUtils.getStringResource(
                                mContext,
                                R.string.title_mailing_email_verification_code),
                        DataUtils.getStringResource(
                                mContext,
                                R.string.msg_mailing_email_verification_code)
                );

                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        NetworkUrls.UserURLS.URL_SEND_EMAIL_VERIFICATION_CODE,
                        response -> {

                            // Log response
                            // Log.d(TAG, "Send email verification code response: "
                            //        + response);

                            // Hide ProgressDialog
                            ViewsUtils.dismissProgressDialog(progressDialog);

                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                boolean error = jsonObject.getBoolean(VolleyUtils.KEY_ERROR);

                                // Check for error in Json
                                if (!error) {

                                    JSONObject objectSendVerificationCode =
                                            jsonObject.getJSONObject(
                                                    VolleyUtils.KEY_SEND_VERIFICATION_CODE);

                                    // Get verification code and success message
                                    String strVerificationCode = objectSendVerificationCode.
                                            getString(UserAccountUtils.FIELD_VERIFICATION_CODE);

                                    // Check for verification code
                                    if (!DataUtils.isEmptyString(strVerificationCode)) {

                                        isEmailSent = true; // Update email Sent Status

                                        // Show verification layout
                                        showVerificationLayout(true);

                                        // Set Verify code message
                                        textVerifyCodeMessage.setText(
                                                DataUtils.getStringResource(
                                                        mContext,
                                                        R.string.msg_email_verification_message)
                                        );

                                        // Clear verification code EditText in case user clicked
                                        // resend on enter code page
                                        editVerificationCode.setText(null);

                                        // Show resend code disabled layout
                                        textResendCodeDisabled.setVisibility(View.VISIBLE);

                                        // Start resend code timer
                                        countDownResendCode.start();

                                        // Toast message
                                        CustomToast.infoMessage(mContext,
                                                DataUtils.getStringResource(
                                                        mContext,
                                                        R.string.msg_verification_code_sent),
                                                true, 0);
                                    }
                                } else {

                                    // Show send code layout
                                    showSendCodeLayout(true);

                                    // Get the error message
                                    String errorMsg =
                                            jsonObject.getString(VolleyUtils.KEY_ERROR_MESSAGE);

                                    // Toast Error message
                                    CustomToast.errorMessage(mContext, errorMsg, 0);
                                }
                            } catch (JSONException ignored) {
                            }
                        }, volleyError -> {

                    // Log response
                    // Log.e(TAG, "Send email verification code Error: "
                    //        + volleyError.getMessage());

                    // Hide ProgressDialog
                    ViewsUtils.dismissProgressDialog(progressDialog);

                    // networkErrorMessage, serverErrorMessage, authFailureErrorMessage,
                    // parseErrorMessage, noConnectionErrorMessage, timeoutErrorMessage;
                    if (volleyError.getMessage() == null
                            || volleyError instanceof NetworkError
                            || volleyError instanceof ServerError
                            || volleyError instanceof AuthFailureError
                            || volleyError instanceof TimeoutError) {

                        // Show no connection layout
                        showNoConnectionLayout(true);

                    } else {

                        // Show send code layout
                        showSendCodeLayout(true);
                    }

                    // Show connection error message
                    CustomToast.errorMessage(mContext, DataUtils.getStringResource(mContext,
                            R.string.error_network_connection_error_message_short),
                            R.drawable.ic_sad_cloud_100px_white);

                    // Cancel pending request
                    ApplicationClass
                            .getClassInstance().cancelPendingRequests(
                            NetworkTags.UserNetworkTags.TAG_SEND_EMAIL_VERIFICATION_STRING_REQUEST
                    );
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<>();

                        // Put params
                        params.put(UserAccountUtils.FIELD_USER_ID, userId);
                        params.put(UserAccountUtils.FIELD_VERIFICATION_TYPE,
                                UserAccountUtils.KEY_VERIFICATION_TYPE_EMAIL_ACCOUNT);

                        return params;
                    }
                };

                // Set retry policy
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        DataUtils.getIntegerResource(mContext,
                                R.integer.int_volley_account_request_initial_timeout_ms),
                        DataUtils.getIntegerResource(mContext,
                                R.integer.int_volley_account_request_max_timeout_retry),
                        1.0f));

                stringRequest.setShouldCache(false); // Disabling caching

                // Set request priority
                ApplicationClass.getClassInstance().setPriority(Request.Priority.HIGH);

                // Adding request to request queue
                ApplicationClass.getClassInstance().addToRequestQueue(stringRequest,
                        NetworkTags.UserNetworkTags.TAG_SEND_EMAIL_VERIFICATION_STRING_REQUEST);

        } else {
            // Not Connected
            showNoConnectionLayout(true); // Show no connection layout
        }
    }

    /**
     * Function to verify users email address
     *
     * @param verificationCode - Verification code sent on mail
     */
    public void verifyEmailAddress(final String verificationCode) {

        // Check Internet connection
        if (InternetConnectivity.isConnectedToAnyNetwork(mContext)) {
                // Connected

                showLogoLayout(false); // Hide logo layout
                showVerificationLayout(false); // Show verification layout
                showSendCodeLayout(false); // Hide send code layout
                showNoConnectionLayout(false); // Hide no connection layout

                // Show ProgressDialog
                ViewsUtils.showProgressDialog(progressDialog,
                        DataUtils.getStringResource(
                                mContext,
                                R.string.title_verifying_email_address),
                        DataUtils.getStringResource(
                                mContext,
                                R.string.msg_verifying_email_address)
                );

                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        NetworkUrls.UserURLS.URL_VERIFY_EMAIL_ADDRESS,
                        response -> {

                            // Log response
                            Log.d(TAG, "Email verification response: " + response);

                            // Hide ProgressDialog
                            ViewsUtils.dismissProgressDialog(progressDialog);

                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                boolean error = jsonObject.getBoolean(VolleyUtils.KEY_ERROR);

                                // Check for error
                                if (!error) {

                                    // Get email verification object
                                    JSONObject objectEmailVerification = jsonObject.getJSONObject(
                                                    VolleyUtils.KEY_EMAIL_VERIFICATION);

                                    // Get success message
                                    String successMessage = objectEmailVerification.getString(
                                            VolleyUtils.KEY_SUCCESS_MESSAGE);

                                    // Check if message was received
                                    if (!DataUtils.isEmptyString(successMessage)) {

                                        countDownResendCode.cancel(); // Stop resend code counter
                                        editVerificationCode.setText(null); // Clear Current code Input

                                        // Show email verified success message
                                        CustomToast.infoMessage(
                                                mContext,
                                                DataUtils.getStringResource(mContext,
                                                        R.string.msg_email_address_verified),
                                                false,
                                                R.drawable.ic_baseline_email_24_white);

                                        finish(); // Exit activity
                                    }
                                } else {

                                    // Show verification layout
                                    showVerificationLayout(true);

                                    // Get the error message
                                    String errorMsg =
                                            jsonObject.getString(VolleyUtils.KEY_ERROR_MESSAGE);

                                    // Toast Error message
                                    CustomToast.errorMessage(mContext, errorMsg, 0);
                                }
                            } catch (JSONException ignored) {
                            }
                        }, volleyError -> {

                    // Log response
                    Log.e(TAG, "Email verification error: " + volleyError.getMessage());

                    // Hide ProgressDialog
                    ViewsUtils.dismissProgressDialog(progressDialog);

                    // Clear Current code input
                    editVerificationCode.setText(null);

                    // networkErrorMessage, serverErrorMessage, authFailureErrorMessage,
                    // parseErrorMessage, noConnectionErrorMessage, timeoutErrorMessage;
                    if (volleyError.getMessage() == null || volleyError instanceof NetworkError
                            || volleyError instanceof ServerError
                            || volleyError instanceof AuthFailureError
                            || volleyError instanceof TimeoutError
                    ) {

                        // Show no connection layout
                        showNoConnectionLayout(true);

                    } else {

                        // Show verification layout
                        showVerificationLayout(true);
                    }

                    CustomToast.errorMessage(mContext,
                            DataUtils.getStringResource(
                                    mContext,
                                    R.string.error_network_connection_error_message_short),
                            R.drawable.ic_sad_cloud_100px_white);
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<>();

                        // Put User Id
                        params.put(UserAccountUtils.FIELD_USER_ID,
                                database.getUserAccountInfo(null).get(0).getUserId());

                        // Put verification code
                        params.put(UserAccountUtils.FIELD_VERIFICATION_CODE, verificationCode);

                        // Put verification type
                        params.put(
                                UserAccountUtils.FIELD_VERIFICATION_TYPE,
                                UserAccountUtils.KEY_VERIFICATION_TYPE_EMAIL_ACCOUNT
                        );

                        return params;
                    }
                };

                // Set retry policy
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        DataUtils.getIntegerResource(mContext,
                                R.integer.int_volley_account_request_initial_timeout_ms),
                        DataUtils.getIntegerResource(mContext,
                                R.integer.int_volley_account_request_max_timeout_retry),
                        1.0f));

                // Set Request Caching To False
                stringRequest.setShouldCache(true);

                // Set Request Priority
                ApplicationClass.getClassInstance().setPriority(Request.Priority.HIGH);

                // Adding Request to request queue
                ApplicationClass.getClassInstance().addToRequestQueue(stringRequest,
                        NetworkTags.UserNetworkTags.TAG_VERIFY_EMAIL_STRING_REQUEST);

        } else {
            // Not Connected

            // Show no connection layout
            showNoConnectionLayout(true);
        }
    }

    /**
     * Function to show send code layout
     *
     * @param status - boolean - (Show / Hide layout)
     */
    private void showSendCodeLayout(final boolean status) {

        if (status) {
            // True

            // Show Main In layout
            llSendVerificationLayout.setVisibility(View.VISIBLE);

            // Update Last visible layout
            lastActiveLayout = 0;

            // Update Logo
            imageLogo.setImageResource(R.drawable.ic_baseline_email_24_white);

            // Show Logo layout
            showLogoLayout(true);

        } else {
            // False

            // Hide Main In layout
            llSendVerificationLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Function to show logo layout
     *
     * @param status - boolean - (Show / Hide layout)
     */
    private void showLogoLayout(final boolean status) {

        if (status) {
            // True

            // Show layout
            llLogoLayout.setVisibility(View.VISIBLE);

        } else {
            // False

            // Hide layout
            llLogoLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Function to show verification layout
     *
     * @param status - boolean - (Show / Hide layout)
     */
    private void showVerificationLayout(final boolean status) {

        if (status) {
            // True

            // Show layout
            llVerificationLayout.setVisibility(View.VISIBLE);

            // Update Last visible layout
            lastActiveLayout = 1;

            // Update Logo
            imageLogo.setImageResource(R.drawable.ic_unverified_96px_white);

            // Show Logo layout
            showLogoLayout(true);

        } else {
            // False

            // Hide layout
            llVerificationLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Function to show no connection layout
     *
     * @param status - boolean - (Show / Hide layout)
     */
    private void showNoConnectionLayout(final boolean status) {

        if (status) {

            // Show Cannot Connect to the internet layout
            llCannotConnect.setVisibility(View.VISIBLE);

            // Update Last visible layout
            lastActiveLayout = 2;

            // Hide Other layouts
            showLogoLayout(false);
            showSendCodeLayout(false);
            showVerificationLayout(false);

        } else {

            // Show Cannot Connect to the internet layout
            llCannotConnect.setVisibility(View.GONE);
        }
    }
}
