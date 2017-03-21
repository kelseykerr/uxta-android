package iuxta.nearby;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import layout.HistoryFragment;
import iuxta.nearby.model.History;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.Response;
import iuxta.nearby.model.Transaction;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 8/28/16.
 */
public class HistoryCardAdapter extends RecyclerView.Adapter<HistoryCardAdapter.HistoryCardViewHolder> {
    private List<History> recentHistory;
    private User user;
    private LayoutInflater mInflater;
    private HistoryFragment historyFragment;
    private Context context;
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy hh:mm a");
    private static final String TAG = "HistoryCardAdapter";


    public HistoryCardAdapter(List<History> history, HistoryFragment fragment) {
        this.recentHistory = history;
        this.historyFragment = fragment;
    }

    @Override
    public int getItemCount() {
        return recentHistory.size();
    }

    public void onBindViewHolder(final HistoryCardViewHolder requestViewHolder, int i) {
        final History h = recentHistory.get(i);
        final Request r = h.getRequest();
        requestViewHolder.exchangeSwipe.setVisibility(View.GONE);
        if (h.getTransaction() != null && !r.getStatus().toLowerCase().equals("closed") &&
                (h.getTransaction().getCanceled() != null && !h.getTransaction().getCanceled())) {
            setUpTransactionCard(requestViewHolder, r, h);
        } else if (user.getId().equals(r.getUser().getId())) { // this is a request the user made
            setUpRequestCard(requestViewHolder, r, h);
        } else { //this is an offer the user made
            setUpOfferCard(requestViewHolder, r, h);
        }
        List<String> values = new ArrayList();
        for (Response resp : h.getResponses()) {
            if (resp == null || resp.getSeller() == null) {
                return;
            }
            BigDecimal price = AppUtils.formatCurrency(resp.getOfferPrice());
            String htmlString = "<font color='#767474'>" + resp.getSeller().getFirstName() +
                    " made an offer for $" + price +
                    "<br/>" + resp.getResponseStatus().toString() + "</font>";
            values.add(htmlString);
        }
    }


    public static void justifyListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    @Override
    public HistoryCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        user = PrefUtils.getCurrentUser(viewGroup.getContext());
        Context context = viewGroup.getContext();
        this.context = context;
        mInflater = LayoutInflater.from(viewGroup.getContext());
        View view = mInflater.inflate(R.layout.my_history_card, viewGroup, false);

