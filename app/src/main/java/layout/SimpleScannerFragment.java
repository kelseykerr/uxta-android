package layout;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import superstartupteam.nearby.ScannerActivity;

/**
 * Created by kerrk on 9/30/16.
 */
public class SimpleScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private String transactionId;

    public static SimpleScannerFragment newInstance(String transactionId) {
        SimpleScannerFragment fragment = new SimpleScannerFragment();
        Bundle args = new Bundle();
        args.putString("TRANSACTION_ID", transactionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            transactionId = getArguments().getString("TRANSACTION_ID");
        }
        mScannerView = new ZXingScannerView(getActivity());
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String code = rawResult.getText();
        Log.i("**", "Scanned code was: " + code);
        mScannerView.stopCamera();
        ((ScannerActivity)getActivity()).verifyCode(code);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri, String nextFragment);
    }
}
