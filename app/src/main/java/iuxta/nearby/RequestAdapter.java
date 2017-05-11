package iuxta.nearby;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import layout.AccountFragment;
import layout.HomeFragment;
import layout.PaymentDialogFragment;
import layout.UpdateAccountDialogFragment;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.User;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requests;
    public static User user;
    private View view;
    private HomeFragment homeFragment;
    private static final String TAG = "RequestAdapter";

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
        requestViewHolder.setUpProfileImage(r.getUser());
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
        requestViewHolder.card.setTag(i);
        requestViewHolder.card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                makeOffer(v, requestViewHolder);
            }
        });
        requestViewHolder.offerSwipe.setTag(i);
        requestViewHolder.offerSwipe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                makeOffer(v, requestViewHolder);
            }
        });
    }

    public void makeOffer(View v, final RequestViewHolder requestViewHolder) {
        boolean goodMerchantStatus = user.getCanRespond();
        if (!AppUtils.canAddPayments(user)) {
            Snackbar snack = Snackbar.make(view.getRootView(), "Please finish filling out your account info",
                    Constants.LONG_SNACK);
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
            homeFragment.showDialog(r.getId(), r.getRental());
        } else {
            String title;
            boolean showAction = false;
            showAction = true;
            title = "Please link your bank account to your profile";
            Snackbar snack = Snackbar.make(view.getRootView(), title,
                    Constants.LONG_SNACK);
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
        private TextView vItemName;
        private TextView vCategoryName;
        private TextView vPostedDate;
        private TextView vDescription;
        protected Context context;
        private ImageView profileImage;
        private RelativeLayout card;
        private LinearLayout offerSwipe;
        private SwipeRevealLayout swipeLayout;

        public RequestViewHolder(Context context, View v) {
            super(v);
            vItemName = (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView) v.findViewById(R.id.category_name);
            vPostedDate = (TextView) v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            profileImage = (ImageView) v.findViewById(R.id.profile_image);
            card = (RelativeLayout) v.findViewById(R.id.request_card);
            offerSwipe = (LinearLayout) v.findViewById(R.id.offer_layout);
            swipeLayout = (SwipeRevealLayout) v.findViewById(R.id.swipe_layout);
            swipeLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
                boolean wasOpen = false;
                @Override
                public void onClosed(SwipeRevealLayout view) {
                    if (!wasOpen) {
                        card.performClick();
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
            this.context = context;
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
