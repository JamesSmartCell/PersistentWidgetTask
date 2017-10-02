package xyz.jamesb.widgettask;

import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import xyz.jamesb.widgettask.receiver.WidgetReceiver;
import xyz.jamesb.widgettask.widget.WidgetProvider;

import static xyz.jamesb.widgettask.LocationService.LOCATION.ACTION_TOGGLE;
import static xyz.jamesb.widgettask.LocationService.LOCATION.FINAL;
import static xyz.jamesb.widgettask.LocationService.LOCATION.RESTART;

/**
 * Created by James on 24/09/2017.
 */


public class LocationService extends Service
{
    private static final String 	TAG = "LocationService";
    private static Context mCtx;

    public static final int STATE_ACTIVE = 1;
    public static final int STATE_OFF = 2;

    public enum LOCATION
    {
        UPDATE,
        GOT_HOME,
        ACTION_TOGGLE,
        RESTART,

        FINAL
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mCtx = this;

        try
        {
            int cValue = Integer.valueOf(intent.getAction());
            int widgetId = intent.getIntExtra("id", 0);
            int state = intent.getIntExtra("state", 0);
            if (cValue < FINAL.ordinal())
            {
                LOCATION l = LOCATION.values()[cValue];

                switch (l)
                {
                    case GOT_HOME:
                        updateWidget(10);
                        break;

                    case UPDATE:
                        updateWidget(1);
                        break;

                    case ACTION_TOGGLE:
                        toggleButton(widgetId, state);
                        updateWidget(0);
                        break;

                    case RESTART:
                        break;

                    default:
                        break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    private int getWidgetState(int widgetId)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String key = "wid" + widgetId + "state";
        int state = sp.getInt(key, 0);
        return state;
    }

    private void toggleButton(int widgetId, int state)
    {
        if (state == STATE_OFF)
        {
            state = STATE_ACTIVE;
        }
        else
        {
            state = STATE_OFF;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String key = "wid" + widgetId + "state";
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, state);
        editor.apply();
    }

    public static Context getContext()
    {
        return mCtx;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mCtx = this;
        Util.scheduleJob(getApplicationContext()); //ensure we always start
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void updateWidget(int amount)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        AppWidgetManager man = AppWidgetManager.getInstance(this);
        int[] ids = man.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
        for (int widgetId : ids)
        {
            //pull data for this widget
            String baseKey = "wid" + widgetId;
            int counter = sp.getInt(baseKey + "count", 0);
            int buttonState = sp.getInt(baseKey + "state", STATE_OFF);
            if (buttonState == STATE_ACTIVE)
            {
                counter = updateWidgetCounter(widgetId, amount, counter);
            }

            String buttonText = getButtonText(buttonState);

            //Restart app if widget clicked outside of the box, keeping track of which widget was clicked
            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(String.valueOf(RESTART.ordinal()));
            resultIntent.putExtra("id", widgetId);
            resultIntent.setFlags(widgetId);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent rPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //add intent to broadcast a message. This is added onto the button
            Intent intent = new Intent(this, WidgetReceiver.class);
            intent.setAction(String.valueOf(ACTION_TOGGLE.ordinal()));
            intent.putExtra("id", widgetId);
            intent.putExtra("state", buttonState);
            int uid = baseKey.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    uid, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                    R.layout.widget);
            // Set the text
            remoteViews.setTextViewText(R.id.textActive, "" + counter);
            remoteViews.setOnClickPendingIntent(R.id.relLayout, rPI);
            remoteViews.setOnClickPendingIntent(R.id.button2, pendingIntent);
            remoteViews.setTextViewText(R.id.button2, buttonText);
            man.updateAppWidget(widgetId, remoteViews);
        }
    }

    private String getButtonText(int state)
    {
        String buttonText = "";
        if (state == STATE_OFF)
        {
            buttonText = getResources().getString(R.string.start);
        }
        else
        {
            buttonText = getResources().getString(R.string.stop);
        }

        return buttonText;
    }

    private int updateWidgetCounter(int widgetId, int amount, int counter)
    {
        if (amount != 0)
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String key = "wid" + widgetId + "count";
            counter += amount;
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, counter);
            editor.apply();
        }

        return counter;
    }
}