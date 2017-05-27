package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.CropCircleTransform;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.User;

/**
 * Created by kelseykerr on 5/27/17.
 */

public class RequestPreviewFragment extends DialogFragment {
    private Request request;
    private User user;
    private Context context;
    private NewOfferDialogFragment.OnFragmentInteractionListener mListener;
    ScrollView scrollView;
    private Spinner rentalSpinner;
    private Button createOfferBtn;
    private View view;
    private EditText itemName;
    private EditText description;
    private TextInputLayout descriptionLayout;
    private static HomeFragment homeFragment;
    ImageView requesterImage;
    TextView requesterName;
    private static final String TAG = "RequestPreviewFragment";

    public RequestPreviewFragment() {
        // Required empty public constructor
    }

    public static RequestPreviewFragment newInstance(Request request, HomeFragment homeFragment) {
        RequestPreviewFragment.homeFragment = homeFragment;
        RequestPreviewFragment fragment = new RequestPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable("REQUEST", request);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        if (getArguments() != null) {
            request = (Request) getArguments().getSerializable("REQUEST");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.request_preview_dialog, container, false);
        scrollView = (ScrollView) view.findViewById(R.id.scrollview);
        requesterImage = (ImageView) view.findViewById(R.id.requester_image);
        setUpProfileImage(request.getUser(), requesterImage);
        requesterName = (TextView) view.findViewById(R.id.requester_name);
        requesterName.setText(request.getUser().getFirstName());
        rentalSpinner = (Spinner) view.findViewById(R.id.rental_spinner);
        ArrayAdapter<String> rentBuyAdapter;
        List<String> rentBuyList = new ArrayList<>();
        rentBuyList.add("rent");
        rentBuyList.add("buy");
        rentBuyAdapter = new ArrayAdapter<String>(context, R.layout.simple_spinner_item, rentBuyList);
        rentBuyAdapter.setDropDownViewResource(R.layout.spinner_item);
        rentalSpinner.setAdapter(rentBuyAdapter);
        if (request.getRental()) {
            rentalSpinner.setSelection(0);
        } else {
            rentalSpinner.setSelection(1);
        }
        rentalSpinner.setEnabled(false);

        createOfferBtn = (Button) view.findViewById(R.id.create_offer_button);
        createOfferBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createOfferBtn.setEnabled(false);
                createOffer();
            }
        });
        itemName = (EditText) view.findViewById(R.id.item_name);
        itemName.setText(request.getItemName());
        itemName.setEnabled(false);
        description = (EditText) view.findViewById(R.id.request_description);
        if (request.getDescription() == null || request.getDescription().length() == 0) {
            descriptionLayout = (TextInputLayout) view.findViewById(R.id.request_description_layout);
            descriptionLayout.setVisibility(View.GONE);
        } else {
            description.setText(request.getDescription());
            description.setEnabled(false);
        }
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.close_detail);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        this.view = view;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            //TODO: This doesn't work!!
            dialog.getWindow().setWindowAnimations(R.style.RequestDialog);
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof NewOfferDialogFragment.OnFragmentInteractionListener) {
            mListener = (NewOfferDialogFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void createOffer() {
        boolean goodMerchantStatus = user.getCanRespond();
        if (!AppUtils.canAddPayments(user)) {
            Snackbar snack = Snackbar.make(view.getRootView(), "Please finish filling out your account info",
                    Constants.LONG_SNACK);
            snack.setAction("update account", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                    FragmentManager fm = ((Activity) context).getFragmentManager();
                    AccountFragment.updateAccountDialog.show(fm, "dialog");
                }
            });
            snack.show();
            createOfferBtn.setEnabled(true);
        } else if (user.getStripeManagedAccountId() != null && goodMerchantStatus) {
            homeFragment.showNewOfferDialog(request.getId(), request.getRental());
            dismiss();
        } else {
            String title;
            boolean showAction = true;
            title = "Please link your bank account to your profile";
            Snackbar snack = Snackbar.make(view.getRootView(), title,
                    Constants.LONG_SNACK);
            if (showAction) {
                snack.setAction("update account", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AccountFragment.paymentDialogFragment = PaymentDialogFragment.newInstance();
                        FragmentManager fm = ((Activity) context).getFragmentManager();
                        AccountFragment.paymentDialogFragment.show(fm, "dialog");
                    }
                });
            }
            snack.show();
            createOfferBtn.setEnabled(true);
        }

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


}
