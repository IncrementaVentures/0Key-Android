package com.incrementaventures.okey.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShareKeyActivity extends ActionBarActivity {
    @Bind(R.id.share_key_email)
    EditText mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_key);
        setUpActionBar();
        ButterKnife.bind(this);
    }

    private void setUpActionBar() {
        getSupportActionBar().setElevation(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_key, menu);
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
        else if (id == R.id.action_share){
            User user = User.getUser(mEmail.getText().toString());
            if (user == null) {
                Toast.makeText(this, R.string.user_doesnt_exist, Toast.LENGTH_SHORT).show();
                return true;
            }
            Master master = Master.getMaster(getIntent().getStringExtra(Master.ID),
                    User.getLoggedUser().getUUID());
            Permission permission =
                    Permission.create(
                            user,
                            master,
                            getIntent().getIntExtra(Permission.TYPE, 0),
                            getIntent().getStringExtra(Permission.KEY),
                            getIntent().getStringExtra(Permission.START_DATE),
                            getIntent().getStringExtra(Permission.END_DATE),
                            getIntent().getIntExtra(Permission.SLAVE_ID, 0));
            permission.save();
            Toast.makeText(this, R.string.key_will_be_shared, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
