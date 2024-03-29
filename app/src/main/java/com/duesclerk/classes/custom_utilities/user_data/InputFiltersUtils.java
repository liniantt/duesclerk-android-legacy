package com.duesclerk.classes.custom_utilities.user_data;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Patterns;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.duesclerk.R;
import com.duesclerk.classes.custom_views.toast.CustomToast;

import java.util.Objects;

/**
 * This class filters inputs on different fields. It contains input filters for
 * persons names (first and last),
 * business names,
 * phone numbers,
 * email addresses,
 * country,
 * city,
 * password,
 * gender and
 * verification codes
 */
public class InputFiltersUtils {

    // Input Field Lengths
    public static final int LENGTH_MIN_SINGLE_NAME = 1;
    public static final int LENGTH_MIN_PASSWORD = 8;
    public static final int LENGTH_MAX_SINGLE_NAME = 50;
    public static final int LENGTH_MAX_PHONE_NUMBER = 15;
    public static final int LENGTH_MAX_EMAIL_ADDRESS = 320;
    public static final int LENGTH_VERIFICATION_CODE = 6;
    private static final int LENGTH_MIN_PHONE_NUMBER = 7;


    // This filter ensures only digits input to the names fields
    // It also capitalizes the first character of a name and sets the rest to lower case
    public static InputFilter filterNames = (source, start, end, dest, dstart,
                                             dend) -> {

        for (int i = start; i < end; i++) {

            if (!Character.isLetter(source.charAt(i))) {

                if (!Character.isWhitespace(source.charAt(i))) {

                    return "";
                }
            }
        }
        return null;
    };

