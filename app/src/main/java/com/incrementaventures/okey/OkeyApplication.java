package com.incrementaventures.okey;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.incrementaventures.okey.Views.Adapters.FontsOverride;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by andres on 06-07-15.
 */
public class OkeyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FontsOverride.setDefaultFont(this, "MONOSPACE", "avenir.ttf");

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        // ParseCrashReporting.enable(this);
        Parse.initialize(this, "EAAYulhYX56NsqKAkxRGZjRz8DIDLndXENykO59I",
                "Mw37xHHNFlcwGsY1akSEbQRH7YZQGrL7xhA9jhwf");
    }

    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e("RESTART APP", "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e("RESTART APP", "Was not able to restart application, PM null");
                }
            } else {
                Log.e("RESTART APP", "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e("RESTART APP", "Was not able to restart application");
        }
    }
}