package xyz.jamesb.widgettask.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import xyz.jamesb.widgettask.LocationService;
import xyz.jamesb.widgettask.MainActivity;
import xyz.jamesb.widgettask.R;

import static xyz.jamesb.widgettask.LocationService.LOCATION.ACTION_TOGGLE;

public class WidgetProvider extends AppWidgetProvider
{
	@SuppressLint("NewApi")
	private RemoteViews inflateLayout(Context context, int appWidgetId)
	{
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setAction("startWidget");
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent rPI = stackBuilder.getPendingIntent(0,  PendingIntent.FLAG_UPDATE_CURRENT);
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		remoteViews.setOnClickPendingIntent(R.id.relLayout, rPI);

		//I think this is called when the widget is created. So, now call the service to begin with
		Intent service = new Intent(context, LocationService.class);
		service.setAction(String.valueOf(LocationService.LOCATION.UPDATE.ordinal()));
		context.startService(service);

        return remoteViews;
	}


	@Override
	public void onUpdate(Context context, AppWidgetManager man, int[] appWidgetIds)
	{	
		int state = 0;
		try
		{
			state = LocationService.getCurrentState();
			RemoteViews remoteView;

			SharedPreferences sp = context.getSharedPreferences("ZaiGnarly", Context.MODE_PRIVATE);
			if (sp != null)
			{
				state = sp.getInt("currentState", 0);
			}

			//we only refresh the widget here if there's not an active service
			for (int widgetId : appWidgetIds)
			{
				remoteView = getRemoteViewFromState(state, context, widgetId);

				if (remoteView != null)
				{
					setRemoteView(man, context, widgetId, remoteView);
				}
			}
		}
		catch (Exception e)
		{

		}

		super.onUpdate(context, man, appWidgetIds);
	}

	@SuppressLint("NewApi")
	private void setRemoteView(AppWidgetManager appWidgetManager, Context context,
                               int appWidgetId, RemoteViews remoteView)
	{
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setAction("startWidget");
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent rPI = stackBuilder.getPendingIntent(0,  PendingIntent.FLAG_UPDATE_CURRENT);
		remoteView.setOnClickPendingIntent(R.id.relLayout, rPI);
        appWidgetManager.updateAppWidget(appWidgetId, remoteView);
	}

    private RemoteViews getRemoteViewFromState(int action, Context context, int widgetId)
	{
		RemoteViews remoteView = inflateLayout(context, widgetId); //MBGService handles updates in this state - but we could easily pull the data from the service and do update here

		Intent startIntent = new Intent(context, xyz.jamesb.widgettask.LocationService.class);
		startIntent.setAction(String.valueOf(ACTION_TOGGLE.ordinal()));
		//context.startService(startIntent);
		PendingIntent scheduleAction = PendingIntent.getBroadcast(LocationService.getContext(), 0,
				startIntent, PendingIntent.FLAG_ONE_SHOT);

		remoteView.setOnClickPendingIntent(R.id.button2, scheduleAction);
    	
    	return remoteView;
	}
}
