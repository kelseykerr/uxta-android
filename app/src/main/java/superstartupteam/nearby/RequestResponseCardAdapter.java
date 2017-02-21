package superstartupteam.nearby;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import layout.HistoryFragment;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 11/8/16.
 */

public class RequestResponseCardAdapter extends ArrayAdapter<Response> {

    private List<Response> responses;
    private Request request;
    private HistoryFragment historyFragment;
    private User user;
    private Context context;

    public RequestResponseCardAdapter(Context context, int resource, List<Response> responses,
                                      Request request, HistoryFragment historyFragment) {
        super(context, resource, responses);
        this.responses = responses;
        this.request = request;
        this.historyFragment = historyFragment;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.request_responses, null);
        } else {
            v = convertView;
        }
        final Response response = responses.get(position);
        ImageButton profileImage = (ImageButton) v.findViewById(R.id.profileImage);
        setUpProfileImage(response.getSeller(), profileImage);
        TextView offerText = (TextView) v.findViewById(R.id.offer_text);
        if (response == null || response.getSeller() == null) {
            return v;
        }
        BigDecimal price = AppUtils.formatCurrency(response.getOfferPrice());
        String htmlString = "<font color='#767474'>" + response.getSeller().getFirstName() +
                " made an offer for $" + price + "</font>";
        offerText.setText(Html.fromHtml(htmlString));
        TextView responseStatus = (TextView) v.findViewById(R.id.response_status);
        if (response.getSellerStatus().equals(Response.SellerStatus.ACCEPTED)) {
            responseStatus.setText("OPEN");
        } else if (response.getBuyerStatus().equals(Response.BuyerStatus.ACCEPTED)) {
            responseStatus.setText("PENDING SELLER ACCEPTANCE");
        } else {
            responseStatus.setText(response.getResponseStatus().toString());
        }
        ImageButton msgUser = (ImageButton) v.findViewById(R.id.message_user_button);
        final String phone = response.getSeller().getPhone();
        if (phone == null || phone.isEmpty() || response.getResponseStatus().toString().equalsIgnoreCase("closed")) {
            msgUser.setVisibility(View.GONE);
        } else {
            msgUser.setVisibility(View.VISIBLE);
            msgUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", phone);
                    smsIntent.putExtra("sms_body","");
                    context.startActivity(Intent.createChooser(smsIntent, "SMS:"));
                }
            });
        }
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.response_card);
        if (!response.getResponseStatus().toString().toLowerCase().equals("closed")
                && !request.getStatus().toLowerCase().equals("closed")) {
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(response);
                }
            });
        }
        return v;

    }

    private void setUpProfileImage(final User user, final ImageButton imageBtn) {
        final boolean isGoogle = user.getAuthMethod() != null &&
                user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD);
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                URL imageURL = null;
                try {
                    imageURL = new URL(isGoogle ? user.getPictureUrl() + "?sz=80" : "https://graph.facebook.com/" + user.getUserId() + "/picture?width=80");
                    Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    return bitmap;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                processBitmap(bitmap, imageBtn);
                super.onPostExecute(bitmap);
            }
        }.execute();

    }

    public void processBitmap(Bitmap bitmap, ImageButton imageButton) {
        imageButton.setImageBitmap(bitmap);
    }

}
