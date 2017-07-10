package iuxta.uxta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import iuxta.uxta.model.Request;
import iuxta.uxta.model.Response;
import iuxta.uxta.model.User;
import layout.HistoryFragment;

/**
 * Created by kerrk on 11/8/16.
 */
public class RequestResponseCardAdapter extends RecyclerView.Adapter<RequestResponseCardAdapter.ResponseCardViewHolder> {

    private List<Response> responses;
    private Request request;
    private HistoryFragment historyFragment;
    private User user;
    private Context context;
    private LayoutInflater mInflater;
    private static final String TAG = "RequestResponseCardAdap";

    public RequestResponseCardAdapter(Context context, List<Response> responses, Request request, HistoryFragment hf) {
        this.historyFragment = hf;
        this.responses = responses;
        this.request = request;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }

    public void onBindViewHolder(final RequestResponseCardAdapter.ResponseCardViewHolder rvh, int i) {
        final Response response = responses.get(i);
        setUpProfileImage(response.getResponder(), rvh.profilePic);
        if (response == null || response.getResponder() == null) {
            return;
        }
        final BigDecimal price = AppUtils.formatCurrency(response.getOfferPrice());
        String htmlString = "";
        if (response.getIsOfferToBuyOrRent()) {
            htmlString = "<font color='#767474'>" + response.getResponderName() +
                    " requested to " + (request.getType().equals(Request.Type.selling) ? "buy " : "rent ") +
                    "for $" + price + "</font>";
        } else {
            htmlString = "<font color='#767474'>" + response.getResponderName() +
                    " made an offer for $" + price + "</font>";
        }
        rvh.offerText.setText(Html.fromHtml(htmlString));
        if (response.isClosed()) {
            String statusString = "CLOSED";
            if (response.getCanceledReason() != null && response.getCanceledReason().length() > 0) {
                statusString += (" - " + response.getCanceledReason());
            }
            rvh.responseStatus.setText(statusString);
            rvh.editBtn.setVisibility(View.GONE);
            rvh.rejectBtn.setVisibility(View.GONE);
            rvh.acceptBtn.setVisibility(View.GONE);
        } else {
              if (response.getSellerStatus().equals(Response.SellerStatus.ACCEPTED)) {
                  if (request.isInventoryListing()) {
                      rvh.responseStatus.setText("PENDING BUYER ACCEPTANCE");
                  } else {
                      rvh.responseStatus.setText("OPEN");
                  }
            } else if (response.getBuyerStatus().equals(Response.BuyerStatus.ACCEPTED)) {
                  if (request.isInventoryListing()) {
                      rvh.responseStatus.setText("OPEN");
                  } else {
                      rvh.responseStatus.setText("PENDING SELLER ACCEPTANCE");
                  }
            } else {
                rvh.responseStatus.setText(response.getResponseStatus().toString());
            }
            rvh.editBtn.setEnabled(true);
            rvh.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(response);
                }
            });
            rvh.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    historyFragment.showResponseDialog(response);
                }
            });
            rvh.rejectBtn.setEnabled(true);
            setUpRejectBtn(rvh, response, price);
            rvh.acceptBtn.setEnabled(true);
            setUpAcceptBtn(rvh, response, price);
        }
        rvh.flagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyFragment.showReportDialog(request, response);
            }
        });
        String diff = AppUtils.getTimeDiffString(response.getResponseTime());
        rvh.postedDate.setText(diff);

        String formatedExchangeTime = response.getExchangeTime() != null ? Constants.DATE_FORMATTER.format(response.getExchangeTime()) : null;
        if (formatedExchangeTime != null) {
            String exchangeTime = "<b>exchange time:</b> " + formatedExchangeTime;
            rvh.exchangeTime.setText(Html.fromHtml(exchangeTime));
            rvh.exchangeTime.setVisibility(View.VISIBLE);
        } else {
            rvh.exchangeTime.setVisibility(View.GONE);
        }
        if (response.getReturnLocation() != null && response.getReturnLocation().length() > 0) {
            String exchangeLocation = "<b>exchange location:</b> " + response.getReturnLocation();
            rvh.exchangeLocation.setText(Html.fromHtml(exchangeLocation));
            rvh.exchangeLocation.setVisibility(View.VISIBLE);
        } else {
            rvh.exchangeLocation.setVisibility(View.GONE);
        }
        String formatedReturnTime = response.getReturnTime() != null ? Constants.DATE_FORMATTER.format(response.getReturnTime()) : null;
        if (formatedReturnTime != null) {
            String returnTime = "<b>return time:</b> " + formatedReturnTime;
            rvh.returnTime.setText(Html.fromHtml(returnTime));
            rvh.returnTime.setVisibility(View.VISIBLE);
        } else {
            rvh.returnTime.setVisibility(View.GONE);
        }
        if (response.getReturnLocation() != null && response.getReturnLocation().length() > 0) {
            String returnLocation = "<b>return location:</b> " + response.getReturnLocation();
            rvh.returnLocation.setText(Html.fromHtml(returnLocation));
            rvh.returnLocation.setVisibility(View.VISIBLE);
        } else {
            rvh.returnLocation.setVisibility(View.GONE);
        }
        if (response.getMessagesEnabled() != null && response.getMessagesEnabled()) {
            setUpMessageBtn(rvh, response);
        } else {
            rvh.messageUserBtn.setEnabled(false);
            rvh.messageUserBtn.setColorFilter(ContextCompat.getColor(context,R.color.mutedGrey));
        }
    }

    private void setUpMessageBtn(final RequestResponseCardAdapter.ResponseCardViewHolder rvh,
                                 final Response response) {
        rvh.messageUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                PackageManager packageManager = ((MainActivity)historyFragment.getActivity()).getPackageManager();
                List activities = packageManager.queryIntentActivities(smsIntent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;
                if (!isIntentSafe) {
                    Snackbar snackbar = Snackbar
                            .make(view, "no messaging app found", Constants.LONG_SNACK);
                    snackbar.show();
                } else {
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    String phone;
                    phone = response.getResponder().getPhone().replace("-", "");
                    smsIntent.putExtra("address", phone);
                    smsIntent.putExtra("sms_body", "");
                    context.startActivity(Intent.createChooser(smsIntent, "SMS:"));
                }
            }
        });
    }

    private void setUpRejectBtn(final RequestResponseCardAdapter.ResponseCardViewHolder rvh,
                                final Response response, final BigDecimal price) {
        rvh.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                String message = "Are you sure you want to reject " +
                        response.getResponder().getFirstName() + "'s offer for $" + price + "?";
                AlertDialog ad = dialog.setMessage(message)
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                dialoginterface.cancel();
                            }
                        })
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                if (request.isInventoryListing()) {
                                    response.setSellerStatus(Response.SellerStatus.WITHDRAWN);
                                } else {
                                    response.setBuyerStatus(Response.BuyerStatus.DECLINED);
                                }
                                historyFragment.updateOffer(response, request, null, "view_request", historyFragment);
                            }
                        })
                        .create();
                ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                ad.show();

            }
        });
    }

    private void setUpAcceptBtn(final RequestResponseCardAdapter.ResponseCardViewHolder rvh,
                                final Response response, final BigDecimal price) {
        rvh.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                boolean accepted = request.isInventoryListing() ? response.getSellerStatus().equals(Response.SellerStatus.ACCEPTED) :
                        response.getBuyerStatus().equals(Response.BuyerStatus.ACCEPTED);
                if (accepted) {
                    String message = "You already accepted " +
                            response.getResponder().getFirstName() + "'s offer for $" + price + ".";
                    AlertDialog ad = dialog.setMessage(message)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    dialoginterface.cancel();
                                }
                            })
                            .create();
                    ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    ad.show();
                } else {
                    String message = "Are you sure you want to accept " +
                            response.getResponder().getFirstName() + "'s offer for $" + price + "?";
                    AlertDialog ad = dialog.setMessage(message)
                            .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    dialoginterface.cancel();
                                }
                            })
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    historyFragment.updateOffer(response, request, dialoginterface, "view_transaction", historyFragment);
                                }
                            })
                            .create();
                    ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    ad.show();
                }
            }
        });

    }

    @Override
    public RequestResponseCardAdapter.ResponseCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        user = PrefUtils.getCurrentUser(viewGroup.getContext());
        Context context = viewGroup.getContext();
        this.context = context;
        mInflater = LayoutInflater.from(viewGroup.getContext());
        View view = mInflater.inflate(R.layout.request_responses, viewGroup, false);

        return new RequestResponseCardAdapter.ResponseCardViewHolder(context, view);
    }

    public void setUpProfileImage(final User user, final ImageView imageBtn) {
        final boolean isGoogle = user.getAuthMethod() != null &&
                user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD);
        try {
            URL imageURL = new URL(isGoogle ? user.getPictureUrl() + "?sz=500" : "https://graph.facebook.com/" + user.getUserId() + "/picture?width=500");
            Glide.with(context)
                    .load(imageURL)
                    .asBitmap()
                    .transform(new CropCircleTransform(context))
                    .into(imageBtn);
        } catch (MalformedURLException e) {
            Log.e(TAG, "malformed url: " + e.getMessage());
        }
    }

    public static void processBitmap(Bitmap bitmap, ImageButton imageButton) {
        imageButton.setImageBitmap(bitmap);
    }


    public static class ResponseCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePic;
        private TextView offerText;
        private TextView responseStatus;
        private TextView postedDate;
        private TextView exchangeTime;
        private TextView exchangeLocation;
        private TextView returnTime;
        private TextView returnLocation;
        private Context context;
        private ImageButton editBtn;
        private ImageButton acceptBtn;
        private ImageButton rejectBtn;
        private ImageButton messageUserBtn;
        private ImageButton flagBtn;
        private CardView cardView;

        public ResponseCardViewHolder(Context context, View v) {
            super(v);
            this.context = context;
            cardView = (CardView) v.findViewById(R.id.my_history_card_view);
            profilePic = (ImageView) v.findViewById(R.id.profile_image);
            offerText = (TextView) v.findViewById(R.id.offer_text);
            responseStatus = (TextView) v.findViewById(R.id.response_status);
            postedDate = (TextView) v.findViewById(R.id.posted_date);
            editBtn = (ImageButton) v.findViewById(R.id.edit_btn);
            acceptBtn = (ImageButton) v.findViewById(R.id.accept_btn);
            rejectBtn = (ImageButton) v.findViewById(R.id.reject_btn);
            messageUserBtn = (ImageButton) v.findViewById(R.id.message_user_btn);
            flagBtn = (ImageButton) v.findViewById(R.id.flag_btn);
            exchangeTime = (TextView) v.findViewById(R.id.exchange_time);
            exchangeLocation = (TextView) v.findViewById(R.id.exchange_location);
            returnTime = (TextView) v.findViewById(R.id.return_time);
            returnLocation = (TextView) v.findViewById(R.id.return_location);
        }
    }

}
