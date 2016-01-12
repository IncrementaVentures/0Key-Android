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

    /**
     * I'm not proud of this, but is the only solution for this *** BLE API. Only called when
     * the app enters in the state that I named "lagoon". In this state, the app cannot connect
     * to the master unless a restart of the app, or a waiting of aprox. 25 seconds happens.
     * Disabling and enabling the bluetooth don't fix this issue. I tried every possible solution
     * and the error just keep happening. The probable cause of this issue is the poor BLE API in
     * the SDK 18. This issue happens aprox. in 1 of 20 connections attempts. I hope that the
     * programmer that take this app in a future, don't have to suffer the pain I suffered. If you
     * will continue working with SDK 18, think it twice. My recommendation: change to SDK 21 or
     * change the communication to Wi-fi.
     * @param context Context in which this method is called.
     */
    public static void doRestart(Context context) {
        try {
            //check if the context is given
            if (context != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = context.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            context.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(context, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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