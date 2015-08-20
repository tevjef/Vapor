package com.tevinjeffrey.vapr.services;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

public class IntentBridge extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        getIntent();
        Intent intent = new Intent(this, UploadService.class);
        intent.setData(getIntent().getData());
        startService(intent);
    }
}