    // This filter prevents white spaces input to the email address field
    public static InputFilter filterEmailAddress = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (Character.isWhitespace(source.charAt(i))) {
                return "";
            }
        }
        return null;
    };

    //  This filter ensures only letters and digits input to the verification code field
    public static InputFilter filterVerificationCodes = (source, start, end,
                                                         dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (!Character.isLetterOrDigit(source.charAt(i))) {
                return "";
            }
        }
        return null;
    };

    /**
     * This function prevents the EditText value from starting with a specified character
     * If the specified character is detected as the first, it will be deleted
     *
     * @param context       - for Toast
     * @param editable      - Changing text
     * @param editText      - Associated input field
     * @param specifiedChar - Specified character
     */
    public static void blockLeadingSpaces(final Context context, final Editable editable,
                                          final EditText editText, final String specifiedChar) {

        // Check editable length
        if (editable.length() > 0) {

            // Check if first char on editable is the specified character
            if (String.valueOf(editable.charAt(0)).equals(specifiedChar)) {

                String currentValue, trimmed;

                // Check editable length
                if (editable.length() > 0) {

                    currentValue = editable.toString();
                    trimmed = currentValue.substring(1); // Remove the leading specified character

                    editText.setText(null); // Set text to null

                    for (int i = 0; i < trimmed.length(); i++) {

                        char c = trimmed.charAt(i);
                        editText.append(String.valueOf(c)); // Set char to textView
                    }
                } else {

                    editText.setText(null); // Set text to null
                }

                // Set error message
                String errorMessage = DataUtils.getStringResource(context,
                        R.string.error_full_name_cannot_start_with_a, editable.charAt(0));

                // Check if specified character is a space
                if (specifiedChar.equals(" ")) {

                    // Set error message
                    errorMessage = DataUtils.getStringResource(context,
                            R.string.error_full_name_cannot_start_with_spaces);
                }

                // Toast error message
                CustomToast.errorMessage(context, errorMessage, 0);
            }
        }
    }

    /**
     * Function to check persons name length and notify on error
     *
     * @param context  - Context to get string resources
     * @param editText - Associated EditText
     *
     * @return boolean
     */
    public static boolean checkFullNameOrBusinessNameLengthNotify(Context context,
                                                                  @NonNull EditText editText) {

        if (DataUtils.isEmptyEditText(editText)) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_full_name_or_business_name_null),
                    R.drawable.ic_baseline_person_24_white);

            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_full_name_or_business_name_null)); // Enable error icon


        } else if (Objects.requireNonNull(editText.getText()).length()
                < InputFiltersUtils.LENGTH_MIN_SINGLE_NAME) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_full_name_or_business_name_length,
                    String.valueOf(InputFiltersUtils.LENGTH_MIN_SINGLE_NAME)),
                    R.drawable.ic_baseline_person_24_white);

            // Enable error icon
            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_full_name_or_business_name_length_short));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check business name length then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkFullNameLengthNotify(Context context,
                                                    @NonNull EditText editText) {

        if (Objects.requireNonNull(editText.getText()).length() == 0) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_full_name_null),
                    R.drawable.ic_baseline_person_24_white);

            // Enable error icon
            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_full_name_null));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check email address length and format validity then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkEmailAddressValidNotify(@NonNull Context context,
                                                       @NonNull EditText editText) {

        if ((Objects.requireNonNull(editText.getText()).length()
                > InputFiltersUtils.LENGTH_MAX_EMAIL_ADDRESS)
                || (!Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches())
        ) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_email_address_invalid),
                    R.drawable.ic_baseline_email_24_white);

            // Enable error icon
            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_email_address_invalid_short));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check phone number length and format validity then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkPhoneNumberValidNotify(@NonNull Context context,
                                                      @NonNull EditText editText) {

        if ((Objects.requireNonNull(editText.getText()).length()
                < InputFiltersUtils.LENGTH_MIN_PHONE_NUMBER)
                || (!Patterns.PHONE.matcher(editText.getText().toString()).matches())
        ) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_phone_number_invalid),
                    R.drawable.ic_baseline_phone_24_white);

            // Enable error icon
            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_phone_number_invalid_short));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check phone number length and format validity then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkPasswordLengthNotify(@NonNull Context context,
                                                    @NonNull EditText editText) {

        if (Objects.requireNonNull(editText.getText()).length() == 0) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_password_length,
                    String.valueOf(LENGTH_MIN_PASSWORD)),
                    R.drawable.ic_baseline_alternate_email_24_white);

            // Enable error icon
            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_password_length_short));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to compare current and new password then notify on error
     *
     * @param context             - To show toast
     * @param editCurrentPassword - Character Sequence
     *
     * @return boolean
     */
    public static boolean comparePasswordChangeNotify(@NonNull Context context,
                                                      @NonNull EditText editCurrentPassword,
                                                      @NonNull EditText editNewPassword) {

        if (editCurrentPassword.getText().toString().equals(
                editNewPassword.getText().toString())) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_password_select_new),
                    R.drawable.ic_baseline_lock_24_white);

            // Enable error icon
            editNewPassword.setError(DataUtils.getStringResource(context,
                    R.string.error_password_select_new));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to compare new passwords then notify on error
     *
     * @param context             - To show toast
     * @param editNewPassword     - New password
     * @param editConfirmPassword - Confirmation of new password
     *
     * @return boolean
     */
    public static boolean compareNewPasswords(@NonNull Context context,
                                              @NonNull EditText editNewPassword,
                                              @NonNull EditText editConfirmPassword) {

        if (!editNewPassword.getText().toString().equals(
                editConfirmPassword.getText().toString())) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_new_password_mismatch),
                    R.drawable.ic_baseline_lock_24_white);

            // Enable error icon
            editConfirmPassword.setError(DataUtils.getStringResource(context,
                    R.string.error_new_password_mismatch));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check country length then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkCountryLengthNotify(@NonNull Context context,
                                                   @NonNull EditText editText) {

        if (!(Objects.requireNonNull(editText.getText()).length() > 0)) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_country_null),
                    R.drawable.ic_baseline_location_on_24_white);

            editText.setError(null); // Enable error icon

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check verification code length then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkVerificationLengthNotify(Context context,
                                                        @NonNull EditText editText) {

        if (Objects.requireNonNull(editText.getText()).length() < LENGTH_VERIFICATION_CODE) {

            // Toast error message
            CustomToast.errorMessage(context, DataUtils.getStringResource(context,
                    R.string.error_verification_code_length,
                    String.valueOf(LENGTH_VERIFICATION_CODE)),
                    0);

            // Enable error icon
            editText.setError(DataUtils.getStringResource(context,
                    R.string.error_verification_code_length,
                    String.valueOf(LENGTH_VERIFICATION_CODE)));

        } else {

            return true; // Return true on value acceptable
        }

        return false; // Return false on value not acceptable
    }

    /**
     * Function to check debt amount length then notify on error
     *
     * @param context  - To show toast
     * @param editText - Character Sequence
     *
     * @return boolean
     */
    public static boolean checkDebtAmountLengthNotify(Context context,
                                                      @NonNull EditText editText,
                                                      final boolean notify) {

        String errorMessage; // Error message

        // Check text length
        if (Objects.requireNonNull(editText.getText()).length() == 0) {

            errorMessage = DataUtils.getStringResource(context,
                    R.string.error_debt_amount_null);

        // Check for period at the end of the string
        } else if (editText.getText().toString().endsWith(".")) {

            // Toast error message
            errorMessage = DataUtils.getStringResource(context,
                    R.string.error_debt_amount_ends_with_period);

        } else {

            // Check if value is zero as a string in case of float number
            if (editText.getText().toString().equals("0")) {

                // Toast error message
                errorMessage = DataUtils.getStringResource(context,
                        R.string.error_debt_amount_zero);

            } else {

                return true; // Return true on value acceptable
            }
        }

        // Check if error message is empty
        if (!DataUtils.isEmptyString(errorMessage)) {

            // Check if notify permitted
            if (notify) {

                // Toast error message
                CustomToast.errorMessage(context, errorMessage,
                        R.drawable.ic_baseline_attach_money_24_white);

                // Enable error icon
                editText.setError(errorMessage);
            }
        }

        return false; // Return false on value not acceptable
    }
}
