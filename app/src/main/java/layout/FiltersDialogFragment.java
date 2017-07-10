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
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iuxta.uxta.Constants;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.model.User;

/**
 * Created by kerrk on 11/23/16.
 */

public class FiltersDialogFragment extends DialogFragment {
    private User user;
    private Context context;
    private View view;
    private String sortBy;
    private Boolean sellLoan;
    private Boolean rentBuy;
    private Spinner sortBySpinner;
    private FiltersDialogFragment.OnFragmentInteractionListener mListener;
    private Map<Double, String> radiusMap = new HashMap<Double, String>();
    private SwitchCompat sellingLoaning;
    private SwitchCompat buyingRenting;


    public static FiltersDialogFragment newInstance() {
        FiltersDialogFragment fragment = new FiltersDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_filters_dialog, container, false);
        sortBy = HomeFragment.sortBy;
        sellLoan = HomeFragment.sellingLoaning;
        rentBuy = HomeFragment.buyingRenting;

        sortBySpinner = (Spinner) view.findViewById(R.id.sort_by_spinner);
        final ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.sortByItems));
        sortByAdapter.setDropDownViewResource(R.layout.spinner_item);
        sortBySpinner.setAdapter(sortByAdapter);
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                sortBy = selectedItem;
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // set sort by spinner to current sort by
        if (sortBy != null && !sortBy.isEmpty()) {
            String [] sortItems = getResources().getStringArray(R.array.sortByItems);
            for (int i = 0; i < sortItems.length; i++) {
                if (sortItems[i].equals(sortBy)) {
                    sortBySpinner.setSelection(i+1);
                    break;
                }
            }
        } else {
            sortBySpinner.setSelection(1);
        }

        sellingLoaning = (SwitchCompat) view.findViewById(R.id.selling_loaning);
        if (sellLoan != null) {
            sellingLoaning.setChecked(sellLoan);
        } else {
            sellLoan = true;
            sellingLoaning.setChecked(true);
        }
        buyingRenting = (SwitchCompat) view.findViewById(R.id.buying_renting);
        if (rentBuy != null) {
            buyingRenting.setChecked(rentBuy);
        } else {
            rentBuy = true;
            buyingRenting.setChecked(true);
        }

        sellingLoaning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    buyingRenting.setChecked(true);
                }
            }
        });
        buyingRenting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    sellingLoaning.setChecked(true);
                }
            }
        });

        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.close_filters);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        Button submitBtn = (Button) view.findViewById(R.id.filter_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HomeFragment.sortBy = sortBy;
                HomeFragment.sellingLoaning = sellingLoaning.isChecked();
                HomeFragment.buyingRenting = buyingRenting.isChecked();
                String nextFragment = " ";
                Uri url = null;
                mListener.onFragmentInteraction(url, nextFragment, Constants.FPPR_SUBMIT_FILTERS);
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
        if (context instanceof FiltersDialogFragment.OnFragmentInteractionListener) {
            mListener = (FiltersDialogFragment.OnFragmentInteractionListener) context;
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
        mListener = (FiltersDialogFragment.OnFragmentInteractionListener) activity;

    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }


    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
