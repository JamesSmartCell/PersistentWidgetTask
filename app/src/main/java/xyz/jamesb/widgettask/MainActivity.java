package xyz.jamesb.widgettask;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class MainActivity extends FragmentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start service for the first time to initialise the widget
        Intent service = new Intent(getApplicationContext(), LocationService.class);
        service.setAction(String.valueOf(LocationService.LOCATION.UPDATE.ordinal()));
        getApplicationContext().startService(service);

        //schedule first job
        Util.scheduleJob(getApplicationContext());
        finish();
    }
}
