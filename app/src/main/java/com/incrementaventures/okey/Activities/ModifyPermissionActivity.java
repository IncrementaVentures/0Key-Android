package com.incrementaventures.okey.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.incrementaventures.okey.R;

public class ModifyPermissionActivity extends ActionBarActivity {

    public static final String PERMISSION_TYPE = "permission_type";
    public static final String PERMISSION_START_DATE = "permission_start_date";
    public static final String PERMISSION_START_HOUR = "permission_start_hour";
    public static final String PERMISSION_END_DATE = "permission_end_date";
    public static final String PERMISSION_END_HOUR = "permission_end_hour";
    public static final String PERMISSION_KEY = "permission_key";
    public static final String PERMISSION_NEW_SLAVE = "permission_new_slave";
    public static final String PERMISSION_OLD_SLAVE = "permission_old_slave";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_permission);
        setUpActionBar();
        if (getIntent().getIntExtra(DoorActivity.REQUEST_CODE,
                DoorActivity.NEW_PERMISSION_REQUEST) == DoorActivity.EDIT_PERMISSION_REQUEST){
            setTitle(R.string.edit_permission);
        } else{
            setTitle(R.string.title_activity_new_permission);
        }
    }

    private void setUpActionBar() {
        getSupportActionBar().setElevation(0);
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
