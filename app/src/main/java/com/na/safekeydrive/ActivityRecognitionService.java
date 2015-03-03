package com.na.safekeydrive;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderApi;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ActivityRecognitionService extends IntentService {
    private String TAG = this.getClass().getSimpleName();
    public static final String ACTIVITY_TYPE = "activityType";
    public static final String ACTIVITY_TYPE_NAME = "activityTypeName";
    public static final String ACTIVITY_CONFIDENCE = "activityConfidence";
    public static final String ACTION = "com.na.safekeydrive.ACTIVITY_RECOGNITION_DATA";
    public static final String LOCATION_ACTION = "com.na.safekeydrive.LOCATION_DATA";
    public ActivityRecognitionService() {
        super("My Activity Recognition Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)){
//            Toast.makeText(getApplicationContext(),"activity detected",Toast.LENGTH_SHORT).show();
            Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            if (intent.hasExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED)){
                if (BuildConfig.DEBUG){
                    Log.e(TAG,"location update " + location.getSpeed());
                }
            }
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (BuildConfig.DEBUG){
                Log.i(TAG, getType(result.getMostProbableActivity().getType()) + "t" + result.getMostProbableActivity().getConfidence());
            }
            int type = result.getMostProbableActivity().getType();
            Intent i = new Intent("com.na.safekeydrive.ACTIVITY_RECOGNITION_DATA");
            i.putExtra(ACTIVITY_TYPE, type);
            i.putExtra(ACTIVITY_CONFIDENCE, result.getMostProbableActivity().getConfidence());
            i.putExtra(ACTIVITY_TYPE_NAME,getType(type));

            sendBroadcast(i);

        }
    }

    private String getType(int type){
        if(type == DetectedActivity.UNKNOWN)
            return "Unknown";
        else if(type == DetectedActivity.IN_VEHICLE)
            return "In Vehicle";
        else if(type == DetectedActivity.ON_BICYCLE)
            return "On Bicycle";
        else if(type == DetectedActivity.ON_FOOT)
            return "On Foot";
        else if(type == DetectedActivity.STILL)
            return "Still";
        else if(type == DetectedActivity.TILTING)
            return "Tilting";
        else
            return "";
    }

}
