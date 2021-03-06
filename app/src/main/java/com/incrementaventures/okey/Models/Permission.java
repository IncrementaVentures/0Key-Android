package com.incrementaventures.okey.Models;

import android.os.AsyncTask;
import android.text.format.Time;

import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
import com.incrementaventures.okey.Networking.ParseErrorHandler;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Permission implements com.incrementaventures.okey.Models.ParseObject {

    public static final int ADMIN_PERMISSION = 0;
    public static final int PERMANENT_PERMISSION = 1;
    public static final int TEMPORAL_PERMISSION = 2;
    public static final int UNKNOWN_PERMISSION = 3;

    public static final String PERMANENT_DATE = "3000-01-01T00:01";

    public static final String PERMISSION_CLASS_NAME = "Permission";
    public static final String USER_ID = "user_id";
    public static final String MASTER_ID = "master_id";
    public static final String SLAVE_ID = "slave_id";
    public static final String OBJECT_ID = "objectId";
    public static final String TYPE = "type";
    public static final String KEY = "key";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String CREATED_AT = "createdAt";
    public static final String NAME = "name";

    private ParseObject mParsePermission;

    public interface OnNetworkResponseListener {
        void onNewPermissions(ArrayList<Permission> permissions, boolean newPermissions);
        void invalidUserSessionToken();
    }

    private Permission(ParseObject parsePermission){
        mParsePermission = parsePermission;
    }

    private Permission(User user, Master master, int type, String key, String startDate,
                       String endDate, int slaveId) {
        mParsePermission = ParseObject.create(PERMISSION_CLASS_NAME);
        if (user != null) mParsePermission.put(USER_ID, user.getId());
        if (master != null) {
            mParsePermission.put(MASTER_ID, master.getId());
        }
        mParsePermission.put(SLAVE_ID, slaveId);
        mParsePermission.put(TYPE, type);
        mParsePermission.put(KEY, key);
        mParsePermission.put(START_DATE, startDate);
        mParsePermission.put(END_DATE, endDate);
    }

    public static Permission create(User user, Master master, int type, String key,
                                    String startDate, String endDate, int slaveId) {
        return new Permission(user, master, type, key, startDate, endDate, slaveId);
    }

    public static Permission create(ParseObject parsePermission){
        return new Permission(parsePermission);
    }

    public class Builder {
        private User mUser;
        private Master mMaster;
        private int mSlaveId;
        private int mType;
        private String mKey;
        private String mStartDate;
        private String mEndDate;

        public Builder() {

        }

        public Builder setUser(User user) {
            mUser = user;
            return this;
        }

        public Builder setMaster(Master master) {
            mMaster = master;
            return this;
        }

        public Builder setType(int type) {
            mType = type;
            return this;
        }

        public Builder setKey(String key) {
            mKey = key;
            return this;
        }

        public Builder setStartDate(String startDate) {
            mStartDate = startDate;
            return this;
        }

        public Builder setEndDate(String endDate) {
            mEndDate = endDate;
            return this;
        }

        public Builder setSlaveId(int slaveId) {
            mSlaveId = slaveId;
            return this;
        }

        public Permission build() {
            return new Permission(mUser, mMaster, mType, mKey, mStartDate, mEndDate, mSlaveId);
        }
    }

    public void share() {
        save();
    }

    public String getUserUuid() {
        return mParsePermission.getString(USER_ID);
    }

    @Override
    public String getObjectId() {
        return mParsePermission.getObjectId();
    }

    @Override
    public void deleteFromLocal() {
        try {
            mParsePermission.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllLocal() {
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        try {
            List<com.parse.ParseObject> localPermissions = query.find();
            for (com.parse.ParseObject localPermission : localPermissions) {
                localPermission.unpinInBackground();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        deleteFromLocal();
        mParsePermission.deleteEventually();
    }

    @Override
    public void save() {
        mParsePermission.pinInBackground();
        mParsePermission.saveEventually();
    }

    public void saveLocal() {
        mParsePermission.pinInBackground();
    }

    public String getId() {
        return mParsePermission.getObjectId();
    }

    public Master getMaster(){
        ParseQuery query = new ParseQuery(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Master.ID, mParsePermission.getString(MASTER_ID));
        try {
            ParseObject o = query.getFirst();
            return Master.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMasterId() {
        return mParsePermission.getString(MASTER_ID);
    }

    public static Permission getPermission(String userId, String masterId, int slaveId) {
        ParseQuery query = new ParseQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(MASTER_ID, masterId);
        query.whereEqualTo(USER_ID, userId);
        query.whereEqualTo(SLAVE_ID, slaveId);
        try {
            ParseObject o = query.getFirst();
            return Permission.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Permission> getAllPermissions() {
        ParseQuery query = new ParseQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        ArrayList<Permission> permissions = new ArrayList<>();
        try {
            List<ParseObject> list = query.find();
            if (list != null) {
                for (ParseObject permission : list) {
                    permissions.add(Permission.create(permission));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public static ArrayList<Permission> getPermissions(String userId) {
        ParseQuery query = new ParseQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(USER_ID, userId);
        query.orderByDescending(Permission.CREATED_AT);
        ArrayList<Permission> permissions = new ArrayList<>();
        try {
            List<ParseObject> list = query.find();
            if (list != null) {
                for (ParseObject permission : list) {
                    permissions.add(Permission.create(permission));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public static Permission getPermission(String uuid) {
        ParseQuery query = new ParseQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(OBJECT_ID, uuid);
        try {
            ParseObject o = query.getFirst();
            return Permission.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User fetchUser(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.OBJECT_ID, mParsePermission.getString(USER_ID));
        try {
            ParseUser o = query.getFirst();
            return User.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo(User.OBJECT_ID, mParsePermission.getString(USER_ID));
        try {
            ParseUser o = query.getFirst();
            return User.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getType(){
        switch (mParsePermission.getInt(TYPE)){
            case TEMPORAL_PERMISSION:
                return "Temporal";
            case PERMANENT_PERMISSION:
                return "Permanent";
            case ADMIN_PERMISSION:
                return  "Administrator";
            default:
                return "Unknown";
        }
    }

    public static int getType(String type){
        switch (type) {
            case "Temporal":
                return TEMPORAL_PERMISSION;
            case "Permanent":
                return PERMANENT_PERMISSION;
            case "Administrator":
                return ADMIN_PERMISSION;
            default:
                return 3;
        }
    }

    public void setType(int type){
        mParsePermission.put(TYPE, type);
    }
    public void setSlaveId(int slaveId){
        mParsePermission.put(SLAVE_ID, slaveId);
    }

    public String getKey(){
        return mParsePermission.getString(KEY);
    }

    private boolean started(Time time) {
        if (mParsePermission.getString(START_DATE) == null) return true;
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                    Locale.getDefault()).parse(mParsePermission.getString(START_DATE));
            Date currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                    Locale.getDefault()).parse(BluetoothProtocol.formatDate(time));
            boolean started = currentDate.after(startDate);
            return started;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean finished(Time time){
        if (mParsePermission.getString(END_DATE) == null) return true;
        try {
            Date endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                    Locale.getDefault()).parse(mParsePermission.getString(END_DATE));
            Date currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                    Locale.getDefault()).parse(BluetoothProtocol.formatDate(time));
            boolean finished = currentDate.after(endDate);
            return finished;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValid(){
        Time time = new Time();
        time.setToNow();
        if (mParsePermission.getInt(TYPE) == ADMIN_PERMISSION) return true;
        else if (mParsePermission.getInt(TYPE) == PERMANENT_PERMISSION && started(time)){
            return true;
        }
        else if (mParsePermission.getInt(TYPE) == TEMPORAL_PERMISSION && started(time) && !finished(time)){
            return true;
        }
        else if (mParsePermission.getInt(TYPE) == UNKNOWN_PERMISSION) {
            return true;
        }
        return false;
    }

    public String getStartDate(){
        return mParsePermission.getString(START_DATE);
    }

    public static String getFormattedDate(String stringDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                    Locale.getDefault()).parse(stringDate);
            return new SimpleDateFormat("MMM dd, yyy",
                    Locale.getDefault()).format(date);
        } catch (java.text.ParseException e) {
            return stringDate;
        }
    }

    public static String getDefaultDateString(String formattedDate) {
        try {
            Date date = new SimpleDateFormat("MMM dd, yyyy",
                    Locale.getDefault()).parse(formattedDate);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        } catch (java.text.ParseException e) {
            return formattedDate;
        }
    }


    public void setStartDate(String startDate){
        mParsePermission.put(START_DATE, startDate);
    }

    public String getEndDate(){
        return mParsePermission.getString(END_DATE);
    }

    public String getFormattedEndDate() {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
                    Locale.getDefault()).parse(mParsePermission.getString(END_DATE));
            return new SimpleDateFormat("MMM dd, yyy",
                    Locale.getDefault()).format(date);
        } catch (java.text.ParseException e) {
            return mParsePermission.getString(END_DATE);
        }
    }

    public void setEndDate(String endDate){
        mParsePermission.put(END_DATE, endDate);
    }

    public void setKey(String key){
        mParsePermission.put(KEY, key);
    }

    public static void unpinAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
                query.fromLocalDatastore();

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        for (ParseObject o : list) {
                            try {
                                o.unpin();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    public boolean isAdmin(){
        if (mParsePermission.getInt(TYPE) == ADMIN_PERMISSION)
            return true;
        return false;
    }

    public int getSlaveId() {
        return mParsePermission.getInt(SLAVE_ID);
    }

    public Slave getSlave() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Slave.SLAVE_CLASS_NAME);
        query.orderByDescending(Slave.ID);
        query.whereEqualTo(Slave.ID, getSlaveId());
        query.fromLocalDatastore();
        Slave slave = null;
        try {
            ParseObject parseSlave = query.getFirst();
            if (parseSlave != null) {
                slave = Slave.create(parseSlave);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return slave;
    }

    public static void fetchPermissions(final OnNetworkResponseListener listener, User user) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Permission.PERMISSION_CLASS_NAME);
        query.orderByDescending(Permission.CREATED_AT);
        query.whereEqualTo(Permission.USER_ID, user.getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parsePermissions, com.parse.ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleError(e);
                } else {
                    new ProcessNetworkPermissionsTask(listener).execute(parsePermissions);
                }
            }
        });
    }

    public static void fetchPermissions(final Master master) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Permission.PERMISSION_CLASS_NAME);
        query.orderByDescending(Permission.CREATED_AT);
        query.whereEqualTo(MASTER_ID, master.getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parsePermissions, com.parse.ParseException e) {
                ArrayList<Permission> oldPermissions = master.getAllPermissions();
                for (Permission permission : oldPermissions) {
                    boolean found = false;
                    for (ParseObject parsePermission : parsePermissions) {
                        if (parsePermission.getObjectId().equals(permission.getId()))
                            found = true;
                    }
                    if (!found) {
                        permission.deleteFromLocal();
                    }
                }
                for (ParseObject parseObject : parsePermissions) {
                    parseObject.pinInBackground();
                }
            }
        });
    }

    private static boolean existsLocal(Permission permission) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(OBJECT_ID, permission.getId());
        List<ParseObject> list = query.find();
        if (list == null || list.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        Permission other = (Permission) o;
        return mParsePermission.getObjectId().equals(other.getObjectId());
    }

    private static ArrayList<Permission> getAllLocal() throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        List<ParseObject> list = query.find();
        ArrayList<Permission> localPermissions = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (ParseObject parsePermission : list) {
                localPermissions.add(Permission.create(parsePermission));
            }
        }
        return localPermissions;
    }

    public static class ProcessNetworkPermissionsTask extends
            AsyncTask<List<ParseObject>, Void, ArrayList<Permission>> {

        OnNetworkResponseListener mListener;
        boolean mAddedPermissions;

        public ProcessNetworkPermissionsTask(OnNetworkResponseListener listener) {
            mListener =listener;
        }

        @Override
        protected ArrayList<Permission> doInBackground(List<ParseObject>... params) {
            ArrayList<Permission> permissions = new ArrayList<>();
            try {
                ArrayList<Permission> oldPermissions = Permission.getPermissions(User.getLoggedUser().getId());
                if (params[0] != null) {
                    // Delete invalid local permissions
                    for (Permission oldPermission : oldPermissions) {
                        boolean found = false;
                        for (ParseObject parsePermission : params[0]) {
                            if (parsePermission.getObjectId().equals(oldPermission.getId()))
                                found = true;
                        }
                        if (!found) {
                            oldPermission.deleteFromLocal();
                        }
                    }
                    for (ParseObject parsePermission : params[0]) {
                        Permission permission = Permission.create(parsePermission);
                        // Check if permission is new
                        if (!existsLocal(permission)) {
                            mAddedPermissions = true;
                        }
                        permissions.add(permission);
                        Master master = permission.getMaster();
                        Slave slave = permission.getSlave();
                        if (master != null && slave == null) {
                            slave = Slave.fetchSlave(permission.getMasterId(), permission.getSlaveId());
                        } else {
                            master = Master.fetchMaster(permission.getMasterId());
                            slave = Slave.fetchSlave(permission.getMasterId(), permission.getSlaveId());
                        }
                        if (master != null && permission.getSlaveId() == Slave.ALL_SLAVES) {
                            master.fetchSlaves();
                        }
                        if (Permission.getType(permission.getType()) == 0) {
                            fetchPermissions(master);
                        }
                        permission.saveLocal();
                        if (master != null)
                            master.saveLocal();
                        if (slave != null)
                            slave.saveLocal();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return permissions;
        }

        @Override
        protected void onPostExecute(ArrayList<Permission> permissions) {
            mListener.onNewPermissions(permissions, mAddedPermissions);
        }
    }
}
