package com.na.safekeydrive;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Arthur on 3/3/2015.
 */
public class DeviceAdminSampleReceiver extends DeviceAdminReceiver {

    void showToast(Context context, String msg) {
        //String status = context.getString(R.string.admin_receiver_status, msg);
        //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "context.getString(R.string.admin_receiver_status_enabled)");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        DevicePolicyManager policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        return "The application was deactivated from device administrator";
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
