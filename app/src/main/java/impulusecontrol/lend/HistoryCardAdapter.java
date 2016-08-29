package impulusecontrol.lend;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import java.util.List;

import impulusecontrol.lend.model.Request;

/**
 * Created by kerrk on 8/28/16.
 */
public class HistoryCardAdapter extends RecyclerView.Adapter<HistoryCardAdapter.HistoryCardViewHolder> {
    private List<Request> requests;

    public HistoryCardAdapter(List<Request> requests) {
        this.requests = requests;
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    @Override
    public void onBindViewHolder(final HistoryCardViewHolder requestViewHolder, int i) {
        Request r = requests.get(i);
        String htmlString = "requested a <b>" +
                r.getItemName() + "</b>";
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        String diff = AppUtils.getTimeDiffString(r.getPostDate());
        requestViewHolder.vPostedDate.setText(diff);
        // description and category will appear when the card is clicked/expanded
        requestViewHolder.vDescription.setVisibility(View.GONE);
        requestViewHolder.vCategoryName.setVisibility(View.GONE);
        if (r.getCategory() == null) {
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
        } else {
            requestViewHolder.vCategoryName.setText(r.getCategory().getName());
        }
        //this click method will expand/close the card
        requestViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestViewHolder.onClick(v);
            }
        });
    }

    @Override
    public HistoryCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.my_history_card, viewGroup, false);
        Context context = viewGroup.getContext();
        return new HistoryCardViewHolder(context, itemView);
    }

    public void swap(List<Request> newRequests){
        requests.clear();
        requests.addAll(newRequests);
        notifyDataSetChanged();
    }



    public static class HistoryCardViewHolder extends RecyclerView.ViewHolder {
        protected TextView vItemName;
        protected TextView vCategoryName;
        protected TextView vPostedDate;
        protected TextView vDescription;
        protected Context context;
        private int mOriginalHeight = 0;
        private boolean isViewExpanded = false;
        protected CardView cardView;

        public HistoryCardViewHolder(Context context, View v) {
            super(v);
            vItemName =  (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView)  v.findViewById(R.id.category_name);
            vPostedDate = (TextView)  v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            cardView = (CardView) itemView.findViewById(R.id.my_history_card_view);

            this.context = context;
        }

        public void onClick(final View v) {
            if (mOriginalHeight == 0) {
                mOriginalHeight = v.getHeight();
            }
            ValueAnimator valueAnimator;
            if (!isViewExpanded) {
                isViewExpanded = true;
                vDescription.setVisibility(View.VISIBLE);
                vCategoryName.setVisibility(View.VISIBLE);
                valueAnimator = ValueAnimator.ofInt(mOriginalHeight, mOriginalHeight + (int) (mOriginalHeight * 1.5));
            } else {
                isViewExpanded = false;
                vDescription.setVisibility(View.GONE);
                vCategoryName.setVisibility(View.GONE);
                valueAnimator = ValueAnimator.ofInt(mOriginalHeight + (int) (mOriginalHeight * 1.5), mOriginalHeight);
            }
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Integer value = (Integer) animation.getAnimatedValue();
                        v.getLayoutParams().height = value.intValue();
                        v.requestLayout();
                    }
                });
                valueAnimator.start();
        }
    }
}
