package iuxta.uxta;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import layout.AccountFragment;
import layout.CommunitySearchFragment;
import layout.HomeFragment;
import layout.PaymentDialogFragment;
import layout.RequestPreviewFragment;
import layout.UpdateAccountDialogFragment;
import iuxta.uxta.model.Request;
import iuxta.uxta.model.User;

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
        final Request r = requests.get(i);
        requestViewHolder.setUpProfileImage(r.getUser());
        String htmlString = "";
        if (r.getType().equals(Request.Type.loaning)) {
            htmlString = r.getRequesterName() + " has a " + r.getItemName() + " available to rent";
        } else if (r.getType().equals(Request.Type.selling)) {
            htmlString = r.getRequesterName() + " is selling a " + r.getItemName();
        } else {
            htmlString = r.getRequesterName() + " would like to " +
                    (r.isRental() ? " borrow a " : " buy a ") + "<b>" +
                    r.getItemName() + "</b>";
        }
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
        requestViewHolder.card.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                DialogFragment newFragment = RequestPreviewFragment.newInstance(r, homeFragment);
                newFragment.show(((Activity) requestViewHolder.context).getFragmentManager(), "dialog");
                return true;
               // makeOffer(r, requestViewHolder);
            }
        });
        requestViewHolder.offerSwipe.setTag(i);
        if (r.getType().equals(Request.Type.loaning)) {
            requestViewHolder.respondText.setText("request");
        } else {
            requestViewHolder.respondText.setText("respond");
        }
        View.OnClickListener offerClick = new View.OnClickListener() {
            public void onClick(View v) {
                makeOffer(r, requestViewHolder);
            }
        };
        requestViewHolder.offerSwipe.setOnClickListener(offerClick);
        requestViewHolder.offerBtn.setOnClickListener(offerClick);
        View.OnClickListener flagClick = new View.OnClickListener() {
            public void onClick(View v) {
                flagRequest(r);
            }
        };
        requestViewHolder.flagSwipe.setOnClickListener(flagClick);
        requestViewHolder.flagBtn.setOnClickListener(flagClick);
    }

    public void flagRequest(Request request) {
        homeFragment.showReportDialog(request);
    }

    public void makeOffer(Request r, final RequestViewHolder requestViewHolder) {
        if (user.getCommunityId() != null || !user.getCommunityId().isEmpty()) {
            homeFragment.showNewOfferDialog(r.getId(), r.getType().toString());
        } else {
            String title;
            boolean showAction = true;
            title = "You must join a community";
            Snackbar snack = Snackbar.make(view.getRootView(), title,
                    Constants.LONG_SNACK);
            if (showAction) {
                snack.setAction("find community", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AccountFragment.communitySearchFragment = CommunitySearchFragment.newInstance(null);
                        FragmentManager fm = ((Activity) requestViewHolder.context).getFragmentManager();
                        AccountFragment.communitySearchFragment.show(fm, "dialog");
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
        private FrameLayout offerSwipe;
        private ImageButton offerBtn;
        private FrameLayout flagSwipe;
        private ImageButton flagBtn;
        private SwipeRevealLayout swipeLayout;
        private TextView respondText;

        public RequestViewHolder(Context context, View v) {
            super(v);
            vItemName = (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView) v.findViewById(R.id.category_name);
            vPostedDate = (TextView) v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            profileImage = (ImageView) v.findViewById(R.id.profile_image);
            card = (RelativeLayout) v.findViewById(R.id.request_card);
            offerSwipe = (FrameLayout) v.findViewById(R.id.offer_swipe);
            offerBtn = (ImageButton) v.findViewById(R.id.offer_btn);
            flagSwipe = (FrameLayout) v.findViewById(R.id.flag_swipe);
            flagBtn = (ImageButton) v.findViewById(R.id.flag_btn);
            respondText = (TextView) v.findViewById(R.id.respond_text);
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
