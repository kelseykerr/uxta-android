package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.Transaction;
import superstartupteam.nearby.model.User;

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
        final boolean isSeller = !user.getId().equals(history.getRequest().getUser().getId());
        setProfilePic(isSeller ? history.getRequest().getUser() : response.getSeller(), profileImage);
        transactionText = (TextView) view.findViewById(R.id.transaction_text);
        Boolean complete = request.getRental() ? transaction.getExchanged() && transaction.getReturned() :
                transaction.getExchanged();
        String topDescription;
        String beginning;
        if (complete && request.getRental()) {
            beginning = !isSeller ? "Borrowed a " : "Loaned a ";
        } else if (complete) {
            beginning = !isSeller ? "Bought a " : "Sold a ";
        } else if (request.getRental()) {
            beginning = !isSeller ? "Borrowing a " : "Loaning a ";
        } else {
            beginning = !isSeller ? "Buying a " : "Selling a ";
        }
        if (!isSeller) {
            topDescription = beginning + request.getItemName() +
                    " from " + (response.getSeller().getFirstName() != null ?
                    response.getSeller().getFirstName() : response.getSeller().getName());
        } else {
            topDescription = beginning + request.getItemName() +
                    " to " + request.getUser().getFirstName();
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
        } else if (!transaction.getReturned() && request.getRental()) {
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
        setUpFabBtns(request, isSeller);
        closeBtn = (ImageButton) view.findViewById(R.id.close_view);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    private void setUpFabBtns(Request request, final boolean isSeller) {
        exchangeFabText = (TextView) view.findViewById(R.id.exchange_text);
        closeTransactionFabText = (TextView) view.findViewById(R.id.close_transaction_text);
        if ((transaction.getExchanged() && !request.getRental()) || transaction.getReturned() ||
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
                messageUserClick(isSeller);
            }
        });
        messageUserBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messageUserClick(isSeller);
            }
        });
    }

    private void messageUserClick(boolean isSeller) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        String phone;
        if (isSeller) {
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
            phone = resp.getSeller().getPhone().replace("-", "");
        }
        smsIntent.putExtra("address", phone);
        smsIntent.putExtra("sms_body","");
        context.startActivity(Intent.createChooser(smsIntent, "SMS:"));
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
