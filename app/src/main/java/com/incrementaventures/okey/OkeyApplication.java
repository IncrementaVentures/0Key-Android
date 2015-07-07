package com.incrementaventures.okey;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by andres on 06-07-15.
 */
public class OkeyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "EAAYulhYX56NsqKAkxRGZjRz8DIDLndXENykO59I", "Mw37xHHNFlcwGsY1akSEbQRH7YZQGrL7xhA9jhwf");

    }
}