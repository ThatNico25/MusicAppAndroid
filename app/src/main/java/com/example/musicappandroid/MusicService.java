package com.example.musicappandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class MusicService extends Service {

    private IBinder mBinder = new MyBinder();
    private ActionPlaying actionPlaying;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("onStartCommandA", "BIND!!!");
        return mBinder;
    }
    public  class MyBinder extends Binder {
        MusicService getService() { return  MusicService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        String actionName = intent.getStringExtra("ActionNotif");
        Log.d("onStartCommandA", "StartService : " + actionName);
        switch (actionName) {
            case "PAUSE":
                if(actionPlaying != null) {
                    Log.d("onStartCommandA", "Pause! Service!!!");
                    actionPlaying.pauseAction();
                }
                else {
                    Log.d("onStartCommandA", "Service Fail ! ");
                }
                break;
            case "PLAY":
                if(actionPlaying != null) {
                    Log.d("onStartCommandA", "Pause! Service!!!");
                    actionPlaying.playAction();
                }
                else {
                    Log.d("onStartCommandA", "Service Fail ! ");
                }
                break;
            case "PREVIOUS":
                if(actionPlaying != null) {
                    Log.d("onStartCommandA", "Pause! Service!!!");
                    actionPlaying.previousAction();
                }
                else {
                    Log.d("onStartCommandA", "Service Fail ! ");
                }
                break;
            case "NEXT":
                if(actionPlaying != null) {
                    Log.d("onStartCommandA", "Pause! Service!!!");
                    actionPlaying.nextAction();
                }
                else {
                    Log.d("onStartCommandA", "Service Fail ! ");
                }
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    public void setCallback(ActionPlaying a_actionPlayer){
        this.actionPlaying = a_actionPlayer;
        Log.d("onStartCommandA", "ActionSET!!!");
    }
}