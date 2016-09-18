package layout;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
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
    private RecyclerView requestHistoryList;
    private HistoryCardAdapter historyCardAdapter;
    public ScrollView parentScroll;
    private View view;
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
        this.view = view;
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

    public void showRequestDialog(History h) {
        DialogFragment newFragment = RequestDialogFragment
                .newInstance(h.getRequest());
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void showResponseDialog(Response r) {
        String requestId = r.getRequestId();
        Request request = null;
        for (History h: recentHistory) {
            if (h.getRequest() != null && h.getRequest().getId().equals(requestId)) {
                request = h.getRequest();
            }
        }
        DialogFragment newFragment = ViewOfferDialogFragment
                .newInstance(r, request);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void getHistory(final HistoryFragment thisFragment) {
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
                        for (History h:recentHistory) {
                            List<Object> resp = new ArrayList<Object>();
                            for (Response r:h.getResponses()) {
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
                if (historyCardAdapter != null) {
                    historyCardAdapter.swap(recentHistory);
                } else {
                    List<ParentObject> objs = new ArrayList<ParentObject>();
                    for (History h:recentHistory) {
                        objs.add(h);
                    }
                    requestHistoryList = (RecyclerView) view.findViewById(R.id.request_history_list);
                    //requestHistoryList.setHasFixedSize(true);
                    LinearLayoutManager llm = new LinearLayoutManager(context);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    requestHistoryList.setLayoutManager(llm);

                    historyCardAdapter = new HistoryCardAdapter(context, objs, thisFragment);
                    historyCardAdapter.setCustomParentAnimationViewId(R.id.parent_list_item_expand_arrow);
                    historyCardAdapter.setParentClickableViewAnimationDuration(0);
                    historyCardAdapter.setParentAndIconExpandOnClick(false);
                    requestHistoryList.setAdapter(historyCardAdapter);
                }
            }
        }.execute();
    }
}
