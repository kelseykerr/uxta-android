package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import iuxta.uxta.AppUtils;
import iuxta.uxta.Constants;
import iuxta.uxta.MainActivity;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.model.History;
import iuxta.uxta.model.Request;
import iuxta.uxta.model.Response;
import iuxta.uxta.model.Transaction;
import iuxta.uxta.model.User;

/**
 * Created by kerrk on 2/26/17.
 */
public class ViewTransactionFragment extends DialogFragment {

    private static History history;
    private static Transaction transaction;
    private static Response response;
    private Context context;
    private ImageView profileImage;
    private TextView transactionText;
    private ImageButton closeBtn;
    private TextView transactionStatus;
    private TextView exchangeTime;
    private TextView exchangeLocation;
    private TextView returnTime;
    private TextView returnLocation;
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy hh:mm a");
    private User user;
    private View view;
    private ViewRequestFragment.OnFragmentInteractionListener mListener;
    private static HistoryFragment historyFragment;
    private FloatingActionButton messageUserBtn;
    private FloatingActionButton exchangeBtn;
    private FloatingActionButton closeTransactionBtn;
    private TextView exchangeFabText;
    private TextView messageUserFabText;
    private TextView closeTransactionFabText;

    public ViewTransactionFragment() {}

    public static ViewTransactionFragment newInstance(History h, HistoryFragment hf, Response resp) {
        ViewTransactionFragment fragment = new ViewTransactionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        history = h;
        historyFragment = hf;
        transaction = h.getTransaction();
        response = resp;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = PrefUtils.getCurrentUser(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // request a window without the title
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        Request request = history.getRequest();
        View view = inflater.inflate(R.layout.fragment_view_transaction, container, false);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);
        Boolean isSeller = false;
        if (!user.getId().equals(request.getUser().getId()) && (request.getType().equals(Request.Type.renting) || request.getType().equals(Request.Type.buying))) {
            isSeller = true;
        } else if (user.getId().equals(request.getUser().getId()) && (request.getType().equals(Request.Type.loaning) || request.getType().equals(Request.Type.selling))) {
            isSeller = true;
        }
        User forProfile = null;
        boolean isNormalRequest = request.getType().equals(Request.Type.renting) || request.getType().equals(Request.Type.buying);
        if (isSeller) {
            forProfile = isNormalRequest ? request.getUser() : response.getResponder();
        } else {
            forProfile = isNormalRequest ? response.getResponder() : request.getUser();
        }
        setProfilePic(forProfile, profileImage);
        transactionText = (TextView) view.findViewById(R.id.transaction_text);
        Boolean complete = request.isRental() ? transaction.getExchanged() && transaction.getReturned() :
                transaction.getExchanged();
        String topDescription;
        String beginning;
        if (complete && (request.getType().equals(Request.Type.renting) || request.getType().equals(Request.Type.loaning))) {
            beginning = !isSeller ? "Borrowed a " : "Loaned a ";
        } else if (complete) {
            beginning = !isSeller ? "Bought a " : "Sold a ";
        } else if (request.getType().equals(Request.Type.renting) || request.getType().equals(Request.Type.loaning)) {
            beginning = !isSeller ? "Borrowing a " : "Loaning a ";
        } else {
            beginning = !isSeller ? "Buying a " : "Selling a ";
        }
        if (!isSeller) {
            if (isNormalRequest) {
                topDescription = beginning + request.getItemName() +
                        " from " + (response.getResponder().getFirstName() != null ?
                        response.getResponder().getFirstName() : response.getResponder().getName());
            } else {
                topDescription = beginning + request.getItemName() +
                        " from " + (request.getUser().getFirstName() != null ?
                        request.getUser().getFirstName() : request.getUser().getName());
            }

        } else {
            if (isNormalRequest) {
                topDescription = beginning + request.getItemName() +
                        " to " + request.getUser().getFirstName();
            } else {
                topDescription = beginning + request.getItemName() +
                        " to " + response.getResponder().getFirstName();
            }
        }
        BigDecimal formattedValue = AppUtils.formatCurrency(response.getOfferPrice());
        topDescription += " for $" + formattedValue;
        transactionText.setText(topDescription);
        transactionStatus = (TextView) view.findViewById(R.id.transaction_status);
        exchangeTime = (TextView) view.findViewById(R.id.exchange_time);
        exchangeLocation = (TextView) view.findViewById(R.id.exchange_location);
        returnTime = (TextView) view.findViewById(R.id.return_time);
        returnLocation = (TextView) view.findViewById(R.id.return_location);
        if (!transaction.getExchanged()) {
            transactionStatus.setText("Awaiting initial exchange");
            String formattedDate = response.getExchangeTime() != null ? formatter.format(response.getExchangeTime()) : null;
            if (formattedDate != null) {
                String exchangeTimeText = "<b>exchange time:</b> " + formattedDate;
                exchangeTime.setText(Html.fromHtml(exchangeTimeText));

            } else {
                exchangeTime.setVisibility(View.GONE);
            }
            if (response.getExchangeLocation() != null && response.getExchangeLocation().length() > 0) {
                String exchangeLocationText = "<b>exchange location:</b> " + response.getExchangeLocation();
                exchangeLocation.setText(Html.fromHtml(exchangeLocationText));
            } else {
                exchangeLocation.setVisibility(View.GONE);
            }
            returnTime.setVisibility(View.GONE);
            returnLocation.setVisibility(View.GONE);
        } else if (!transaction.getReturned() && (request.getType().equals(Request.Type.renting) || request.getType().equals(Request.Type.loaning))) {
            transactionStatus.setText("Awaiting return");
            String formattedDate = transaction.getExchangeTime() != null ? formatter.format(transaction.getExchangeTime()) : null;
            exchangeLocation.setVisibility(View.GONE);
            String exchangedText = "<i>exchanged on " + formattedDate + "</i>";
            exchangeTime.setText(Html.fromHtml(exchangedText));
            String returnDate = response.getReturnTime() != null ? formatter.format(response.getReturnTime()) : null;
            if (returnDate != null) {
                String returnTimeText = "<b>return time:</b> " + returnDate;
                returnTime.setText(Html.fromHtml(returnTimeText));

            } else {
                returnTime.setVisibility(View.GONE);
            }
            if (response.getReturnLocation() != null && response.getReturnLocation().length() > 0) {
                String returnLocationText = "<b>return location:</b> " + response.getReturnLocation();
                returnLocation.setText(Html.fromHtml(returnLocationText));
            } else {
                returnLocation.setVisibility(View.GONE);
            }
        } else {
            String statusText = request.getStatus().toLowerCase().equals("transaction_pending") ? "Transaction Pending" : request.getStatus();
            transactionStatus.setText(statusText);
            exchangeLocation.setVisibility(View.GONE);
            returnLocation.setVisibility(View.GONE);
            String formattedDate = transaction.getExchangeTime() != null ? formatter.format(transaction.getExchangeTime()) : null;
            String exchangedText = "<i>exchanged on " + formattedDate + "</i>";
            exchangeTime.setText(Html.fromHtml(exchangedText));
            String returnedDate = transaction.getReturnTime() != null ? formatter.format(transaction.getExchangeTime()) : null;
            String returnedText = "<i>returned on " + formattedDate + "</i>";
            if (returnedDate != null) {
                returnTime.setText(Html.fromHtml(returnedText));
            } else {
                returnTime.setVisibility(View.GONE);
            }
        }
        exchangeBtn = (FloatingActionButton) view.findViewById(R.id.exchange_fab);
        messageUserBtn = (FloatingActionButton) view.findViewById(R.id.message_user_fab);
        closeTransactionBtn = (FloatingActionButton) view.findViewById(R.id.close_transaction_fab);
        this.view = view;
        setUpFabBtns(request, isSeller, isNormalRequest);
        closeBtn = (ImageButton) view.findViewById(R.id.close_view);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    private void setUpFabBtns(Request request, final boolean isSeller, final boolean isNormalRequest) {
        exchangeFabText = (TextView) view.findViewById(R.id.exchange_text);
        closeTransactionFabText = (TextView) view.findViewById(R.id.close_transaction_text);
        if ((transaction.getExchanged() && !request.isRental()) || transaction.getReturned() ||
                (!request.getStatus().equals("OPEN") && !request.getStatus().equals("TRANSACTION_PENDING"))) {
            exchangeBtn.setVisibility(View.GONE);
            exchangeFabText.setVisibility(View.GONE);
            closeTransactionBtn.setVisibility(View.GONE);
            closeTransactionFabText.setVisibility(View.GONE);
        } else {
            exchangeBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    exchangeBtnClick(isSeller);
                }
            });
            exchangeFabText.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    exchangeBtnClick(isSeller);
                }
            });
            closeTransactionBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    historyFragment.showCancelTransactionDialog(transaction.getId());
                }
            });
            closeTransactionFabText.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    historyFragment.showCancelTransactionDialog(transaction.getId());
                }
            });
        }
        messageUserFabText = (TextView) view.findViewById(R.id.message_user_text);
        messageUserFabText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messageUserClick(isSeller, isNormalRequest);
            }
        });
        messageUserBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messageUserClick(isSeller, isNormalRequest);
            }
        });
    }

    private void messageUserClick(boolean isSeller, boolean isNormalRequest) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        PackageManager packageManager = ((MainActivity)getActivity()).getPackageManager();
        List activities = packageManager.queryIntentActivities(smsIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            smsIntent.setType("vnd.android-dir/mms-sms");
            String phone;
            if (isSeller) {
                if (isNormalRequest) {
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
                    phone = resp.getResponder().getPhone().replace("-", "");
                }
            } else {
                if (isNormalRequest) {
                    Response resp = null;
                    final Transaction transaction = history.getTransaction();
                    for (Response res : history.getResponses()) {
                        if (res.getId().equals(transaction.getResponseId())) {
                            resp = res;
                            break;
                        }
                    }
                    phone = resp.getResponder().getPhone().replace("-", "");
                } else {
                    phone = history.getRequest().getUser().getPhone().replace("-", "");
                }
            }
            smsIntent.putExtra("address", phone);
            smsIntent.putExtra("sms_body","");
            context.startActivity(Intent.createChooser(smsIntent, "SMS:"));
        } else {
            Snackbar snackbar = Snackbar
                    .make(view, "no messaging app found", Constants.LONG_SNACK);
            snackbar.show();
        }
    }

    private void exchangeBtnClick(boolean isSeller) {
        if (isSeller) {
            if (!transaction.getExchanged()) {
                historyFragment.showExchangeCodeDialog(history.getTransaction(), false);
                dismiss();
            } else {
                historyFragment.showScanner(history.getTransaction().getId(), false);
                dismiss();
            }
        } else {
            if (!transaction.getExchanged()) {
                historyFragment.showScanner(history.getTransaction().getId(), true);
                dismiss();
            } else {
                historyFragment.showExchangeCodeDialog(history.getTransaction(), true);
                dismiss();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }

    public void setProfilePic(final User user, final ImageView imageBtn) {
        final boolean googlePic = user.getAuthMethod() != null &&
                user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD) &&
                user.getPictureUrl() != null;
        final boolean facebookPic = user.getAuthMethod() != null &&
                user.getAuthMethod().equals(Constants.FB_AUTH_METHOD);
        if (googlePic || facebookPic) {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    URL imageURL = null;
                    try {
                        imageURL = new URL(googlePic ? user.getPictureUrl() + "?sz=800" : "https://graph.facebook.com/" + user.getUserId() + "/picture?height=800");
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
                    super.onPostExecute(bitmap);
                    profileImage.setImageBitmap(bitmap);
                }
            }.execute();
        }
    }
}
