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
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
        if (h.hasOpenTransaction()) {
            setUpTransactionCard(requestViewHolder, r, h);
        } else if (r.isMyRequest(user)) { // this is a request the user made
            if (r.getType().equals(Request.Type.loaning) && r.getDuplicate()) {
                setUpInventoryOfferCard(requestViewHolder, r, h);
            } else {
                setUpRequestCard(requestViewHolder, r, h);
            }
        } else { //this is an offer the user made
            setUpOfferCard(requestViewHolder, r, h);
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
        String htmlString = "Offered a " + r.getItemName() + " to " + r.getRequesterName();
        if (r.getType() != null && r.getType().equals(Request.Type.renting)) {
            htmlString = "Offered to loan a " + r.getItemName() + " to " + r.getRequesterName();
        } else if (r.getType() != null && r.getType().equals(Request.Type.buying)) {
            htmlString = "Offered to sell a " + r.getItemName() + " to " + r.getRequesterName();
        } else if (r.getType() != null && r.getType().equals(Request.Type.selling)) {
            htmlString = "Requested to buy a " + r.getItemName() + " from " + r.getRequesterName();
        } else if (r.getType() != null && r.getType().equals(Request.Type.loaning)) {
            htmlString = "Requested to borrow a " + r.getItemName() + " from " + r.getRequesterName();
        }
        requestViewHolder.setUpProfileImage(r.getUser());
        String diff = AppUtils.getTimeDiffString(resp.getResponseTime());
        requestViewHolder.vFirstRowText.setText(Html.fromHtml(htmlString));
        requestViewHolder.vPostedDate.setText(diff);
        if (!r.isInventoryListing() && resp.getSellerStatus().equals(Response.SellerStatus.OFFERED)) {
            requestViewHolder.vSecondRowText.setText("Buyer updated offer, awaiting your approval");
            requestViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
        } else if (r.isInventoryListing() && resp.getBuyerStatus().equals(Response.BuyerStatus.OPEN)) {
            requestViewHolder.vSecondRowText.setText("Seller updated offer, awaiting your approval");
            requestViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
        } else {
            requestViewHolder.vSecondRowText.setText("");
            requestViewHolder.vSecondRowText.setVisibility(View.GONE);
        }
        requestViewHolder.vDescription.setText("");
        requestViewHolder.vDescription.setVisibility(View.GONE);
        requestViewHolder.vStatus.setText(resp.getResponseStatus().toString().toUpperCase());
        if (resp.isClosed()) {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
            requestViewHolder.moreSwipe.setVisibility(View.GONE);

        } else {
            requestViewHolder.closeSwipe.setVisibility(View.VISIBLE);
            View.OnClickListener closeClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeOffer(resp, r);
                }
            };
            requestViewHolder.closeSwipe.setOnClickListener(closeClick);
            requestViewHolder.closeSwipeBtn.setOnClickListener(closeClick);
            requestViewHolder.moreSwipe.setVisibility(View.VISIBLE);
            requestViewHolder.moreSwipeText.setText("edit");
            requestViewHolder.moreSwipeBtn.setImageResource(R.drawable.ic_mode_edit_black_24dp);
            requestViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(resp);
                }
            });
            requestViewHolder.moreSwipeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(resp);
                }
            });
        }
        setResponseStatusColor(requestViewHolder.vStatus, resp.getResponseStatus().toString());
        requestViewHolder.historyCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!resp.getResponseStatus().equals(Response.Status.CLOSED)) {
                    historyFragment.showResponseDialog(resp);
                }
                return true;
            }
        });

    }

    private void setUpRequestCard(final HistoryCardViewHolder requestViewHolder, final Request r, final History h) {
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
        String htmlString = "Requested a " +
                r.getItemName();
        if (r.getType() != null && r.getType().equals(Request.Type.renting)) {
            htmlString = "Requested to rent a " +
                    r.getItemName();
        } else if (r.getType() != null && r.getType().equals(Request.Type.buying)) {
            htmlString = "Requested to buy a " +
                    r.getItemName();
        } else if (r.getType() != null && r.getType().equals(Request.Type.selling)) {
            htmlString = "Selling a " +
                    r.getItemName();
        } else if (r.getType() != null && r.getType().equals(Request.Type.loaning)) {
            htmlString = "Offering to loan out a " +
                    r.getItemName();
        }
        requestViewHolder.vFirstRowText.setText(Html.fromHtml(htmlString));
        String diff = AppUtils.getTimeDiffString(r.getPostDate());
        requestViewHolder.vPostedDate.setText(diff);
        if (r.getCategory() != null) {
            requestViewHolder.vSecondRowText.setText(r.getCategory().getName());
        } else {
            requestViewHolder.vSecondRowText.setVisibility(View.GONE);
        }

        if (AppUtils.validateField(r.getDescription())) {
            requestViewHolder.vDescription.setText(r.getDescription());
        } else {
            requestViewHolder.vDescription.setVisibility(View.GONE);
        }
        requestViewHolder.vStatus.setText(r.getStatus().toUpperCase());
        setRequestStatusColor(requestViewHolder.vStatus, r.getStatus());
        requestViewHolder.setUpProfileImage(user);

        if (r.isOpen()) {
            requestViewHolder.closeSwipe.setVisibility(View.VISIBLE);
            View.OnClickListener closeClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeRequest(r);
                }
            };
            requestViewHolder.closeSwipe.setOnClickListener(closeClick);
            requestViewHolder.closeSwipeBtn.setOnClickListener(closeClick);
            int count = 0;
            for (Response resp : h.getResponses()) {
                if (resp.isPending()) {
                    count++;
                }
            }
            if (count > 0) {
                String ooString = "<u>" + count + " pending" + (count > 1 ? " offers</u>" : " offer</u>");
                requestViewHolder.vOpenOffers.setText(Html.fromHtml(ooString));
            }
        } else {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
        }
        requestViewHolder.historyCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                historyFragment.showRequestDialog(h);
                return true;
            }
        });
        requestViewHolder.moreSwipe.setVisibility(View.VISIBLE);
        requestViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showRequestDialog(h);
            }
        });
        requestViewHolder.moreSwipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showRequestDialog(h);
            }
        });

    }

    private void setUpInventoryOfferCard(HistoryCardViewHolder historyViewHolder, final Request r, final History h) {
        historyViewHolder.vPostedDate.setVisibility(View.GONE);
        final Response resp = h.getResponses().get(0);
        historyViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
        BigDecimal formattedValue = AppUtils.formatCurrency(resp.getOfferPrice());
        String htmlString = resp.getResponderName() + " requested to borrow your " +
                r.getItemName() + " for $" + formattedValue;
        historyViewHolder.vFirstRowText.setText(Html.fromHtml(htmlString));

        historyViewHolder.setUpProfileImage(resp.getResponder());
        String diff = AppUtils.getTimeDiffString(resp.getResponseTime());
        historyViewHolder.vFirstRowText.setText(Html.fromHtml(htmlString));
        historyViewHolder.vPostedDate.setText(diff);
        if (resp.getSellerStatus().equals(Response.SellerStatus.OFFERED)) {
            historyViewHolder.vSecondRowText.setText("Awaiting your approval");
            historyViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
        } else if (resp.getBuyerStatus().equals(Response.BuyerStatus.OPEN)){
            historyViewHolder.vSecondRowText.setText("Awaiting buyer approval");
            historyViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
        } else {
            historyViewHolder.vSecondRowText.setText("");
            historyViewHolder.vSecondRowText.setVisibility(View.GONE);
        }
        historyViewHolder.vDescription.setText("");
        historyViewHolder.vDescription.setVisibility(View.GONE);
        historyViewHolder.vStatus.setText(resp.getResponseStatus().toString().toUpperCase());
        if (resp.isClosed()) {
            historyViewHolder.closeSwipe.setVisibility(View.GONE);
            historyViewHolder.moreSwipe.setVisibility(View.GONE);

        } else {
            historyViewHolder.closeSwipe.setVisibility(View.VISIBLE);
            View.OnClickListener closeClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeOffer(resp, r);
                }
            };
            historyViewHolder.closeSwipe.setOnClickListener(closeClick);
            historyViewHolder.closeSwipeBtn.setOnClickListener(closeClick);
            historyViewHolder.moreSwipe.setVisibility(View.VISIBLE);
            historyViewHolder.moreSwipeText.setText("edit");
            historyViewHolder.moreSwipeBtn.setImageResource(R.drawable.ic_mode_edit_black_24dp);
            historyViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(resp);
                }
            });
            historyViewHolder.moreSwipeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(resp);
                }
            });
        }
        setResponseStatusColor(historyViewHolder.vStatus, resp.getResponseStatus().toString());
        historyViewHolder.historyCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!resp.getResponseStatus().equals(Response.Status.CLOSED)) {
                    historyFragment.showResponseDialog(resp);
                }
                return true;
            }
        });
    }

    private void setUpTransactionCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        boolean isBuyer = false;
        if (r.isMyRequest(user) && !r.isInventoryListing()) {
            isBuyer = true;
        } else if (!r.isMyRequest(user) && r.isInventoryListing()) {
            isBuyer = true;
        }
        final boolean isSeller = !isBuyer;
        requestViewHolder.historyCard.setBackground(context.getResources().getDrawable(R.drawable.card_border_left));
        requestViewHolder.vPostedDate.setVisibility(View.GONE);
        requestViewHolder.vStatus.setVisibility(View.GONE);
        requestViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
        requestViewHolder.vDescription.setVisibility(View.VISIBLE);
        final Response resp = h.getAcceptedOffer();
        final Transaction transaction = h.getTransaction();
        if (h.isTransactionComplete()) {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
            requestViewHolder.exchangeSwipe.setVisibility(View.GONE);
            requestViewHolder.vTransactionStatus.setVisibility(View.GONE);
        } else {
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
            requestViewHolder.exchangeSwipe.setVisibility(View.VISIBLE);
            handleExchangeClick(requestViewHolder, transaction, isSeller);
            requestViewHolder.closeSwipe.setVisibility(View.GONE);
        }
        if (!r.isInventoryListing()) {
            requestViewHolder.setUpProfileImage(isSeller ? r.getUser() : resp.getResponder());
        } else {
            requestViewHolder.setUpProfileImage(isSeller ? resp.getResponder() : r.getUser());
        }
        requestViewHolder.vFirstRowText.setText(getTransactionDescription(h));
        if (!transaction.getExchanged()) {
            String formattedDate = resp.getExchangeTime() != null ? Constants.DATE_FORMATTER.format(resp.getExchangeTime()) : "";
            String exchangeTime = "<b>exchange time:</b> " + formattedDate;
            if (resp.getExchangeTime() != null) {
                requestViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
                requestViewHolder.vSecondRowText.setText(Html.fromHtml(exchangeTime));
            } else {
                requestViewHolder.vSecondRowText.setVisibility(View.GONE);
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
                handleOverride(h, r, resp, true);
            }
        } else if (!transaction.getReturned() && (r.getType().equals(Request.Type.renting) || r.getType().equals(Request.Type.loaning))) {
            String formatedDate = resp.getReturnTime() != null ? Constants.DATE_FORMATTER.format(resp.getReturnTime()) : "";
            String returnTime = "<b>return time:</b> " + formatedDate;
            if (resp.getReturnTime() != null) {
                requestViewHolder.vSecondRowText.setVisibility(View.VISIBLE);
                requestViewHolder.vSecondRowText.setText(Html.fromHtml(returnTime));
            } else {
                requestViewHolder.vSecondRowText.setVisibility(View.GONE);
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
                handleOverride(h, r, resp, false);
            }
        } else if (r.getStatus().equals("TRANSACTION_PENDING")) {
            BigDecimal formattedValue = AppUtils.formatCurrency(transaction.getCalculatedPrice());
            String calculatedPrice = "<b>calculated price:</b> " + formattedValue;
            requestViewHolder.vSecondRowText.setText(Html.fromHtml(calculatedPrice));
            requestViewHolder.vDescription.setVisibility(View.GONE);
            if (isBuyer) {
                requestViewHolder.vTransactionStatus.setText("Processing Payment");
                requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            } else {
                String name = r.isInventoryListing() ? resp.getResponderName() : r.getRequesterName();
                final String description = "For " + (r.isRental() ? "loaning " : "selling ") + "your " +
                        r.getItemName() + " to " + name;
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
            if (r.isRental()) {
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
            requestViewHolder.vSecondRowText.setVisibility(View.GONE);
            requestViewHolder.vStatus.setVisibility(View.VISIBLE);
            requestViewHolder.vStatus.setText(r.getStatus().toUpperCase());
            requestViewHolder.vStatus.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_blue));
        }
        requestViewHolder.historyCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                historyFragment.showTransactionDialog(h, resp);
                return true;
            }
        });
        requestViewHolder.moreSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showTransactionDialog(h, resp);
            }
        });
        requestViewHolder.moreSwipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showTransactionDialog(h, resp);
            }
        });
    }

    private void handleExchangeClick(HistoryCardViewHolder requestViewHolder, final Transaction transaction, final boolean isSeller) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
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
        };
        requestViewHolder.exchangeSwipeBtn.setOnClickListener(onClickListener);
        requestViewHolder.exchangeSwipe.setOnClickListener(onClickListener);
    }

    private void handleOverride(History history, Request request, Response response, boolean isExchange) {
        Transaction transaction = history.getTransaction();
        boolean isMyRequest = request.isMyRequest(user);
        boolean showInitialDialog = (request.isInventoryListing() && !isMyRequest) || (!request.isInventoryListing() && isMyRequest);
        if (isExchange && showInitialDialog) {
            String name = request.isInventoryListing() ? request.getRequesterName() : response.getResponderName();
            String description = name +
                    " submitted an exchange override. Did you exchange the " +
                    request.getItemName() + " at the time above?";
            String time = Constants.DATE_FORMATTER.format(transaction.getExchangeOverride().time);
            historyFragment.showConfirmExchangeOverrideDialog(time, description, transaction.getId(), false);
        } else if (!isExchange && !showInitialDialog) {
            String name = request.isInventoryListing() ? response.getResponderName() : request.getRequesterName();
            String description = name +
                    " submitted a return override. Was the " +
                    request.getItemName() + " returned at the time above?";
            String time = Constants.DATE_FORMATTER.format(transaction.getReturnOverride().time);
            historyFragment.showConfirmExchangeOverrideDialog(time, description, transaction.getId(), true);
        }


    }

    private String getTransactionDescription(History history) {
        Request request = history.getRequest();
        String description = "";
        boolean isBuyer = false;
        if (request.isMyRequest(user) && !request.isInventoryListing()) {
            isBuyer = true;
        } else if (!request.isMyRequest(user) && request.isInventoryListing()) {
            isBuyer = true;
        }
        if (history.isTransactionComplete()) {
            if (request.isRental()) {
                description = isBuyer ? "Borrowed a " : "Loaned a ";
            } else {
                description = isBuyer ? "Bought a " : "Sold a ";
            }
        } else {
            if (request.isRental()) {
                description = isBuyer ? "Borrowing a " : "Loaning a ";
            } else {
                description = isBuyer ? "Buying a " : "Selling a ";
            }
        }
        description += request.getItemName();
        if (isBuyer) {
            if (!request.isInventoryListing()) {
                Response resp = history.getAcceptedOffer();
                description += " from " + resp.getResponderName();
            } else {
                description += " from " + request.getRequesterName();
            }
        } else {
            if (!request.isInventoryListing()) {
                description += " to " + request.getRequesterName();
            } else {
                Response resp = history.getAcceptedOffer();
                description += " to " + resp.getResponderName();
            }
        }

        return description;
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
                        if (request.isInventoryListing()) {
                            response.setBuyerStatus(Response.BuyerStatus.DECLINED);
                            historyFragment.updateOffer(response, request, null, null, null);
                        } else {
                            response.setSellerStatus(Response.SellerStatus.WITHDRAWN);
                            historyFragment.updateOffer(response, request, null, null, null);
                        }
                    }
                })
                .create();
        ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        ad.show();
    }


    public static class HistoryCardViewHolder extends RecyclerView.ViewHolder {
        private TextView vFirstRowText;
        private TextView vSecondRowText;
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
        private FrameLayout moreSwipe;
        private TextView moreSwipeText;
        private ImageButton moreSwipeBtn;
        private FrameLayout closeSwipe;
        private TextView closeSwipeText;
        private ImageButton closeSwipeBtn;
        private FrameLayout exchangeSwipe;
        private TextView exchangeSwipeText;
        private ImageButton exchangeSwipeBtn;
        private SwipeRevealLayout swipeLayout;

        public HistoryCardViewHolder(Context context, View v) {
            super(v);
            vFirstRowText = (TextView) v.findViewById(R.id.first_row_text);
            vSecondRowText = (TextView) v.findViewById(R.id.second_row_text);
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
            moreSwipe = (FrameLayout) v.findViewById(R.id.more_swipe);
            moreSwipeText = (TextView) v.findViewById(R.id.more_swipe_text);
            moreSwipeBtn = (ImageButton) v.findViewById(R.id.more_swipe_btn);
            closeSwipe = (FrameLayout) v.findViewById(R.id.close_swipe);
            closeSwipeText = (TextView) v.findViewById(R.id.close_swipe_text);
            closeSwipeBtn = (ImageButton) v.findViewById(R.id.close_swipe_btn);
            exchangeSwipe = (FrameLayout) v.findViewById(R.id.exchange_swipe);
            exchangeSwipeText = (TextView) v.findViewById(R.id.exchange_swipe_text);
            exchangeSwipeBtn = (ImageButton) v.findViewById(R.id.exchange_swipe_btn);
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
