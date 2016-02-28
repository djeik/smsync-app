package me.jerrington.smsync;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_DENIED) {
                final String[] perms = {"android.permission.RECEIVE_SMS"};
                requestPermissions(perms, 1337);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final Map<String, Integer> grantResultMap = new HashMap<String, Integer>();
        for (int i = 0; i < permissions.length; i++) {
            grantResultMap.put(permissions[i], grantResults[i]);
        }
        if (grantResultMap.get("android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_GRANTED) {
            Timber.d("Yay!");
        } else {
            Timber.d("Oh no!");
        }
    }

}
