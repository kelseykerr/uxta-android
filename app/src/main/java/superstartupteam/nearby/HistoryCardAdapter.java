package superstartupteam.nearby;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        ViewGroup header = (ViewGroup) mInflater.inflate(R.layout.header_footer, requestViewHolder.responseList, false);
        requestViewHolder.responseList.addHeaderView(header, null, false);
        if (h.getTransaction() != null && !r.getStatus().toLowerCase().equals("closed")) {
            setUpTransactionCard(requestViewHolder, r, h);
        } else if (user.getId().equals(r.getUser().getId())) { // this is a request the user made
           setUpRequestCard(requestViewHolder, r, h);
        } else { //this is an offer the user made
            setUpOfferCard(requestViewHolder, r, h);
        }
        if (!requestViewHolder.showConfirmChargeIcon && !requestViewHolder.showEditIcon &&
                !requestViewHolder.showCancelTransactionIcon && !requestViewHolder.showExchangeIcon
                && !requestViewHolder.showMessageUserIcon) {
            requestViewHolder.menuBtn.setVisibility(View.GONE);
        } else {
            requestViewHolder.menuBtn.setVisibility(View.VISIBLE);
        }
        requestViewHolder.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(requestViewHolder.menuBtn, h, requestViewHolder);
            }
        });
        List<String> values = new ArrayList();
        for (Response resp:h.getResponses()) {
            if (resp == null || resp.getSeller() == null) {
                return;
            }
            BigDecimal price = AppUtils.formatCurrency(resp.getOfferPrice());
            String htmlString = "<font color='#767474'>" + resp.getSeller().getFirstName() +
                    " made an offer for $" + price +
                    "<br/>" + resp.getResponseStatus().toString() + "</font>";
            values.add(htmlString);
        }
        RequestResponseCardAdapter adapter = new RequestResponseCardAdapter(context, 0,
                h.getResponses(), r, historyFragment);
        requestViewHolder.responseList.setAdapter(adapter);
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

    private void setUpOfferCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        requestViewHolder.showExchangeIcon = false;
        if (r.getUser().getPhone() != null) {
            requestViewHolder.showMessageUserIcon = true;
        }
        requestViewHolder.vTransactionStatus.setVisibility(View.GONE);
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.mCardBackground.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
        requestViewHolder.showEditIcon = false;
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
        setResponseStatusColor(requestViewHolder.vStatus, resp.getResponseStatus().toString());
        if (resp.getResponseStatus().equals(Response.Status.CLOSED)) {
            requestViewHolder.showEditIcon = false;
        } else {
            requestViewHolder.showEditIcon = true;
        }
    }

    private void setUpRequestCard(final HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.mCardBackground.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
        requestViewHolder.showExchangeIcon = false;
        requestViewHolder.showMessageUserIcon = false;
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
            requestViewHolder.showEditIcon = false;
        } else {
            requestViewHolder.showEditIcon = true;
            int count = 0;
            for (Response resp: h.getResponses()) {
                if (resp.getResponseStatus().toString().toLowerCase().equals("pending")) {
                    count++;
                }
            }
            if (count > 0) {
                String ooString="<u>" + count + " pending" + (count > 1 ? " offers</u>" : " offer</u>");
                requestViewHolder.vOpenOffers.setText(Html.fromHtml(ooString));
            }
        }
        if (h.getResponses() != null && h.getResponses().size() > 0 &&
                !r.getStatus().toString().equalsIgnoreCase("closed")) {
            requestViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestViewHolder.dropdownExpanded = !requestViewHolder.dropdownExpanded;
                    if (requestViewHolder.dropdownExpanded) {
                        /*requestViewHolder.responseSeparator.setVisibility(View.VISIBLE);*/
                        requestViewHolder.responseList.setVisibility(View.VISIBLE);
                        justifyListViewHeightBasedOnChildren(requestViewHolder.responseList);
                    } else {
                        requestViewHolder.responseSeparator.setVisibility(View.GONE);
                        requestViewHolder.responseList.setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    private void setUpTransactionCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        boolean isBuyer = user.getId().equals(r.getUser().getId());
        final boolean isSeller = !isBuyer;
        requestViewHolder.mCardBackground.setBackground(context.getResources().getDrawable(R.drawable.card_border_left));
        requestViewHolder.showEditIcon = false;
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
            requestViewHolder.showExchangeIcon = true;
            if (isSeller && r.getUser().getPhone() != null) {
                requestViewHolder.showMessageUserIcon = true;
            }
            if (isBuyer && resp.getSeller().getPhone() != null) {
                requestViewHolder.showMessageUserIcon = true;
            }
            String formattedDate = resp.getExchangeTime() != null ? formatter.format(resp.getExchangeTime()) : null;
            String exchangeTime = "<b>exchange time:</b> " + formattedDate;
            requestViewHolder.vCategoryName.setText(Html.fromHtml(exchangeTime));
            String exchangeLocation = "<b>exchange location:</b> " + resp.getExchangeLocation();
            requestViewHolder.vDescription.setText(Html.fromHtml(exchangeLocation));
            requestViewHolder.vTransactionStatus.setText("Awaiting initial exchange");
            requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            Transaction.ExchangeOverride exchangeOverride = transaction.getExchangeOverride();
            requestViewHolder.showCancelTransactionIcon = true;
            if (exchangeOverride != null && !exchangeOverride.buyerAccepted && !exchangeOverride.declined) {
                requestViewHolder.showExchangeIcon = false;
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
            requestViewHolder.showExchangeIcon = true;
            if (isSeller && r.getUser().getPhone() != null) {
                requestViewHolder.showMessageUserIcon = true;
            }
            if (isBuyer && resp.getSeller().getPhone() != null) {
                requestViewHolder.showMessageUserIcon = true;
            }
            String formatedDate = resp.getReturnTime() != null ? formatter.format(resp.getReturnTime()) : null;
            String returnTime = "<b>return time:</b> " + formatedDate;
            requestViewHolder.vCategoryName.setText(Html.fromHtml(returnTime));
            String returnLocation = "<b>return location:</b> " + resp.getReturnLocation();
            requestViewHolder.vDescription.setText(Html.fromHtml(returnLocation));
            requestViewHolder.vTransactionStatus.setText("Awaiting return");
            requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
            Transaction.ExchangeOverride returnOverride = transaction.getReturnOverride();
            if (returnOverride != null && !returnOverride.sellerAccepted && !returnOverride.declined) {
                requestViewHolder.showExchangeIcon = false;
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
            if (isSeller && r.getUser().getPhone() != null) {
                requestViewHolder.showMessageUserIcon = true;
            }
            if (isBuyer && resp.getSeller().getPhone() != null) {
                requestViewHolder.showMessageUserIcon = true;
            }
            BigDecimal formattedValue = AppUtils.formatCurrency(transaction.getCalculatedPrice());
            String calculatedPrice = "<b>calculated price:</b> " + formattedValue;
            requestViewHolder.vCategoryName.setText(Html.fromHtml(calculatedPrice));
            requestViewHolder.vDescription.setVisibility(View.GONE);
            if (isBuyer) {
                requestViewHolder.vTransactionStatus.setText("Processing Payment");
                requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
                requestViewHolder.showExchangeIcon = false;
            } else {
                requestViewHolder.showExchangeIcon = false;
                final String description = "For " + (r.getRental() ? "loaning " : "selling ") + "your " +
                        r.getItemName() + " to " + r.getUser().getFirstName();
                historyFragment.showConfirmChargeDialog(transaction.getCalculatedPrice(),
                        description, transaction.getId());
                requestViewHolder.vTransactionStatus.setText("CONFIRM CHARGE!");
                requestViewHolder.vTransactionStatus.setTextColor(ContextCompat.getColor(context, R.color.redPink));
                requestViewHolder.vTransactionStatus.setVisibility(View.VISIBLE);
                requestViewHolder.showEditIcon = false;
                requestViewHolder.showConfirmChargeIcon = true;
            }
        } else {
            requestViewHolder.mCardBackground.setBackground(context.getResources().getDrawable(R.drawable.request_card_background));
            requestViewHolder.showExchangeIcon = false;
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
    }

    private void showPopupMenu(View view, History h, HistoryCardViewHolder rvh) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.history_card_menu, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        if (!rvh.showExchangeIcon) {
            popupMenu.removeItem(R.id.exchange_icon);
        }
        if (!rvh.showCancelTransactionIcon) {
            popupMenu.removeItem(R.id.cancel_transaction_button);
        }
        if (!rvh.showEditIcon) {
            popupMenu.removeItem(R.id.edit_button);
        }
        if (!rvh.showConfirmChargeIcon) {
            popupMenu.removeItem(R.id.confirm_charge);
        }
        if (!rvh.showMessageUserIcon) {
            popupMenu.removeItem(R.id.message_user);
        }
        popup.setOnMenuItemClickListener(new MenuClickListener(h));
        popup.show();
    }

    class MenuClickListener implements PopupMenu.OnMenuItemClickListener {
        private History history;
        private Transaction transaction;
        public MenuClickListener(History h) {
            history = h;
            transaction = h.getTransaction();
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            boolean isSeller = !user.getId().equals(history.getRequest().getUser().getId());
            switch (menuItem.getItemId()) {
                case R.id.cancel_transaction_button:
                    historyFragment.showCancelTransactionDialog(transaction.getId());
                    return true;
                case R.id.edit_button:
                    if (isSeller) {
                        historyFragment.showResponseDialog(history.getResponses().get(0));
                    } else {
                        historyFragment.showRequestDialog(history);
                    }
                    return true;
                case R.id.exchange_icon:
                    if (isSeller) {
                        if (!transaction.getExchanged()) {
                            historyFragment.showExchangeCodeDialog(history.getTransaction(), false);
                        } else {
                            historyFragment.showScanner(history.getTransaction().getId(), false);
                        }
                    } else {
                        if (!transaction.getExchanged()) {
                            historyFragment.showScanner(history.getTransaction().getId(), true);
                        } else {
                            historyFragment.showExchangeCodeDialog(history.getTransaction(), true);
                        }
                    }
                    return true;
                case R.id.confirm_charge:
                    Request r = history.getRequest();
                    String description = "For " + (r.getRental() ? "loaning " : "selling ") + "your " +
                            r.getItemName() + " to " + r.getUser().getFirstName();
                    historyFragment.showConfirmChargeDialog(transaction.getCalculatedPrice(),
                            description, transaction.getId());
                case R.id.message_user:
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    String phone;
                    if (isSeller) {
                        phone = history.getRequest().getUser().getPhone().replace("-", "");
                    } else {
                        Response resp = null;
                        final Transaction transaction = history.getTransaction();
                        for (Response res : history.getResponses()) {
                            if (res.getId().equals(transaction.getResponseId())) {
                                resp = res;
                                break;
                            }
                        }
                        phone = resp.getSeller().getPhone().replace("-", "");
                    }
                    smsIntent.putExtra("address", phone);
                    smsIntent.putExtra("sms_body","");
                    context.startActivity(Intent.createChooser(smsIntent, "SMS:"));
                default:
            }
            return false;
        }
    }


    public static class HistoryCardViewHolder extends ParentViewHolder {
        private TextView vItemName;
        private TextView vCategoryName;
        private TextView vPostedDate;
        private TextView vDescription;
        private Context context;
        private TextView vStatus;
        private TextView vOpenOffers;
        private RelativeLayout mCardBackground;
        private CardView historyCard;
        private ImageView menuBtn;
        private boolean showExchangeIcon = false;
        private boolean showCancelTransactionIcon = false;
        private boolean showEditIcon = true;
        private boolean showConfirmChargeIcon = false;
        private boolean dropdownExpanded = false;
        private boolean showMessageUserIcon = false;
        private ListView responseList;
        private FrameLayout cardView;
        private LinearLayout responseSeparator;
        private TextView vTransactionStatus;
        private ImageButton profileImage;

        public HistoryCardViewHolder(Context context, View v) {
            super(v);
            vItemName = (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView) v.findViewById(R.id.category_name);
            vPostedDate = (TextView) v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            cardView = (FrameLayout) itemView.findViewById(R.id.my_history_card_view);
            vStatus = (TextView) v.findViewById(R.id.history_card_status);
            vOpenOffers = (TextView) v.findViewById(R.id.open_offers);
            this.context = context;
            mCardBackground = (RelativeLayout) itemView.findViewById(R.id.card_layout);
            historyCard = (CardView) v.findViewById(R.id.my_history_card_view);
            historyCard.setMaxCardElevation(7);
            menuBtn = (ImageView) v.findViewById(R.id.card_menu);
            responseList = (ListView) v.findViewById(R.id.response_list);
            responseSeparator = (LinearLayout) v.findViewById(R.id.response_separator);
            vTransactionStatus = (TextView) v.findViewById(R.id.transaction_status);
            profileImage = (ImageButton) v.findViewById(R.id.profileImage);
        }

        private void setUpProfileImage(final User user) {
            final boolean isGoogle = user.getAuthMethod() != null &&
                    user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD);
            new AsyncTask<Void, Void, Void>() {
                Bitmap bitmap;
                @Override
                protected Void doInBackground(Void... params) {
                    URL imageURL = null;
                    try {
                        imageURL = new URL(isGoogle ? user.getPictureUrl() + "?sz=100" : "https://graph.facebook.com/" + user.getUserId() + "/picture?width=100");
                        bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
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

        }
    }
}
