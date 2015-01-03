package deadpixel.app.vapor.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import deadpixel.app.vapor.MenuHandler;
import deadpixel.app.vapor.R;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.database.FilesManager;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.libs.TouchImageView;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 8/26/2014.
 */
public class ImageViewActivity extends ActionBarActivity {

    private DatabaseItem dbItem;
    ActionBar ab;
    private ClipboardManager clipboard;
    // Creates a new text clip to put on the clipboard
    private ClipData clip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);



        ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);

        ab.setIcon(R.drawable.ic_image_white);

        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        int subTitleId = getResources().getIdentifier("action_bar_subtitle", "id",
                "android");

        TextView tv = (TextView) findViewById(titleId);
        tv.setTextColor(getResources().getColor(R.color.white));
        //tv.setTypeface(AppUtils.getTextStyle(AppUtils.TextStyle.LIGHT_NORMAL));

        tv = (TextView) findViewById(subTitleId);
        tv.setTypeface(AppUtils.getTextStyle(AppUtils.TextStyle.LIGHT_NORMAL));
        tv.setTextColor(getResources().getColor(R.color.text_highlight_color));


        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        String name = intent.getStringExtra(MainActivity.EXTRA_NAME);
        dbItem = DatabaseManager.getItemsByName(name).get(0);
        ab.setTitle(dbItem.getName());
        TouchImageView img = (TouchImageView) findViewById(R.id.touch_view);
        Picasso.with(this).load(dbItem.getRemoteUrl()).into(img);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.normal_item_popup_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;

        }

        final String link = MenuHandler.getLink(dbItem);

        switch (id) {
            case R.id.popup_copy_link:
                clip = ClipData.newPlainText("Copied: " + link, link);

                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied: " + link, Toast.LENGTH_SHORT).show();
                break;

            case R.id.popup_delete:

                new AlertDialog.Builder(this)
                        .setTitle("Are you sure?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                if (AppUtils.getInstance().isNetworkConnected()) {
                                    FilesManager.deleteItem(dbItem);
                                    finish();
                                } else {
                                    AppUtils.getEventBus().post(new ErrorEvent(null, AppUtils.NO_CONNECTION));
                                }

                            }

                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
                break;

            case R.id.popup_share_link:
                AppUtils.getInstance().linkShareIntent(link);
                break;
            case R.id.popup_download:
                AppUtils.getInstance().linkDownloadIntent(dbItem.getDownloadUrl());
                break;

            case R.id.popup_rename:
                // Set an EditText view to get user input
                final EditText input = new EditText(this);


                new AlertDialog.Builder(this)
                        .setTitle("Rename file")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();


                                if (AppUtils.getInstance().isNetworkConnected()) {
                                    try {
                                        AppUtils.addToRequestQueue(AppUtils.api.rename(dbItem, value));
                                    } catch (CloudAppException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    AppUtils.getEventBus().post(new ErrorEvent(null, AppUtils.NO_CONNECTION));
                                }

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();

                break;
            default:
                break;



        }

        return true;

    }


}
