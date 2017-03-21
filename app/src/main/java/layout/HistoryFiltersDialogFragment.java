package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import iuxta.nearby.Constants;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 3/12/17.
 */

public class HistoryFiltersDialogFragment extends DialogFragment {
    private User user;
    private Context context;
    private View view;
    private CheckBox transactions;
    private AppCompatCheckBox requests;
    private AppCompatCheckBox offers;
    private AppCompatCheckBox statusOpen;
    private AppCompatCheckBox statusClosed;
    private HistoryFiltersDialogFragment.OnFragmentInteractionListener mListener;

    public static HistoryFiltersDialogFragment newInstance() {
        HistoryFiltersDialogFragment fragment = new HistoryFiltersDialogFragment();
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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_filters_dialog, container, false);
        transactions = (AppCompatCheckBox) view.findViewById(R.id.transactions_box);
        requests = (AppCompatCheckBox) view.findViewById(R.id.requests_box);
        offers = (AppCompatCheckBox) view.findViewById(R.id.offers_box);
        statusOpen = (AppCompatCheckBox) view.findViewById(R.id.open_box);
        statusClosed = (AppCompatCheckBox) view.findViewById(R.id.closed_box);
        transactions.setChecked(HistoryFragment.showTransactions);
        requests.setChecked(HistoryFragment.showRequests);
        offers.setChecked(HistoryFragment.showOffers);
        statusOpen.setChecked(HistoryFragment.showStatusOpen);
        statusClosed.setChecked(HistoryFragment.showStatusClosed);
        Button submitBtn = (Button) view.findViewById(R.id.filter_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HistoryFragment.showTransactions = transactions.isChecked();
                HistoryFragment.showRequests = requests.isChecked();
                HistoryFragment.showOffers = offers.isChecked();
                HistoryFragment.showStatusOpen = statusOpen.isChecked();
                HistoryFragment.showStatusClosed = statusClosed.isChecked();
                String nextFragment = " ";
                Uri url = null;
                mListener.onFragmentInteraction(url, nextFragment, Constants.FPPR_HISTORY_FILTERS);
                dismiss();
            }
        });
        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.close_filters);
        closeBtn.setOnClickListener(new View.OnClickListener() {
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
        if (context instanceof HistoryFiltersDialogFragment.OnFragmentInteractionListener) {
            mListener = (HistoryFiltersDialogFragment.OnFragmentInteractionListener) context;
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
        mListener = (HistoryFiltersDialogFragment.OnFragmentInteractionListener) activity;
    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

}
