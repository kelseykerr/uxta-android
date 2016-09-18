package superstartupteam.nearby;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.ArrayList;
import java.util.List;

import layout.HistoryFragment;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 8/28/16.
 */
public class HistoryCardAdapter extends ExpandableRecyclerAdapter<HistoryCardAdapter.HistoryCardViewHolder, HistoryCardAdapter.ResponseViewHolder> {
    private List<History> recentHistory;
    private User user;
    private LayoutInflater mInflater;
    private HistoryFragment historyFragment;


    public HistoryCardAdapter(Context context, List<ParentObject> parentItemList, HistoryFragment fragment) {
        super(context, parentItemList);
        List<History> objs = new ArrayList<>();
        for (ParentObject p: parentItemList) {
            objs.add((History) p);
        }
        this.recentHistory = objs;
        historyFragment = fragment;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void onBindChildViewHolder(final ResponseViewHolder responseViewHolder, int i, Object obj) {
        final Response r = (Response) obj;
        responseViewHolder.mOfferAmount.setText(r.getOfferPrice().toString());
        responseViewHolder.mResponderName.setText(r.getSeller().getFirstName());
        if (r.getPriceType().equals("per_hour")) {
            responseViewHolder.mPriceType.setText(" per hour");
        } else if (r.getPriceType().equals("per_day")) {
            responseViewHolder.mPriceType.setText(" per day");
        }
        if (r.getResponseStatus().equals(Response.Status.CLOSED)) {
            responseViewHolder.mResponseDetailsButton.setVisibility(View.GONE);
        }
        responseViewHolder.mResponseStatus.setText(r.getResponseStatus().toString());
        responseViewHolder.mResponseDetailsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                historyFragment.showResponseDialog(r);
            }
        });
    }

    @Override
    public void onBindParentViewHolder(final HistoryCardViewHolder requestViewHolder, int i, Object obj) {
        final History h = (History) obj;
        Request r = h.getRequest();
        // this is a request the user made
        if (user.getId().equals(r.getUser().getId())) {
            String htmlString = "requested a <b>" +
                    r.getItemName() + "</b>";
            requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
            String diff = AppUtils.getTimeDiffString(r.getPostDate());
            requestViewHolder.vPostedDate.setText(diff);
            if (r.getCategory() != null)  {
                requestViewHolder.vCategoryName.setText(r.getCategory().getName());
            } else {
                requestViewHolder.vCategoryName.setVisibility(View.GONE);
            }

            if (r.getDescription() != null) {
                requestViewHolder.vDescription.setText(r.getDescription());
            } else {
                requestViewHolder.vDescription.setVisibility(View.GONE);
            }
            requestViewHolder.vStatus.setText(r.getStatus());
            /*
             * only display the edit button if the request is open...they shouldn't need to edit
             * closed requests
             */
            if (!r.getStatus().equals("OPEN")) {
                requestViewHolder.vEditButton.setVisibility(View.GONE);
            } else {
                requestViewHolder.vEditButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        historyFragment.showRequestDialog(h);
                    }
                });
            }
        } else { //this is an offer the user made
            Response resp = h.getResponses().get(0);
            String buyerName = r.getUser().getFirstName() != null ?
                    r.getUser().getFirstName() : r.getUser().getFullName();
            String htmlString = "offered a <b>" +
                    r.getItemName() + "</b> to " + buyerName;
            String diff = AppUtils.getTimeDiffString(h.getResponses().get(0).getResponseTime());
            requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
            requestViewHolder.vPostedDate.setText(diff);
            requestViewHolder.vCategoryName.setText("");
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
            requestViewHolder.vDescription.setText("");
            requestViewHolder.vDescription.setVisibility(View.GONE);
            requestViewHolder.vStatus.setText(resp.getResponseStatus().toString());
            // only edit if the offer is pending
            if (!resp.getResponseStatus().equals(Response.Status.PENDING)) {
                requestViewHolder.vEditButton.setVisibility(View.GONE);
            }
            requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
        }
    }

    @Override
    public HistoryCardViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        user = PrefUtils.getCurrentUser(viewGroup.getContext());
        Context context = viewGroup.getContext();
        View view = mInflater.inflate(R.layout.my_history_card, viewGroup, false);
        return new HistoryCardViewHolder(context, view);
    }

    @Override
    public ResponseViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.request_responses, viewGroup, false);
        return new ResponseViewHolder(view);
    }

    public void swap(List<History> newHistory){
        recentHistory.clear();
        recentHistory.addAll(newHistory);
        notifyDataSetChanged();
    }

    public static class ResponseViewHolder extends ChildViewHolder {

        public TextView mResponderName;
        public TextView mItemName;
        public TextView mOfferAmount;
        public TextView mPriceType;
        public TextView mResponseStatus;
        private ImageButton mResponseDetailsButton;

        public ResponseViewHolder(View itemView) {
            super(itemView);
            mResponderName = (TextView) itemView.findViewById(R.id.responder_name);
            mItemName = (TextView) itemView.findViewById(R.id.item_name);
            mOfferAmount = (TextView) itemView.findViewById(R.id.offer_amount);
            mPriceType = (TextView) itemView.findViewById(R.id.offer_type);
            mResponseStatus = (TextView) itemView.findViewById(R.id.response_status);
            mResponseDetailsButton = (ImageButton) itemView.findViewById(R.id.view_response_button);
        }
    }


    public static class HistoryCardViewHolder extends ParentViewHolder {
        protected TextView vItemName;
        protected TextView vCategoryName;
        protected TextView vPostedDate;
        protected TextView vDescription;
        protected Context context;
        private ImageButton vParentDropDownArrow;
        private ImageButton vEditButton;
        private TextView vStatus;

        protected FrameLayout cardView;

        public HistoryCardViewHolder(Context context, View v) {
            super(v);
            vItemName =  (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView)  v.findViewById(R.id.category_name);
            vPostedDate = (TextView)  v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            cardView = (FrameLayout) itemView.findViewById(R.id.my_history_card_view);
            vParentDropDownArrow = (ImageButton) itemView.findViewById(R.id.parent_list_item_expand_arrow);
            vStatus = (TextView) v.findViewById(R.id.history_card_status);
            vEditButton = (ImageButton) v.findViewById(R.id.edit_button);
            this.context = context;
        }
    }
}
