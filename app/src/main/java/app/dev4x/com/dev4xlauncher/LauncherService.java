package app.dev4x.com.dev4xlauncher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.app.Activity;
import android.app.ActivityManager;
import java.util.List;

/**
 * Created by hirendave on 9/25/15.
 */
public class LauncherService extends Service {
    private Thread t = null;
    private Context ctx = null;
    private boolean running = false;
    private static final long INTERVAL = 2000;
    private SharedPreferencesHelper spHelper;
    private String[] supportedPackages = {
        "com.moonshot.dev4x","com.einbrain.swahili01","kr.co.smartstudy.phonicsiap_android_googlemarket","com.originatorkids.EndlessAlphabet"
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running =false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        ctx = this;
        spHelper = new SharedPreferencesHelper(this);
        // start a thread that periodically checks if your app is in the foreground
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    //do somehting here.
                    String launchMainApp = spHelper.getPreferenceValue("launch");
                    Log.v("Service","Service Thread");
                    if(launchMainApp.equals("yes")){
                        Log.v("Service","Leanring app mode is on if user try to go to other app restart activity");
                        //get current running app.
                        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                        if(taskInfo.get(0) != null){
                            String runningPackageName = taskInfo.get(0).topActivity.getPackageName();
                            if(runningPackageName.equals("app.dev4x.com.dev4xlauncher")){
                                //No problem
                            }else{
                                boolean isValidPackage = false;
                                for(int i=0;i<supportedPackages.length;i++){
                                    if(supportedPackages[i].equals(runningPackageName)){
                                        isValidPackage = true;
                                        break;
                                    }
                                }

                                if(isValidPackage == true){
                                    Log.v("Package","Package is valid");
                                }else{
                                    Log.v("Package","Not a valid package");
                                    Intent i = new Intent(ctx, LauncherActvity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ctx.startActivity(i);
                                }
                            }
                        }

                    }else{
                        Log.v("Service","Leanring app mode is off let user go to other activity");
                    }
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                    }
                }while(running);
                stopSelf();
            }
        });

        t.start();
        return Service.START_NOT_STICKY;
    }
}
