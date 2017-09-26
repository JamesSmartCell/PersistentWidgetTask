package xyz.jamesb.widgettask.receiver;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import xyz.jamesb.widgettask.LocationService;
import xyz.jamesb.widgettask.Util;


public class NetworkStateReceiver extends BroadcastReceiver
{
	public void onReceive(Context context, Intent intent)
	{
		if(intent.getExtras()!=null) 
		{
			NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			
			if (netInfo != null && netInfo.getState().toString().equals("CONNECTED"))
	    	{
				if (netInfo.getTypeName().equals("WIFI"))
				{
					if (netInfo.getExtraInfo().equals("\"Ten Forward\""))
					{
						if (netInfo.getState() == NetworkInfo.State.CONNECTED)
						{
							//We connected at home
							Util.scheduleJob(context);
							//sendServiceIntent(String.valueOf(LocationService.LOCATION.GOT_HOME.ordinal()), context);
						}
					}
				}
	    	}
		}
	}

	private static void sendServiceIntent(String intent, Context rcvCtx)
	{
		Context ctx = LocationService.getContext();
		if (ctx == null)
		{
			//restart MBGService
			Intent startIntent = new Intent(rcvCtx, LocationService.class);
			startIntent.setAction(intent);
			rcvCtx.startService(startIntent);
		}
		else
		{
			try
			{
				Intent newIntent = new Intent(rcvCtx, LocationService.class);
				newIntent.setAction(intent);
				PendingIntent scanComms = PendingIntent.getService(rcvCtx, 0,
						newIntent, 0);

				scanComms.send();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}