package com.incrementaventures.okey;

import android.app.Application;

import com.incrementaventures.okey.Views.Adapters.FontsOverride;
import com.parse.Parse;
import com.parse.ParseCrashReporting;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by andres on 06-07-15.
 */
public class OkeyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "MONOSPACE", "avenir.ttf");

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        ParseCrashReporting.enable(this);
        Parse.initialize(this, "EAAYulhYX56NsqKAkxRGZjRz8DIDLndXENykO59I",
                "Mw37xHHNFlcwGsY1akSEbQRH7YZQGrL7xhA9jhwf");
    }
}