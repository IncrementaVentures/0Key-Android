package com.incrementaventures.okey.Activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
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
import com.incrementaventures.okey.Networking.ParseErrorHandler;
import com.incrementaventures.okey.OkeyApplication;
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
        NameHolderFragment.OnTextHolderFragmentClick,
        ParseErrorHandler.OnParseErrorListener {

    public static final int REQUEST_ENABLE_BT = 1;


    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.main_view)
    View mRootView;

    private MasterFragment mMasterFragment;
    private PermissionsFragment mPermissionsFragment;
    private User mCurrentUser;
    private ArrayList<String> mScannedMasters;
    private ArrayAdapter<String> mScannedMastersAdapter;
    private AlertDialog mScannedMastersDialog;
    private InsertPinFragment mPinDialog;
    private boolean mActivatingBluetooth;
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initializeParseErrorHandler();
        authenticateUser();
        if (mCurrentUser == null) return;
        checkNewPermissions();
        checkBluetoothLeSupport();
        mMasterFragment = new MasterFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mMasterFragment, MasterFragment.TAG)
                .commit();
        setUpToolbar();
    }

    @Override
    protected void onResume() {
        if (!mActivatingBluetooth) {
            checkPreferences();
        }
        super.onResume();
    }

    @Override
    public void onMenuClick() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new MenuFragment(), MenuFragment.TAG).
                addToBackStack(MenuFragment.TAG).commit();
    }

    private void initializeParseErrorHandler() {
        ParseErrorHandler.initialize(this);
    }


    View.OnClickListener mShowMenuFragmentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMenuFragment();
        }
    };

    View.OnClickListener mBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private void showMenuFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new MenuFragment(), MenuFragment.TAG)
                .addToBackStack(MenuFragment.TAG).commit();
    }

    private void setUpToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_action_menu);
        mToolbar.setNavigationOnClickListener(mShowMenuFragmentListener);
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
        if (mPinDialog == null) {
            mPinDialog = new InsertPinFragment();
            mPinDialog.setCancelable(false);
        }
        if (sharedPref.getBoolean("protect_with_pin", true) && !mPinDialog.isAdded()) {
            mPinDialog.show(getFragmentManager(), "dialog_pin");
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
                if ((masterFragment != null && masterFragment.isVisible())) {
                    setUpToolbar();
                    showToolbar();
                    mToolbar.findViewById(R.id.logo_toolbar).setVisibility(ImageView.VISIBLE);
                    mToolbar.setTitle("");
                    hideAddPermissionAndRefreshButtons();
                } else if (permissionsFragment != null && permissionsFragment.isVisible()) {
                    showToolbar();
                    mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
                    mToolbar.setNavigationOnClickListener(mBackListener);
                    mToolbar.findViewById(R.id.logo_toolbar).setVisibility(ImageView.GONE);
                    if (mCurrentUser.getAdminPermission(mMasterFragment.getSelectedMaster()) != null) {
                        showAddPermissionAndRefreshButtons();
                    } else {
                        showRefreshButton();
                    }
                }
            } catch (IllegalStateException e) { }
        }
    }

    private void showAddPermissionAndRefreshButtons() {
        showAddPermissionButton();
        showRefreshButton();
    }

    private void showAddPermissionButton() {
        mOptionsMenu.findItem(R.id.action_add_permission).setVisible(true);
    }

    private void showRefreshButton() {
        mOptionsMenu.findItem(R.id.action_refresh_data).setVisible(true);
    }

    private void hideAddPermissionAndRefreshButtons() {
        hideAddPermissionButton();
        hideRefreshButton();
    }

    private void hideAddPermissionButton() {
        mOptionsMenu.findItem(R.id.action_add_permission).setVisible(false);
    }

    private void hideRefreshButton() {
        mOptionsMenu.findItem(R.id.action_refresh_data).setVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mOptionsMenu = menu;
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
        } else if (id == R.id.action_add_permission) {
            onAddNewPermissionClicked(null);
        } else if (id == R.id.action_refresh_data) {
            Snackbar.make(mRootView, R.string.syncing_data, Snackbar.LENGTH_LONG).show();
            checkNewPermissions();
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
        mActivatingBluetooth = true;
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
        logAnswers(new CustomEvent("Device not found"));
        Snackbar.make(mRootView, R.string.device_not_found, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void stopScanning() {
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
        logAnswers(new CustomEvent("Door opened"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableOpenButton();
                Snackbar.make(mRootView, R.string.door_opened, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void logAnswers(CustomEvent event) {
        Answers.getInstance().logCustom(event);
    }

    @Override
    public void doorOpening() {
        Snackbar.make(mRootView, R.string.opening_door, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void noPermission() {
        Snackbar.make(mRootView, R.string.no_virtual_key, Snackbar.LENGTH_LONG).show();
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
        logAnswers(new CustomEvent("Permission created"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mRootView, R.string.virtual_key_created, Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(mRootView, R.string.master_configured, Snackbar.LENGTH_LONG).show();
                mMasterFragment.onMasterReceived(master);
            }
        });

    }

    @Override
    public void permissionEdited(String key, Permission newPermission) {
        newPermission.save();
        logAnswers(new CustomEvent("Permission edited"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mRootView, R.string.virtual_key_edited, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void permissionDeleted(Permission permission) {
        permission.delete();
        logAnswers(new CustomEvent("Permission deleted"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mRootView, R.string.virtual_key_deleted, Snackbar.LENGTH_LONG).show();
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
                        logAnswers(new CustomEvent("Door not respond"));
                        Snackbar.make(mRootView, R.string.door_cant_open_timeout,
                                Snackbar.LENGTH_LONG).show();
                        /**
                         * @see OkeyApplication#doRestart(Context).
                         */
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) { }

                        OkeyApplication.doRestart(MainActivity.this);
                        break;
                    case BluetoothClient.STILL_SCANNING:
                        Snackbar.make(mRootView, R.string.try_again_please,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.RESPONSE_INCORRECT:
                        logAnswers(new CustomEvent("Door not responding as expected"));
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
                        Snackbar.make(mRootView, R.string.virtual_key_not_created,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.PERMISSION_NOT_EDITED:
                        Snackbar.make(mRootView, R.string.virtual_key_not_edited,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION:
                        Snackbar.make(mRootView, R.string.no_virtual_key,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION_THIS_HOUR:
                        Snackbar.make(mRootView, R.string.no_virtual_key_this_hour,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case BluetoothClient.BAD_INPUT:
                        logAnswers(new CustomEvent("Communication problem"));
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
    public void invalidUserSessionToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                logout();
            }
        }).start();
        Snackbar.make(mRootView, R.string.invalid_session, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onNewPermissions(final ArrayList<Permission> permissions, final boolean newPermissions) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (permissions != null && permissions.size() > 0 && newPermissions) {
                    Snackbar.make(mRootView, R.string.new_virtual_keys, Snackbar.LENGTH_LONG).show();
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
                if (mPermissionsFragment != null && mPermissionsFragment.isVisible()) {
                    mPermissionsFragment.onPermissionsReceived(permissions, newPermissions);
                }
            }
        }).start();
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
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.link_0key_webpage)));
        startActivity(browserIntent);
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
        // showMasterFragment();
        onBackPressed();
    }

    public void onAddNew0keyClicked(View view) {
        mCurrentUser.scanDevices();
        showScannedMastersDialog();
    }

    public void onGet0keyClicked(View view) {
        get0keySelected();
    }

    public void onAddNewDoorClicked(View view) {
        NewDoorFragment fragment = new NewDoorFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, NewDoorFragment.TAG)
                .addToBackStack(NewDoorFragment.TAG)
                .commit();
    }

    public void onShowAllPermissionsClicked(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPermissionsFragment = new PermissionsFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mPermissionsFragment, PermissionsFragment.TAG)
                .addToBackStack(PermissionsFragment.TAG)
                .commit();
        Master selectedMaster = mMasterFragment.getSelectedMaster();
        if (selectedMaster != null && mCurrentUser.getAdminPermission(mMasterFragment.getSelectedMaster()) != null) {
            showAddPermissionAndRefreshButtons();
        } else {
            showRefreshButton();
        }
        showToolbar();
        mToolbar.findViewById(R.id.logo_toolbar).setVisibility(ImageView.GONE);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
        mToolbar.setNavigationOnClickListener(mBackListener);
    }


    public void onShowPermissionsClicked(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString(Master.ID, mMasterFragment.getSelectedMaster().getId());
        mPermissionsFragment = new PermissionsFragment();
        mPermissionsFragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.container, mPermissionsFragment, PermissionsFragment.TAG)
                .addToBackStack(PermissionsFragment.TAG)
                .commit();
        if (mCurrentUser.getAdminPermission(mMasterFragment.getSelectedMaster()) != null) {
            showAddPermissionAndRefreshButtons();
        } else {
            showRefreshButton();
        }
        mToolbar.findViewById(R.id.logo_toolbar).setVisibility(ImageView.GONE);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
        mToolbar.setNavigationOnClickListener(mBackListener);
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

    private void logout() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("protect_with_pin", true);
        editor.putString(InsertPinFragment.PROTECT_PIN, "EMPTY");
        editor.apply();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Master.unpinAll();
                Permission.unpinAll();
                Slave.unpinAll();
            }
        }).start();
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLogoutClicked(View view) {
        mCurrentUser.logout(new User.OnParseUserLogoutListener() {
            @Override
            public void userLoggedOut() {
                logout();
            }
        });
        Snackbar.make(mRootView, R.string.logging_out, Snackbar.LENGTH_LONG).show();
    }

    public void onGoBackClicked(View view) {
        onBackPressed();
    }

    public void onGoBackToMain(View view) {
        // showMasterFragment();
        onBackPressed();
    }

    @Override
    public void onCreatePermissionClicked(Permission permission, String userKey) {
        Snackbar.make(mRootView, R.string.creating_virtual_key,
                Snackbar.LENGTH_LONG).show();
        User.getLoggedUser().createNewPermission(permission, userKey, permission.getMaster().getId());
    }

    @Override
    public void onModifyPermissionClicked(Permission toEditPermission, int oldSlaveId,
                                          String userKey, String doorId) {
        Snackbar.make(mRootView, R.string.editing_virtual_key, Snackbar.LENGTH_LONG).show();
        mCurrentUser.editPermission(toEditPermission, oldSlaveId, userKey, doorId);
    }

    @Override
    public void onDeletePermissionClicked(Permission permission, String userKey) {
        mCurrentUser.deletePermission(permission.getMaster().getId(), userKey, permission);
        Snackbar.make(mRootView, R.string.deleting_virtual_key, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onConfigureMasterClick(String permissionKey, String defaultKey, Master master) {
        Snackbar.make(mRootView, R.string.configuring_door_dialog, Snackbar.LENGTH_LONG).show();
        mCurrentUser.makeFirstAdminConnection(permissionKey, defaultKey, master);
    }

    @Override
    public void onModifyPermissionAdapterClicked(Permission permission) {
        if (permission.getUser() == null) {
            Snackbar.make(mRootView, R.string.user_not_loaded_yet, Snackbar.LENGTH_LONG).show();
            return;
        }
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
            Snackbar.make(mRootView, R.string.deleting_virtual_key, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mRootView, R.string.no_virtual_key, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPairRequested(Master master, int slaveId) {
        Permission permission = mCurrentUser.getAdminPermission(master);
        if (permission != null) {
            mCurrentUser.pairSlaves(
                    master.getId(), permission.getKey(), permission.getSlaveId(), slaveId);
        } else {
            Snackbar.make(mRootView, R.string.you_are_not_admin, Snackbar.LENGTH_LONG).show();
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

    @Override
    public void sessionExpired() {
        Snackbar.make(mRootView, R.string.session_expired_login_again, Snackbar.LENGTH_LONG).show();
        logout();
    }

    @Override
    public void defaultError() {
        Snackbar.make(mRootView, R.string.an_error_occurred, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void timeout() {
        Snackbar.make(mRootView, R.string.check_internet_connection, Snackbar.LENGTH_LONG).show();
    }
}
