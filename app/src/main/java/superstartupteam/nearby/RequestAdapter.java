package superstartupteam.nearby;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import layout.AccountFragment;
import layout.HomeFragment;
import layout.PaymentDialogFragment;
import layout.UpdateAccountDialogFragment;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.User;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requests;
    public static User user;
    private View view;
    private HomeFragment homeFragment;

    public RequestAdapter(List<Request> requests, HomeFragment homeFragment) {
        this.requests = requests;
        this.homeFragment = homeFragment;
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    @Override
    public void onBindViewHolder(final RequestViewHolder requestViewHolder, int i) {
        Request r = requests.get(i);

        String htmlString = r.getUser().getFirstName() + " would like to " +
                (r.getRental() ? " borrow a " : " buy a ") + "<b>" +
                r.getItemName() + "</b>";
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        String diff = AppUtils.getTimeDiffString(r.getPostDate());
        requestViewHolder.vPostedDate.setText(diff);
        if (r.getCategory() == null) {
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
        } else {
            requestViewHolder.vCategoryName.setText(r.getCategory().getName());
        }
        if (r.getDescription() == null || r.getDescription().length() == 0) {
            requestViewHolder.vDescription.setVisibility(View.GONE);

        } else {
            requestViewHolder.vDescription.setText(r.getDescription());
        }
        requestViewHolder.vMakeOfferButton.setTag(i);
        requestViewHolder.vMakeOfferButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean goodMerchantStatus = user.getCanRespond();
                if (!AppUtils.canAddPayments(user)) {
                    Snackbar snack = Snackbar.make(view.getRootView(), "Please finish filling out your account info",
                            Snackbar.LENGTH_LONG);
                    snack.setAction("update account", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                            FragmentManager fm = ((Activity) requestViewHolder.context).getFragmentManager();
                            AccountFragment.updateAccountDialog.show(fm, "dialog");
                        }
                    });
                    snack.show();
                } else if (user.getStripeManagedAccountId() != null && goodMerchantStatus) {
                    int position = (Integer) v.getTag();
                    Request r = requests.get(position);
                    homeFragment.showDialog(r.getId());
                } else {
                    String title;
                    boolean showAction = false;
                    /*if (user.getMerchantStatus() != null &&
                            user.getMerchantStatus().toString().toLowerCase().equals("pending")) {
                        title = "Your merchant account is pending, please try again later";
                    }*/
                    showAction = true;
                    title = "Please link your bank account to your profile";
                    Snackbar snack = Snackbar.make(view.getRootView(), title,
                            Snackbar.LENGTH_LONG);
                    if (showAction) {
                        snack.setAction("update account", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AccountFragment.paymentDialogFragment = PaymentDialogFragment.newInstance();
                                FragmentManager fm = ((Activity) requestViewHolder.context).getFragmentManager();
                                AccountFragment.paymentDialogFragment.show(fm, "dialog");
                            }
                        });
                    }
                    snack.show();
                }

            }
        });
    }


    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.request_card, viewGroup, false);
        this.view = itemView;
        Context context = viewGroup.getContext();
        this.user = PrefUtils.getCurrentUser(context);
        return new RequestViewHolder(context, itemView);
    }

    public void swap(List<Request> newRequests) {
        requests.clear();
        requests.addAll(newRequests);
        notifyDataSetChanged();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        protected TextView vItemName;
        protected TextView vCategoryName;
        protected TextView vPostedDate;
        protected TextView vDescription;
        protected Context context;
        protected ImageButton vMakeOfferButton;

        public RequestViewHolder(Context context, View v) {
            super(v);
            vItemName = (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView) v.findViewById(R.id.category_name);
            vPostedDate = (TextView) v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            vMakeOfferButton = (ImageButton) v.findViewById(R.id.make_offer_button);
            this.context = context;
        }
    }
}
