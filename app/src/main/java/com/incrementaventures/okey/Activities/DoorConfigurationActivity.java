package com.incrementaventures.okey.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DoorConfigurationActivity extends ActionBarActivity {
    @Bind(R.id.default_key)
    EditText mDefaultKeyEditText;
    @Bind(R.id.new_key)
    EditText mNewKeyEditText;
    @Bind(R.id.door_name_config)
    EditText mDoorNameEditText;

    @Bind(R.id.insert_first_config_key_button)
    Button mOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_admin_config);
        ButterKnife.bind(this);
        setTitle(R.string.title_activity_first_admin_config);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultKey = mDefaultKeyEditText.getText().toString();
                String newKey = mNewKeyEditText.getText().toString();
                String doorName = mDoorNameEditText.getText().toString();

                Intent results = new Intent();
                results.putExtra(MainActivity.DEFAULT_KEY_EXTRA, defaultKey);
                results.putExtra(MainActivity.NEW_KEY_EXTRA, newKey);
                results.putExtra(MainActivity.DOOR_NAME_EXTRA, doorName);

                setResult(RESULT_OK, results);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_admin_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
