package layout;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import superstartupteam.nearby.R;
import superstartupteam.nearby.model.Response;

/**
 * Created by kerrk on 9/16/16.
 */
public class ViewOfferDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private static Response response;
    private HistoryFragment.OnFragmentInteractionListener mListener;
    private Context context;
    private EditText offerPrice;
    private Spinner offerType;
    private String exchangeLocation;
    private String returnLocation;
    private Button updateRequestBtn;
    private Button rejectRequestBtn;
    private ImageButton backButton;


    public ViewOfferDialogFragment() {

    }

    public static ViewOfferDialogFragment newInstance(Response r) {
        response = r;
        ViewOfferDialogFragment fragment = new ViewOfferDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_offer_dialog, container, false);
        offerPrice = (EditText) view.findViewById(R.id.response_offer_price);
        offerPrice.setText(response.getOfferPrice().toString());
        offerType = (Spinner) view.findViewById(R.id.offer_type);
        if (response.getPriceType().toLowerCase().equals("flat")) {
            offerType.setSelection(0);
        } else if (response.getPriceType().toLowerCase().equals("per_hour")) {
            offerType.setSelection(1);
        } else {
            offerType.setSelection(2);
        }
        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        updateRequestBtn = (Button) view.findViewById(R.id.accept_offer_button);
        updateRequestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        rejectRequestBtn = (Button) view.findViewById(R.id.reject_offer_button);
        rejectRequestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        return view;
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
        this.context = context;
        super.onAttach(context);
        if (context instanceof HistoryFragment.OnFragmentInteractionListener) {
            mListener = (HistoryFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a radius from the spinner
        String value = (String) parent.getItemAtPosition(position);

    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
