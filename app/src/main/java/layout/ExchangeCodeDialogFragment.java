package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 9/27/16.
 */
public class ExchangeCodeDialogFragment extends DialogFragment {

    private Context context;
    private ExchangeCodeDialogFragment.OnFragmentInteractionListener mListener;
    private String transactionId;
    private String exchangeCode;
    private User user;
    private View view;
    private ImageButton qrCodeView;
    private ProgressBar loadingSpinner;
    private Bitmap bitmap;
    private Button switchCodeViewBtn;
    private Button refreshCodeBtn;
    private TextView exchangeCodeView;
    private Boolean loading = false;
    private String heading;
    private TextView forgotBtn;
    private boolean forgot = false;

    public static ExchangeCodeDialogFragment newInstance(String transactionId, String heading) {
        ExchangeCodeDialogFragment fragment = new ExchangeCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString("TRANSACTION_ID", transactionId);
        args.putString("HEADING", heading);
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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionId = getArguments().getString("TRANSACTION_ID");
            heading = getArguments().getString("HEADING");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_exchange_dialog, container, false);
        TextView dialogHeader = (TextView) view.findViewById(R.id.confirm_exchange_text);
        dialogHeader.setText(heading);
        qrCodeView = (ImageButton) view.findViewById(R.id.qr_code);
        loadingSpinner = (ProgressBar) view.findViewById(R.id.loading_spinner);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_confirm_request);
        exchangeCodeView = (TextView) view.findViewById(R.id.exchange_code);

        switchCodeViewBtn = (Button) view.findViewById(R.id.show_code_button);
        switchCodeViewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchCodeView();
            }
        });
        refreshCodeBtn = (Button) view.findViewById(R.id.refresh_code_button);
        refreshCodeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!loading) {
                    getExchangeCode(transactionId);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity ma = (MainActivity) getActivity();
                ma.goToHistory(null);
                dismiss();
            }
        });

        forgotBtn = (TextView) view.findViewById(R.id.forgot_scan_btn);

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryFragment callback = (HistoryFragment) getTargetFragment();
                callback.showExchangeOverrideDialog(transactionId, heading, "If you forgot to " +
                        "submit the exchange when it happened, enter the time the exchange occurred and " +
                        "the other user will be asked to verify the exchange happened.");
            }
        });

        this.view = view;
        getExchangeCode(transactionId);
        return view;

    }

    private void switchCodeView() {
        if (qrCodeView.getVisibility() == View.VISIBLE) {
            qrCodeView.setVisibility(View.GONE);
            switchCodeViewBtn.setText("Show QR Code");
            exchangeCodeView.setText(exchangeCode);
            exchangeCodeView.setVisibility(View.VISIBLE);
        } else {
            switchCodeViewBtn.setText("Show Code");
            exchangeCodeView.setVisibility(View.GONE);
            qrCodeView.setVisibility(View.VISIBLE);
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 350, 350, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                Integer black = ContextCompat.getColor(context, R.color.black);
                Integer white = ContextCompat.getColor(context, R.color.white);
                pixels[offset + x] = result.get(x, y) ? black : white;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 350, 0, 0, w, h);
        return bitmap;
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
        if (context instanceof ExchangeCodeDialogFragment.OnFragmentInteractionListener) {
            mListener = (ExchangeCodeDialogFragment.OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }


    public void getExchangeCode(final String transactionId) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                loadingSpinner.setVisibility(View.VISIBLE);
                qrCodeView.setVisibility(View.GONE);
                refreshCodeBtn.setEnabled(false);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/transactions/"
                            + transactionId + "/code");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    String output = AppUtils.getResponseContent(conn);
                    exchangeCode = output;
                    try {
                        Bitmap bitmap = encodeAsBitmap(exchangeCode);
                        return bitmap;
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get exchange code: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                bitmap = b;
                qrCodeView.setImageBitmap(b);
                if (!forgot) {
                    qrCodeView.setVisibility(View.VISIBLE);
                }
                loadingSpinner.setVisibility(View.GONE);
                refreshCodeBtn.setEnabled(true);

            }
        }.execute();
    }
}
