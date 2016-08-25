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
import android.widget.TextView;

import com.facebook.login.LoginManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import impulusecontrol.lend.AppUtils;
import impulusecontrol.lend.LoginActivity;
import impulusecontrol.lend.PrefUtils;
import impulusecontrol.lend.R;
import impulusecontrol.lend.model.Request;
import impulusecontrol.lend.RequestAdapter;
import impulusecontrol.lend.model.User;

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
    private User user;
    private ImageView profileImage;
    private List<Request> recentRequests = new ArrayList<>();
    private RecyclerView recentRequestList;
    private RequestAdapter requestAdapter;

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
        requestAdapter = new RequestAdapter(recentRequests);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        profileImage = (ImageView) view.findViewById(R.id.profileImage);

        // fetching facebook's profile picture
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                URL imageURL = null;
                try {
                    imageURL = new URL("https://graph.facebook.com/" + user.getUserId() + "/picture?type=large");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    bitmap  = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
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

        TextView myAwesomeTextView = (TextView)view.findViewById(R.id.user_profile_name);
        myAwesomeTextView.setText(user.getName());

        recentRequestList = (RecyclerView) view.findViewById(R.id.recent_request_list);
        recentRequestList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recentRequestList.setLayoutManager(llm);
        recentRequestList.setAdapter(requestAdapter);
        getRequests();

        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        void onFragmentInteraction(Uri uri);
    }

    private void getRequests() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("http://ec2-54-242-74-234.compute-1.amazonaws.com/api/users/me/requests");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("x-auth-token", user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    try {
                        recentRequests = AppUtils.jsonStringToRequestList(output);
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to fetch " +
                                "requests from server, please try again later!");
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get requests: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (requestAdapter != null) {
                    requestAdapter.swap(recentRequests);
                }
            }
        }.execute();
    }
}
