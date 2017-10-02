package xyz.jamesb.widgettask.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import xyz.jamesb.widgettask.LocationService;

import static xyz.jamesb.widgettask.LocationService.LOCATION.GOT_HOME;

public class NetworkStateReceiver extends BroadcastReceiver
{
	private static final String WIFI_NAME = "Ten Forward"; //Your home WiFi SSID

	public void onReceive(Context context, Intent intent)
	{
		try
		{
			if (intent.getExtras() != null)
			{
				NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				if (netInfo != null && netInfo.getState().toString().equals("CONNECTED"))
				{
					if (netInfo.getTypeName().equals("WIFI"))
					{
						if (netInfo.getExtraInfo().equals("\"" + WIFI_NAME + "\""))
						{
							if (netInfo.getState() == NetworkInfo.State.CONNECTED)
							{
								Intent bIntent = new Intent(context, LocationService.class);
								bIntent.setAction(String.valueOf(GOT_HOME.ordinal()));
								bIntent.putExtra("id", 0);
								bIntent.putExtra("state", 0);
								context.startService(bIntent);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}