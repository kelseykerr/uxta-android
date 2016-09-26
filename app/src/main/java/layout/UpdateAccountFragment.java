package layout;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.HistoryCardAdapter;
import superstartupteam.nearby.LoginActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateAccountFragment extends Fragment {
    private Context context;
    private Bitmap bitmap;
    private User user;
    private TextView applyButton;
    private TextView cancelButton;
    private RecyclerView requestHistoryList;
    public ScrollView parentScroll;
    private View view;

    private TextView mAddressStreet;
    private TextView mAddressCityZip;

    private OnFragmentInteractionListener mListener;

    public UpdateAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountFragment.
     */
    public static UpdateAccountFragment newInstance() {
        UpdateAccountFragment fragment = new UpdateAccountFragment();
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
        final View view = inflater.inflate(R.layout.fragment_update_account, container, false);
        parentScroll = (ScrollView) view.findViewById(R.id.account_parent_scrollview);

        TextView myAwesomeTextView = (TextView) view.findViewById(R.id.newAddressLabel);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        applyButton = (TextView) view.findViewById(R.id.applyAccountUpdateButton);
        cancelButton = (TextView) view.findViewById(R.id.cancelAccountUpdateButton);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deliver done-with-update request to MainActivity so that it can return Account fragment
                Uri url = Uri.parse("http://www.google.com");
                String nextFragment = Constants.ACCOUNT_FRAGMENT_TAG;

                TextView newAddress = (TextView) view.findViewById(R.id.newAddressEditField);
                user.setAddress(newAddress.getText().toString());

                TextView newAddress2 = (TextView) view.findViewById(R.id.newAddressLine2EditField);
                user.setAddressLine2(newAddress2.getText().toString());

                putAccountInfo();

                PrefUtils.setCurrentUser(user, context);

                mListener.onFragmentInteraction(url, nextFragment);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deliver done-with-update request to MainActivity so that it can return Account fragment
                Uri url = Uri.parse("http://www.google.com");
                String nextFragment = Constants.ACCOUNT_FRAGMENT_TAG;

                mListener.onFragmentInteraction(url, nextFragment);
            }
        });

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

    private void putAccountInfo() {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                Log.i (Constants.UPDATE_ACCOUNT_FRAGMENT_TAG, "In doInBackground");
                try {

                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/" + user.getUserId());

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");

                    ObjectMapper mapper = new ObjectMapper();

                    String updateJson = mapper.writeValueAsString(user);
                    Log.i("updated account: ", updateJson);
                    byte[] outputInBytes = updateJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();

                    Log.i("PUT /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update account: " + e.getMessage());
                }
                return 0;
            }

        };
    }
}