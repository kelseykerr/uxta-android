package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.RequestResponseCardAdapter;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * Created by kerrk on 2/25/17.
 */

public class ViewRequestFragment extends DialogFragment {

    private static History history;
    private Request request;
    private Context context;
    private TextView requestText;
    private TextView requestDescription;
    private ImageButton closeBtn;
    private TextView requestStatus;
    private TextView postedDate;
    private TextView openOffers;
    private User user;
    private View view;
    private ViewRequestFragment.OnFragmentInteractionListener mListener;
    private RecyclerView responseList;
    private RequestResponseCardAdapter requestResponseCardAdapter;
    private FloatingActionButton editFab;
    private static HistoryFragment historyFragment;
    private static final String TAG = "ViewRequestFragment";

    public ViewRequestFragment() {
    }

    public static ViewRequestFragment newInstance(History h, HistoryFragment hf) {
        ViewRequestFragment fragment = new ViewRequestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        history = h;
        historyFragment = hf;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = PrefUtils.getCurrentUser(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // request a window without the title
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        request = history.getRequest();
        View view = inflater.inflate(R.layout.fragment_view_request, container, false);
        requestText = (TextView) view.findViewById(R.id.request_text);
        requestText.setText("Requested a " + request.getItemName());
        requestDescription = (TextView) view.findViewById(R.id.request_description_text);
        if (request.getDescription() != null && request.getDescription().length() > 0) {
            requestDescription.setText(request.getDescription());
        }
        requestStatus = (TextView) view.findViewById(R.id.view_request_status);
        requestStatus.setText(request.getStatus().toUpperCase());
        setRequestStatusColor(request.getStatus());
        postedDate = (TextView) view.findViewById(R.id.view_posted_date);
        String diff = AppUtils.getTimeDiffString(request.getPostDate());
        postedDate.setText(diff);
        openOffers = (TextView) view.findViewById(R.id.view_open_offers);
        responseList = (RecyclerView) view.findViewById(R.id.request_responses_list);
        requestResponseCardAdapter = new RequestResponseCardAdapter(context, history.getResponses(), request, historyFragment);
        responseList.setAdapter(requestResponseCardAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        responseList.setLayoutManager(llm);
        int count = 0;
        for (Response resp : history.getResponses()) {
            if (resp.getResponseStatus().toString().toLowerCase().equals("pending")) {
                count++;
            }
        }
        String ooString = "<u>" + count + " pending" + (count > 1 || count == 0 ? " offers</u>" : " offer</u>");
        openOffers.setText(Html.fromHtml(ooString));
        closeBtn = (ImageButton) view.findViewById(R.id.close_view);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        editFab = (FloatingActionButton) view.findViewById(R.id.edit_fab);
        editFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!request.getStatus().toLowerCase().equals("closed")) {
                    historyFragment.showEditRequestDialog(history);
                }
            }
        });
        this.view = view;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }

    private void setRequestStatusColor(String status) {
        status = status.toLowerCase();
        switch (status) {
            case "open":
                requestStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_green));
                break;
            case "closed":
                requestStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_red));
                break;
            case "fulfilled":
                requestStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_blue));
                break;
        }
    }

}
