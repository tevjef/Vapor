package deadpixel.app.vapor.services;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ClipboardReceiver extends BroadcastReceiver {

    private ClipboardManager clipboard;
    private ClipData clipData;

    public static String CLIPBOARD_TEXT = "clipboard_text";

    public ClipboardReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        String link = intent.getStringExtra(CLIPBOARD_TEXT);

        clipData = ClipData.newPlainText("Copied: " + link, link);

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clipData);

        Toast.makeText(context, "Copied: " + link, Toast.LENGTH_SHORT).show();
    }
}
