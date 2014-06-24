package deadpixel.app.vapor.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * Created by Tevin on 6/24/2014.
 */
public class AppUtils {

    Context mContext;

    public AppUtils (Context context) {
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public final static boolean isValidPass(CharSequence target) {
        return !TextUtils.isEmpty(target);
    }


    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return (ni != null)&& ni.isConnected();
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
}