        return new HistoryCardViewHolder(context, view);
    }


    public void swap(List<History> newHistory) {
        recentHistory.clear();
        recentHistory.addAll(newHistory);
        notifyDataSetChanged();
    }

    private void setResponseStatusColor(TextView responseStatus, String status) {
        status = status.toLowerCase();
        switch (status) {
            case "accepted":
                responseStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_blue));
                break;
            case "pending":
                responseStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_yellow));
                break;
            case "closed":
                responseStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_red));
                break;
        }
    }

    private void setRequestStatusColor(TextView requestStatus, String status) {
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

    private void setUpOfferCard(HistoryCardViewHolder requestViewHolder, final Request r, final History h) {
        requestViewHolder.vTransactionStatus.setVisibility(View.GONE);
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
        final Response resp = h.getResponses().get(0);
        String buyerName = r.getUser().getFirstName() != null ?
                r.getUser().getFirstName() : r.getUser().getFullName();
        String htmlString = "Offered a " +
                r.getItemName() + " to " + buyerName;
        requestViewHolder.setUpProfileImage(r.getUser());
        String diff = AppUtils.getTimeDiffString(h.getResponses().get(0).getResponseTime());
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        requestViewHolder.vPostedDate.setText(diff);
        if (h.getResponses().get(0).getSellerStatus().equals(Response.SellerStatus.OFFERED)) {
            requestViewHolder.vCategoryName.setText("Buyer updated offer, awaiting your approval");
            requestViewHolder.vCategoryName.setVisibility(View.VISIBLE);
        } else {
            requestViewHolder.vCategoryName.setText("");
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
        }
        requestViewHolder.vDescription.setText("");
        requestViewHolder.vDescription.setVisibility(View.GONE);
        requestViewHolder.vStatus.setText(resp.getResponseStatus().toString().toUpperCase());
        if (resp.getResponseStatus().toString().toLowerCase().equals("closed")) {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
        } else {
            requestViewHolder.closeSwipe.setVisibility(View.VISIBLE);
            requestViewHolder.closeSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeOffer(resp, r);
                }
            });
        }
        setResponseStatusColor(requestViewHolder.vStatus, resp.getResponseStatus().toString());
        requestViewHolder.historyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!resp.getResponseStatus().equals(Response.Status.CLOSED)) {
                    historyFragment.showResponseDialog(resp);
                }
            }
        });
        if (!resp.getResponseStatus().equals(Response.Status.CLOSED)) {
            requestViewHolder.moreSwipe.setVisibility(View.VISIBLE);
            requestViewHolder.moreSwipe.setText("edit");
            requestViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(resp);
                }
            });
        } else {
            requestViewHolder.moreSwipe.setVisibility(View.GONE);
        }

    }

    private void setUpRequestCard(final HistoryCardViewHolder requestViewHolder, final Request r, final History h) {
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
        String htmlString = "Requested a " +
                r.getItemName();
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        String diff = AppUtils.getTimeDiffString(r.getPostDate());
        requestViewHolder.vPostedDate.setText(diff);
        if (r.getCategory() != null) {
            requestViewHolder.vCategoryName.setText(r.getCategory().getName());
        } else {
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
        }

        if (r.getDescription() != null && !r.getDescription().isEmpty()) {
            requestViewHolder.vDescription.setText(r.getDescription());
        } else {
            requestViewHolder.vDescription.setVisibility(View.GONE);
        }
        requestViewHolder.vStatus.setText(r.getStatus().toUpperCase());
        setRequestStatusColor(requestViewHolder.vStatus, r.getStatus());
        requestViewHolder.setUpProfileImage(user);

            /*
             * only display the edit button if the request is open...they shouldn't need to edit
             * closed requests
             */
        if (!r.getStatus().equals("OPEN")) {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
        } else {
            requestViewHolder.closeSwipe.setVisibility(View.VISIBLE);
            requestViewHolder.closeSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeRequest(r);
                }
            });
            int count = 0;
            for (Response resp : h.getResponses()) {
                if (resp.getResponseStatus().toString().toLowerCase().equals("pending")) {
                    count++;
                }
            }
            if (count > 0) {
                String ooString = "<u>" + count + " pending" + (count > 1 ? " offers</u>" : " offer</u>");
                requestViewHolder.vOpenOffers.setText(Html.fromHtml(ooString));
            }
        }
        requestViewHolder.historyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showRequestDialog(h);
            }
        });
        requestViewHolder.moreSwipe.setVisibility(View.VISIBLE);
        requestViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showRequestDialog(h);
            }
        });

    }


    private void setUpTransactionCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        boolean isBuyer = user.getId().equals(r.getUser().getId());
        final boolean isSeller = !isBuyer;
        requestViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.card_border_left));
        requestViewHolder.vPostedDate.setVisibility(View.GONE);
        requestViewHolder.vStatus.setVisibility(View.GONE);
        requestViewHolder.vCategoryName.setVisibility(View.VISIBLE);
        requestViewHolder.vDescription.setVisibility(View.VISIBLE);
        Response resp = null;
        final Transaction transaction = h.getTransaction();
        for (Response res : h.getResponses()) {
            if (res.getId().equals(transaction.getResponseId())) {
                resp = res;
                break;
            }
        }
        String topDescription;
        Boolean complete = r.getRental() ? transaction.getExchanged() && transaction.getReturned() :
                transaction.getExchanged();
        String beginning;
        if (complete) {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
            requestViewHolder.exchangeSwipe.setVisibility(View.GONE);
        } else {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
            requestViewHolder.exchangeSwipe.setVisibility(View.VISIBLE);
            requestViewHolder.exchangeSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isSeller) {
                        if (!transaction.getExchanged()) {
                            historyFragment.showExchangeCodeDialog(transaction, false);
                        } else {
                            historyFragment.showScanner(transaction.getId(), false);
                        }
                    } else {
                        if (!transaction.getExchanged()) {
                            historyFragment.showScanner(transaction.getId(), true);
                        } else {
                            historyFragment.showExchangeCodeDialog(transaction, true);
                        }
                    }
                }
            });
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
        }
        if (complete && r.getRental()) {
            beginning = isBuyer ? "Borrowed a " : "Loaned a ";
            requestViewHolder.vTransactionStatus.setVisibility(View.GONE);
        } else if (complete) {
            beginning = isBuyer ? "Bought a " : "Sold a ";
            requestViewHolder.vTransactionStatus.setVisibility(View.GONE);
        } else if (r.getRental()) {
            beginning = isBuyer ? "Borrowing a " : "Loaning a ";
        } else {
            beginning = isBuyer ? "Buying a " : "Selling a ";
        }
        if (isBuyer) {
            topDescription = beginning + r.getItemName() +
                    " from " + (resp.getSeller().getFirstName() != null ?
                    resp.getSeller().getFirstName() : resp.getSeller().getName());
        } else {
            topDescription = beginning + r.getItemName() +
                    " to " + r.getUser().getFirstName();
        }
        requestViewHolder.setUpProfileImage(isSeller ? r.getUser() : resp.getSeller());
        requestViewHolder.vItemName.setText(topDescription);
        if (!transaction.getExchanged()) {
            String formattedDate = resp.getExchangeTime() != null ? formatter.format(resp.getExchangeTime()) : "";
            String exchangeTime = "<b>exchange time:</b> " + formattedDate;
            if (resp.getExchangeTime() != null) {
                requestViewHolder.vCategoryName.setVisibility(View.VISIBLE);
                requestViewHolder.vCategoryName.setText(Html.fromHtml(exchangeTime));
            } else {
                requestViewHolder.vCategoryName.setVisibility(View.GONE);
            }
            String exchangeLocation = "<b>exchange location:</b> " + resp.getExchangeLocation();
            if (resp.getExchangeLocation() != null && resp.getExchangeLocation().length() > 0) {
                requestViewHolder.vDescription.setVisibility(View.VISIBLE);
                requestViewHolder.vDescription.setText(Html.fromHtml(exchangeLocation));
            } else {
                requestViewHolder.vDescription.setVisibility(View.GONE);
            }
            requestViewHolder.vTransactionStatus.setText("Awaiting initial exchange");
            requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            Transaction.ExchangeOverride exchangeOverride = transaction.getExchangeOverride();
            if (exchangeOverride != null && !exchangeOverride.buyerAccepted && !exchangeOverride.declined) {
                requestViewHolder.vTransactionStatus.setText("Pending exchange override approval");
                if (isBuyer) {
                    String description = resp.getSeller().getFirstName() +
                            " submitted an exchange override. Did you exchange the " +
                            r.getItemName() + " at the time above?";
                    historyFragment.showConfirmExchangeOverrideDialog(
                            formatter.format(transaction.getExchangeOverride().time),
                            description, transaction.getId(), isSeller);
                }
            }
        } else if (!transaction.getReturned() && r.getRental()) {
            String formatedDate = resp.getReturnTime() != null ? formatter.format(resp.getReturnTime()) : "";
            String returnTime = "<b>return time:</b> " + formatedDate;
            if (resp.getReturnTime() != null) {
                requestViewHolder.vCategoryName.setVisibility(View.VISIBLE);
                requestViewHolder.vCategoryName.setText(Html.fromHtml(returnTime));
            } else {
                requestViewHolder.vCategoryName.setVisibility(View.GONE);
            }
            String returnLocation = "<b>return location:</b> " + resp.getReturnLocation();
            if (resp.getReturnLocation() != null && resp.getReturnLocation().length() > 0) {
                requestViewHolder.vDescription.setVisibility(View.VISIBLE);
                requestViewHolder.vDescription.setText(Html.fromHtml(returnLocation));
            } else {
                requestViewHolder.vDescription.setVisibility(View.GONE);
            }
            requestViewHolder.vTransactionStatus.setText("Awaiting return");
            requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            Transaction.ExchangeOverride returnOverride = transaction.getReturnOverride();
            if (returnOverride != null && !returnOverride.sellerAccepted && !returnOverride.declined) {
                requestViewHolder.vTransactionStatus.setText("Pending return override approval");
                if (isSeller) {
                    String description = r.getUser().getFirstName() +
                            " submitted a return override. Was the " +
                            r.getItemName() + " returned at the time above?";
                    historyFragment.showConfirmExchangeOverrideDialog(
                            transaction.getExchangeOverride().time.toString(),
                            description,
                            transaction.getId(),
                            isSeller);
                }
            }
        } else if (r.getStatus().equals("TRANSACTION_PENDING")) {
            BigDecimal formattedValue = AppUtils.formatCurrency(transaction.getCalculatedPrice());
            String calculatedPrice = "<b>calculated price:</b> " + formattedValue;
            requestViewHolder.vCategoryName.setText(Html.fromHtml(calculatedPrice));
            requestViewHolder.vDescription.setVisibility(View.GONE);
            if (isBuyer) {
                requestViewHolder.vTransactionStatus.setText("Processing Payment");
                requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            } else {
                final String description = "For " + (r.getRental() ? "loaning " : "selling ") + "your " +
                        r.getItemName() + " to " + r.getUser().getFirstName();
                historyFragment.showConfirmChargeDialog(transaction.getCalculatedPrice(),
                        description, transaction.getId());
                requestViewHolder.vTransactionStatus.setText("CONFIRM CHARGE!");
                requestViewHolder.vTransactionStatus.setTextColor(ContextCompat.getColor(context, R.color.redPink));
                requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            }
        } else {
            requestViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
            requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
            Date completedDate;
            if (r.getRental()) {
                completedDate = transaction.getReturnTime() != null ?
                        transaction.getReturnTime() : transaction.getReturnOverride().time;
            } else {
                completedDate = transaction.getExchangeTime() != null ?
                        transaction.getExchangeTime() : transaction.getExchangeOverride().time;
            }
            String diff = AppUtils.getTimeDiffString(completedDate);
            requestViewHolder.vPostedDate.setText(diff);
            BigDecimal formattedValue = AppUtils.formatCurrency(transaction.getFinalPrice());
            if (isBuyer) {
                String price = "<b>Payment:</b> -$" + formattedValue;
                requestViewHolder.vDescription.setText(Html.fromHtml(price));
            } else {
                String price = "<b>Payment:</b> $" + formattedValue;
                requestViewHolder.vDescription.setText(Html.fromHtml(price));
            }
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
            requestViewHolder.vStatus.setVisibility(View.VISIBLE);
            requestViewHolder.vStatus.setText(r.getStatus().toUpperCase());
            requestViewHolder.vStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_blue));
        }
        final Response response = resp;
        requestViewHolder.historyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showTransactionDialog(h, response);
            }
        });
        requestViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showTransactionDialog(h, response);
            }
        });
    }

    public void closeRequest(final Request request) {
        request.setExpireDate(new Date(new Date().getTime() - 60000));
        String message = "Are you sure you want to close this request?";
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        AlertDialog ad = dialog.setMessage(message)
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                })
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        request.setExpireDate(new Date(new Date().getTime() - 60000));
                        historyFragment.updateRequest(request);
                    }
                })
                .create();
        ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        ad.show();
    }

    public void closeOffer(final Response response, final Request request) {
        String message = "Are you sure you want to close your offer?";
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        AlertDialog ad = dialog.setMessage(message)
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                })
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        response.setSellerStatus(Response.SellerStatus.WITHDRAWN);
                        historyFragment.updateOffer(response, request, null, null, null);
                    }
                })
                .create();
        ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        ad.show();
    }


    public static class HistoryCardViewHolder extends RecyclerView.ViewHolder {
        private TextView vItemName;
        private TextView vCategoryName;
        private TextView vPostedDate;
        private TextView vDescription;
        private Context context;
        private TextView vStatus;
        private TextView vOpenOffers;
        private RelativeLayout historyCard;
        private CardView cardView;
        private LinearLayout responseSeparator;
        private TextView vTransactionStatus;
        private ImageView profileImage;
        private TextView moreSwipe;
        private TextView closeSwipe;
        private TextView exchangeSwipe;
        private SwipeRevealLayout swipeLayout;

        public HistoryCardViewHolder(Context context, View v) {
            super(v);
            vItemName = (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView) v.findViewById(R.id.category_name);
            vPostedDate = (TextView) v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            cardView = (CardView) itemView.findViewById(R.id.my_history_card_view);
            cardView.setMaxCardElevation(7);
            vStatus = (TextView) v.findViewById(R.id.history_card_status);
            vOpenOffers = (TextView) v.findViewById(R.id.open_offers);
            this.context = context;
            historyCard = (RelativeLayout) v.findViewById(R.id.history_card);
            responseSeparator = (LinearLayout) v.findViewById(R.id.response_separator);
            vTransactionStatus = (TextView) v.findViewById(R.id.transaction_status);
            profileImage = (ImageView) v.findViewById(R.id.profileImage);
            moreSwipe = (TextView) v.findViewById(R.id.more_swipe);
            closeSwipe = (TextView) v.findViewById(R.id.close_swipe);
            exchangeSwipe = (TextView) v.findViewById(R.id.exchange_swipe);
            swipeLayout = (SwipeRevealLayout) v.findViewById(R.id.swipe_layout);
            swipeLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
                boolean wasOpen = false;
                @Override
                public void onClosed(SwipeRevealLayout view) {
                    if (!wasOpen) {
                        historyCard.performClick();
                    }
                    wasOpen = false;
                }

                @Override
                public void onOpened(SwipeRevealLayout view) {
                    wasOpen = true;
                }

                @Override
                public void onSlide(SwipeRevealLayout view, float slideOffset) {
                    if (slideOffset > 0.02) {
                        wasOpen = true;
                    }
                }
            });
        }

        private void setUpProfileImage(final User user) {
            final boolean isGoogle = user.getAuthMethod() != null &&
                    user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD);
            try {
                URL imageURL = new URL(isGoogle ? user.getPictureUrl() + "?sz=500" : "https://graph.facebook.com/" + user.getUserId() + "/picture?height=500");
                Glide.with(context)
                        .load(imageURL)
                        .asBitmap()
                        .transform(new CropCircleTransform(context))
                        .into(profileImage);
            } catch (MalformedURLException e) {
                Log.e(TAG, "malformed url: " + e.getMessage());
            }
        }
    }
}
