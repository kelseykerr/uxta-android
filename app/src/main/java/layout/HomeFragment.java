package layout;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import iuxta.uxta.AppUtils;
import iuxta.uxta.Constants;
import iuxta.uxta.MainActivity;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.RequestAdapter;
import iuxta.uxta.model.Request;
import iuxta.uxta.model.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    GoogleApiClient mGoogleApiClient;
    LatLng latLng;
    FragmentManager fm;
    FragmentTransaction ft;
    private Context context;
    public static User user;
    private List<Request> requests = new ArrayList<>();
    private RecyclerView recList;
    private RequestAdapter requestAdapter;
    private RelativeLayout noResultsLayout;
    private ScrollView listView;
    private View view;
    public static Double currentRadius = 10.0;
    public static String sortBy = "newest";
    public static Boolean sellingLoaning = true;
    public static Boolean buyingRenting = true;
    public static String searchTerm;
    private static final String TAG = "HomeFragment";
    public static String snackbarMessage = null;


    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        view = v;
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        user = PrefUtils.getCurrentUser(context);
        listView = (ScrollView) v.findViewById(R.id.list_view);
        recList = (RecyclerView) v.findViewById(R.id.request_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        noResultsLayout = (RelativeLayout) v.findViewById(R.id.no_results_layout);
        noResultsLayout.setVisibility(View.GONE);
        if (snackbarMessage != null) {
            Snackbar snackbar = Snackbar
                    .make(view, snackbarMessage, Constants.LONG_SNACK);
            snackbar.show();
        }
        snackbarMessage = null;
        return v;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated ***");
        getRequests(currentRadius);
    }

    public void showNewOfferDialog(String requestId, String type) {
        DialogFragment newFragment = NewOfferDialogFragment.newInstance(requestId, type);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void showRequestPreviewDialog(Request request) {
        DialogFragment newFragment = RequestPreviewFragment.newInstance(request, this);
        newFragment.show(getFragmentManager(), "dialog");
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


    public void getRequests(final Double radius) {
        if (!MainActivity.isNetworkConnected()) {
            showNoConnectionSnackbar();
            return;
        }
        if (!isAdded()) {
            return;
        }
        else if (user == null) {
            return;
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                if (latLng == null) {
                    return null;
                }
                try {
                    Double r = radius != null ? radius : currentRadius;
                    String urlString = "/requests?includeMine=false&expired=false";
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        urlString += ("&searchTerm=" + searchTerm);
                    }
                    if (sortBy != null && !sortBy.isEmpty() && !sortBy.equals("best match")) {
                        urlString += ("&sort=" + sortBy);
                    }
                    if (!buyingRenting && sellingLoaning) {
                        urlString += ("&type=offers");
                    } else if (!sellingLoaning && buyingRenting) {
                        urlString += ("&type=requests");
                    }
                    HttpURLConnection conn = AppUtils.getHttpConnection(urlString, "GET", user);
                    String output = AppUtils.getResponseContent(conn);
                    int responseCode = conn.getResponseCode();
                    Log.i(TAG, "GET /requests response code : " + responseCode);
                    //Nearby is not available in this location
                    if (responseCode == 403) {
                        return responseCode;
                    }
                    try {
                        requests = AppUtils.jsonStringToRequestList(output);
                    } catch (IOException e) {
                        Log.e(TAG, "error getting requests: " + output);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get requests: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (!isAdded()) {
                    return;
                }
                //Nearby not in this location yet
                if (responseCode != null && responseCode == 403) {
                } else {
                    if (requestAdapter != null) {
                        requestAdapter.swap(requests);
                    }
                    if (requests.size() < 1) {
                        noResultsLayout.setVisibility(View.VISIBLE);
                    } else {
                        noResultsLayout.setVisibility(View.GONE);
                    }
                }
            }
        }.execute();
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

    public void displayNoCommunitySnackbar() {
        Snackbar snack = Snackbar.make(view.getRootView(), "You must join a community to create posts",
                Constants.LONG_SNACK)
                .setAction("find my community", new View.OnClickListener() {
                    @Override
                    //TODO: change this to community dialog
                    public void onClick(View view) {
                        AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                        AccountFragment.updateAccountDialog.show(getFragmentManager(), "dialog");
                    }
                });
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                snack.getView().getRootView().getLayoutParams();

        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + 150);

        snack.getView().getRootView().setLayoutParams(params);
        snack.show();
    }

    public void showReportDialog(Request request) {
        ReportRequestFragment frag = ReportRequestFragment.newInstance(request, null, null, true, false);
        frag.show(getFragmentManager(), "dialog");
    }

}
