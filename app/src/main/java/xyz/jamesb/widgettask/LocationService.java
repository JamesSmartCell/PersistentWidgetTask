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

/**
 * Created by James on 24/09/2017.
 */

//periodically wake the phone, get the location, write to blockchain

public class LocationService extends Service
{
    private static final String 	TAG = "LocationService";
    private static Context mCtx;

    public static final int STATE_ACTIVE = 1;
    public static final int STATE_OFF = 2;

    private static int mCounter = 0;
    private static int mCurrentState = STATE_OFF;

    public static int getCurrentState()
    {
        return mCurrentState;
    }

    public enum LOCATION
    {
        UPDATE,
        GOT_HOME,
        ACTION_TOGGLE,


        FINAL
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mCtx = this;

        try
        {
            int cValue = Integer.valueOf(intent.getAction());
            if (cValue < FINAL.ordinal())
            {
                LOCATION l = LOCATION.values()[cValue];

                switch (l)
                {
                    case GOT_HOME:
                        mCounter+=10;
                        break;

                    case UPDATE:
                        updateCycle();
                        break;

                    case ACTION_TOGGLE:
                        toggleButton();
                        break;

                    default:
                        break;
                }

                updateWidget();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    private void updateCycle()
    {
        if (mCurrentState == STATE_ACTIVE)
        {
            mCounter++;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("count", mCounter);
            editor.apply();
        }
    }

    public void toggleButton()
    {
        if (mCurrentState == STATE_OFF)
        {
            mCurrentState = STATE_ACTIVE;
        }
        else
        {
            mCurrentState = STATE_OFF;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("currentState", mCurrentState);
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp != null)
        {
            mCurrentState = sp.getInt("currentState", STATE_OFF);
            mCounter = sp.getInt("count", 0);
        }
        else
        {
            mCurrentState = STATE_OFF;
            mCounter = 0;
        }

        mCtx = this;
        Util.scheduleJob(getApplicationContext()); //ensure we always start
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void updateWidget()
    {
        String buttonText = "";
        if (mCurrentState == STATE_OFF)
        {
            buttonText = getResources().getString(R.string.start);
        }
        else
        {
            buttonText = getResources().getString(R.string.stop);
        }

        AppWidgetManager man = AppWidgetManager.getInstance(this);
        int[] ids = man.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        //Restart app if widget clicked outside of the box
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction("restart");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent rPI = stackBuilder.getPendingIntent(0,  PendingIntent.FLAG_UPDATE_CURRENT);

        //add intent to broadcast a message. This is added onto the button
        Intent intent = new Intent(this, WidgetReceiver.class);
        intent.setAction(String.valueOf(ACTION_TOGGLE.ordinal()));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        for (int widgetId : ids)
        {
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                    R.layout.widget);
            // Set the text
            remoteViews.setTextViewText(R.id.textActive, "" + mCounter);
            remoteViews.setOnClickPendingIntent(R.id.relLayout, rPI);
            remoteViews.setOnClickPendingIntent(R.id.button2, pendingIntent);
            remoteViews.setTextViewText(R.id.button2, buttonText);
            man.updateAppWidget(widgetId, remoteViews);
        }
    }
}
