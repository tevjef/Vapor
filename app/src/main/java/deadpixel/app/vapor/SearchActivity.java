package deadpixel.app.vapor;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;

import deadpixel.app.vapor.adapter.FilesListViewAdapter;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.database.model.DatabaseItem;

/**
 * Created by Tevin on 8/26/2014.
 */
public class SearchActivity extends SherlockListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        getListView().setBackgroundResource(R.color.activity_bg);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        getListView().setFastScrollEnabled(true);
        getListView().setSelector(android.R.color.transparent);



        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());


    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            ArrayList<DatabaseItem> dbItems =  DatabaseManager.getItemsByName(query);
            getSupportActionBar().setTitle("Results for: " + query);
            getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.ic_search_white));
            setListAdapter(new FilesListViewAdapter(getApplicationContext(), dbItems));

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            onBackPressed();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {


        DatabaseItem dbItem = (DatabaseItem) l.getAdapter().getItem(position);
        String name = dbItem.getName();

        switch(dbItem.getItemType()) {
            case IMAGE:
                Intent intent = new Intent(this, ImageViewActivity.class);
                intent.putExtra(MainActivity.EXTRA_NAME, name);
                startActivity(intent);

                break;
            default:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(dbItem.getContentUrl()));
                startActivity(i);

        }
    }
}
