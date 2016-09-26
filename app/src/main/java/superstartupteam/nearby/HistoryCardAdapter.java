package superstartupteam.nearby;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import superstartupteam.nearby.model.Transaction;
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

        if (r.getResponseStatus().toString().toLowerCase().equals("closed")) {
            responseViewHolder.mResponseDetailsButton.setVisibility(View.GONE);
        } else {
            responseViewHolder.mResponseDetailsButton.setVisibility(View.VISIBLE);
        }
        responseViewHolder.mResponseStatus.setText(r.getResponseStatus().toString());
        setResponseStatusColor(responseViewHolder.mResponseStatus, r.getResponseStatus().toString());
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
        if (h.getTransaction() != null && (h.getTransaction().getSellerAccepted() == null ||
                !h.getTransaction().getSellerAccepted())) {
            requestViewHolder.mExchangeIcon.setVisibility(View.VISIBLE);
            requestViewHolder.mCardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.card_border_left));
            requestViewHolder.vEditButton.setVisibility(View.GONE);
            requestViewHolder.vResponseDetailsButton.setVisibility(View.GONE);
            requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
            requestViewHolder.vPostedDate.setVisibility(View.GONE);
            requestViewHolder.vCategoryName.setVisibility(View.VISIBLE);
            requestViewHolder.vDescription.setVisibility(View.VISIBLE);
            Response resp = null;
            Transaction transaction = h.getTransaction();
            for (Response res:h.getResponses()) {
                if (res.getId().equals(transaction.getResponseId())) {
                    resp = res;
                    break;
                }
            }
            String topDescription = null;
            boolean isBuyer = user.getId().equals(r.getUser().getId());
            boolean isSeller = !isBuyer;
            if (isBuyer) {
                topDescription = (r.getRental() ? "Borrowing a " : "Buying a ") + r.getItemName() +
                        " from " + resp.getSeller().getFirstName();
            } else {
                topDescription = (r.getRental() ? "Loaning a " : "Selling a ") + r.getItemName() +
                        " to " + r.getUser().getFirstName();
            }
            requestViewHolder.vItemName.setText(topDescription);
            if (!transaction.getExchanged()) {
                String exchangeTime = "<b>exchange time:</b> " + resp.getExchangeTime();
                requestViewHolder.vCategoryName.setText(Html.fromHtml(exchangeTime));
                String exchangeLocation = "<b>exchange location:</b> " + resp.getExchangeLocation();
                requestViewHolder.vDescription.setText(Html.fromHtml(exchangeLocation));
                requestViewHolder.vStatus.setText("Awaiting initial exchange");
            } else if (!transaction.getReturned() && r.getRental()) {
                String returnTime = "<b>return time:</b> " + resp.getReturnTime();
                requestViewHolder.vCategoryName.setText(Html.fromHtml(returnTime));
                String returnLocation = "<b>return location:</b> " + resp.getReturnLocation();
                requestViewHolder.vDescription.setText(Html.fromHtml(returnLocation));
                requestViewHolder.vStatus.setText("Awaiting return");
            }

        } else if (user.getId().equals(r.getUser().getId())) { // this is a request the user made
            requestViewHolder.vResponseDetailsButton.setVisibility(View.GONE);
            requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
            requestViewHolder.mCardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.request_card_background));
            requestViewHolder.mExchangeIcon.setVisibility(View.GONE);
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
            setRequestStatusColor(requestViewHolder.vStatus, r.getStatus());
            /*
             * only display the edit button if the request is open...they shouldn't need to edit
             * closed requests
             */
            if (!r.getStatus().equals("OPEN")) {
                requestViewHolder.vEditButton.setVisibility(View.GONE);
            } else {
                requestViewHolder.vEditButton.setVisibility(View.VISIBLE);
                requestViewHolder.vEditButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        historyFragment.showRequestDialog(h);
                    }
                });
            }
            if (h.getResponses() == null || h.getResponses().size() < 1) {
                requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
                if (r.getStatus().equals("OPEN")) {
                    RelativeLayout.LayoutParams lp =
                            (RelativeLayout.LayoutParams) requestViewHolder.vEditButton.getLayoutParams();
                    lp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    requestViewHolder.vEditButton.setLayoutParams(lp);
                }
            } else {
                requestViewHolder.vParentDropDownArrow.setVisibility(View.VISIBLE);
            }
        } else { //this is an offer the user made
            requestViewHolder.mExchangeIcon.setVisibility(View.GONE);
            requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
            requestViewHolder.mCardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.request_card_background));
            requestViewHolder.vEditButton.setVisibility(View.GONE);
            final Response resp = h.getResponses().get(0);
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
            setResponseStatusColor(requestViewHolder.vStatus, resp.getResponseStatus().toString());
            requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
            if (resp.getResponseStatus().equals(Response.Status.CLOSED)) {
                requestViewHolder.vResponseDetailsButton.setVisibility(View.GONE);
            } else {
                requestViewHolder.vResponseDetailsButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        historyFragment.showResponseDialog(resp);
                    }
                });
            }
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

    private void setResponseStatusColor(TextView responseStatus, String status) {
        status = status.toLowerCase();
        switch (status) {
            case "accepted":
                responseStatus.setTextColor(Color.parseColor("#4EE2EC"));
                break;
            case "pending":
                responseStatus.setTextColor(Color.parseColor("#FFD700"));
                break;
            case "closed":
                responseStatus.setTextColor(Color.parseColor("#E52B50"));
                break;
        }
    }

    private void setRequestStatusColor(TextView requestStatus, String status) {
        status = status.toLowerCase();
        switch (status) {
            case "open":
                requestStatus.setTextColor(Color.GREEN);
                break;
            case "closed":
                requestStatus.setTextColor(Color.parseColor("#E52B50"));
                break;
            case "fulfilled":
                requestStatus.setTextColor(Color.parseColor("#4EE2EC"));
                break;
        }
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
        private ImageButton vResponseDetailsButton;
        public RelativeLayout mCardBackground;
        public ImageButton mExchangeIcon;
        private CardView historyCard;


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
            vResponseDetailsButton = (ImageButton) itemView.findViewById(R.id.view_response_button);
            this.context = context;
            mCardBackground = (RelativeLayout) itemView.findViewById(R.id.card_layout);
            mExchangeIcon = (ImageButton) v.findViewById(R.id.exchange_icon);
            historyCard = (CardView) v.findViewById(R.id.my_history_card_view);
            historyCard.setMaxCardElevation(7);

        }
    }
}
