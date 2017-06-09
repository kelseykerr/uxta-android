package iuxta.nearby;

import java.text.SimpleDateFormat;

/**
 * Created by kerrk on 8/24/16.
 */
public class Constants {
    public static final String HOME_FRAGMENT_TAG = "home";

    public static final String ACCOUNT_FRAGMENT_TAG = "account";

    public static final String UPDATE_ACCOUNT_FRAGMENT_TAG = "updateAccount";

    public static final String HISTORY_FRAGMENT_TAG = "history";

    //prod
    //public static final String NEARBY_API_PATH = "https://server.thenearbyapp.com/api";

    //alpha
    //public static final String NEARBY_API_PATH = "https://alpha-server.thenearbyapp.com/api";

    //local
    public static final String NEARBY_API_PATH = "http://192.168.0.173:8080/api";

    public static final String SELECT_CATEGORY_STRING = "select a category";

    public static final String AUTH_HEADER = "x-auth-token";

    public static final String METHOD_HEADER = "x-auth-method";

    public static final String IP_HEADER = "x-auth-ip";

    public static final String SCANNER_FRAGMENT_TAG = "scanner";

    public static final String CODE_FRAGMENT_TAG = "code";

    public static final int FPPR_SUBMIT_FILTERS = 102;

    public static final int FPPR_HISTORY_FILTERS = 103;

    public static final String GOOGLE_AUTH_METHOD = "google";

    public static final String FB_AUTH_METHOD = "facebook";

    public static final String GOOGLE_WEB_CLIENT_ID = "491459641376-npvugv8od8v8j0a45asbsmem6r4qelhq.apps.googleusercontent.com";

    //alpha (fake money)
    //public static final String STRIPE_PUBLISHABLE_KEY = "pk_test_XXhtxu1S44u1en0gH6ozoB7t";

    //prod (real money)
    public static final String STRIPE_PUBLISHABLE_KEY = "pk_live_qgUYywOaOSnKHZvAUSrbaDch";

    public static final Double MINIMUM_OFFER_PRICE = 0.50;

    public static final int LONG_SNACK = 6000;

    public static final  SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE MMM dd yyyy hh:mm a");

    public static final String RENT_TEXT = "request to rent an item";
    public static final String BUY_TEXT = "request to buy an item";
    public static final String SELL_TEXT = "sell an item";
    public static final String LOAN_TEXT = "list a rentable item";

    public static final String NEARBY_BUCKET = "nearbyappphotos";

}
