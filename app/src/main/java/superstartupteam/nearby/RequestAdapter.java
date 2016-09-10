package superstartupteam.nearby;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import layout.HomeFragment;
import layout.NewOfferDialogFragment;
import superstartupteam.nearby.model.Request;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requests;

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
    public void onBindViewHolder(RequestViewHolder requestViewHolder, int i) {
        Request r = requests.get(i);
        String htmlString = r.getUser().getFirstName() + " requested a <b>" +
                r.getItemName() + "</b>";
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        String diff = AppUtils.getTimeDiffString(r.getPostDate());
        requestViewHolder.vPostedDate.setText(diff);
        if (r.getCategory() == null) {
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
        } else {
            requestViewHolder.vCategoryName.setText(r.getCategory().getName());
        }
        requestViewHolder.vDescription.setText(r.getDescription());
        requestViewHolder.vMakeOfferButton.setTag(i);
        requestViewHolder.vMakeOfferButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int position=(Integer)v.getTag();
                Request r = requests.get(position);
                homeFragment.showDialog(r.getId());
            }
        });
    }



    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.request_card, viewGroup, false);
        Context context = viewGroup.getContext();
        return new RequestViewHolder(context, itemView);
    }

    public void swap(List<Request> newRequests){
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
        protected TextView vMakeOfferButton;

        public RequestViewHolder(Context context, View v) {
            super(v);
            vItemName =  (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView)  v.findViewById(R.id.category_name);
            vPostedDate = (TextView)  v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            vMakeOfferButton = (TextView) v.findViewById(R.id.make_offer_button);
            this.context = context;
        }
    }
}
