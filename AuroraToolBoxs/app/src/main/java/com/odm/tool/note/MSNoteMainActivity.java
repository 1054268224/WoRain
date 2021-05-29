package com.odm.tool.note;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;


public class MSNoteMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.app.odm.supernote", "app.odm.supernote.module.notes.main.NoteMainActivity");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
		finish();
    }

}
