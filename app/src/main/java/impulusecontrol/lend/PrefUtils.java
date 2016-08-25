package impulusecontrol.lend;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import impulusecontrol.lend.model.User;

/**
 * Created by kerrk on 7/17/16.
 */
public class PrefUtils {
    public static LatLng latLng;

    public static void setCurrentUser(User currentUser, Context ctx){
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "user_prefs", 0);
        complexPreferences.putObject("current_user_value", currentUser);
        complexPreferences.commit();
    }

    public static User getCurrentUser(Context ctx){
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "user_prefs", 0);
        User currentUser = complexPreferences.getObject("current_user_value", User.class);
        return currentUser;
    }

    public static void clearCurrentUser(Context ctx){
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "user_prefs", 0);
        complexPreferences.clearObject();
        complexPreferences.commit();
    }

    public static void setLatLng(LatLng newLatLng) {
        latLng = newLatLng;
    }

    public static LatLng getLatLng() {
        return latLng;
    }
}
