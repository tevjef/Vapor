package com.tevinjeffrey.vapor.services;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

public class IntentBridge extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, UploadService.class);
        intent.setData(getIntent().getData());
        intent.setAction(UploadService.ACTION_UPLOAD);
        startService(intent);
        finish();
    }
}
