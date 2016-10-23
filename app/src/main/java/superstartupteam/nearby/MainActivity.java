package superstartupteam.nearby;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import layout.AccountFragment;
import layout.ExchangeCodeDialogFragment;
import layout.ExchangeOverrideDialogFragment;
import layout.HistoryFragment;
import layout.HomeFragment;
import layout.NewOfferDialogFragment;
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
        BraintreeListener {

    private User user;
    private Toolbar toolbar;
    private BottomBar mBottomBar;
    private Integer currentMenuItem;
    private ImageButton newRequestButton;
    private TextView listMapText;
    private String snackbarMessage;
    String currentText = "list";
    FragmentManager fragmentManager = getFragmentManager();
    private String notificationType;
    private Response response;
    private Request request;
    private boolean readNotification = false;
    private BraintreeFragment mBraintreeFragment;

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
        user = PrefUtils.getCurrentUser(MainActivity.this);
        if (user == null || user.getAccessToken() == null) {
            if(user != null && user.getAccessToken() != null){
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }
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
        listMapText = (TextView) findViewById(R.id.list_map_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        newRequestButton = (ImageButton) findViewById(R.id.new_request_button);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.new_request_button_layout);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(v);
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

        getBraintreeClientToken(this);
        Log.i("user access token: ", user.getAccessToken() + " ****************");
        Log.i("user name: ", user.getName() + " ****************");
        Log.i("****FCM TOKEN*", FirebaseInstanceId.getInstance().getToken() + "***");
        NearbyInstanceIdService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken(), this);
        checkNotificationOnOpen();
    }


    public void showDialog(View view) {
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


    public void setmBottomBarListener() {
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHomeItem) {
                    listMapText.setVisibility(View.VISIBLE);
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
                    listMapText.setVisibility(View.INVISIBLE);
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
                    int secondAnim = selectHistoryFragment(menuItemId);
                    hideOtherFragments(Constants.HISTORY_FRAGMENT_TAG, secondAnim);
                }
                currentMenuItem = menuItemId;
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHistoryItem) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
                    if (historyFragment != null) {
                        fragmentTransaction.remove(historyFragment);
                    }
                    HistoryFragment.snackbarMessage = snackbarMessage;
                    snackbarMessage = null;
                    int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
                    int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                    fragmentTransaction
                            .setCustomAnimations(firstAnim, secondAnim)
                            .add(R.id.content_frame, HistoryFragment.newInstance(), Constants.HISTORY_FRAGMENT_TAG)
                            .commit();
                } else if (menuItemId == R.id.bottomBarAccountItem) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    AccountFragment accountFragment = (AccountFragment) fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG);
                    if (accountFragment != null) {
                        fragmentTransaction.remove(accountFragment);
                    }
                    AccountFragment.snackbarMessage = snackbarMessage;
                    snackbarMessage = null;
                    int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
                    int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                    fragmentTransaction
                            .setCustomAnimations(firstAnim, secondAnim)
                            .add(R.id.content_frame, AccountFragment.newInstance(), Constants.ACCOUNT_FRAGMENT_TAG)
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
        listMapText.setVisibility(View.INVISIBLE);
        int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
        int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
        HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
        if (historyFragment != null) {
            historyFragment.getHistory(historyFragment);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(firstAnim, secondAnim)
                    .show(historyFragment)
                    .commit();
        } else {
            historyFragment = HistoryFragment.newInstance();
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

        // We should now have payment information => get nonce from braintree so that we can create a customer
        if (fragmentPostProcessingRequest == Constants.FPPR_REGISTER_BRAINTREE_CUSTOMER) {
            CardBuilder cardBuilder = new CardBuilder()
                    .cardNumber("4111111111111111")
                    .expirationDate("09/2018");

            Card.tokenize(mBraintreeFragment, cardBuilder);   // returns NONCE to PaymentMethodNonceCreatedListener above
        }
/*
        if (nextFragment.equals(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG)){

            listMapText.setVisibility(View.INVISIBLE);

            // Hide Account Fragment
            if (fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(0, R.animator.exit_to_right)
                        .hide(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG))
                        .commit();
            }

            if (fragmentManager.findFragmentByTag(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right)
                        .show(fragmentManager.findFragmentByTag(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG))
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right)
                        .add(R.id.content_frame, UpdateAccountDialogFragment.newInstance(), Constants.UPDATE_ACCOUNT_FRAGMENT_TAG)
                        .commit();
            }

        }
        if (nextFragment.equals(Constants.ACCOUNT_FRAGMENT_TAG)){

            listMapText.setVisibility(View.INVISIBLE);

            // Hide Update Account Fragment
            if (fragmentManager.findFragmentByTag(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(0, R.animator.exit_to_left)
                        .hide(fragmentManager.findFragmentByTag(Constants.UPDATE_ACCOUNT_FRAGMENT_TAG))
                        .commit();
            }

            if (fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG) != null) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_right)
                        .show(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG))
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_right)
                        .add(R.id.content_frame, UpdateAccountDialogFragment.newInstance(), Constants.ACCOUNT_FRAGMENT_TAG)
                        .commit();
            }

        }
*/
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
            fragment.parentScroll.scrollTo(0, 0);
            HistoryFragment.snackbarMessage = message;
        }
        snackbarMessage = message;
        mBottomBar.selectTabAtPosition(1, true);
    }

    public void goToAccount(String message) {
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
                PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(
                        BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
                );
                String nonce = paymentMethodNonce.getNonce();
                user.setPaymentMethodNonce(nonce);
                SharedAsyncMethods.updateUser(user, this);
                String deviceData = data.getStringExtra(BraintreePaymentActivity.EXTRA_DEVICE_DATA);
                Log.i("***", deviceData + "***device data");
            }
        }
    }

    public void getBraintreeClientToken(final Context ctx) {
        final MainActivity act = this;
        final User user = PrefUtils.getCurrentUser(ctx);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/braintree/token");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String token = AppUtils.getResponseContent(conn);
                    Log.i("braintree token", "***" + token);
                    user.setBraintreeClientToken(token);
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get braintree token from server: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Create braintree fragment
                try {
                    String mAuthorization = user.getBraintreeClientToken();
                    mBraintreeFragment = BraintreeFragment.newInstance(act, mAuthorization);
                    mBraintreeFragment.addListener(new PaymentMethodNonceCreatedListener() {
                        @Override
                        public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                            user.setPaymentMethodNonce(paymentMethodNonce.getNonce());
                            SharedAsyncMethods.updateUser(user, act, act, mLayout);
                        }
                    });
                } catch (InvalidArgumentException e) {
                    Log.e("**", "error creating braintree fragment: " + e.getMessage());
                }
            }
        }.execute();
    }


}
