package iuxta.nearby.model;

import android.view.View;

import java.util.List;

/**
 * Created by kerrk on 9/13/16.
 */
public interface AsyncResponse {
    View processFinish(List<History> history);
}
