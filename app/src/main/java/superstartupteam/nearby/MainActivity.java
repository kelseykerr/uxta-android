package superstartupteam.nearby;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;


import layout.ExchangeCodeDialogFragment;
import layout.ExchangeOverrideDialogFragment;
import layout.NewOfferDialogFragment;
import layout.UpdateAccountFragment;
import superstartupteam.nearby.model.User;
import layout.AccountFragment;
import layout.HistoryFragment;
import layout.HomeFragment;
import layout.RequestDialogFragment;
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
        UpdateAccountFragment.OnFragmentInteractionListener,
        ExchangeCodeDialogFragment.OnFragmentInteractionListener,
        ExchangeOverrideDialogFragment.OnFragmentInteractionListener {

    private User user;
    private Toolbar toolbar;
    private BottomBar mBottomBar;
    private Integer currentMenuItem;
    private ImageButton newRequestButton;
    private TextView listMapText;
    String currentText = "list";
    FragmentManager fragmentManager = getFragmentManager();

    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

    public static final String TAG = "MainActivity";

    /**
     * Id to identify the fine location permission request.
     */
    private static final int REQUEST_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        newRequestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(v);
            }
        });
        Rect clickableArea = new Rect();
        newRequestButton.getHitRect(clickableArea);
        // Extend the touch area of the ImageButton beyond its bounds
        //TODO: this doesn't work
        clickableArea.left += 400;
        clickableArea.bottom += 400;
        clickableArea.top += 400;
        clickableArea.bottom += 400;
        TouchDelegate touchDelegate = new TouchDelegate(clickableArea,
                newRequestButton);
        // Sets the TouchDelegate on the parent view, such that touches
        // within the touch delegate bounds are routed to the child.
        if (View.class.isInstance(newRequestButton.getParent())) {
            ((View) newRequestButton.getParent()).setTouchDelegate(touchDelegate);
        }

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottom_bar);
        setmBottomBarListener();

        user = PrefUtils.getCurrentUser(MainActivity.this);
        if (user == null || user.getAccessToken() == null) {
            if(user != null && user.getAccessToken() != null){
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }
        Log.i("user access token: ", user.getAccessToken() + " ****************");
        Log.i("user name: ", user.getName() + " ****************");
        Log.i("****FCM TOKEN*", FirebaseInstanceId.getInstance().getToken() + "***");
        NearbyInstanceIdService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken(), this);
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
                    //TODO: destroy old ones or just refresh call to getHistory
                    listMapText.setVisibility(View.INVISIBLE);
                    int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
                    int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                    HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
                    if (historyFragment != null) {
                        historyFragment.getHistory(historyFragment);
                        Log.i("**", "**attempting to refresh history now!");
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(firstAnim, secondAnim)
                                .show(historyFragment)
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(firstAnim, secondAnim)
                                .add(R.id.content_frame, HistoryFragment.newInstance(), Constants.HISTORY_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.HISTORY_FRAGMENT_TAG, secondAnim);
                }
                currentMenuItem = menuItemId;
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

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHistoryItem) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG);
                    if (historyFragment != null) {
                        fragmentTransaction.remove(historyFragment);
                    }
                    HistoryFragment.snackbarMessage = null;
                    int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
                    int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                    fragmentTransaction
                            .setCustomAnimations(firstAnim, secondAnim)
                            .add(R.id.content_frame, HistoryFragment.newInstance(), Constants.HISTORY_FRAGMENT_TAG)
                            .commit();
                }
            }
        });
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

    public void onFragmentInteraction(Uri url, String nextFragment) {

        Log.i ("MainActivity", "onFragmentInteraction> arg = " + nextFragment);

        if (nextFragment == Constants.UPDATE_ACCOUNT_FRAGMENT_TAG){

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
                        .add(R.id.content_frame, UpdateAccountFragment.newInstance(), Constants.UPDATE_ACCOUNT_FRAGMENT_TAG)
                        .commit();
            }

        }
        if (nextFragment == Constants.ACCOUNT_FRAGMENT_TAG){

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
                        .add(R.id.content_frame, UpdateAccountFragment.newInstance(), Constants.ACCOUNT_FRAGMENT_TAG)
                        .commit();
            }

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
            fragment.parentScroll.scrollTo(0, 0);
            HistoryFragment.snackbarMessage = message;
        }
        mBottomBar.selectTabAtPosition(1, true);

    }

}
