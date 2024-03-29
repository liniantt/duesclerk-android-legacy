package com.duesclerk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.duesclerk.R;
import com.duesclerk.classes.contacts.DeleteContactsDebts;
import com.duesclerk.classes.custom_utilities.application.ApplicationClass;
import com.duesclerk.classes.custom_utilities.application.BroadCastUtils;
import com.duesclerk.classes.custom_utilities.application.ViewsUtils;
import com.duesclerk.classes.custom_utilities.application.VolleyUtils;
import com.duesclerk.classes.custom_utilities.user_data.ContactUtils;
import com.duesclerk.classes.custom_utilities.user_data.DataUtils;
import com.duesclerk.classes.custom_utilities.user_data.DebtUtils;
import com.duesclerk.classes.custom_utilities.user_data.UserAccountUtils;
import com.duesclerk.classes.custom_views.fragments.bottom_sheet_dialog_fragments.BottomSheetFragment_SortLists;
import com.duesclerk.classes.custom_views.fragments.dialog_fragments.DialogFragment_AddDebt;
import com.duesclerk.classes.custom_views.fragments.dialog_fragments.DialogFragment_UpdateContact;
import com.duesclerk.classes.custom_views.fragments.dialog_fragments.DialogFragment_UpdateDebt;
import com.duesclerk.classes.custom_views.recycler_view_adapters.RVLA_Debts;
import com.duesclerk.classes.custom_views.swipe_refresh.MultiSwipeRefreshLayout;
import com.duesclerk.classes.custom_views.toast.CustomToast;
import com.duesclerk.classes.custom_views.view_decorators.Decorators;
import com.duesclerk.classes.java_beans.JB_Debts;
import com.duesclerk.classes.network.InternetConnectivity;
import com.duesclerk.classes.network.NetworkTags;
import com.duesclerk.classes.network.NetworkUrls;
import com.duesclerk.classes.sort.SortLists;
import com.duesclerk.classes.storage_adapters.UserDatabase;
import com.duesclerk.enums.ListType;
import com.duesclerk.enums.SortType;
import com.duesclerk.interfaces.Interface_Debts;
import com.duesclerk.interfaces.Interface_IDS;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactDetailsAndDebtsActivity extends AppCompatActivity implements
        AppBarLayout.OnOffsetChangedListener, Interface_IDS, Interface_Debts {

    // Get class simple name
    // private final String TAG = ContactDetailsAndDebtsActivity.class.getSimpleName();

    private RelativeLayout rlNoConnection;
    private FloatingActionButton fabAddDebt, fabDeleteSelectedDebts;
    private RVLA_Debts rvlaDebts;
    private Context mContext;
    private TextView textTitle, textContactFullName, textContactPhoneNumber,
            textContactEmailAddress, textContactAddress, textNoDebtMessage, textDebtsTotalAmount;
    private MultiSwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener;
    private JSONArray fetchedContactDetails;
    private ArrayList<JB_Debts> fetchedDebts;
    private AppBarLayout appBarLayout;
    private UserDatabase database;
    private String contactId, contactType, contactFullName;
    private ShimmerFrameLayout shimmerContactDetails;
    private LinearLayout llContactDetails, llNoDebts, llContactEmailAddress, llContactAddress;
    private RecyclerView recyclerView;
    private BroadcastReceiver bcrReloadDebts, bcrSortContacts;
    private SearchView searchView;
    private DialogFragment_UpdateContact dialogFragmentUpdateContact;
    private DeleteContactsDebts deleteContactsOrDebts;
    private ImageView imageDeleteDebts, imageHideCheckBoxes, imageExpandListOptionsMenu,
            imageCollapseListOptionsMenu;
    private ExpandableLayout expandableListOptions;
    private boolean showingCheckBoxes = false;
    private BottomSheetFragment_SortLists bottomSheetFragmentSortLists;

    // Default sort type
    private SortLists sortLists;
    private SortType selectedSortType = SortType.DEBT_DATE_ADDED_DESCENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details_and_debts);

        mContext = this; // Get application context

        // ImageViews
        ImageView imageExit = findViewById(R.id.imageContactDetailsAndDebtsActivity_Exit);
        textTitle = findViewById(R.id.textContactDetailsAndDebtsActivity_Title);
        appBarLayout = findViewById(R.id.appBarLayout);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshContactDetailsAndDebtsActivity);
        imageDeleteDebts = findViewById(R.id.imageContactDetailsAndDebtsActivity_DeleteDebts);
        imageHideCheckBoxes = findViewById(R.id.imagePeopleOwingMe_HideCheckBoxes);
        imageExpandListOptionsMenu = findViewById(
                R.id.imageContactDetailsAndDebtsActivity_ShowMenu);
        imageCollapseListOptionsMenu = findViewById(
                R.id.imageContactDetailsAndDebtsActivity_CollapseOptionsMenu);
        ImageView imageSortList = findViewById(
                R.id.imageContactDetailsAndDebtsActivity_SortLists);

        // LinearLayouts
        LinearLayout llEditContact = findViewById(
                R.id.llContactDetailsAndDebtsActivity_EditContact);
        LinearLayout llDeleteContact = findViewById(
                R.id.llContactDetailsAndDebtsActivity_DeleteContact);

        llContactDetails = findViewById(R.id.llContactDetailsAndDebtsActivity_ContactDetails);
        LinearLayout llNoConnectionTryAgain = findViewById(R.id.llNoConnection_TryAgain);
        llNoDebts = findViewById(R.id.llContactDetailsAndDebts_NoDebt);
        LinearLayout llAddDebt = findViewById(R.id.llNoDebts_AddDebt);
        llContactEmailAddress = findViewById(
                R.id.llContactDetailsAndDebtsActivity_ContactEmailAddress);
        llContactAddress = findViewById(
                R.id.llContactDetailsAndDebtsActivity_ContactAddress);

        // RelativeLayout
        rlNoConnection = findViewById(R.id.rlContactDetailsAndDebtsActivity_NoConnection);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewDebts);

        // FloatingActionButtons
        fabAddDebt = findViewById(R.id.fabContactDetailsAndDebtsActivity_AddDebt);
        fabDeleteSelectedDebts = findViewById(
                R.id.fabContactDetailsAndDebtsActivity_DeleteDebts);

        // TextViews
        textContactFullName = findViewById(
                R.id.textContactDetailsAndDebtsActivity_ContactFullName);
        textContactPhoneNumber = findViewById(
                R.id.textContactDetailsAndDebtsActivity_ContactPhoneNumber);
        textContactEmailAddress = findViewById(
                R.id.textContactDetailsAndDebtsActivity_ContactEmailAddress);
        textContactAddress = findViewById(R.id.textContactDetailsAndDebtsActivity_ContactAddress);
        textDebtsTotalAmount = findViewById(
                R.id.textPeopleOwingMe_DebtsTotalAmount);
        textNoDebtMessage = findViewById(R.id.textNoDebt_Message);

        // ShimmerFrameLayout
        shimmerContactDetails = findViewById(R.id.shimmerContactDetailsAndDebtsActivity);

        // ExpandableLayouts
        expandableListOptions = findViewById(R.id.expandableContactDetailsAndDebtsActivity_Menu);

        // RecyclerView LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL,
                false);

        // Initialize and set RecyclerView decorator
        Decorators decorators = new Decorators(this);

        recyclerView.addItemDecoration(decorators); // Add item decoration
        recyclerView.setLayoutManager(layoutManager); // Set layout manager
        recyclerView.setHasFixedSize(false); // Set has fixed size to false

        // Broadcast receiver
        bcrReloadDebts = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {

                // Check if adapter is null
                if (rvlaDebts != null) {

                    // Empty selected debt ids ArrayList
                    DataUtils.clearArrayList(rvlaDebts.checkedDebtsIds);
                }

                String action = intent.getAction(); // Get action

                // Check for BroadCast action
                if (action.equals(BroadCastUtils.bcrActionReloadContactDetailsAndDebtsActivity)) {

                    // Check if adapter is null
                    if (rvlaDebts != null) {
                        // Adapter not null

                        // Check if CheckBoxes showing
                        if (rvlaDebts.showingCheckBoxes()) {

                            rvlaDebts.setShownListCheckBoxes(false);
                        }
                    }

                    // Start/Stop swipe SwipeRefresh
                    ViewsUtils.showSwipeRefreshLayout(true, true,
                            swipeRefreshLayout, swipeRefreshListener);
                }

            }
        };

        // BroadcastReceiver - Sort contacts by SortBy_ContactName
        bcrSortContacts = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {

                String action = intent.getAction(); // Get action

                // Check action
                if (action.equals(BroadCastUtils.bcrAction_SortLists)) {
                    // Sorting by SortBy_ContactName

                    // Update selected sort type
                    selectedSortType = (SortType) intent.getSerializableExtra("SORT_TYPE");

                    // Check if ArrayList is empty
                    if (!DataUtils.isEmptyArrayList(fetchedDebts)) {

                        sortAndLoadDebts(fetchedDebts);
                    }
                }
            }
        };

        // Get intent and values passed
        Intent intent = getIntent();
        this.contactId = intent.getStringExtra(ContactUtils.FIELD_CONTACT_ID); // Get contact id
        this.contactFullName = intent.getStringExtra(ContactUtils.FIELD_CONTACT_FULL_NAME);
        this.contactType = intent.getStringExtra(ContactUtils.FIELD_CONTACT_TYPE);

        // Set activity title
        setActivityTitle(contactType, contactFullName);

        // Dialog fragment to add debt
        DialogFragment_AddDebt dialogFragmentAddDebt = new DialogFragment_AddDebt(mContext,
                contactId, contactFullName);
        dialogFragmentAddDebt.setCancelable(false); // Disable cancelable
        dialogFragmentAddDebt.setRetainInstance(true); // Set retain instance

        // Dialog fragment to edit contact
        dialogFragmentUpdateContact = new DialogFragment_UpdateContact(
                mContext);
        dialogFragmentUpdateContact.setCancelable(false); // Disable cancelable
        dialogFragmentUpdateContact.setRetainInstance(true); // Set retain instance

        bottomSheetFragmentSortLists = new BottomSheetFragment_SortLists(mContext,
                ListType.LIST_DEBTS, selectedSortType);
        bottomSheetFragmentSortLists.setRetainInstance(true);

        // Add CoordinatorLayout as swipeable child
        swipeRefreshLayout.setSwipeableChildren(R.id.coordinator);

        sortLists = new SortLists(); // Initialize sort lists
        fetchedDebts = new ArrayList<>(); // Initialize ArrayList

        database = new UserDatabase(mContext); // Initialize database

        // Initialize delete contacts or debts class
        deleteContactsOrDebts = new DeleteContactsDebts(ContactDetailsAndDebtsActivity.this);

        // SwipeRefreshLayout listener
        swipeRefreshListener =
                () -> {

                    if (!DataUtils.isEmptyJSONArray(fetchedContactDetails)) {
                        fetchedContactDetails = null; // Clear contact details JSONArray
                    }

                    // Check if contact id is empty
                    if (!DataUtils.isEmptyString(contactId)) {

                        // Check if list adapter is null
                        if (rvlaDebts != null) {

                            // Check if CheckBoxes are showing
                            if (rvlaDebts.showingCheckBoxes()) {

                                swipeRefreshLayout.setRefreshing(false); // Stop SwipeRefresh
                                return; // Break
                            }
                        }

                        // Fetch contact details
                        fetchContactData(contactId);
                    }
                };

        // Add refresh listener to SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);

        // Set view offset
        swipeRefreshLayout.setProgressViewOffset(false,
                DataUtils.getIntegerResource(mContext, R.integer.int_swipe_refresh_offset_start),
                DataUtils.getIntegerResource(mContext, R.integer.int_swipe_refresh_offset_end));

        // Setup SearchView
        searchView = ViewsUtils.initSearchView(this, R.id.searchViewDebts);

        // Add query text listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                // Filter text input
                rvlaDebts.getFilter().filter(query);
                return false;
            }
        });

        // Exit onClick
        imageExit.setOnClickListener(v -> finish());

        // No connection try again onClick
        llNoConnectionTryAgain.setOnClickListener(v -> {

            // Start/Stop swipe SwipeRefresh
            ViewsUtils.showSwipeRefreshLayout(true, true, swipeRefreshLayout,
                    swipeRefreshListener);
        });

        // FAB add debt onClick
        fabAddDebt.setOnClickListener(v -> {

            // Show add debt dialog
            ViewsUtils.showDialogFragment(getSupportFragmentManager(),
                    dialogFragmentAddDebt, true);
        });

        // FAB delete debts onClick
        fabDeleteSelectedDebts.setOnClickListener(v -> {

            // Get selected debt ids
            String[] debtIds = rvlaDebts.getCheckedDebtsIds();

            // Check if debt ids
            if (!DataUtils.isEmptyStringArray(debtIds)) {

                // Delete multiple debts
                deleteContactsOrDebts.confirmAndDeleteContactsOrDebts(false,
                        contactFullName, contactId, debtIds);
            }
        });

        // LinearLayout add debt onClick
        llAddDebt.setOnClickListener(v -> {

            fabAddDebt.performClick(); // Click fab add debt
        });

        // Edit contact onClick - Show edit contact dialog
        llEditContact.setOnClickListener(v -> {

            // Show add debt dialog
            ViewsUtils.showDialogFragment(getSupportFragmentManager(),
                    dialogFragmentUpdateContact, true);
        });

        // Delete contact onClick - Show delete contact confirmation
        llDeleteContact.setOnClickListener(v ->

                // Delete contact
                deleteContactsOrDebts.confirmAndDeleteContactsOrDebts(
                        true, contactFullName,
                        database.getUserAccountInfo(null).get(0).getUserId(),
                        new String[]{contactId}));

        // Show menu onClick
        imageExpandListOptionsMenu.setOnClickListener(v -> {

            showMenuButton(false); // Hide show-menu button
            expandMenuExpandableLayout(true); // Expand ExpandableLayout
        });

        // Collapse menu onClick
        imageCollapseListOptionsMenu.setOnClickListener(v -> {

            showMenuButton(true); // Show show-menu button
            expandMenuExpandableLayout(false); // Collapse ExpandableLayout
        });

        // Delete debts onClick
        imageDeleteDebts.setOnClickListener(v -> {

            expandMenuExpandableLayout(false); // Collapse ExpandableLayout

            // Check if ExpandableLayout is expanded
            if (!expandableListOptions.isExpanded()) {
                // Check if CheckBoxes are not showing
                if (!rvlaDebts.showingCheckBoxes()) {

                    showingCheckBoxes = true; // Set showing CheckBoxes
                    showDeleteButton(false); // Hide delete button
                    rvlaDebts.setShownListCheckBoxes(true); // Show list CheckBoxes
                    fabAddDebt.setVisibility(View.GONE); // Hide add debt FAB
                }

                showSearchView(false); // Hide SearchView
            }
        });

        // Sort list onClick
        imageSortList.setOnClickListener(v -> {

            expandMenuExpandableLayout(false); // Collapse ExpandableLayout

            // Show sort contacts BottomSheet
            ViewsUtils.showBottomSheetDialogFragment(getSupportFragmentManager(),
                    bottomSheetFragmentSortLists, true);
        });

        // Hide CheckBoxes onClick
        imageHideCheckBoxes.setOnClickListener(v -> {

            showSearchView(true); // Show SearchView

            // Check if CheckBoxes are showing
            if (rvlaDebts.showingCheckBoxes()) {

                showingCheckBoxes = false; // Set showing CheckBoxes
                rvlaDebts.setShownListCheckBoxes(false); // Hide list CheckBoxes
                showDeleteButton(true); // Show delete button
                showFabAddDebt(true); // Show FAB add debt
                showFabDeleteSelectedDebts(false); // Hide FAB delete selected debt records
            }
        });

        // Create ItemTouchHelper call back
        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NotNull RecyclerView recyclerView,
                                  RecyclerView.@NotNull ViewHolder viewHolder,
                                  RecyclerView.@NotNull ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.@NotNull ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT:

                        rvlaDebts.setExpandedDebtOptionsMenu(true, viewHolder.getAdapterPosition());
                        break;

                    case ItemTouchHelper.RIGHT:

                        rvlaDebts.setExpandedDebtOptionsMenu(false, viewHolder.getAdapterPosition());
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView,
                                    RecyclerView.@NotNull ViewHolder viewHolder, float dX,
                                    float dY, int actionState, boolean isCurrentlyActive) {
            }
        };

        // Initialize ItemTouchHelper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);

        // Attach ItemTouchHelper to RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Register reload broadcast
        BroadCastUtils.registerBroadCasts(ContactDetailsAndDebtsActivity.this, bcrReloadDebts,
                BroadCastUtils.bcrActionReloadContactDetailsAndDebtsActivity);

        // Register sort BroadcastReceiver
        BroadCastUtils.registerBroadCasts(ContactDetailsAndDebtsActivity.this,
                bcrSortContacts, BroadCastUtils.bcrAction_SortLists);

        // Start / Stop swipe SwipeRefresh
        ViewsUtils.showSwipeRefreshLayout(true, true, swipeRefreshLayout,
                swipeRefreshListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister reload BroadcastReceiver
        BroadCastUtils.unRegisterBroadCast(ContactDetailsAndDebtsActivity.this,
                bcrReloadDebts);

        // Unregister sort BroadcastReceiver
        BroadCastUtils.unRegisterBroadCast(ContactDetailsAndDebtsActivity.this,
                bcrSortContacts);

        // Broadcast to refresh contacts
        Intent intentBroadcastPeopleOwingMe = new Intent(
                BroadCastUtils.bcrActionReloadPeopleOwingMe);

        Intent intentBroadcastPeopleIOwe = new Intent(
                BroadCastUtils.bcrActionReloadPeopleIOwe);

        // Send broadcasts
        sendBroadcast(intentBroadcastPeopleOwingMe);
        sendBroadcast(intentBroadcastPeopleIOwe);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save dialog inputs to outState

        // Check for field values and set to outState
        if (!DataUtils.isEmptyString(contactId)) {

            outState.putString(ContactUtils.FIELD_CONTACT_ID, contactId); // Put contact id
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {

            // Get dialog inputs from savedInstanceState
            String savedContactId = savedInstanceState.getString(ContactUtils.FIELD_CONTACT_ID);

            // Check for values
            if (!DataUtils.isEmptyString(savedContactId)) {

                contactId = savedContactId;// Set contact id
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        appBarLayout.removeOnOffsetChangedListener(this); // Remove offset changed listener
    }

    @Override
    protected void onResume() {
        super.onResume();

        appBarLayout.addOnOffsetChangedListener(this); // Add offset changed listener
    }

    @Override
    public void onBackPressed() {

        // Check if adapter is empty
        if (rvlaDebts != null) {

            // CheckBoxes hidden and Expandable views collapsed
            if (rvlaDebts.showingCheckBoxes() || rvlaDebts.isExpandedOptionsOrDetails()) {

                if (rvlaDebts.showingCheckBoxes()) {
                    // CheckBoxes showing

                    // Hide CheckBoxes
                    rvlaDebts.setShownListCheckBoxes(false);
                    showDeleteButton(true); // Show delete button
                }

                if (rvlaDebts.isExpandedOptionsOrDetails()) {
                    // Views expanded in RecyclerView adapter

                    // Collapse expanded views in RecyclerView adapter
                    rvlaDebts.setCollapsedExpandedLayouts(false);
                }
            } else {

                super.onBackPressed(); // EXit activity
            }
        } else {

            super.onBackPressed(); // Exit activity
        }
    }

    /**
     * Function to set and update activity title
     *
     * @param contactType     - String activity title
     * @param contactFullName - Contact full name
     */
    private void setActivityTitle(String contactType, String contactFullName) {

        String title = "";
        if (contactType.equals(ContactUtils.KEY_CONTACT_TYPE_PEOPLE_OWING_ME)) {

            title = DataUtils.getStringResource(mContext, R.string.title_debts_people_owing_me,
                    contactFullName);

        } else if (contactType.equals(ContactUtils.KEY_CONTACT_TYPE_PEOPLE_I_OWE)) {

            title = DataUtils.getStringResource(mContext, R.string.title_debts_people_i_owe,
                    contactFullName);
        }
        textTitle.setText(title); // Set activity title
    }

    /**
     * Function to show / hide SearchView
     *
     * @param show - boolean - (show / hide view)
     */
    private void showSearchView(boolean show) {

        if (show) {

            searchView.setVisibility(View.VISIBLE); // Show SearchView

        } else {

            // Hide keyboard
            ViewsUtils.hideKeyboard(ContactDetailsAndDebtsActivity.this);

            searchView.setVisibility(View.GONE); // Hide SearchView
        }
    }

    /**
     * Function to show / hide add debt fab
     *
     * @param show - boolean - (show / hide view)
     */
    private void showFabAddDebt(boolean show) {

        if (show) {

            fabAddDebt.setVisibility(View.VISIBLE); // Show FAB
            showFabDeleteSelectedDebts(false); // Hide delete debts FAB

        } else {

            fabAddDebt.setVisibility(View.GONE); // Hide FAB
        }
    }

    /**
     * Function to show / hide delete selected debts fab
     *
     * @param show - boolean - (show / hide view)
     */
    private void showFabDeleteSelectedDebts(boolean show) {

        if (show) {

            fabDeleteSelectedDebts.setVisibility(View.VISIBLE); // Show FAB

        } else {

            fabDeleteSelectedDebts.setVisibility(View.GONE); // Hide FAB
        }
    }

    /**
     * Function to show / hide contact details
     *
     * @param show - boolean - (show / hide view)
     */
    private void showContactDetails(boolean show) {

        if (show) {

            swipeRefreshLayout.setVisibility(View.VISIBLE); // Show SwipeRefreshLayout
            llContactDetails.setVisibility(View.VISIBLE); // Show contact details

        } else {

            llContactDetails.setVisibility(View.GONE); // Hide contact details
        }

        showNoConnectionLayout(!show); // Show / hide no connection layout
    }

    /**
     * Function to show / hide connection layout
     *
     * @param show - boolean - (show / hide view)
     */
    private void showNoConnectionLayout(boolean show) {

        if (show) {

            rlNoConnection.setVisibility(View.VISIBLE); // Show SwipeRefreshLayout
            swipeRefreshLayout.setVisibility(View.GONE); // Hide contact details

        } else {

            rlNoConnection.setVisibility(View.GONE); // Hide no connection layout
        }
    }

    /**
     * Function to show / hide RecyclerView
     *
     * @param show - boolean - (show / hide view)
     */
    private void showRecyclerView(boolean show) {

        if (show) {

            recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
            textDebtsTotalAmount.setVisibility(View.VISIBLE); // Show total debts amount
            showNoDebtsLayout(false); // Hide no debts layout

        } else {

            recyclerView.setVisibility(View.GONE); // Hide RecyclerView
            textDebtsTotalAmount.setVisibility(View.GONE); // Hide total debts amount
        }
    }

    /**
     * Function to show / hide debts layout
     *
     * @param show - boolean - (show / hide view)
     */
    private void showNoDebtsLayout(boolean show) {

        if (show) {

            String noDebtsMessage = DataUtils.getStringResource(mContext,
                    R.string.msg_debts_empty, contactFullName);

            textNoDebtMessage.setText(noDebtsMessage); // Set no debts message

            llNoDebts.setVisibility(View.VISIBLE); // Show no debts layout
            showRecyclerView(false); // Hide RecyclerView
            imageHideCheckBoxes.setVisibility(View.GONE); // Hide (hide CheckBoxes button

        } else {

            llNoDebts.setVisibility(View.GONE); // Hide no debts layout
        }

        showSearchView(!show); // Show / Hide SearchView
    }

    /**
     * Function to show / hide show-menu button and (Hide CheckBoxes) button
     *
     * @param show - Show / hide delete button
     */
    private void showMenuButton(boolean show) {

        if (show) {

            imageExpandListOptionsMenu.setVisibility(View.VISIBLE); // Show delete button
            imageCollapseListOptionsMenu.setVisibility(View.GONE); // Hide show-menu button

            // Check if CheckBoxes are showing
            if (!showingCheckBoxes) {

                imageHideCheckBoxes.setVisibility(View.GONE); // Hide (Hide delete) button
            }
        } else {

            imageExpandListOptionsMenu.setVisibility(View.GONE); // HIde delete button
            imageCollapseListOptionsMenu.setVisibility(View.VISIBLE); // Show collapse-menu button

            // Check if CheckBoxes are showing
            if (showingCheckBoxes) {

                imageHideCheckBoxes.setVisibility(View.VISIBLE); // Show (Hide delete) button
                imageCollapseListOptionsMenu.setVisibility(View.GONE); // Hide collapse button
            }
        }
    }

    /**
     * Function to expand and collapse ExpandableLayout,
     * while hiding and showing dismiss button
     *
     * @param expand - Expand / collapse ExpandableLayout
     */
    private void expandMenuExpandableLayout(boolean expand) {

        // Expand / collapse ExpandableLayout
        ViewsUtils.expandExpandableLayout(expand, expandableListOptions);

        // Check if expanding
        if (!expand) {

            showMenuButton(true); // Show show-menu button
        }
    }

    /**
     * Function to respond to connection failures
     *
     * @param connected - boolean - (network connected / not connected)
     */
    private void handleNetworkConnectionEvent(boolean connected) {

        // Check connection state
        if (!connected) {
            // No connection

            // Hide swipe SwipeRefresh
            ViewsUtils.showSwipeRefreshLayout(false, false, swipeRefreshLayout,
                    swipeRefreshListener);

            showContactDetails(false); // Hide contact details

            // Toast connection error message
            CustomToast.errorMessage(mContext,
                    DataUtils.getStringResource(mContext,
                            R.string.error_network_connection_error_message_short),
                    R.drawable.ic_sad_cloud_100px_white);
        }

        showNoConnectionLayout(!connected); // Show / hide no connection layout
    }

    /**
     * Function to show / hide delete button and (Hide CheckBoxes) button
     *
     * @param show - Show / hide delete button
     */
    private void showDeleteButton(boolean show) {

        if (show) {

            imageDeleteDebts.setVisibility(View.VISIBLE); // Show delete button
            imageHideCheckBoxes.setVisibility(View.GONE); // Hide (Hide delete) button

        } else {

            imageDeleteDebts.setVisibility(View.GONE); // HIde delete button
            imageHideCheckBoxes.setVisibility(View.VISIBLE); // Show (Hide delete) button
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        // Check if SwipeRefreshLayout is refreshing
        if (swipeRefreshLayout.isRefreshing()) {

            swipeRefreshLayout.setEnabled(true); // Enable SwipeRefreshLayout

        } else {

            // Refresh will only be enabled when the offset is zero
            swipeRefreshLayout.setEnabled(verticalOffset == 0);
        }
    }

    /**
     * Function to fetch/retrieve contact data
     *
     * @param contactId - contact id
     */
    private void fetchContactData(String contactId) {

        // Check internet connection state
        if (InternetConnectivity.isConnectedToAnyNetwork(mContext)) {
            // Connected

            handleNetworkConnectionEvent(true); // Set connection established to true

            // Create string request
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkUrls.ContactURLS.URL_FETCH_CONTACT_DETAILS_AND_DEBTS, response -> {

                // Log Response
                // Log.d(TAG, "Fetching contact data response:" + response);

                showFabAddDebt(false); // Hide add debt FAB

                // Hide SwipeRefreshLayout
                ViewsUtils.showSwipeRefreshLayout(false, true,
                        swipeRefreshLayout, swipeRefreshListener);

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean(VolleyUtils.KEY_ERROR);

                    // Check for error
                    if (!error) {
                        // Contact data fetched

                        extractContactDetails(jsonObject); // Extract contact details

                        // Get JSONArray From JSONObject
                        JSONArray contactDebts = jsonObject.getJSONArray(
                                DebtUtils.KEY_DEBTS);

                        // Split JSONArray to get contact debts records
                        extractDebtsJSONArray(contactDebts);

                    } else {
                        // Error updating details

                        String errorMessage = jsonObject.getString(
                                VolleyUtils.KEY_ERROR_MESSAGE);

                        // Toast error message
                        CustomToast.errorMessage(
                                mContext,
                                errorMessage,
                                R.drawable.ic_baseline_edit_24_white);

                        // Cancel Pending Request
                        ApplicationClass.getClassInstance().cancelPendingRequests(
                                NetworkTags.ContactsNetworkTags.TAG_FETCH_CONTACT_DETAILS_AND_DEBTS_STRING_REQUEST);
                    }
                } catch (Exception ignored) {
                }
            }, volleyError -> {

                // Log Response
                // Log.e(TAG, "Fetch contact data response error : "
                // + volleyError.getMessage());

                // Hide SwipeRefreshLayout
                ViewsUtils.showSwipeRefreshLayout(false, true,
                        swipeRefreshLayout, swipeRefreshListener);


                // Check request response
                if (volleyError.getMessage() == null || volleyError instanceof NetworkError
                        || volleyError instanceof ServerError || volleyError instanceof
                        AuthFailureError || volleyError instanceof TimeoutError) {

                    CustomToast.errorMessage(mContext, DataUtils.getStringResource(mContext,
                            R.string.error_network_connection_error_message_short),
                            R.drawable.ic_sad_cloud_100px_white);

                } else {

                    // Toast Connection Error Message
                    CustomToast.errorMessage(mContext, volleyError.getMessage(),
                            R.drawable.ic_sad_cloud_100px_white);
                }

                // Clear url cache
                ApplicationClass.getClassInstance().deleteUrlVolleyCache(
                        NetworkUrls.ContactURLS.URL_FETCH_CONTACT_DETAILS_AND_DEBTS);
            }) {
                @Override
                protected void deliverResponse(String response) {
                    super.deliverResponse(response);
                }

                    /*@Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put(VolleyUtils.KEY_API_KEY, VolleyUtils.getApiKey(mContext));
                        return headers;
                    }*/

                @Override
                protected Map<String, String> getParams() {
                    @SuppressWarnings({"unchecked", "rawtypes"}) Map<String, String> params =
                            new HashMap();

                    // Put userId and contactId to Map params
                    params.put(UserAccountUtils.FIELD_USER_ID,
                            database.getUserAccountInfo(null).get(0).getUserId());
                    params.put(ContactUtils.FIELD_CONTACT_ID, contactId);

                    return params; // Return params
                }

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    return super.parseNetworkError(volleyError);
                }

                @Override
                public void deliverError(VolleyError error) {
                    super.deliverError(error);
                }
            };

            // Set Request Priority
            ApplicationClass.getClassInstance().setPriority(Request.Priority.HIGH);

            // Set retry policy
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DataUtils.getIntegerResource(mContext,
                            R.integer.int_volley_account_request_initial_timeout_ms),
                    DataUtils.getIntegerResource(mContext,
                            R.integer.int_volley_account_request_max_timeout_retry),
                    1.0f));

            // Set request caching to false
            stringRequest.setShouldCache(false);

            // Adding request to request queue
            ApplicationClass.getClassInstance().addToRequestQueue(stringRequest,
                    NetworkTags.ContactsNetworkTags.TAG_FETCH_CONTACT_DETAILS_AND_DEBTS_STRING_REQUEST);

        } else {

            handleNetworkConnectionEvent(false); // Handle no connection event

            // Toast network connection message
            CustomToast.errorMessage(
                    mContext,
                    DataUtils.getStringResource(mContext,
                            R.string.error_network_connection_error_message_long),
                    R.drawable.ic_sad_cloud_100px_white);
        }
    }

    /**
     * Function to extract debts from jsonArray
     *
     * @param jsonObject - JSONObject to get contact details JSONObject
     */
    private void extractContactDetails(JSONObject jsonObject) {

        // Check if JSONObject is empty
        if (!DataUtils.isEmptyJSONObject(jsonObject)) {

            try {

                // Get JSONObject from JSONObject
                JSONObject contactDetails = jsonObject.getJSONObject(
                        ContactUtils.KEY_CONTACT_DETAILS);

                // Get contact details
                this.contactFullName = contactDetails.getString(
                        ContactUtils.FIELD_CONTACT_FULL_NAME);
                String contactPhoneNumber = contactDetails.getString(
                        ContactUtils.FIELD_CONTACT_PHONE_NUMBER);
                String contactEmailAddress = contactDetails.getString(
                        ContactUtils.FIELD_CONTACT_EMAIL_ADDRESS);
                String contactAddress = contactDetails.getString(
                        ContactUtils.FIELD_CONTACT_ADDRESS);
                String debtsTotalAmount = DataUtils.getStringResource(
                        mContext,
                        R.string.label_debts_total_amount,
                        contactDetails.getString(DebtUtils.KEY_DEBTS_TOTAL_AMOUNT)
                );

                setActivityTitle(contactType, contactFullName); // Update activity title

                // Set contact details
                this.textContactFullName.setText(contactFullName);
                this.textContactPhoneNumber.setText(contactPhoneNumber);
                this.textDebtsTotalAmount.setText(debtsTotalAmount);

                // Check if contact email address is null
                if (!DataUtils.isEmptyString(contactEmailAddress)) {

                    // Set contact email address
                    this.textContactEmailAddress.setText(contactEmailAddress);

                    // Show contact email address layout
                    this.llContactEmailAddress.setVisibility(View.VISIBLE);

                } else {

                    // Hide contact email address layout
                    this.llContactEmailAddress.setVisibility(View.GONE);
                }

                // Check if contact address is null
                if (!DataUtils.isEmptyString(contactAddress)) {

                    // Set contact address
                    this.textContactAddress.setText(contactAddress);

                    // Show contact address layout
                    this.llContactAddress.setVisibility(View.VISIBLE);

                } else {

                    // Hide contact address layout
                    this.llContactAddress.setVisibility(View.GONE);
                }

                // Show ShimmerFrameLayout
                ViewsUtils.showShimmerFrameLayout(false, shimmerContactDetails);

                showContactDetails(true); // Show contact details

                showFabAddDebt(true); // Show add debt FAB

                // Pass contact details to
                dialogFragmentUpdateContact.setContactDetails(contactId, contactFullName,
                        contactPhoneNumber, contactEmailAddress, contactAddress);

            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Function to extract debts from jsonArray
     *
     * @param jsonArray - JSONArray with debts
     */
    private void extractDebtsJSONArray(JSONArray jsonArray) {

        if (jsonArray != null) {

            if (jsonArray.length() > 0) {
                // Looping through all the elements of the json array

                fetchedDebts.clear(); // Clear ArrayList

                for (int i = 0; i < jsonArray.length(); i++) {

                    // Creating a json object of the current index
                    JSONObject jsonObject;
                    JB_Debts jbDebts = new JB_Debts();

                    try {

                        // Getting Data json object
                        jsonObject = jsonArray.getJSONObject(i);

                        // Getting Data from json object
                        String debtId, debtAmount, debtDateAdded, debtDateIssued, debtDateDue,
                                debtDescription, contactId, contactType, userId;

                        debtId = jsonObject.getString(DebtUtils.FIELD_DEBT_ID);
                        debtAmount = jsonObject.getString(DebtUtils.FIELD_DEBT_AMOUNT);
                        debtDateAdded = jsonObject
                                .getString(DebtUtils.FIELD_DEBT_DATE_ADDED);
                        debtDateIssued = jsonObject.getString(DebtUtils.FIELD_DEBT_DATE_ISSUED);
                        debtDateDue = jsonObject.getString(DebtUtils.FIELD_DEBT_DATE_DUE);
                        debtDescription = jsonObject.getString(DebtUtils.FIELD_DEBT_DESCRIPTION);
                        contactId = jsonObject.getString(ContactUtils.FIELD_CONTACT_ID);
                        contactType = jsonObject.getString(ContactUtils.FIELD_CONTACT_TYPE);
                        userId = jsonObject.getString(UserAccountUtils.FIELD_USER_ID);

                        // Set data to java bean
                        jbDebts.setDebtId(debtId);
                        jbDebts.setDebtAmount(debtAmount);
                        jbDebts.setDebtDateAdded(debtDateAdded);
                        jbDebts.setDebtDateIssued(debtDateIssued);
                        jbDebts.setDebtDateDue(debtDateDue);
                        jbDebts.setDebtDescription(debtDescription);
                        jbDebts.setContactId(contactId);
                        jbDebts.setContactType(contactType);
                        jbDebts.setUserId(userId);

                        // Add java bean to ArrayList
                        fetchedDebts.add(jbDebts);

                    } catch (Exception ignored) {
                    }
                }

                // Sort and load ArrayList
                sortAndLoadDebts(fetchedDebts);

            } else {
                // Array is empty

                showNoDebtsLayout(true); // Show no debts view
                imageDeleteDebts.setVisibility(View.GONE); // Hide delete multiple debts button
                showFabAddDebt(false); // Hide add debt FAB for user to use add debt layout button
            }
        } else {

            showNoDebtsLayout(true); // Show no debts view
        }
    }

    /**
     * Function to load debts into RecyclerView
     *
     * @param debts - ArrayList with contacts debts
     */
    private void sortAndLoadDebts(ArrayList<JB_Debts> debts) {

        // Sort ArrayList
        this.fetchedDebts = sortLists.sortDebtsList(debts, selectedSortType);

        // Check for fetched debt records
        if (!DataUtils.isEmptyArrayList(fetchedDebts)) {
            // Debt records found

            showNoConnectionLayout(false); // Hide no debts view

            // Creating RecyclerView adapter object
            rvlaDebts = new RVLA_Debts(mContext, fetchedDebts, this);

            // Check for adapter observers
            if (!rvlaDebts.hasObservers()) {

                rvlaDebts.setHasStableIds(true); // Set has stable ids
            }

            recyclerView.setAdapter(rvlaDebts); // Setting Adapter to RecyclerView
            rvlaDebts.notifyDataSetChanged(); // Notify Data Set Changed

            showSearchView(true); // Show SearchView
            showDeleteButton(true); // Show delete button

            // Filter text input
            if (!DataUtils.isEmptyString(searchView.getQuery().toString())) {

                // Set adapter filter query
                rvlaDebts.getFilter().filter(searchView.getQuery().toString());
            }

            showRecyclerView(true); // Show RecyclerView
            showMenuButton(true); // Show menu button
        } else {

            showNoDebtsLayout(true); // Show no debts view
        }
    }

    @Override
    public void passContactsIds(String[] contactsIds) {
    }

    @Override
    public void passDebtsIds(String[] debtsIds) {

        // Delete debt
        deleteContactsOrDebts.confirmAndDeleteContactsOrDebts(false, contactFullName,
                this.contactId, debtsIds);

        // Empty selected debt ids ArrayList
        DataUtils.clearArrayList(rvlaDebts.checkedDebtsIds);
    }

    @Override
    public void showDeleteDebtsFab(boolean show) {

        showFabDeleteSelectedDebts(show); // Show / hide fab delete selected debts
    }

    @Override
    public void passDebtDetails(JB_Debts jbDebt) {

        // Initialize DialogFragment_UpdateDebt
        DialogFragment_UpdateDebt dialogFragmentUpdateDebt = new DialogFragment_UpdateDebt(
                mContext,
                contactId,
                jbDebt.getDebtId(),
                jbDebt.getDebtAmount(),
                jbDebt.getDebtDateIssued(),
                jbDebt.getDebtDateDue(),
                jbDebt.getDebtDescription()
        );

        // Set to retain instance
        dialogFragmentUpdateDebt.setRetainInstance(true);

        // Show DialogFragment_UpdateDebt
        ViewsUtils.showDialogFragment(getSupportFragmentManager(), dialogFragmentUpdateDebt,
                true);
    }
}
