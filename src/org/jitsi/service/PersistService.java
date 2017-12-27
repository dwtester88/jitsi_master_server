package org.jitsi.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.JitsiApplication;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PersistService extends Service {

    private static final int INTERVAL = 10000; // poll every 3 secs
    private static final String YOUR_APP_PACKAGE_NAME = "YOUR_APP_PACKAGE_NAME";

    private static boolean stopTask;
    private PowerManager.WakeLock mWakeLock;

    /**
     * The logger
     */
    private static final Logger logger
            = Logger.getLogger(PersistService.class);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        stopTask = false;

        // Optional: Screen Always On Mode!
        // Screen will never switch off this way
        mWakeLock = null;
        /*if (settings.pmode_scrn_on){
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "a_tag");
            mWakeLock.acquire();
        }
*/
        // Start your (polling) task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {


               // isAppRunning(getApplicationContext(),"org.jitsi");

                // If you wish to stop the task/polling
                /*if (stopTask){
                    this.cancel();
                }*/

               // logger.info("mychange persistservice is ruuning");

                // The first in the list of RunningTasks is always the foreground task.
                ActivityManager am = (ActivityManager)  getSystemService(Context.ACTIVITY_SERVICE);
                // The first in the list of RunningTasks is always the foreground task.
                ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
                String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
                PackageManager pm = getApplicationContext().getPackageManager();
//                PackageInfo foregroundAppPackageInfo = null;
//                try {
//                    foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }
//                String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();

                //mychange
                //logger.info("mychange persistservice foregroundapp name "+foregroundTaskAppName);
                logger.info("mychange persistservice foregroundapp package name "+foregroundTaskPackageName);
               // ActivityManager.RunningTaskInfo foregroundTaskInfo = ActivityManager.RunningTaskInfo.getRunningTasks(1).get(0);
                //String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();

                // Check foreground app: If it is not in the foreground... bring it!
                if (!foregroundTaskPackageName.equals("org.jitsi")){
                  //  logger.info("mychange persistservice if condition foregroundapp name "+foregroundTaskAppName);
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.jitsi");
                    startActivity(LaunchIntent);
                }

            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, INTERVAL);
    }

    @Override
    public void onDestroy(){
        stopTask = true;
        if (mWakeLock != null)
            mWakeLock.release();
        super.onDestroy();
    }




        /*public boolean isAppRunning(final Context context, final String packageName) {

            final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
            if (procInfos != null)
            {
                for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                    logger.info("mychange persistService "+processInfo.processName);
                    if (processInfo.processName.equals(packageName)) {

                        return true;
                    }
                }
            }


            return false;
        }*/

}