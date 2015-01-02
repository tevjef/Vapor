package deadpixel.app.vapor.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.R;
import deadpixel.app.vapor.model.NavDrawerItem;

/**
 * Created by Tevin on 2/1/14.
 */
public class NavDrawerListAdapter extends ArrayAdapter {

    private Context context;
    private String[] navTitles;
    private TypedArray navIcons;
    private int type;
    private ImageView imgIcon = null;
    private TextView txtTitle = null;
    private ArrayList<NavDrawerItem> item;

    static class ViewHolder {
        public TextView text;
        public ImageView image;
    }


    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> item , int type){
        super(context, -1, item);
        this.context = context;
        this.type = type;
        this.item = item;
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
        return item.size();
    }

    @Override
    public Object getItem(int position) {
        return item.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder viewHolder;
        if (rowView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            viewHolder = new ViewHolder();

            switch(type) {
                case 0:
                    rowView = mInflater.inflate(R.layout.drawer_list_item, null);
                    viewHolder.image = (ImageView) rowView.findViewById(R.id.drawer_listview_icon);
                    viewHolder.text = (TextView) rowView.findViewById(R.id.drawer_listview_text);
                    break;
                case 1:
                    rowView = mInflater.inflate(R.layout.drawer_list_subitem, null);
                    viewHolder.image = (ImageView) rowView.findViewById(R.id.drawer_sublist_icon);
                    viewHolder.text = (TextView) rowView.findViewById(R.id.drawer_sublist_text);
                    break;
                default:
                    rowView = mInflater.inflate(R.layout.drawer_list_item, null);
                    viewHolder.image = (ImageView) rowView.findViewById(R.id.drawer_listview_icon);
                    viewHolder.text = (TextView) rowView.findViewById(R.id.drawer_listview_text);
                    break;
            }
            rowView.setTag(viewHolder);
        } else  {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        viewHolder.image.setImageResource(item.get(position).getIcon());
        viewHolder.text.setText(item.get(position).getTitle());

        return rowView;
    }
}
