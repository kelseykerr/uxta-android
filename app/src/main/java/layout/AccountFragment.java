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
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.login.LoginManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import superstartupteam.nearby.Constants;
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
public class AccountFragment extends Fragment implements View.OnFocusChangeListener {
    private Context context;
    private Bitmap bitmap;
    private TextView btnLogout;
    private TextView editProfile;
    private User user;
    private ImageView profileImage;
    public ScrollView parentScroll;

    private TextView addressStreet;
    private TextView addressCityZip;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        profileImage = (ImageView) view.findViewById(R.id.profileImage);

        updateAccountRequest = false;

        view.setOnFocusChangeListener(this);

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
                UpdateAccountDialogFragment newFragment = UpdateAccountDialogFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
            }
        });

        parentScroll = (ScrollView) view.findViewById(R.id.account_parent_scrollview);

        TextView myAwesomeTextView = (TextView) view.findViewById(R.id.user_profile_name);
        myAwesomeTextView.setText(user.getFullName());
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        addressStreet = (TextView) view.findViewById(R.id.user_home_address_street);
        addressStreet.setText(user.getAddress());

        addressCityZip = (TextView) view.findViewById(R.id.user_city_zip);
        addressCityZip.setText(user.getAddressLine2());

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
        TextView notificationsNearHome = (TextView) view.findViewById(R.id.notifications_near_home);
        if (user.getHomeLocationNotifications() != null && user.getHomeLocationNotifications()) {
            String htmlString = "Receive notifications about requests near your home address: <b>enabled</b>";
            notificationsNearHome.setText(Html.fromHtml(htmlString));
        } else {
            String htmlString = "Receive notifications about requests near your home address: <b>disabled</b>";
            notificationsNearHome.setText(Html.fromHtml(htmlString));
        }
        TextView notificationsNearby = (TextView) view.findViewById(R.id.notifications_near_me);
        if (user.getCurrentLocationNotifications() != null && user.getCurrentLocationNotifications()) {
            String htmlString = "Receive notifications about requests near my current location: <b>enabled</b>";
            notificationsNearby.setText(Html.fromHtml(htmlString));
        } else {
            String htmlString = "Receive notifications about requests near my current location: <b>disabled</b>";
            notificationsNearby.setText(Html.fromHtml(htmlString));
        }
        TextView notificationsRadius = (TextView) view.findViewById(R.id.notifications_radius);
        if (user.getNewRequestNotificationsEnabled() != null && user.getNewRequestNotificationsEnabled()) {
            String htmlString = "Notifications radius: <b>" + user.getNotificationRadius() + " miles</b>";
            notificationsRadius.setText(Html.fromHtml(htmlString));
        } else {
            notificationsRadius.setVisibility(View.GONE);
        }
        TextView notificationsKeywords = (TextView) view.findViewById(R.id.notifications_keywords);
        if (user.getNotificationKeywords() != null) {
            String keywords = "";
            for (String k:user.getNotificationKeywords()) {
                keywords += (k + " ");
            }
            notificationsKeywords.setText("notification keywords: " + keywords);
        } else {
            notificationsKeywords.setVisibility(View.GONE);
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

    public void onFocusChange(View v, boolean hasFocus) {

        Boolean isTrue = hasFocus;
        if(isTrue){
            user = PrefUtils.getCurrentUser(context);
            addressStreet = (TextView) v.findViewById(R.id.user_home_address_street);
            addressStreet.setText(user.getAddress());

            Log.i (Constants.ACCOUNT_FRAGMENT_TAG, "address=" + user.getAddress());

            addressCityZip = (TextView) v.findViewById(R.id.user_city_zip);
            addressCityZip.setText(user.getAddressLine2());
        }
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
        public void onFragmentInteraction(Uri url, String nextFragment);
    }

}
