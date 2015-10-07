package app.dev4x.com.dev4xlauncher;

import app.dev4x.com.dev4xlauncher.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.content.ComponentName;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.view.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import android.view.Window;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.pm.ResolveInfo;
import java.util.Collections;
import android.widget.GridView;
import app.dev4x.com.dev4xlauncher.AndroidApplicationInfo;
import android.widget.ArrayAdapter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.content.Context;
import android.content.res.Resources;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View.OnClickListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LauncherActvity extends Activity implements OnItemClickListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));

    private static final String PREF_LAUNCHER_MODE = "pref_launcher_mode";
    private SharedPreferencesHelper spHelper = new SharedPreferencesHelper(this);

    private static ArrayList<AndroidApplicationInfo> mApplications;
    private GridView mAllApps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launcher_actvity);

        String password = spHelper.getPreferenceValue("password");
        String launchMainApp = spHelper.getPreferenceValue("launch");

        findViewById(R.id.learningAppIcon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLearningApp(null);
            }
        });

        if(password == null){
            spHelper.savePreferences("password","dev4x");
        }

        if(launchMainApp == null){
            spHelper.savePreferences("launch","yes");
        }

        launchMainApp = spHelper.getPreferenceValue("launch");
        if(launchMainApp.equals("yes")){
            launchLearningApp(null);
        }else{
            loadApplications();
            mAllApps = (GridView) findViewById(R.id.applicationsGrid);
            mAllApps.setAdapter(new ApplicationsAdapter(this, mApplications));
            mAllApps.setOnItemClickListener(this);
            findViewById(R.id.exitLauncherButton).setVisibility(View.GONE);
            findViewById(R.id.startLauncherButton).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        String launchMainApp = spHelper.getPreferenceValue("launch");
        if(launchMainApp.equals("yes")){
            findViewById(R.id.learningAppIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.exitLauncherButton).setVisibility(View.VISIBLE);
            findViewById(R.id.startLauncherButton).setVisibility(View.GONE);
            findViewById(R.id.applicationsGrid).setVisibility(View.GONE);
        }else{
            findViewById(R.id.learningAppIcon).setVisibility(View.GONE);
            findViewById(R.id.exitLauncherButton).setVisibility(View.GONE);
            findViewById(R.id.startLauncherButton).setVisibility(View.VISIBLE);
            findViewById(R.id.applicationsGrid).setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onBackPressed() {
        // nothing to do here
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
        final AndroidApplicationInfo appInfo = (AndroidApplicationInfo) mAllApps.getAdapter().getItem(
                position);
        startActivity(appInfo.intent);
    }

    public void exitLauncher(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().equals(spHelper.getPreferenceValue("password"))){
                    spHelper.savePreferences("launch","no");
                    dialog.cancel();
                    finish();
                    System.exit(0);
                }else{
                    showWrongPasswordPrompt();
                    dialog.cancel();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void launchLearningApp(View view){
        //launching learning app.
        findViewById(R.id.exitLauncherButton).setVisibility(View.VISIBLE);
        findViewById(R.id.startLauncherButton).setVisibility(View.GONE);
        findViewById(R.id.learningAppIcon).setVisibility(View.VISIBLE);
        spHelper.savePreferences("launch", "yes");
        PackageManager pm = this.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo("com.moonshot.dev4x", 0);
        }catch (PackageManager.NameNotFoundException e) {
            info = null;
        }

        if(info == null){
            //Applicaiton not found, nothing to do but show Toast to user.
            Toast toast = Toast.makeText(this, "Learning App Not Found", Toast.LENGTH_LONG);
            toast.show();
        }else{
            //Launch the application
            Intent launchIntent = pm.getLaunchIntentForPackage("com.moonshot.dev4x");
            startActivity(launchIntent);
        }
    }

    public void showWrongPasswordPrompt(){
        Toast toast = Toast.makeText(this, "Wrong Password", Toast.LENGTH_LONG);
        toast.show();
    }

    public void showPasswordSetPrompt(){
        Toast toast = Toast.makeText(this, "Your password is set", Toast.LENGTH_LONG);
        toast.show();
    }

    public void changePassword(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setText(spHelper.getPreferenceValue("password"));
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPasswordSetPrompt();
                spHelper.savePreferences("password",input.getText().toString());
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void loadApplications(){
        final PackageManager manager = getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            if (mApplications == null) {
                mApplications = new ArrayList<AndroidApplicationInfo>(count);
            }
            mApplications.clear();

            for (int i = 0; i < count; i++) {
                final AndroidApplicationInfo application = new AndroidApplicationInfo();
                final ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.setActivity(new ComponentName(
                                info.activityInfo.applicationInfo.packageName, info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(manager);

                mApplications.add(application);
            }
        }
    }

    private class ApplicationsAdapter extends ArrayAdapter<AndroidApplicationInfo> {
        private final Rect mOldBounds = new Rect();
        private final int mAppIconHeight;
        private final int mAppIconWidth;

        public ApplicationsAdapter(Context context, ArrayList<AndroidApplicationInfo> apps) {
            super(context, 0, apps);
            final Resources resources = getContext().getResources();
            mAppIconWidth = (int) resources.getDimension(android.R.dimen.app_icon_size);
            mAppIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AndroidApplicationInfo info = mApplications.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.launcher_item, parent, false);
            }

            Drawable icon = info.icon;

            if (!info.filtered) {
                int width = mAppIconWidth;
                int height = mAppIconHeight;

                final int iconWidth = icon.getIntrinsicWidth();
                final int iconHeight = icon.getIntrinsicHeight();

                if (icon instanceof PaintDrawable) {
                    final PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                }

                if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
                    final float ratio = (float) iconWidth / iconHeight;

                    if (iconWidth > iconHeight) {
                        height = (int) (width / ratio);
                    } else if (iconHeight > iconWidth) {
                        width = (int) (height * ratio);
                    }

                    final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565;
                    final Bitmap thumb = Bitmap.createBitmap(width, height, c);
                    final Canvas canvas = new Canvas(thumb);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
                    // Copy the old bounds to restore them later
                    // If we were to do oldBounds = icon.getBounds(),
                    // the call to setBounds() that follows would
                    // change the same instance and we would lose the
                    // old bounds
                    mOldBounds.set(icon.getBounds());
                    icon.setBounds(0, 0, width, height);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = info.icon = new BitmapDrawable(getResources(), thumb);
                    info.filtered = true;
                }
            }

            final TextView textView = (TextView) convertView.findViewById(R.id.label);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            textView.setText(info.title);

            return convertView;
        }
    }
}
