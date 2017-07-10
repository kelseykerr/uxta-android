package layout;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import iuxta.uxta.AppUtils;
import iuxta.uxta.Constants;
import iuxta.uxta.LoginActivity;
import iuxta.uxta.MainActivity;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.SharedAsyncMethods;
import iuxta.uxta.model.Community;
import iuxta.uxta.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    private Context context;
    private Bitmap bitmap;
    private TextView noCommunityText;
    private User user;
    private ImageView profileImage;
    public ScrollView parentScroll;
    private View view;
    public static String snackbarMessage = null;
    public static UpdateAccountDialogFragment updateAccountDialog;
    public static CommunitySearchFragment communitySearchFragment;
    public static PaymentDialogFragment paymentDialogFragment;
    private RelativeLayout editAccntLayout;
    private RelativeLayout logoutLayout;
    private RelativeLayout shareLayout;
    private RelativeLayout privacyLayout;
    private TextView versionText;
    private LinearLayout communityBtn;
    private TextView communityBtnText;
    private static final String TAG = "AccountFragment";
    private Community community;

    private boolean updateAccountRequest;

    private OnFragmentInteractionListener mListener;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountFragment.
     */
    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = PrefUtils.getCurrentUser(context);
        SharedAsyncMethods.getUserInfoFromServer(user, context);
    }

    public static void dismissUpdateAccountDialog() {
        if (updateAccountDialog != null) {
            updateAccountDialog.dismiss();
        }
    }

    public static void dismissPaymentDialog() {
        if (paymentDialogFragment != null) {
            paymentDialogFragment.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        user = PrefUtils.getCurrentUser(context);

        final View view = inflater.inflate(R.layout.fragment_account, container, false);
        profileImage = (ImageView) view.findViewById(R.id.profileImage);
        setProfilePic();
        updateAccountRequest = false;

        logoutLayout = (RelativeLayout) view.findViewById(R.id.logout_layout);
        handleLogout();

        shareLayout = (RelativeLayout) view.findViewById(R.id.share_layout);
        handleShare();

        editAccntLayout = (RelativeLayout) view.findViewById(R.id.edit_accnt_layout);
        handleEditAccount();

        privacyLayout = (RelativeLayout) view.findViewById(R.id.privacy_layout);
        handlePrivacy();

        parentScroll = (ScrollView) view.findViewById(R.id.account_parent_scrollview);

        TextView userName = (TextView) view.findViewById(R.id.user_profile_name);
        userName.setText(user.getFirstName() + " " + user.getLastName());
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        TextView addressStreet = (TextView) view.findViewById(R.id.user_address_line_1);
        addressStreet.setText(user.getAddress());

        TextView addressLine2 = (TextView) view.findViewById(R.id.user_address_line_2);
        if (user.getAddressLine2() != null && user.getAddressLine2().length() > 0) {
            addressLine2.setText(user.getAddressLine2());
        } else {
            addressLine2.setVisibility(View.GONE);
        }
        TextView cityStateZip = (TextView) view.findViewById(R.id.user_city_state_zip);
        if (user.getCity() != null || user.getState() != null || user.getZip() != null) {
            String csz = "";
            if (user.getCity() != null) {
                csz += user.getCity();
                if (user.getState() != null) {
                    csz += ", ";
                }
            }
            if (user.getState() != null) {
                csz += user.getState() + " ";
            }
            if (user.getZip() != null) {
                csz += user.getZip();
            }
            cityStateZip.setText(csz);
        } else {
            cityStateZip.setVisibility(View.GONE);
        }

        TextView userEmail = (TextView) view.findViewById(R.id.user_email);
        if (user.getEmail() != null) {
            userEmail.setText(user.getEmail());
        } else {
            userEmail.setVisibility(View.GONE);
        }
        TextView userPhone = (TextView) view.findViewById(R.id.user_phone);
        if (user.getPhone() != null) {
            userPhone.setText(user.getPhone());
        } else {
            userPhone.setVisibility(View.GONE);
        }
        TextView notificationsText = (TextView) view.findViewById(R.id.notifications_text);
        if (user.getNewRequestNotificationsEnabled() != null && user.getNewRequestNotificationsEnabled()) {
            String htmlString = "new post notifications are on";
            notificationsText.setText(htmlString);
        }
        if (snackbarMessage != null) {
            Snackbar snackbar = Snackbar
                    .make(view, snackbarMessage, Constants.LONG_SNACK);
            snackbar.show();
        }
        communityBtn = (LinearLayout) view.findViewById(R.id.community_btn);
        communityBtnText = (TextView) view.findViewById(R.id.community_btn_text);
        if (user.getCommunityId() != null && !user.getCommunityId().isEmpty()) {
            getCommunityBtnText(user.getCommunityId(), false);
        } else if (user.getRequestedCommunityId() != null && !user.getRequestedCommunityId().isEmpty()) {
            getCommunityBtnText(user.getRequestedCommunityId(), true);
        }
        final AccountFragment accountFragment = this;
        communityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getCommunityId() != null && !user.getCommunityId().isEmpty() && community != null) {
                    CommunityDetailFragment communityDetailFragment = CommunityDetailFragment.newInstance(community, accountFragment);
                    communityDetailFragment.show(getFragmentManager(), "dialog");
                } else {
                    communitySearchFragment = CommunitySearchFragment.newInstance(accountFragment);
                    communitySearchFragment.show(getFragmentManager(), "dialog");
                }
            }
        });
        versionText = (TextView) view.findViewById(R.id.version_text);
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            versionText.setText("version " + versionName);
        } catch (Exception e) {
            Log.e(TAG, "error getting version name");
            versionText.setVisibility(View.GONE);
        }
        this.view = view;
        return view;

    }

    public void requestedAccess(String communityName) {
        communitySearchFragment.dismiss();
        Snackbar snackbar = Snackbar
                .make(view.getRootView(), "Your request to join " + communityName + " will be reviewed shortly.", Constants.LONG_SNACK);
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                snackbar.getView().getRootView().getLayoutParams();

        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + 150);
        snackbar.getView().getRootView().setLayoutParams(params);
        snackbar.show();
        communityBtnText.setText("Pending approval for " + communityName);
        //snackbar
    }

    public void requestedRemoval(String communityName) {
        if (communitySearchFragment != null) {
            communitySearchFragment.dismiss();
        }
        Snackbar snackbar = Snackbar
                .make(view.getRootView(), "You have been removed from " + communityName + " community.", Constants.LONG_SNACK);
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                snackbar.getView().getRootView().getLayoutParams();

        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + 150);
        snackbar.getView().getRootView().setLayoutParams(params);
        snackbar.show();
        communityBtnText.setText("find your community");
        //snackbar
    }

    public void handlePrivacy() {
        privacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                PackageManager packageManager = ((MainActivity)getActivity()).getPackageManager();
                List activities = packageManager.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    String url = "http://thenearbyapp.com/privacy";
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view.getRootView(), "no browser found, view our privacy policy at http://thenearbyapp.com/privacy", Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        });
    }

    public void handleEditAccount() {
        editAccntLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isNetworkConnected()) {
                    showNoConnectionSnackbar();
                } else {
                    updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                    updateAccountDialog.show(getFragmentManager(), "dialog");
                }
            }
        });
    }

    public void handleLogout() {
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    public void handleShare () {
        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

    private void showNoConnectionSnackbar() {
        Snackbar snackbar = Snackbar.make(view.getRootView(), R.string.noNetworkConnection,
                Constants.LONG_SNACK)
                .setAction("open settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                snackbar.getView().getRootView().getLayoutParams();

        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + 150);
        snackbar.getView().getRootView().setLayoutParams(params);
        snackbar.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.i(TAG, "onConnectionFailed:" + connectionResult);
    }

    public void logout() {
        Log.i(TAG, "************************logging out");
        if (user.getAuthMethod() == null || user.getAuthMethod().equals(Constants.FB_AUTH_METHOD)) {
            // We can logout from facebook by calling following method
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(context, LoginActivity.class);
            PrefUtils.clearCurrentUser(context);
            startActivity(intent);
            //finish();
        } else {
            ((MainActivity)getActivity()).handleGoogleSignout(context);
        }
    }

    public void share() {
        Log.i(TAG, "************************ Sharing :-)");
/* Create the Intent */
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

/* Fill it with Data */
 //       emailIntent.setType("message/rfc822");
        emailIntent.setType("text/html");
/*        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});  */
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Try Nearby - A Great New App");
//        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
                .append("<html>")
                .append("Nearby is a mobile app that just launched in Washington, DC.")
                .append("  Are you looking to borrow or buy something?")
                .append("  Do you want to make money from stuff you have sitting around? Learn more at ")
                .append("<a href=\"http://thenearbyapp.com!\">http://thenearbyapp.com</a>")
                .append(" or download it using one of the following links:")
                .append("<br/>")
                .append("<br/>")
                .append("Google Play Store: ")
                .append("<a href=\"Android Link\">https://play.google.com/store/apps/details?id=iuxta.nearby</a>")
                .append("<br/>")
                .append("<br/>")
                .append("App Store: ")
                .append("<a href=\"Apple Link\">https://itunes.apple.com/us/app/nearby-share-sell-borrow/id1223745552</a>")
                .toString())
        );

/* Send it off to the Activity-Chooser */
        context.startActivity(Intent.createChooser(emailIntent, "Send mail using..."));

    }

    public void setProfilePic() {
        final boolean googlePic = user.getAuthMethod() != null &&
                user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD) &&
                user.getPictureUrl() != null;
        final boolean facebookPic = user.getAuthMethod() != null &&
                user.getAuthMethod().equals(Constants.FB_AUTH_METHOD);
        if (googlePic || facebookPic) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    URL imageURL = null;
                    try {
                        imageURL = new URL(googlePic ? user.getPictureUrl() + "?sz=800" : "https://graph.facebook.com/" + user.getUserId() + "/picture?height=800");
                        bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    profileImage.setImageBitmap(bitmap);
                }
            }.execute();
        }
    }

    private void getCommunityBtnText(final String communityId, final Boolean isPending) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    String path = "/communities/" + communityId;
                    HttpURLConnection conn = AppUtils.getHttpConnection(path, "GET", user);
                    Integer responseCode = conn.getResponseCode();
                    Log.i(TAG, "GET /communities/{id} response code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    String output = AppUtils.getResponseContent(conn);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        Community c = mapper.readValue(output, new TypeReference<Community>() {});
                        community = c;
                    } catch (IOException e) {
                        Log.e(TAG, "error converting string to community: " + e.getMessage());
                        throw new IOException(e);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "ERROR could not get community: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (community != null && community.getName() != null) {
                    if (!isPending) {
                        communityBtnText.setText(community.getName());
                    } else {
                        communityBtnText.setText("Pending approval for " + community.getName());
                    }
                }
            }
        }.execute();
    }


}
