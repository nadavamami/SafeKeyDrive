package com.na.safekeydrive;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Arthur on 3/4/2015.
 */
public class MainActivity extends Activity {
    protected static final int REQUEST_CODE_ENABLE_ADMIN=1;
    private static final int SECURITY_PRIVILEGES = 10;
    public static int RESULT = 1;
    static final int ACTIVATION_REQUEST = 47; // identifies our request id
    Button button;
    DevicePolicyManager policyManager;
    ComponentName deviceAdmin;
    static final int RESULT_ENABLE = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pref);
        policyManager = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdmin = new ComponentName(this, DeviceAdminSampleReceiver.class);
        DevicePolicyManager mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdminSample = new ComponentName(MainActivity.this, com.na.safekeydrive.DeviceAdminSampleReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        //   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,mDeviceAdminSample);
        startActivityForResult(intent, RESULT_ENABLE);
        //addListenerOnButton();
        finish();
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!policyManager.isAdminActive(deviceAdmin)) {
                   /* Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            deviceAdmin);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Need new Admin");
                    startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);*/
                    DevicePolicyManager mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName mDeviceAdminSample = new ComponentName(MainActivity.this, com.na.safekeydrive.DeviceAdminSampleReceiver.class);
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                 //   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,mDeviceAdminSample);
                    startActivityForResult(intent, RESULT_ENABLE);
	        /*intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
	            "Additional text explaining why this needs to be added.");
	       */
                    // Toast.makeText(getApplicationContext(), "activity is to be started now",Toast.LENGTH_LONG).show();


                }
                else
                {
                    policyManager.lockNow();
                }



            }

        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVATION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Administration enabled!");
                } else {
                    Log.i("DeviceAdminSample", "Administration enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            super.onActivityResult(requestCode, resultCode, intent);
            if (REQUEST_ENABLE == requestCode)
            {
                if (requestCode == RESULT_OK) {

                    Toast.makeText(MainActivity.this, "YAYYYYYY",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }*/

    public class DeviceAdminSampleReceiver extends DeviceAdminReceiver {

        void showToast(Context context, String msg) {
            //String status = context.getString(R.string.admin_receiver_status, msg);
            Toast.makeText(context, "status", Toast.LENGTH_SHORT).show();
        }
        public DeviceAdminSampleReceiver()
        {

        }
        @Override
        public void onEnabled(Context context, Intent intent) {
            showToast(context, "context.getString(R.string.admin_receiver_status_enabled)");
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {

            return "context.getString(R.string.admin_receiver_status_disable_warning)";
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            showToast(context, "context.getString(R.string.admin_receiver_status_disabled)");
        }

        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            showToast(context, "context.getString(R.string.admin_receiver_status_pw_changed)");
        }
    }
}
