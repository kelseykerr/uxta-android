package iuxta.uxta;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;

import layout.AccountFragment;
import layout.ExchangeCodeDialogFragment;
import layout.ExchangeOverrideDialogFragment;
import layout.FiltersDialogFragment;
import layout.HistoryFiltersDialogFragment;
import layout.HistoryFragment;
import layout.HomeFragment;
import layout.NewOfferDialogFragment;
import layout.PaymentDestinationDialogFragment;
import layout.PaymentDetailsDialogFragment;
import layout.PaymentDialogFragment;
import layout.ReportRequestFragment;
import layout.RequestDialogFragment;
import layout.RequestPreviewFragment;
import layout.UpdateAccountDialogFragment;
import layout.ViewOfferDialogFragment;
import layout.ViewRequestFragment;
import iuxta.uxta.model.Request;
import iuxta.uxta.model.Response;
import iuxta.uxta.model.User;
import iuxta.uxta.service.NearbyInstanceIdService;
import iuxta.uxta.service.NearbyMessagingService;

/**
 * Created by kerrk on 7/17/16.
 */
public class MainActivity extends AppCompatActivity
        implements AccountFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        RequestDialogFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        NewOfferDialogFragment.OnFragmentInteractionListener,
        UpdateAccountDialogFragment.OnFragmentInteractionListener,
        ExchangeCodeDialogFragment.OnFragmentInteractionListener,
        ExchangeOverrideDialogFragment.OnFragmentInteractionListener,
        FiltersDialogFragment.OnFragmentInteractionListener,
        HistoryFiltersDialogFragment.OnFragmentInteractionListener,
        GoogleApiClient.OnConnectionFailedListener,
        ViewRequestFragment.OnFragmentInteractionListener,
        ReportRequestFragment.OnFragmentInteractionListener {

    public static User user;
    private Toolbar toolbar;
    private BottomBar mBottomBar;
    private Integer currentMenuItem;
    private RelativeLayout toolbarLine2;
    private RelativeLayout toolbarHistory;
    private EditText searchBar;
    private ImageButton searchBtn;
    private String snackbarMessage;
    String currentText = "list";
    FragmentManager fragmentManager = getFragmentManager();
    private String notificationType;
    private Response response;
    private Request request;
    private boolean readNotification = false;
    public static GoogleApiClient mGoogleApiClient;
    public static ConnectivityManager connMgr;
    public static LocationManager locationMgr;
    private FloatingActionButton fab;
    private ProgressDialog mProgressDialog;
    private LocalBroadcastManager mLocalBroadcastManager;
    public static CognitoCachingCredentialsProvider credentialsProvider;
    public static AmazonS3 s3;
    public static TransferUtility transferUtility;


    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

    public static final String TAG = "MainActivity";

    /**
     * Id to identify the fine location permission request.
     */
    private static final int REQUEST_FINE_LOCATION = 0;

    private static final int REQUEST_WIFI_STATE = 1;

    private static final int REQUEST_CAMERA = 2;

    private static final int READ_EXTERNAL_STORAGE = 3;

    private boolean hasMessage() {
        notificationType = getIntent().getStringExtra("type");
        return notificationType != null;
    }

    private void checkNotificationOnOpen() {
        notificationType = getIntent().getStringExtra("type");
        if (notificationType != null) {
            if (notificationType.equals(NearbyMessagingService.NotificationType.response_update.toString())) {
                String responseJson = getIntent().getStringExtra("response");
                String requestJson = getIntent().getStringExtra("request");
                try {
                    response = new ObjectMapper().readValue(responseJson, Response.class);
                    request = new ObjectMapper().readValue(requestJson, Request.class);
                    DialogFragment newFragment = ViewOfferDialogFragment
                            .newInstance(response, request);
                    newFragment.show(getFragmentManager(), "dialog");
                    readNotification = true;
                } catch (IOException e) {
                    Log.e(TAG, "Error reading json from notification**" + e.getMessage());
                }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        user = PrefUtils.getCurrentUser(MainActivity.this);
        if (user == null || user.getAccessToken() == null) {
            //TODO: WTF is this?!
            if (user != null && user.getAccessToken() != null) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SharedAsyncMethods.getUserInfoFromServer(user, this);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_landing);
        mLayout = findViewById(R.id.landing_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    R.string.permission_fine_location_rationale, REQUEST_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_WIFI_STATE,
                    R.string.permission_wifi_state_rationale, REQUEST_WIFI_STATE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.CAMERA,
                    R.string.permission_camera_rationale, REQUEST_CAMERA);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    R.string.permission_external_storage, READ_EXTERNAL_STORAGE);
        }

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        toolbarLine2 = (RelativeLayout) findViewById(R.id.toolbar_line_2);
        toolbarHistory = (RelativeLayout) findViewById(R.id.toolbar_history_filter);
        searchBar = (EditText) findViewById(R.id.search_bar);
        setSearchBarDone();
        searchBtn = (ImageButton) findViewById(R.id.search_button);
        setSearchBtnClick();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setUpFab();

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottom_bar);
        // if this is being opened from a notification message, default to history view
        if (hasMessage() && !notificationType.equals("request_notification")) {
            mBottomBar.setDefaultTabPosition(1);
        }
        //TODO: if request_notification, the radius should be set to the user's settings
        setmBottomBarListener();
        Log.i(TAG, "user access token: " +  user.getAccessToken() + " ****************");
        Log.i(TAG, "user name: " + user.getName() + " ****************");
        Log.i(TAG, "auth method: " + user.getAuthMethod() + "********");
        Log.i(TAG, "****FCM TOKEN: " +  FirebaseInstanceId.getInstance().getToken());
        NearbyInstanceIdService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken(), this);
        checkNotificationOnOpen();
        //scheduleNotificationsAlarm();
        if (user.getTosAccepted() == null || user.getTosAccepted().equals(Boolean.FALSE) || !AppUtils.canAddPayments(user)) {
            AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
            AccountFragment.updateAccountDialog.show(getFragmentManager(), "dialog");
        }
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:c505ec70-228a-498f-87e5-d9035d10a8e3", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    public void setUpFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (user.getCommunityId() == null || user.getCommunityId().isEmpty()) {
                    HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                    homeFragment.displayNoCommunitySnackbar();
                } else {
                    showNewRequestDialog(v);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBroadcastReceiver();
        if (user != null && user.getAuthMethod() != null && user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD)) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        unregisterBroadcastReceiver();
    }

    private void setSearchBtnClick() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String searchTerm = searchBar.getText().toString();
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                searchBar.clearFocus();
                HomeFragment.searchTerm = searchTerm;
                HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                homeFragment.getRequests(null);
            }
        });
    }

    private void setSearchBarDone() {
        searchBar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchBtn.performClick();
                    return true;
                }
                return false;
            }
        });
    }


    public void showNewRequestDialog(View view) {
        DialogFragment newFragment = RequestDialogFragment
                .newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void filterRequests(View view) {
        DialogFragment newFragment = FiltersDialogFragment
                .newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void filterHistory(View view) {
        DialogFragment newFragment = HistoryFiltersDialogFragment
                .newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void setmBottomBarListener() {
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHomeItem) {
                    toolbar.setVisibility(View.VISIBLE);
                    toolbarLine2.setVisibility(View.VISIBLE);
                    toolbarHistory.setVisibility(View.GONE);
                    HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                    if (homeFragment != null) {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                                .show(homeFragment)
                                .commit();
                        homeFragment.getRequests(null);
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                                .add(R.id.content_frame, HomeFragment.newInstance(), Constants.HOME_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.HOME_FRAGMENT_TAG, R.animator.exit_to_left);
                } else if (menuItemId == R.id.bottomBarAccountItem) {
                    HomeFragment.snackbarMessage = null;
                    toolbar.setVisibility(View.GONE);
                    if (fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG) != null) {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right)
                                .show(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG))
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right)
                                .add(R.id.content_frame, AccountFragment.newInstance(), Constants.ACCOUNT_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.ACCOUNT_FRAGMENT_TAG, R.animator.exit_to_right);
                } else {
                    HomeFragment.snackbarMessage = null;
                    toolbar.setVisibility(View.VISIBLE);
                    toolbarLine2.setVisibility(View.GONE);
                    toolbarHistory.setVisibility(View.VISIBLE);
                    reselectHistory(menuItemId);
                    int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                    hideOtherFragments(Constants.HISTORY_FRAGMENT_TAG, secondAnim);
                }
                currentMenuItem = menuItemId;
            }

            public void reselectHistory(int menuItemId) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
                if (historyFragment != null) {
                    fragmentTransaction.remove(historyFragment);
                }
                HistoryFragment.snackbarMessage = snackbarMessage;
                snackbarMessage = null;
                int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                fragmentTransaction
                        //.setCustomAnimations(firstAnim, secondAnim)
                        .add(R.id.content_frame, HistoryFragment.newInstance(), Constants.HISTORY_FRAGMENT_TAG)
                        .commit();
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHistoryItem) {
                    reselectHistory(menuItemId);
                } else if (menuItemId == R.id.bottomBarAccountItem) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    AccountFragment accountFragment = (AccountFragment) fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG);
                    if (accountFragment != null) {
                        fragmentTransaction.remove(accountFragment);
                    }
                    AccountFragment.snackbarMessage = snackbarMessage;
                    snackbarMessage = null;
                    fragmentTransaction
                            .add(R.id.content_frame, AccountFragment.newInstance(), Constants.ACCOUNT_FRAGMENT_TAG)
                            .commit();
                } else if (menuItemId == R.id.bottomBarHomeItem) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    currentText = "list";
                    HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                    if (homeFragment != null) {
                        fragmentTransaction.remove(homeFragment);
                    }
                    fragmentTransaction
                            .add(R.id.content_frame, HomeFragment.newInstance(), Constants.HOME_FRAGMENT_TAG)
                            .commit();
                }
            }
        });
    }

    public void hideOtherFragments(String current, int leaveAnimation) {
        if (!current.equals(Constants.HOME_FRAGMENT_TAG)) {
            if (fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(0, leaveAnimation)
                        .hide(fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG))
                        .commit();
            }
        }
        if (!current.equals(Constants.HISTORY_FRAGMENT_TAG)) {
            if (fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(0, leaveAnimation)
                        .hide(fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG))
                        .commit();
            }
        }
        if (!current.equals(Constants.ACCOUNT_FRAGMENT_TAG)) {
            if (fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(0, leaveAnimation)
                        .hide(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG))
                        .commit();
            }
        }
        if (!current.equals(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG)) {
            if (fragmentManager.findFragmentByTag(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(0, leaveAnimation)
                        .hide(fragmentManager.findFragmentByTag(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG))
                        .commit();
            }
        }
    }

    private int selectHistoryFragment(int menuItemId) {
        int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
        int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
        HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
        if (historyFragment != null) {
            historyFragment.getHistory(historyFragment);
            historyFragment.snackbarMessage = snackbarMessage;
            fragmentManager.beginTransaction()
                    .setCustomAnimations(firstAnim, secondAnim)
                    .show(historyFragment)
                    .commit();
        } else {
            historyFragment = HistoryFragment.newInstance();
            historyFragment.snackbarMessage = snackbarMessage;
            fragmentManager.beginTransaction()
                    .setCustomAnimations(firstAnim, secondAnim)
                    .add(R.id.content_frame, historyFragment, Constants.HISTORY_FRAGMENT_TAG)
                    .commit();
        }
        return secondAnim;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }

    public void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest) {

        Log.i("MainActivity", "onFragmentInteraction> arg = " + nextFragment);
        if (fragmentPostProcessingRequest == Constants.FPPR_SUBMIT_FILTERS) {
            HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
            homeFragment.getRequests(null);
        } else if (fragmentPostProcessingRequest == Constants.FPPR_HISTORY_FILTERS) {
            HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
            historyFragment.getHistory(historyFragment);
        }
    }

    private void requestPermission(final String permission, int rationale, final int requestCode) {
        Log.e(TAG, permission + " permission has NOT been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            // Provide an additional rationale to the user if the permission was not granted
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            Snackbar snackbar = Snackbar.make(viewGroup.getRootView(), rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{permission},
                                    requestCode);
                            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                goToHome(null);
                            }
                        }
                    });
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                    snackbar.getView().getRootView().getLayoutParams();

            params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 260);
            snackbar.getView().getRootView().setLayoutParams(params);
            snackbar.show();
        } else {
            // Permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    requestCode);
        }
    }

    public void goToHistory(String message) {
        HistoryFragment fragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.snackbarMessage = message;
        }
        snackbarMessage = message;
        mBottomBar.selectTabAtPosition(1, true);
    }

    public void goToAccount(String message) {
        SharedAsyncMethods.getUserInfoFromServer(user, this);
        AccountFragment fragment = (AccountFragment) fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.parentScroll.scrollTo(0, 0);
            AccountFragment.snackbarMessage = message;
        }
        snackbarMessage = message;
        mBottomBar.selectTabAtPosition(2, true);
    }

    public void goToHome(String message) {
        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
        if (homeFragment != null) {
            //homeFragment.getRequests(null);
        }
        HomeFragment.snackbarMessage = message;
        mBottomBar.selectTabAtPosition(0, true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 404) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }

    public void scheduleNotificationsAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), ConnectivityReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, ConnectivityReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    public static boolean isNetworkConnected() {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean areLocationServicesOn() {
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, "couldn't check if gps is enabled, go error: " + ex.getMessage());
        }

        try {
            networkEnabled = locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, "couldn't check if network location is enabled, go error: " + ex.getMessage());
        }
        return gpsEnabled || networkEnabled;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            try {
                Log.i("token-> ", acct.getIdToken());
                //user = new User();
                user.setGoogleId(acct.getId());
                user.setAccessToken(acct.getIdToken());
                user.setAuthMethod(Constants.GOOGLE_AUTH_METHOD);
                PrefUtils.setCurrentUser(user, MainActivity.this);
                HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                if (homeFragment != null) {
                    homeFragment.user = user;
                    homeFragment.getRequests(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Signed out, show unauthenticated UI.
            Log.e(TAG, "error signing in: " + result.toString() + "**" + result.getStatus().getStatusCode());
            //TODO: show error message
        }
    }

    public void handleGoogleSignout(final Context context) {
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "User Logged out");
                                Intent intent = new Intent(context, LoginActivity.class);
                                PrefUtils.clearCurrentUser(context);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Google API Client Connection Suspended");
            }

        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void updateStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    private void registerBroadcastReceiver() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NOTIFICATION_MESSAGE");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentMenuItem == R.id.bottomBarHistoryItem) {
                selectHistoryFragment(currentMenuItem);
            }
            String message = intent.getStringExtra("message");
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            Snackbar snackbar = Snackbar.make(viewGroup.getRootView(), message, Constants.LONG_SNACK);
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                    snackbar.getView().getRootView().getLayoutParams();

            params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 150);
            String type = intent.getStringExtra("type");
            View.OnClickListener mOnClickListener;
            Response response = null;
            Request request = null;
            boolean hasRequestResponseParams = type != null &&
                    (type.equals(NearbyMessagingService.NotificationType.response_update.toString())
                            || type.equals(NearbyMessagingService.NotificationType.offer_accepted.toString())
                            || type.equals(NearbyMessagingService.NotificationType.offer_closed.toString()));
            if (hasRequestResponseParams) {
                String responseJson = intent.getStringExtra("response");
                String requestJson = intent.getStringExtra("request");
                try {
                    response = new ObjectMapper().readValue(responseJson, Response.class);
                    request = new ObjectMapper().readValue(requestJson, Request.class);
                } catch (IOException e) {
                    Log.e(TAG, "JSON error when getting request/response objects from notification: " + e.getMessage());
                }
            }

            if (type != null) {
                switch (type) {
                    case "response_update":
                        if (response.getId() != null) {
                            DialogFragment newFragment = null;
                            newFragment = ViewOfferDialogFragment.newInstance(response, request);
                            final DialogFragment frag = newFragment;
                            mOnClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    frag.show(getFragmentManager(), "dialog");
                                }
                            };
                            snackbar.setAction("view", mOnClickListener);
                        }
                        break;
                    case "exchange_confirmed":
                        HistoryFragment.dismissExchangeCodeDialogFragment();
                    default:
                        break;
                }
            }
            snackbar.getView().getRootView().setLayoutParams(params);
            snackbar.show();
        }
    };

    public static void refreshTokenAndGetRequests(GoogleSignInResult result, LatLng latLng) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            try {
                Log.i(TAG, "token-> " + acct.getIdToken());
                //user = new User();
                user.setGoogleId(acct.getId());
                user.setAccessToken(acct.getIdToken());
                user.setAuthMethod(Constants.GOOGLE_AUTH_METHOD);
                getRequestNotifications(latLng);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Signed out, show unauthenticated UI.
            Log.e(TAG, "error signing in: " + result.toString() + "**" + result.getStatus().getStatusCode());
            //TODO: show error message
        }
    }

    public static void getRequestNotifications(final LatLng latLng) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests/notifications" +
                            "?latitude=" + latLng.latitude + "&longitude=" + latLng.longitude);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    int responseCode = conn.getResponseCode();
                    Log.i("GET /notifications", "Response Code : " + responseCode);
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get notifications: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

            }
        }.execute();
    }

    public static void getRequests(final LatLng latLng) {
        if (user != null && user.getAuthMethod() != null && user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD)) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                refreshTokenAndGetRequests(result, latLng);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        refreshTokenAndGetRequests(googleSignInResult, latLng);
                    }
                });
            }
        } else {
            getRequestNotifications(latLng);
        }
    }

    public static String uploadPhoto(File f) {
        String key = UUID.randomUUID().toString();
        TransferObserver observer = transferUtility.upload(
                Constants.NEARBY_BUCKET,     /* The bucket to upload to */
                key,    /* The key for the uploaded object */
                f,        /* The file where the data to upload exists */
                CannedAccessControlList.PublicRead
        );
        return observer.getKey();
    }

    public void fetchPhoto(String key, final File file, final Context context, final RequestDialogFragment frag1, final ImageView photo, final ImageView delete, final ProgressBar spinner, final int order) {
        Log.i(TAG, "fetching photo [" + key + "]");
        try {
            TransferObserver observer = transferUtility.download(
                    Constants.NEARBY_BUCKET,     /* The bucket to download from */
                    key,    /* The key for the object to download */
                    file       /* The file to download the object to */
            );
            observer.setTransferListener(new TransferListener(){

                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.i(TAG, "download state changed" + state.toString() + "**" + state.name());
                    if (state.equals(TransferState.COMPLETED)) {
                        if (!file.exists()) {
                            Log.e(TAG, "**file does not exist");
                        }
                        Uri uri = Uri.fromFile(file);
                        spinner.setVisibility(View.GONE);
                        try {
                            InputStream inputStream = context.getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            photo.setImageBitmap(bitmap);
                            if (frag1 != null) {
                                frag1.bitmaps[order] = bitmap;
                                delete.setVisibility(View.VISIBLE);
                                frag1.setImageClick(photo, uri);
                            }
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    //Display percentage transfered to user
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.e(TAG, ex.getMessage());
                    // do something
                }

            });
        } catch (Exception exception) {
            return;
        }
    }

    public void fetchPreviewPhoto(String key, final File file, final Context context, final ImageView photo, final ProgressBar spinner, final RequestPreviewFragment frag1, final ViewOfferDialogFragment frag2) {
        Log.i(TAG, "fetching photo [" + key + "]");
        try {
            TransferObserver observer = transferUtility.download(
                    Constants.NEARBY_BUCKET,     /* The bucket to download from */
                    key,    /* The key for the object to download */
                    file       /* The file to download the object to */
            );
            observer.setTransferListener(new TransferListener(){

                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.i(TAG, "download state changed" + state.toString() + "**" + state.name());
                    if (state.equals(TransferState.COMPLETED)) {
                        if (!file.exists()) {
                            Log.e(TAG, "**file does not exist");
                        }
                        Uri uri = Uri.fromFile(file);
                        try {
                            InputStream inputStream = context.getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            photo.setImageBitmap(bitmap);
                            spinner.setVisibility(View.GONE);
                            if (frag1 != null) {
                                frag1.setImageClick(photo, uri);
                            } else {
                                frag2.setImageClick(photo, uri);
                                frag2.bitmaps.add(bitmap);
                            }
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    //Display percentage transfered to user
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.e(TAG, ex.getMessage());
                    // do something
                }

            });
        } catch (Exception exception) {
            return;
        }
    }

    public File compressFile(Bitmap bitmap) {
        File dir = this.getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String name = UUID.randomUUID().toString();
        File compressedFile = null;
        try {
            compressedFile = File.createTempFile(name, null, dir);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int MAX_IMAGE_SIZE = 500 * 512;
        int streamLength = MAX_IMAGE_SIZE;
        int compressQuality = 115;
        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
        while (streamLength >= MAX_IMAGE_SIZE && compressQuality > 5) {
            try {
                bmpStream.flush();//to avoid out of memory error
                bmpStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
            compressQuality -= 15;
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            if(BuildConfig.DEBUG) {
                Log.d("test upload", "Quality: " + compressQuality);
                Log.d("test upload", "Size: " + streamLength);
            }
        }

        try {
            FileOutputStream fo = new FileOutputStream(compressedFile);
            fo.write(bmpStream.toByteArray());
            fo.flush();
            fo.close();
            return compressedFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void deletePhoto(final String key) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DeleteObjectsRequest dor = new DeleteObjectsRequest(Constants.NEARBY_BUCKET);
                dor.setKeys(Collections.singletonList(new DeleteObjectsRequest.KeyVersion(key)));
                s3.deleteObjects(dor);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

            }
        }.execute();
    }


}
