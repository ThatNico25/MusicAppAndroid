package com.example.musicappandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

 @Override
 public void onReceive(Context context, Intent intent) {
   String action =  intent.getStringExtra("ACTION");
   Log.d("onStartCommandA", "TESTAAAAA : " + action);
     Log.d("onStartCommandA", "TESTBBBBB : " + intent.getAction());

   if(action!= null) {
    Intent actionInt = new Intent(context, MusicService.class);
    switch (action) {
         case "PAUSE_ACTION":
          Toast.makeText(context, "PAUSE", Toast.LENGTH_SHORT).show();
          actionInt.putExtra("ActionNotif", intent.getAction());
          context.startService(actionInt);
          break;

        case "PLAY_ACTION":
            Toast.makeText(context, "PLAY", Toast.LENGTH_SHORT).show();
            actionInt.putExtra("ActionNotif", intent.getAction());
            context.startService(actionInt);
            break;

        case "PREVIOUS_ACTION":
            Toast.makeText(context, "PREVIOUS", Toast.LENGTH_SHORT).show();
            actionInt.putExtra("ActionNotif", intent.getAction());
            context.startService(actionInt);
            break;

        case "NEXT_ACTION":
            Toast.makeText(context, "NEXT", Toast.LENGTH_SHORT).show();
            actionInt.putExtra("ActionNotif", intent.getAction());
            context.startService(actionInt);
            break;
     default:
      break;
    }
   }
  }


}