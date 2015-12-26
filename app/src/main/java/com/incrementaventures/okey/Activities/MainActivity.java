package com.incrementaventures.okey.Activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Fragments.ConfigurationFragment;
import com.incrementaventures.okey.Fragments.MasterFragment;
import com.incrementaventures.okey.Fragments.InsertPinFragment;
import com.incrementaventures.okey.Fragments.MenuFragment;
import com.incrementaventures.okey.Fragments.ModifyPermissionFragment;
import com.incrementaventures.okey.Fragments.NameHolderFragment;
import com.incrementaventures.okey.Fragments.NewDoorFragment;
import com.incrementaventures.okey.Fragments.PermissionsFragment;
import com.incrementaventures.okey.Fragments.PreferencesFragment;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements
        InsertPinFragment.PinDialogListener,
        User.OnUserBluetoothToActivityResponse,
        User.OnActionMasterResponse,
        User.OnPermissionsResponse,
        Permission.OnNetworkResponseListener,
        MasterFragment.OnMasterFragmentListener,
        MenuFragment.OnMenuButtonClicked,
        ModifyPermissionFragment.OnPermissionModifiedListener,
        ConfigurationFragment.OnMasterConfigurationListener,
        PermissionsFragment.OnPermissionAdapterListener,
        NewDoorFragment.OnPairRequestedListener,
        NameHolderFragment.OnTextHolderFragmentClick {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int FIRST_CONFIG = 2;
    public static final String DEFAULT_KEY_EXTRA = "defaultkey";
    public static final String NEW_KEY_EXTRA = "newkey";
    public static final String MASTER_NAME_EXTRA = "doorname";
    public static final String SCANNED_DOOR_EXTRA = "scanneddoor";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.main_view)
    View mRootView;

    private MasterFragment mMasterFragment;
    private User mCurrentUser;
    private ArrayList<String> mScannedMasters;
    private ArrayAdapter<String> mScannedMastersAdapter;
    private AlertDialog mScannedMastersDialog;
    private boolean mAddingNewMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUpToolbar();
        authenticateUser();
        if (mCurrentUser == null) return;
        checkPreferences();
        checkNewPermissions();
        checkBluetoothLeSupport();
        mMasterFragment = new MasterFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mMasterFragment, MasterFragment.TAG)
                .commit();
        setUpToolbar();
    }

    @Override
    public void onMenuClick() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new MenuFragment(), MenuFragment.TAG).
                addToBackStack(MenuFragment.TAG).commit();
    }

    private void setUpToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_action_menu);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void showToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    public void checkNewPermissions() {
        Permission.fetchPermissions(this, mCurrentUser);
    }

    public void updateData(View view) {
        getSupportFragmentManager().popBackStack();
        Snackbar.make(mRootView, R.string.updating_data, Snackbar.LENGTH_LONG).show();
        checkNewPermissions();
        showToolbar();
    }

    private void checkPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("protect_with_pin", true)) {
            InsertPinFragment dialog = new InsertPinFragment();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "dialog_pin");
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        } else {
            try {
                super.onBackPressed();
                MasterFragment masterFragment = (MasterFragment)
                        getSupportFragmentManager().findFragmentByTag(MasterFragment.TAG);
                PermissionsFragment permissionsFragment = (PermissionsFragment)
                        getSupportFragmentManager().findFragmentByTag(PermissionsFragment.TAG);
                if ((masterFragment != null && masterFragment.isVisible()) ||
                        permissionsFragment != null && permissionsFragment.isVisible()) {
                    showToolbar();
                }
            } catch (IllegalStateException e) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == android.R.id.home) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new MenuFragment(), MenuFragment.TAG)
                    .addToBackStack(MenuFragment.TAG).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the current user, and if it's null, starts AuthActivity and finishes this activity
     */
    private void authenticateUser() {
        mCurrentUser = User.getLoggedUser(this);
        if (mCurrentUser == null) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkBluetoothLeSupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothNotSupported() {
        Toast.makeText(this, R.string.device_doesnt_support_bluetooth, Toast.LENGTH_LONG).show();
    }

    @Override
    public void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        final String deviceName =
                (device.getName() == null) ? device.getAddress() : device.getName();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScannedMasters.add(deviceName);
                mScannedMastersAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void deviceNotFound() {
        enableOpenButton();
        Snackbar.make(mRootView,  R.string.device_not_found, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void stopScanning() {
        mAddingNewMaster = false;
        if (mScannedMasters != null && mScannedMasters.size() == 0) {
            if (mScannedMastersDialog != null) mScannedMastersDialog.dismiss();
            Snackbar.make(mRootView, R.string.devices_not_found, Snackbar.LENGTH_LONG).show();
        }
    }

    private void enableOpenButton() {
        mMasterFragment.enableOpenButton(R.drawable.app_icon_placeholder);
    }

    @Override
    public void doorOpened(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableOpenButton();
                Snackbar.make(mRootView, R.string.door_opened, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void doorOpening() {
        Snackbar.make(mRootView, R.string.opening_door, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void noPermission() {
        Snackbar.make(mRootView, R.string.no_permission, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void slavesFound(Master master, ArrayList<Slave> slaves) {
        for (Slave slave : slaves) {
            slave.setMasterId(master.getId());
            slave.save();
        }
        mMasterFragment.onSlavesReceived(slaves);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mRootView, R.string.slave_paired, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void masterWithNoSlaves() { }

    @Override
    public void permissionCreated(String key, Permission permission) {
        permission.setKey(key);
        permission.share();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMasterFragment();
                Snackbar.make(mRootView, R.string.permission_created, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void masterConfigured(final Master master, final Permission adminPermission) {
        master.save();
        adminPermission.save();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMasterFragment();
                Snackbar.make(mRootView, R.string.master_configured, Snackbar.LENGTH_LONG).show();
                mMasterFragment.onMasterReceived(master);
            }
        });

    }

    @Override
    public void permissionEdited(String key, Permission newPermission) {
        newPermission.save();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMasterFragment();
                Snackbar.make(mRootView, R.string.permission_edited, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void permissionDeleted(Permission permission) {
        permission.delete();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mRootView, R.string.permission_deleted, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void permissionsReceived(ArrayList<HashMap<String, String>> permissionsData) { }

    @Override
    public void permissionReceived(int type, String key, String start, String end) { }

    @Override
    public void error(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableOpenButton();
                switch (code) {
                    case BluetoothClient.TIMEOUT:
                        Snackbar.make(mRootView, R.string.door_cant_open_timeout,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.RESPONSE_INCORRECT:
                        Snackbar.make(mRootView, R.string.door_cant_open_bad_code,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.CANT_OPEN:
                        Snackbar.make(mRootView, R.string.door_cant_open,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.CANT_CONFIGURE:
                        Snackbar.make(mRootView, R.string.door_cant_configure,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.PERMISSION_NOT_CREATED:
                        Snackbar.make(mRootView, R.string.permission_not_created,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.PERMISSION_NOT_EDITED:
                        Snackbar.make(mRootView, R.string.permission_not_edited,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION:
                        Snackbar.make(mRootView, R.string.no_permission,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION_THIS_HOUR:
                        Snackbar.make(mRootView, R.string.no_permission_this_hour,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.BAD_INPUT:
                        Snackbar.make(mRootView, R.string.bad_input_error,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.DOOR_NOT_CONFIGURED:
                        Snackbar.make(mRootView, R.string.door_not_configured,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.REQUEST_ENABLE_BT:
                if (mScannedMastersDialog != null) {
                    mScannedMastersDialog.cancel();
                }
                if (mMasterFragment != null) {
                    mMasterFragment.enableOpenButton(R.drawable.app_icon_placeholder);
                }
                break;
            default:
                Toast.makeText(this, R.string.not_implemented_code_on_result,
                        Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPinDialogPositiveClick(String pin) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getString("protect_pin", "").equals(pin)){
            Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onPinDialogNegativeClick() {
        finish();
    }

    @Override
    public void onNewPermissions(ArrayList<Permission> permissions, boolean newPermissions) {
        if (permissions != null && permissions.size() > 0 && newPermissions) {
            Snackbar.make(mRootView, R.string.new_permissions, Snackbar.LENGTH_LONG).show();
        }
        ArrayList<Slave> slaves = new ArrayList<>();
        ArrayList<Master> masters = new ArrayList<>();
        Master master;
        for (Permission permission : permissions) {
            master = permission.getMaster();
            if (master != null) {
                if (!masters.contains(master))
                    masters.add(master);
                if (permission.getSlaveId() == Slave.ALL_SLAVES) {
                    ArrayList<Slave> masterSlaves = new ArrayList<>(master.getSlaves());
                    for (Slave slave : masterSlaves) {
                        if (!slaves.contains(slave))
                            slaves.add(slave);
                    }
                }
            }
            if (permission.getSlave() != null && !slaves.contains(permission.getSlave())) {
                slaves.add(permission.getSlave());
            }
        }
        mMasterFragment.onMastersReceived(masters);
        mMasterFragment.onSlavesReceived(slaves);
    }


    @Override
    public void shareKeySelected(Master master) {
        Bundle args = new Bundle();
        args.putString(Master.ID, master.getId());
        ModifyPermissionFragment fragment = new ModifyPermissionFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, ModifyPermissionFragment.TAG)
                .addToBackStack(ModifyPermissionFragment.TAG)
                .commit();
    }

    @Override
    public void get0keySelected() {

    }

    @Override
    public void openWhenCloseSelected(Master master, Slave slave, String permissionKey) {

    }

    private void showScannedMastersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.scanned_0keys);
        mScannedMasters = new ArrayList<>();
        mScannedMastersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mScannedMasters);
        builder.setAdapter(mScannedMastersAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConfigurationFragment fragment = new ConfigurationFragment();
                Bundle args = new Bundle();
                args.putString(Master.ID, mScannedMasters.get(which));
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, ConfigurationFragment.TAG)
                        .addToBackStack(ConfigurationFragment.TAG)
                        .commit();
            }
        });
        mScannedMastersDialog = builder.show();
        Snackbar.make(mRootView, R.string.searching, Snackbar.LENGTH_LONG).show();
    }

    private void showMasterFragment() {
        getSupportFragmentManager().popBackStack();
        MasterFragment masterFragment = (MasterFragment)
                getSupportFragmentManager().findFragmentByTag(MasterFragment.TAG);
        if (masterFragment == null || !masterFragment.isVisible()) {
            getSupportFragmentManager().popBackStack(MasterFragment.TAG,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        showToolbar();
    }

    public void onGoHomeClicked(View view) {
        showMasterFragment();
    }

    public void onAddNew0keyClicked(View view) {
        mAddingNewMaster = true;
        mCurrentUser.scanDevices();
        showScannedMastersDialog();
    }

    public void onAddNewDoorClicked(View view) {
        NewDoorFragment fragment = new NewDoorFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, NewDoorFragment.TAG)
                .addToBackStack(NewDoorFragment.TAG)
                .commit();
    }

    public void onShowPermissionsClicked(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString(Master.ID, mMasterFragment.getSelectedMaster().getId());
        PermissionsFragment fragment = new PermissionsFragment();
        fragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, PermissionsFragment.TAG)
                .addToBackStack(PermissionsFragment.TAG)
                .commit();
    }

    public void onAddNewPermissionClicked(View view) {
        if (mMasterFragment.getSelectedMaster() == null) {
            Snackbar.make(mRootView, R.string.no_masters_yet, Snackbar.LENGTH_LONG).show();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString(Master.ID, mMasterFragment.getSelectedMaster().getId());
        Slave slave = mMasterFragment.getSelectedSlave();
        if (slave != null) {
            args.putInt(Slave.ID, slave.getId());
        }
        ModifyPermissionFragment fragment = new ModifyPermissionFragment();
        fragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, ModifyPermissionFragment.TAG)
                .addToBackStack(ModifyPermissionFragment.TAG)
                .commit();
    }

    public void onSettingsClicked(View view) {
        PreferencesFragment fragment = new PreferencesFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PermissionsFragment.TAG)
                .addToBackStack(PreferencesFragment.TAG)
                .commit();
    }

    public void onMenuItemClicked(View view) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new MenuFragment(), MenuFragment.TAG);
        transaction.addToBackStack(MenuFragment.TAG);
        transaction.commit();
    }

    public void onLogoutClicked(View view) {
        mCurrentUser.logout(new User.OnParseUserLogoutListener() {
            @Override
            public void userLoggedOut() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("protect_with_pin", true);
                editor.putString(InsertPinFragment.PROTECT_PIN, "EMPTY");
                editor.apply();
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Snackbar.make(mRootView, R.string.logging_out, Snackbar.LENGTH_LONG).show();
    }

    public void onGoBackClicked(View view) {
        onBackPressed();
    }

    public void onGoBackToMain(View view) {
        showMasterFragment();
    }

    @Override
    public void onCreatePermissionClicked(Permission permission, String userKey) {
        Snackbar.make(mRootView, R.string.creating_permission,
                Snackbar.LENGTH_LONG).show();
        User.getLoggedUser().createNewPermission(permission, userKey, permission.getMaster().getId());
    }

    @Override
    public void onModifyPermissionClicked(Permission toEditPermission, int oldSlaveId,
                                          String userKey, String doorId) {
        Snackbar.make(mRootView, R.string.editing_permission, Snackbar.LENGTH_LONG).show();
        mCurrentUser.editPermission(toEditPermission, oldSlaveId, userKey, doorId);
    }

    @Override
    public void onDeletePermissionClicked(Permission permission, String userKey) {
        mCurrentUser.deletePermission(permission.getMaster().getId(), userKey, permission);
        Snackbar.make(mRootView, R.string.deleting_permission, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onConfigureMasterClick(String permissionKey, String defaultKey, Master master) {
        Snackbar.make(mRootView, R.string.configuring_door_dialog, Snackbar.LENGTH_LONG).show();
        mCurrentUser.makeFirstAdminConnection(permissionKey, defaultKey, master);
    }

    @Override
    public void onModifyPermissionAdapterClicked(Permission permission) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString(Master.ID, permission.getMasterId());
        args.putInt(Slave.ID, permission.getSlaveId());
        args.putInt(ModifyPermissionFragment.PERMISSION_OLD_SLAVE, permission.getSlaveId());
        args.putString(Permission.NAME, permission.getUser().getEmail());
        args.putString(Permission.OBJECT_ID, permission.getId());
        args.putString(Permission.KEY, permission.getKey());
        ModifyPermissionFragment fragment = new ModifyPermissionFragment();
        fragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, ModifyPermissionFragment.TAG)
                .addToBackStack(ModifyPermissionFragment.TAG)
                .commit();
    }

    @Override
    public void onDeletePermissionAdapterClicked(Permission permission) {
        Permission adminPermission = mCurrentUser.getAdminPermission(permission.getMaster());
        if (adminPermission != null) {
            mCurrentUser.deletePermission(permission.getMaster().getId(),
                    adminPermission.getKey(),
                    permission);
            Snackbar.make(mRootView, R.string.deleting_permission, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mRootView, R.string.no_permission, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPairRequested(Master master, int slaveId) {
        Permission permission = mCurrentUser.getAdminPermission(master);
        if (permission != null) {
            mCurrentUser.pairSlaves(
                    master.getId(), permission.getKey(), permission.getSlaveId(), slaveId);
        } else {
            Snackbar.make(mRootView, R.string.no_permission, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMasterNameClick() {
        mMasterFragment.onMasterNameClick();
    }

    @Override
    public void onSlaveNameClick() {
        mMasterFragment.onSlaveNameClick();
    }
}
