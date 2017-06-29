package com.quypn.tramxanggannhat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.example.quypn.tramxanggannhat.MainActivity;
import com.example.quypn.tramxanggannhat.R;

import static android.os.Build.VERSION_CODES.M;

public class Main2Activity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CheckPermission();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    public void CheckPermission() {
        if (Build.VERSION.SDK_INT >= M) {
            if (!HasPermission()) {
                RequestPermission();
            }
            else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    private boolean HasPermission() {
        int res = 0;
        String[] permission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String i : permission) {
            res = checkCallingOrSelfPermission(i);
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void RequestPermission() {
        String[] permission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= M) {
            requestPermissions(permission, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean allowed = true;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int res : grantResults) {
                    allowed = (allowed && (res == PackageManager.PERMISSION_GRANTED));
                }
                break;
            default:
                allowed = false;
                break;
        }
        if (!allowed) {
            finish();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
