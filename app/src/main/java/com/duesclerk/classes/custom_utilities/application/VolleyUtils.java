package com.duesclerk.classes.custom_utilities.application;

import android.content.Context;

import com.duesclerk.R;
import com.duesclerk.classes.custom_utilities.user_data.DataUtils;

public class VolleyUtils {

    // General

    public static final String KEY_ERROR = "Error";
    public static final String KEY_ERROR_MESSAGE = "ErrorMessage";
    public static final String KEY_SUCCESS_MESSAGE = "SuccessMessage";

    // User Account
    public static final String KEY_USER = "User";
    public static final String KEY_UPDATE_PROFILE = "UpdateProfile";

    // SignIn and SignUp
    public static final String KEY_SIGNIN = "SignIn";
    public static final String KEY_SIGNUP = "SignUp";

    // Email Verification
    public static final String KEY_SEND_VERIFICATION_CODE = "SendVerificationCode";
    public static final String KEY_EMAIL_VERIFICATION = "EmailVerification";
    public static final String KEY_PASSWORD_RESET = "PasswordReset";

    // Contacts
    public static final String KEY_UPDATE_CONTACT = "UpdateContact";
    public static final String KEY_DELETE_CONTACTS = "DeleteContacts";

    public static final String KEY_DELETE_DEBTS = "DeleteDebts";

    public static String getApiKey(Context context) {
        return "API_" + DataUtils.getStringResource(context, R.string.app_name).toLowerCase()
                + "_php_2021";
    }
}
