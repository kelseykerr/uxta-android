package iuxta.nearby;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by kelseykerr on 6/6/17.
 */

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        ImageView fullScreenImageView = (ImageView) findViewById(R.id.full_screen_image);
        Intent callingActivityIntent = getIntent();
        if(callingActivityIntent != null) {
            Uri imageUri = callingActivityIntent.getData();
            if(imageUri != null && fullScreenImageView != null) {
                Glide.with(this)
                        .load(imageUri)
                        .into(fullScreenImageView);
            }
        }
    }
}
