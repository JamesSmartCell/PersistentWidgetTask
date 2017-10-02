package xyz.jamesb.widgettask.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import xyz.jamesb.widgettask.LocationService;

/**
 * Created by James on 24/09/2017.
 */

// Receive the broadcast from the widget button and transfer the intent into the service
public class WidgetReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, LocationService.class);
        String action = intent.getAction();
        int widgetId = intent.getIntExtra("id", 0);
        int state = intent.getIntExtra("state", 0);
        i.setAction(action);
        i.putExtra("id", widgetId);
        i.putExtra("state", state);
        context.startService(i);
    }
}