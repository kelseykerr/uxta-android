package impulusecontrol.lend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.List;

import impulusecontrol.lend.model.Request;
import impulusecontrol.lend.model.User;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requests;

    public RequestAdapter(List<Request> requests) {
        this.requests = requests;
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

        public RequestViewHolder(Context context, View v) {
            super(v);
            vItemName =  (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView)  v.findViewById(R.id.category_name);
            vPostedDate = (TextView)  v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            this.context = context;
        }
    }
}
