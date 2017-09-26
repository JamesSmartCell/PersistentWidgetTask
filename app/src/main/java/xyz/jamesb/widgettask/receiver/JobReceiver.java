package xyz.jamesb.widgettask.receiver;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import xyz.jamesb.widgettask.LocationService;
import xyz.jamesb.widgettask.Util;

/**
 * Created by James on 24/09/2017.
 */

public class JobReceiver extends JobService
{
    @Override
    public boolean onStartJob(JobParameters jobParameters)
    {
        int ordinal = LocationService.LOCATION.UPDATE.ordinal();
        int uid = Integer.toString(ordinal).hashCode();
        String xIntent = String.valueOf(ordinal);

        Intent service = new Intent(getApplicationContext(), LocationService.class);
        service.setAction(xIntent);

        getApplicationContext().startService(service);
        Util.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters)
    {
        return false;
    }
}
