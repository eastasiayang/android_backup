package com.yang.backup;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.yang.basic.LogUtils;

public class ActivityAbout extends Activity {

    private final String TAG = ActivityAbout.class.getSimpleName();
    TextView version;

    PackageInfo info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        version = (TextView) findViewById(R.id.TextView_about_version);
        try {
            String packageName = getPackageName();
            info = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "Could not find version info.");
        }
        version.setText(getResources().getString(R.string.version) + ": " + info.versionName);
    }
}
