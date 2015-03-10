package com.na.safekeydrive;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class InputMethodChangeReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = InputMethodChangeReceiver.class.getSimpleName();
    private static final long UPDATE_INTERVAL_MS = 500;
    private static final long FASTEST_INTERVAL_MS = 250;

    public InputMethodChangeReceiver() {
    }

    private static PendingIntent pIntent;
//    private static PendingIntent
    private static  GoogleApiClient mGoogleApiClient;
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {

            mContext = context;
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();

            final int N = mInputMethodProperties.size();

            for (int i = 0; i < N; i++) {

                InputMethodInfo imi = mInputMethodProperties.get(i);

                if (imi.getId().equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {

                    if (imi.getServiceName().equals(SafeDriveKey.class.getName())){
                        // Set the application as a device administrator
                        DevicePolicyManager mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        ComponentName mDeviceAdminSample = new ComponentName(mContext, com.na.safekeydrive.DeviceAdminSampleReceiver.class);
                        if(!mDPM.isAdminActive(mDeviceAdminSample)) {
                            Intent deviceAdminIntent = new Intent(mContext, MainActivity.class);
                            deviceAdminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,mDeviceAdminSample);
                            mContext.startActivity(deviceAdminIntent);
                        }
//                                    int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
//                                    if(resp == ConnectionResult.SUCCESS){
//                                        buildGoogleApiClient(context);
//                                    }
//                                    else
//                                    {
//                                        Intent googleDialog = new Intent(mContext,GooglePlayDialogShellActivity.class);
//                                        Bundle bundle = new Bundle();
//                                        bundle.putInt("errorCode",resp);
//                                        googleDialog.putExtras(bundle);
//                                        googleDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        mContext.startActivity(googleDialog);
//                                    }
//                        XSmsApplication.getInstance().startActivityRecognition();
                    }
                    else
                    {
//                        if (mGoogleApiClient != null){
//                            Log.i(TAG,"Stop activity recognition updates");
//                            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,pIntent);
//                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,pIntent);
//                            mGoogleApiClient.disconnect();
//                        }
//                        XSmsApplication.getInstance().stopActivityRecognition();
                    }
                    break;
                }
            }
        }
    }

    private void buildGoogleApiClient(Context context) {

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(mContext, "google client connected", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mContext, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(mContext, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,500,pIntent);

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient,locationRequest, pIntent);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(mContext,"google connection suspended",Toast.LENGTH_SHORT).show();
        Log.e(InputMethodChangeReceiver.class.getSimpleName(),"google client connection suspended reconnecting");
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(mContext,"google client connection failed",Toast.LENGTH_SHORT).show();
        Log.e(InputMethodChangeReceiver.class.getSimpleName(),"google client connection failed with code " + connectionResult.getErrorCode());
        mGoogleApiClient.connect();
    }
}
