package layout;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.HistoryCardAdapter;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.ScannerActivity;
import iuxta.nearby.model.History;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.Response;
import iuxta.nearby.model.Transaction;
import iuxta.nearby.model.User;

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
    private RelativeLayout spinnerScreen;
    public static String snackbarMessage = null;
    private static ViewRequestFragment viewRequestFragment;
    private static ViewTransactionFragment viewTransactionFragment;
    public static ExchangeCodeDialogFragment exchangeCodeDialogFragment;
    public static ConfirmChargeDialogFragment confirmChargeDialogFragment;
    public static Boolean showTransactions = true;
    public static Boolean showRequests = true;
    public static Boolean showOffers = true;
    public static Boolean showStatusOpen = true;
    public static Boolean showStatusClosed = true;

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
        requestHistoryList.setAdapter(historyCardAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        requestHistoryList.setLayoutManager(llm);
        this.view = view;
        noHistoryLayout = (RelativeLayout) view.findViewById(R.id.no_history_layout);
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.spinner_screen);
        parentScroll = (ScrollView) view.findViewById(R.id.history_parent_scrollview);
        getHistory(this);
        if (snackbarMessage != null) {
            Snackbar snackbar = Snackbar
                    .make(view, snackbarMessage, Constants.LONG_SNACK);
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
        viewRequestFragment = ViewRequestFragment.newInstance(h, this);
        viewRequestFragment.show(getFragmentManager(), "dialog");
    }

    public void showTransactionDialog(History h, Response response) {
        viewTransactionFragment = ViewTransactionFragment.newInstance(h, this, response);
        viewTransactionFragment.show(getFragmentManager(), "dialog");
    }

    public static void dismissViewRequestFragment() {
        if (viewRequestFragment != null) {
            viewRequestFragment.dismiss();
        }
    }

    public static void dismissViewTransactionFragment() {
        if (viewTransactionFragment != null) {
            viewTransactionFragment.dismiss();
        }
    }

    public static void dismissExchangeCodeDialogFragment() {
        if (exchangeCodeDialogFragment != null) {
            exchangeCodeDialogFragment.dismiss();
        }
    }

    public static void dismissConfirmChargeDialog() {
        if (confirmChargeDialogFragment != null) {
            confirmChargeDialogFragment.dismiss();
        }
    }

    public void showEditRequestDialog(History h) {
        DialogFragment newFragment = RequestDialogFragment
                .newInstance(h.getRequest());
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void showExchangeCodeDialog(Transaction t, Boolean buyer) {
        String heading = buyer ? "Confirm Return" : "Confirm Exchange";
        exchangeCodeDialogFragment = ExchangeCodeDialogFragment
                .newInstance(t.getId(), heading, !buyer);
        exchangeCodeDialogFragment.setTargetFragment(this, 0);
        exchangeCodeDialogFragment.show(getFragmentManager(), "dialog");
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
                    .make(view, message, Constants.LONG_SNACK);
            snackbar.show();
        }
        getHistory(this);
    }


    public void showConfirmChargeDialog(Double calculatedPrice, String description, String transactionId) {
        dismissConfirmChargeDialog();
        confirmChargeDialogFragment = ConfirmChargeDialogFragment
                .newInstance(calculatedPrice, description, transactionId);
        confirmChargeDialogFragment.show(getFragmentManager(), "dialog");
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

    public void showExchangeOverrideDialog(String transactionId, String header, String description,
                                           boolean initialExchange) {
        ExchangeOverrideDialogFragment f = ExchangeOverrideDialogFragment
                .newInstance(transactionId, header, description, initialExchange);
        f.show(getFragmentManager(), "dialog");
    }

    public void showCancelTransactionDialog(String transactionId) {
        CancelTransactionDialogFragment f = CancelTransactionDialogFragment.newInstance(transactionId);
        f.show(getFragmentManager(), "dialog");

    }

    private void showNoNetworkSnack() {
        Snackbar.make(view.getRootView(), R.string.noNetworkConnection,
                Constants.LONG_SNACK)
                .setAction("open settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).show();
    }

    public void getHistory(final HistoryFragment thisFragment) {
        parentScroll.setVisibility(View.GONE);
        noHistoryLayout.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        if (!MainActivity.isNetworkConnected()) {
            parentScroll.setVisibility(View.VISIBLE);
            showNoNetworkSnack();
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String typesQuery = null;
                    if (HistoryFragment.showTransactions) {
                        typesQuery = "types=transactions";
                    }
                    if (HistoryFragment.showOffers) {
                        if (typesQuery == null) {
                            typesQuery = "types=offers";
                        } else {
                            typesQuery += "&types=offers";
                        }
                    }
                    if (HistoryFragment.showRequests) {
                        if (typesQuery == null) {
                            typesQuery = "types=requests";
                        } else {
                            typesQuery += "&types=requests";
                        }
                    }
                    String statusQuery = null;
                    if (HistoryFragment.showStatusClosed) {
                        statusQuery = "status=closed";
                    }
                    if (HistoryFragment.showStatusOpen) {
                        if (statusQuery == null) {
                            statusQuery = "status=open";
                        } else {
                            statusQuery += "&status=open";
                        }
                    }
                    String queryString = "";
                    if (typesQuery != null && statusQuery != null) {
                        queryString = "?" + typesQuery + "&" + statusQuery;
                    } else if (typesQuery != null) {
                        queryString = "?" + typesQuery;
                    } else if (statusQuery != null) {
                        queryString = "?" + statusQuery;
                    }
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me/history" + queryString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
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
                spinnerScreen.setVisibility(View.GONE);
                if (recentHistory == null || recentHistory.size() == 0) {
                    noHistoryLayout.setVisibility(View.VISIBLE);
                    parentScroll.setVisibility(View.GONE);
                } else {
                    parentScroll.setVisibility(View.VISIBLE);
                    noHistoryLayout.setVisibility(View.GONE);
                }
                for (History h : recentHistory) {
                    parentObjs.add(h);
                }
                requestHistoryList = (RecyclerView) view.findViewById(R.id.request_history_list);
                historyCardAdapter = new HistoryCardAdapter(recentHistory, thisFragment);
                requestHistoryList.setAdapter(historyCardAdapter);
                LinearLayoutManager llm = new LinearLayoutManager(context);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                requestHistoryList.setLayoutManager(llm);
            }
        }.execute();
    }

    public void updateOffer(final Response response, final Request request,
                            final DialogInterface dialog, final String returnToScreen,
                            final HistoryFragment historyFragment) {
        if (!MainActivity.isNetworkConnected()) {
            showNoNetworkSnack();
            return;
        }
        parentScroll.setVisibility(View.GONE);
        noHistoryLayout.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        dismissViewRequestFragment();
        dismissViewTransactionFragment();
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String apiPath = "/requests/" + request.getId() + "/responses/" + response.getId();
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "PUT", user);
                    if (request.getUser().getId().equals(user.getId()) && dialog != null) {
                        response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                    } else if (dialog != null) {
                        response.setSellerStatus(Response.SellerStatus.ACCEPTED);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    User seller = response.getSeller();
                    response.setSeller(null);
                    String responseJson = mapper.writeValueAsString(response);
                    Log.i("updated response: ", responseJson);
                    response.setSeller(seller);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update offer: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                HistoryFragment.dismissViewRequestFragment();
                if (responseCode == 200) {
                    if (dialog != null) {
                        dialog.dismiss();
                        dismissViewRequestFragment();
                    }
                    if (returnToScreen == null) {
                        parentScroll.setVisibility(View.VISIBLE);
                        spinnerScreen.setVisibility(View.GONE);
                        ((MainActivity) getActivity()).goToHistory("successfully updated offer");
                    } else {
                        getUpdatedHistory(request.getId(), returnToScreen, historyFragment);
                    }

                } else {
                    if (dialog != null) {
                        dialog.dismiss();
                        dismissViewRequestFragment();
                    }
                    if (returnToScreen == null) {
                        parentScroll.setVisibility(View.VISIBLE);
                        spinnerScreen.setVisibility(View.GONE);
                        ((MainActivity) getActivity()).goToHistory("could not update offer");
                    } else {
                        getUpdatedHistory(request.getId(), returnToScreen, historyFragment);
                    }
                }
            }
        }.execute();

    }

    public void getUpdatedHistory(final String requestId, final String screenToRefresh, final HistoryFragment historyFragment) {
        new AsyncTask<Void, Void, History>() {
            @Override
            protected History doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/users/me/history", "GET", user);
                    String output = AppUtils.getResponseContent(conn);
                    try {
                        recentHistory = AppUtils.jsonStringToHistoryList(output);
                        for (History h : recentHistory) {
                            if (h.getRequest().getId().equals(requestId)) {
                                return h;
                            }
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
            protected void onPostExecute(History h) {
                if (screenToRefresh.equals("view_transaction")) {
                    dismissViewTransactionFragment();
                    final Transaction transaction = h.getTransaction();
                    Response resp = null;
                    for (Response res : h.getResponses()) {
                        if (res.getId().equals(transaction.getResponseId())) {
                            resp = res;
                            break;
                        }
                    }
                    getHistory(historyFragment);
                    showTransactionDialog(h, resp);
                } else if (screenToRefresh.equals("view_request")) {
                    dismissViewRequestFragment();
                    getHistory(historyFragment);
                    showRequestDialog(h);
                }
            }
        }.execute();
    }

    public void updateRequest(final Request request) {
        if (!MainActivity.isNetworkConnected()) {
            showNoNetworkSnack();
            return;
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/requests/" + request.getId(), "PUT", user);
                    ObjectMapper mapper = new ObjectMapper();
                    // we don't need to update the location or user info
                    Request r = request;
                    r.setUser(null);
                    r.setLocation(null);
                    String requestJson = mapper.writeValueAsString(r);
                    Log.i("updated request: ", requestJson);
                    byte[] outputInBytes = requestJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /api/requests", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String output = AppUtils.getResponseContent(conn);
                        throw new IOException(output);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update request: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                HistoryFragment.dismissViewRequestFragment();
                if (responseCode == 200) {
                    ((MainActivity) getActivity()).goToHistory("successfully updated request");
                } else {
                    ((MainActivity) getActivity()).goToHistory("could not update request at this time");
                }
            }
        }.execute();
    }
}
