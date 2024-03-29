package com.duesclerk.classes.network;

public class NetworkTags {

    /**
     * Class with user network tags
     */
    public static class UserNetworkTags {

        // Tag used to cancel SignUp request
        public static final String TAG_SIGNUP_PERSONAL_STRING_REQUEST = "TagStringRequestSignUpPersonal";
        public static final String TAG_SIGNUP_BUSINESS_STRING_REQUEST = "TagStringRequestSignUpBusiness";

        // Tag used to cancel Signin request
        public static final String TAG_SIGNIN_STRING_REQUEST = "TagStringRequestSignIn";

        // Tag used to cancel update user profile details request
        public static final String TAG_UPDATE_USER_DETAILS_STRING_REQUEST =
                "TagStringRequestUpdateUserProfile";

        // Tag used to cancel user profile details request
        public static final String TAG_FETCH_USER_PROFILE_STRING_REQUEST =
                "TagStringRequestFetchUserProfile";

        // Tag used to cancel email verification code request
        public static final String TAG_SEND_EMAIL_VERIFICATION_STRING_REQUEST
                = "TagStringRequestSendEmailVerificationCode";

        // Tag used to cancel email verification request
        public static final String TAG_VERIFY_EMAIL_STRING_REQUEST = "TagStringRequestVerifyEmailAddress";

        // Tag used to cancel password reset request
        public static final String TAG_PASSWORD_RESET_REQUEST = "TagStringRequestPasswordReset";

    }

    /**
     * Class with contacts network tags
     */
    public static class ContactsNetworkTags {

        // Tag used to cancel add contact request
        public static final String TAG_ADD_CONTACT_STRING_REQUEST = "TagStringRequestAddContact";

        // Tag used to cancel fetch contact request
        public static final String TAG_FETCH_USER_CONTACTS_STRING_REQUEST =
                "TagStringRequestFetchUserContacts";

        // Tag used to cancel SignUp request
        public static final String TAG_FETCH_CONTACT_DETAILS_AND_DEBTS_STRING_REQUEST =
                "TagStringRequestFetchContactDetailsAndDebts";

        // Tag used to cancel update contacts debt request
        public static final String TAG_UPDATE_CONTACT_STRING_REQUEST =
                "TagStringRequestUpdateContact";

        // Tag used to cancel delete contacts request
        public static final String TAG_DELETE_CONTACTS_STRING_REQUEST =
                "TagStringRequestDeleteContacts";
    }

    /**
     * Class with debts network tags
     */
    public static class DebtsNetworkTags {

        // Tag used to cancel add contacts debt request
        public static final String TAG_ADD_CONTACTS_DEBT_STRING_REQUEST =
                "TagStringRequestAddContactsDebt";

        // Tag used to cancel add contacts debt request
        public static final String TAG_UPDATE_CONTACTS_DEBT_STRING_REQUEST =
                "TagStringRequestUpdateContactsDebt";

        // Tag used to cancel delete contacts debts request
        public static final String TAG_DELETE_CONTACTS_DEBTS_STRING_REQUEST =
                "TagStringRequestDeleteContactsDebts";
    }
}
