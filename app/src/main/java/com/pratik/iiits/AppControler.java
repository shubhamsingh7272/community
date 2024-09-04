package com.pratik.iiits;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class AppControler extends Application implements LifecycleObserver {
    private FirebaseAuth mAuth;
    public static DatabaseReference USERS_DATABASE,INBOX_DATABASE,MESSAGE_DATABASE;
    private static String TAG=AppControler.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        try {
            mAuth = FirebaseAuth.getInstance();
            USERS_DATABASE = FirebaseDatabase.getInstance().getReference().child("users");
            USERS_DATABASE.keepSynced(true);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        Log.e(TAG,"App is in foreground State");
        updateParticularField("onlineStatus","online");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        Log.e(TAG,"App is in Background State");
        updateParticularField("onlineStatus","offline");
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY )
    public void OnDestroy(){
        updateParticularField("onlineStatus","offline");

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE )
    public void OnPause(){
        updateParticularField("onlineStatus","offline");

    }
    public void updateParticularField(String fieldName,String fieldValue){
        try {
            String currentFireBaseId=mAuth.getUid();
            if (!currentFireBaseId.equals("null")) {
                USERS_DATABASE.child(currentFireBaseId).child(fieldName).setValue(fieldValue);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}