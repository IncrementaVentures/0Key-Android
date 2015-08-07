package com.incrementaventures.okey.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.incrementaventures.okey.R;

public class NewPermissionActivity extends ActionBarActivity {

    public static final String NEW_PERMISSION_TYPE = "permission_type";
    public static final String NEW_PERMISSION_DATE = "permission_date";
    public static final String NEW_PERMISSION_HOUR = "permission_hour";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_permission);
        setTitle(R.string.title_activity_new_permission);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_permission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Handle "up" button behavior here.
            Intent data = new Intent();
            data.putExtra(MainActivity.SCANNED_DOOR_EXTRA, false);
            setResult(Activity.RESULT_CANCELED, data);
            finish();
            return true;
        }
        //noinspection SimplifiableIfStatement
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
