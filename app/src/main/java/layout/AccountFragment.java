package layout;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.login.LoginManager;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import superstartupteam.nearby.LoginActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private Context context;
    private Bitmap bitmap;
    private TextView btnLogout;
    private TextView editProfile;
    private TextView noCustomerText;
    private TextView noMerchantText;
    private User user;
    private ImageView profileImage;
    public ScrollView parentScroll;
    public static String snackbarMessage = null;
    public static UpdateAccountDialogFragment updateAccountDialog;

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
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
    }

    public static void dismissUpdateAccountDialog() {
        if (updateAccountDialog != null) {
            updateAccountDialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        user = PrefUtils.getCurrentUser(context);
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        profileImage = (ImageView) view.findViewById(R.id.profileImage);

        updateAccountRequest = false;

        // fetching facebook's profile picture
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                URL imageURL = null;
                try {
                    imageURL = new URL("https://graph.facebook.com/" + user.getUserId() + "/picture?type=large");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
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


        btnLogout = (TextView) view.findViewById(R.id.logout_button);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.clearCurrentUser(context);
                // We can logout from facebook by calling following method
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        editProfile = (TextView) view.findViewById(R.id.updateAccount_button);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                updateAccountDialog.show(getFragmentManager(), "dialog");
            }
        });

        parentScroll = (ScrollView) view.findViewById(R.id.account_parent_scrollview);

        TextView userName = (TextView) view.findViewById(R.id.user_profile_name);
        userName.setText(user.getFullName());
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
        boolean homeNotifs = user.getHomeLocationNotifications() != null &&
                user.getHomeLocationNotifications();
        boolean nearNotifs = user.getCurrentLocationNotifications() != null &&
                user.getCurrentLocationNotifications();
        if (homeNotifs && nearNotifs) {
            String htmlString = "you will receive notifications about requests within " +
                    user.getNotificationRadius() + " miles of your home and your current location";
            notificationsText.setText(htmlString);
        } else if (!homeNotifs && !nearNotifs) {
            String htmlString = "notifications about new requests are disabled";
            notificationsText.setText(htmlString);
        } else {
            String htmlString = "you will receive notifications about requests within " +
                    user.getNotificationRadius() + " of your " + (homeNotifs ? "home" :  "current location");
            notificationsText.setText(htmlString);
        }
        if (snackbarMessage != null) {
            Snackbar snackbar = Snackbar
                    .make(view, snackbarMessage, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        noCustomerText = (TextView) view.findViewById(R.id.no_customer_text);
        noMerchantText = (TextView) view.findViewById(R.id.no_merchant_text);
        boolean displayCustomerStatus = user.getCustomerStatus() != null &&
                !user.getCustomerStatus().toLowerCase().equals("valid");
        if (user.getCustomerId() == null || displayCustomerStatus) {
            noCustomerText.setVisibility(View.VISIBLE);
            if (displayCustomerStatus) {
                noCustomerText.setText(user.getCustomerStatus());
            }
        } else {
            noCustomerText.setVisibility(View.GONE);
        }
        boolean displayMerchantStatus = user.getMerchantStatus() != null &&
                !user.getMerchantStatus().toLowerCase().equals("pending") &&
                !user.getMerchantStatus().toLowerCase().equals("active");
        if (user.getMerchantId() == null || displayMerchantStatus) {
            noMerchantText.setVisibility(View.VISIBLE);
            if (user.getMerchantStatusMessage() != null) {
                noMerchantText.setText(user.getMerchantStatusMessage());
            }
        } else {
            noMerchantText.setVisibility(View.GONE);
        }
        return view;

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

}
