package layout;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
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

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.HistoryCardAdapter;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.ScannerActivity;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.Transaction;
import superstartupteam.nearby.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    private Context context;
    private User user;
    private List<History> recentHistory = new ArrayList<>();
    List<ParentObject> parentObjs = new ArrayList<>();
    private RecyclerView requestHistoryList;
    private HistoryCardAdapter historyCardAdapter;
    public ScrollView parentScroll;
    private View view;
    private RelativeLayout noHistoryLayout;
    public static String snackbarMessage = null;

    private OnFragmentInteractionListener mListener;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryFragment.
     */
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        requestHistoryList = (RecyclerView) view.findViewById(R.id.request_history_list);
        historyCardAdapter = new HistoryCardAdapter(recentHistory, this);
        //historyCardAdapter = new HistoryCardAdapter(context, parentObjs, this);
        //historyCardAdapter.setCustomParentAnimationViewId(R.id.parent_list_item_expand_arrow);
        //historyCardAdapter.setParentClickableViewAnimationDuration(0);
        //historyCardAdapter.setParentAndIconExpandOnClick(false);
        requestHistoryList.setAdapter(historyCardAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        requestHistoryList.setLayoutManager(llm);
        this.view = view;
        noHistoryLayout = (RelativeLayout) view.findViewById(R.id.no_history_layout);
        getHistory(this);
        parentScroll = (ScrollView) view.findViewById(R.id.history_parent_scrollview);
        if (snackbarMessage != null) {
            Snackbar snackbar = Snackbar
                    .make(view, snackbarMessage, Snackbar.LENGTH_LONG);
            snackbar.show();
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

    public void showRequestDialog(History h) {
        DialogFragment newFragment = RequestDialogFragment
                .newInstance(h.getRequest());
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void showExchangeCodeDialog(Transaction t, Boolean buyer) {
        String heading = buyer ? "Confirm Return" : "Confirm Exchange";
        ExchangeCodeDialogFragment fragment = ExchangeCodeDialogFragment
                .newInstance(t.getId(), heading);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), "dialog");
    }

    public void showScanner(String transactionId, Boolean buyer) {
        Intent i = new Intent(getActivity(), ScannerActivity.class);
        i.putExtra("TRANSACTION_ID", transactionId);
        i.putExtra("HEADER", buyer ? "Confirm Exchange" : "Confirm Return");
        startActivityForResult(i, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && data != null && data.getExtras() != null) {
            String message = (String) data.getExtras().get("MESSAGE");
            Snackbar snackbar = Snackbar
                    .make(view, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        getHistory(this);
    }

    public void showConfirmChargeDialog(Double calculatedPrice, String description, String transactionId) {
        ConfirmChargeDialogFragment frag = ConfirmChargeDialogFragment
                .newInstance(calculatedPrice, description, transactionId);
        frag.show(getFragmentManager(), "dialog");
    }

    public void showConfirmExchangeOverrideDialog(String exchangeTime, String description,
                                                  String transactionId, boolean isSeller) {
        ConfirmExchangeOverrideDialogFragment f = ConfirmExchangeOverrideDialogFragment
                .newInstance(exchangeTime, description, transactionId, isSeller);
        f.show(getFragmentManager(), "dialog");
    }

    public void showResponseDialog(Response r) {
        String requestId = r.getRequestId();
        Request request = null;
        for (History h : recentHistory) {
            if (h.getRequest() != null && h.getRequest().getId().equals(requestId)) {
                request = h.getRequest();
            }
        }
        DialogFragment newFragment = ViewOfferDialogFragment
                .newInstance(r, request);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void showExchangeOverrideDialog(String transactionId, String header, String description) {
        ExchangeOverrideDialogFragment f = ExchangeOverrideDialogFragment
                .newInstance(transactionId, header, description);
        f.show(getFragmentManager(), "dialog");
    }

    public void showCancelTransactionDialog(String transactionId) {
        CancelTransactionDialogFragment f = CancelTransactionDialogFragment.newInstance(transactionId);
        f.show(getFragmentManager(), "dialog");

    }

    private void showNoNetworkSnack() {
        Snackbar.make(view.getRootView(), R.string.noNetworkConnection,
                Snackbar.LENGTH_LONG)
                .setAction("open settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).show();
    }

    public void getHistory(final HistoryFragment thisFragment) {
        if (!MainActivity.isNetworkConnected()) {
            showNoNetworkSnack();
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me/history");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    try {
                        recentHistory = AppUtils.jsonStringToHistoryList(output);
                        for (History h : recentHistory) {
                            List<Object> resp = new ArrayList<>();
                            for (Response r : h.getResponses()) {
                                resp.add(r);
                            }
                            h.setChildObjectList(resp);
                        }
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
                parentObjs = new ArrayList<>();
                if (recentHistory == null || recentHistory.size() == 0) {
                    noHistoryLayout.setVisibility(View.VISIBLE);
                } else {
                    noHistoryLayout.setVisibility(View.GONE);
                }
                for (History h : recentHistory) {
                    parentObjs.add(h);
                }
                requestHistoryList = (RecyclerView) view.findViewById(R.id.request_history_list);
                //historyCardAdapter = new HistoryCardAdapter(context, parentObjs, thisFragment);
                historyCardAdapter = new HistoryCardAdapter(recentHistory, thisFragment);
                //historyCardAdapter.setCustomParentAnimationViewId(R.id.parent_list_item_expand_arrow);
                //historyCardAdapter.setParentClickableViewAnimationDuration(0);
                //historyCardAdapter.setParentAndIconExpandOnClick(false);
                requestHistoryList.setAdapter(historyCardAdapter);
                LinearLayoutManager llm = new LinearLayoutManager(context);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                requestHistoryList.setLayoutManager(llm);
            }
        }.execute();
    }
}
