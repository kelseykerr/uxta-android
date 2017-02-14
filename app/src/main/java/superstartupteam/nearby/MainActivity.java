package superstartupteam.nearby;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.io.IOException;

import layout.AccountFragment;
import layout.ExchangeCodeDialogFragment;
import layout.ExchangeOverrideDialogFragment;
import layout.FiltersDialogFragment;
import layout.HistoryFragment;
import layout.HomeFragment;
import layout.NewOfferDialogFragment;
import layout.PaymentDestinationDialogFragment;
import layout.PaymentDetailsDialogFragment;
import layout.PaymentDialogFragment;
import layout.RequestDialogFragment;
import layout.UpdateAccountDialogFragment;
import layout.ViewOfferDialogFragment;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;
import superstartupteam.nearby.service.NearbyInstanceIdService;

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
        PaymentDialogFragment.OnFragmentInteractionListener,
        PaymentDestinationDialogFragment.OnFragmentInteractionListener,
        PaymentDetailsDialogFragment.OnFragmentInteractionListener,
        GoogleApiClient.OnConnectionFailedListener {

    public static User user;
    private Toolbar toolbar;
    private BottomBar mBottomBar;
    private Integer currentMenuItem;
    private TextView listMapText;
    //private RelativeLayout toolbarLine1;
    private RelativeLayout toolbarLine2;
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
    public static User updatedUser;
    public static ConnectivityManager connMgr;
    private FloatingActionButton fab;
    private ProgressDialog mProgressDialog;


    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

    public static final String TAG = "MainActivity";

    /**
     * Id to identify the fine location permission request.
     */
    private static final int REQUEST_FINE_LOCATION = 0;

    private boolean hasMessage() {
        notificationType = getIntent().getStringExtra("type");
        return notificationType != null;
    }

    private void checkNotificationOnOpen() {
        notificationType = getIntent().getStringExtra("type");
        if (notificationType != null) {
            if (notificationType.equals("response_update")) {
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
                    Log.e("JSON ERROR", "**" + e.getMessage());
                }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        user = PrefUtils.getCurrentUser(MainActivity.this);
        if (user == null || user.getAccessToken() == null) {
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
            // Fine location permission has not been granted.
            requestFineLocationPermission();
        } else {
            // Fine location is already available, show the camera preview.
            Log.i(TAG, "FINE LOCATION permission has already been granted.");
        }

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        toolbarLine2 = (RelativeLayout) findViewById(R.id.toolbar_line_2);
        searchBar = (EditText) findViewById(R.id.search_bar);
        setSearchBarDone();
        searchBtn = (ImageButton) findViewById(R.id.search_button);
        setSearchBtnClick();
        listMapText = (TextView) findViewById(R.id.list_map_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!AppUtils.canAddPayments(user)) {
                    HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                    homeFragment.displayUpdateAccountSnackbar();
                } else {
                    boolean goodCustomerStatus = user.getCustomerStatus() != null &&
                            user.getCustomerStatus().equals("valid") && user.isPaymentSetup;
                    if (user.getCustomerId() != null && goodCustomerStatus) {
                        showNewRequestDialog(v);
                    } else {
                        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                        homeFragment.displayNoNewRequestSnackbar();
                    }
                }
            }
        });

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottom_bar);
        // if this is being opened from a notification message, default to history view
        if (hasMessage() && !notificationType.equals("request_notification")) {
            mBottomBar.setDefaultTabPosition(1);
        }
        //TODO: if request_notification, the radius should be set to the user's settings
        setmBottomBarListener();
        Log.i("user access token: ", user.getAccessToken() + " ****************");
        Log.i("user name: ", user.getName() + " ****************");
        Log.i("auth method: ", user.getAuthMethod() + "********");
        Log.i("****FCM TOKEN: ", FirebaseInstanceId.getInstance().getToken() + "***");
        NearbyInstanceIdService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken(), this);
        checkNotificationOnOpen();
        scheduleNotificationsAlarm();
        if (user.getTosAccepted() == null || user.getTosAccepted().equals(Boolean.FALSE)) {
            AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
            AccountFragment.updateAccountDialog.show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
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
    } // Always call the superclass method first


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

    public void switchListMapView(View view) {
        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
        if (currentText.equals("list")) {
            listMapText.setText("map");
            currentText = "map";
            homeFragment.toggleView("list");
        } else {
            listMapText.setText("list");
            currentText = "list";
            homeFragment.toggleView("map");
        }
    }

    public void filterRequests(View view) {
        DialogFragment newFragment = FiltersDialogFragment
                .newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void setmBottomBarListener() {
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHomeItem) {
                    toolbar.setVisibility(View.VISIBLE);
                    HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG);
                    if (homeFragment != null) {
                        homeFragment.getRequests(null);
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                                .show(fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG))
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                                .add(R.id.content_frame, HomeFragment.newInstance(), Constants.HOME_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.HOME_FRAGMENT_TAG, R.animator.exit_to_left);
                } else if (menuItemId == R.id.bottomBarAccountItem) {
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
                    toolbar.setVisibility(View.GONE);
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
                    listMapText.setText("list");
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
        toolbarLine2.setVisibility(View.GONE);
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
        }
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestFineLocationPermission() {
        Log.e(TAG, "FINE LOCATION permission has NOT been granted. Requesting permission.");
        Log.e(TAG, ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) + " *should show rationale");
        // BEGIN_INCLUDE(fine_location_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying fine location permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_fine_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_FINE_LOCATION);
                        }
                    })
                    .show();
        } else {
            // Fine location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION);
        }
        // END_INCLUDE(fine_location_permission_request)
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
                //SharedAsyncMethods.getUserInfoFromServer(user, MainActivity.this);
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


}
