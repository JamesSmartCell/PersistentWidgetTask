package xyz.jamesb.widgettask;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import xyz.jamesb.widgettask.receiver.JobReceiver;

/**
 * Created by James on 24/09/2017.
 */

public class Util
{
    public static void scheduleJob(Context context)
    {
        ComponentName serviceComponent = new ComponentName(context, JobReceiver.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(30 * 1000); //perform count every 30 seconds
        builder.setOverrideDeadline(20 * 1000); //allow up to 20 seconds variance
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
}
