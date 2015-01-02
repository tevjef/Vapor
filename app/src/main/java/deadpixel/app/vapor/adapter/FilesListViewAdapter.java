package deadpixel.app.vapor.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import deadpixel.app.vapor.MenuHandler;
import deadpixel.app.vapor.R;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.FilesManager;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 2/1/14.
 */
public class FilesListViewAdapter extends ArrayAdapter {

    LayoutInflater mInflater;
    private Context context;
    private ClipboardManager clipboard;
    // Creates a new text clip to put on the clipboard
    private ClipData clip;


    private ArrayList<DatabaseItem> databaseItems;

    public FilesListViewAdapter(Context context, ArrayList<DatabaseItem> item){
        super(context, -1, item);
        this.context = context;
        this.databaseItems = item;
        mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Gets a handle to the clipboard service.
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */

    @Override
    public int getCount() {
        return databaseItems.size();
    }

    @Override
    public Object getItem(int position) {
        return databaseItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        View rowView = convertView;
        if (rowView == null) {
            viewHolder = new ViewHolder();


            rowView = mInflater.inflate(R.layout.files_list_item, parent, false);

            viewHolder.image = (ImageView) rowView.findViewById(R.id.files_list_image);
            viewHolder.title = (TextView) rowView.findViewById(R.id.files_list_title);
            viewHolder.date = (TextView) rowView.findViewById(R.id.files_list_date);
            viewHolder.views = (TextView) rowView.findViewById(R.id.files_list_views);

            rowView.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        imageResolver(position, viewHolder, rowView);

        viewHolder.title.setText(databaseItems.get(position).getName());
        viewHolder.views.setText(String.valueOf(databaseItems.get(position).getViewCounter()));
        try {
            viewHolder.date.setText(databaseItems.get(position).getFormattedCreatedAt());
        } catch (CloudAppException e) {
            e.printStackTrace();
        }

        //Copy link view
        final View copyLink = rowView.findViewById(R.id.ic_link);

        //Copy Link container view
        final View copyLinkContainer = rowView.findViewById(R.id.copy_link_container);

        copyLinkContainer.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                copyLink.setEnabled(true);
                copyLink.getHitRect(delegateArea);
                delegateArea.left -= 600;
                delegateArea.bottom += 600;
                delegateArea.top -= 600;
                delegateArea.right += 600;

                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, copyLink);

                if(View.class.isInstance(copyLink.getParent())) {
                    ((View)copyLink.getParent()).setTouchDelegate(touchDelegate);                                       }
            }
        });

        //Set listener for copy link click. Gets appropriate link from dbItem.
        copyLink.setOnClickListener(new View.OnClickListener() {
            final String link = MenuHandler.getLink(databaseItems.get(position));
            @Override
            public void onClick(View v) {
                clip = ClipData.newPlainText("Copied: " + link, link);
                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Copied: " + link, Toast.LENGTH_SHORT).show();
            }
        });



        //View that anchors the popup menu.
        final View anchor = rowView.findViewById(R.id.popup_anchor);

        //Container for the anchor
        final View popupContainer = rowView.findViewById(R.id.popup_container);

        popupContainer.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();

                anchor.setEnabled(true);

                anchor.getHitRect(delegateArea);

                delegateArea.left -= 600;
                delegateArea.bottom += 600;
                delegateArea.top -= 600;
                delegateArea.right += 600;

                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, anchor);

                if(View.class.isInstance(anchor.getParent())) {
                    ((View)anchor.getParent()).setTouchDelegate(touchDelegate);                                       }
            }
        });

        //On anchor click. Open popup menu
        anchor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                PopupMenu popupMenu = null;

                final DatabaseItem dbItem = databaseItems.get(position);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    popupMenu = new PopupMenu(context, anchor);

                    if (dbItem.getItemType() == CloudAppItem.Type.BOOKMARK) {
                        popupMenu.inflate(R.menu.bookmark_item_popup_menu);
                    } else {
                        popupMenu.inflate(R.menu.normal_item_popup_menu);
                    }


                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(android.view.MenuItem item) {



                            int id = item.getItemId();

                            final String link = MenuHandler.getLink(databaseItems.get(position));

                            switch (id) {
                                case R.id.popup_copy_link:
                                    clip = ClipData.newPlainText("Copied: " + link, link);

                                    // Set the clipboard's primary clip.
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(context, "Copied: " + link, Toast.LENGTH_SHORT).show();
                                    break;

                                case R.id.popup_delete:

                                    new AlertDialog.Builder(context)
                                            .setTitle("Are you sure?")
                                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {

                                                    if(AppUtils.getInstance().isNetworkConnected()) {
                                                        FilesManager.deleteItem(dbItem);
                                                        remove(dbItem);
                                                        notifyDataSetChanged();
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
                                    linkShareIntent(link);
                                    break;
                                case R.id.popup_download:
                                    linkDownloadIntent(dbItem.getDownloadUrl());
                                    break;

                                case R.id.popup_rename:
                                    // Set an EditText view to get user input
                                    final EditText input = new EditText(context);


                                    new AlertDialog.Builder(context)
                                            .setTitle("Rename file")
                                            .setView(input)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    String value = input.getText().toString();


                                                    if(AppUtils.getInstance().isNetworkConnected()) {
                                                        try {
                                                            AppUtils.addToRequestQueue(AppUtils.api.rename(dbItem, value));
                                                            dbItem.setName(value);

                                                            notifyDataSetChanged();

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
                    });

                    popupMenu.show();
                }
            }
        });





        return rowView;
    }


    private void imageResolver(int position, ViewHolder viewHolder, View rowView) {

        DatabaseItem dbItem = databaseItems.get(position);

        String s = dbItem.getContentUrl();
        if(s != null) {

            if (s.toLowerCase().endsWith(".jpeg") || s.toLowerCase().endsWith(".png") || s.toLowerCase().endsWith(".jpg") && AppUtils.getInstance().isNetworkConnected()) {

                viewHolder.image.setPadding(0, 0, 0, 0);

                viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.with(context).setIndicatorsEnabled(false);
                Picasso.with(context)
                        .load(databaseItems.get(position).getThumbnailUrl())
                        .resize((int) (getContext().getResources().getDisplayMetrics().density * 100), (int) (getContext().getResources().getDisplayMetrics().density * 80))
                        .centerCrop()
                        .placeholder(R.drawable.cloud_refresh)
                        .error(getContext().getResources().getDrawable(R.drawable.cloud_refresh))
                        .into(viewHolder.image);
                ((LinearLayout)viewHolder.image.getParent()).setBackgroundResource(android.R.color.transparent);

            } else {
                CloudAppItem.Type type = dbItem.getItemType();

                //((LinearLayout)viewHolder.image.getParent()).setBackgroundResource(R.color.image_bg_color);
                int size = 20;
                int padding = (int) (getContext().getResources().getDisplayMetrics().density * size);
                viewHolder.image.setPadding(padding, padding, padding, padding);

                viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ((LinearLayout)viewHolder.image.getParent()).setBackgroundResource(R.color.blue);


                switch (type) {

                    case AUDIO:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_audio_white)
                                .placeholder(R.drawable.ic_audio_white).into(viewHolder.image);

                        break;
                    case ARCHIVE:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_archive_white)
                                .placeholder(R.drawable.ic_archive_white).into(viewHolder.image);

                        break;
                    case BOOKMARK:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_bookmark_white)
                                .placeholder(R.drawable.ic_bookmark_white).into(viewHolder.image);

                        break;
                    case VIDEO:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_video_white)
                                .placeholder(R.drawable.ic_video_white).into(viewHolder.image);

                        break;
                    case UNKNOWN:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_other_white)
                                .placeholder(R.drawable.ic_other_white).into(viewHolder.image);

                        break;
                    case TEXT:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_text_white)
                                .placeholder(R.drawable.ic_text_white).into(viewHolder.image);

                        break;
                    case IMAGE:
                        Picasso.with(AppUtils.getInstance().getApplicationContext()).load(R.drawable.ic_image_white)
                                .placeholder(R.drawable.ic_image_white).into(viewHolder.image);

                        break;
                    default:
                        break;



                }
            }
        }
    }

    public void linkShareIntent(String link) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, link);
        context.startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    public void linkDownloadIntent(String link) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        context.startActivity(i);
    }

    static class ViewHolder {
        public TextView title;
        public TextView date;
        public TextView views;
        public ImageView image;
    }

}
