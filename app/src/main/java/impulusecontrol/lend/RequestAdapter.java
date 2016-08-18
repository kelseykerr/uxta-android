package impulusecontrol.lend;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kerrk on 8/7/16.
 */
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
        requestViewHolder.vItemName.setText(r.getItemName());
        requestViewHolder.vPostedDate.setText(r.getPostDate().toString());
        requestViewHolder.vCategoryName.setText(r.getCategory().getName());
        requestViewHolder.vDescription.setText(r.getDescription());
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.request_card, viewGroup, false);

        return new RequestViewHolder(itemView);
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        protected TextView vItemName;
        protected TextView vCategoryName;
        protected TextView vPostedDate;
        protected TextView vDescription;

        public RequestViewHolder(View v) {
            super(v);
            vItemName =  (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView)  v.findViewById(R.id.category_name);
            vPostedDate = (TextView)  v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
        }
    }
}
