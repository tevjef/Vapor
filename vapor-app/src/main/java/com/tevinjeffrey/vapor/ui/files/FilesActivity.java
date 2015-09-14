package com.tevinjeffrey.vapor.ui.files;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.IntentPickerSheetView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaprApp;
import com.tevinjeffrey.vapor.adapters.ItemClickListener;
import com.tevinjeffrey.vapor.customviews.TouchImageView;
import com.tevinjeffrey.vapor.events.DeleteEvent;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.events.RenameEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.services.UploadService;
import com.tevinjeffrey.vapor.ui.ImageActivity;
import com.tevinjeffrey.vapor.ui.files.fragments.FilesFragment;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BottomSheetPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BottomSheetPresenterImpl;
import com.tevinjeffrey.vapor.ui.files.fragments.views.BottomSheetView;
import com.tevinjeffrey.vapor.ui.login.LoginActivity;
import com.tevinjeffrey.vapor.utils.VaprUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.Icicle;

import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ALL;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ARCHIVE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.AUDIO;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.BOOKMARK;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.IMAGE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.TEXT;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.UNKNOWN;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.VIDEO;

public class FilesActivity extends AppCompatActivity implements ItemClickListener<CloudAppItem, View> {

    private final int FILE_SELECT_CODE = 42;

    @Inject
    UserManager userManager;
    @Inject
    DataManager dataManager;
    @Inject
    Bus bus;
    @Inject
    LayoutManager layoutManager;

    @Inject
    ClipboardManager clipboardManager;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tabs)
    TabLayout tabs;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.main_content)
    CoordinatorLayout mainContent;
    @Bind(R.id.nav_view)
    NavigationView navView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.nav_drawer_email)
    TextView mHeaderEmail;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Icicle
    LayoutManager.NavContext navContext;
    @Bind(R.id.bottomsheet)
    BottomSheetLayout bottomsheet;
    @Bind(R.id.share_bottomsheet)
    BottomSheetLayout shareBottomsheet;

    View sheetview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        VaprApp.objectGraph(this).inject(this);
        bus.register(this);
        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        if (navContext == null) {
            navContext = LayoutManager.NavContext.ALL;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        setEmail();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        layoutManager.setNavContext(LayoutManager.NavContext.ALL);
        if (navView != null) {
            setupDrawerContent(navView);
        }

        if (viewPager != null) {
            setupViewPager(viewPager);
            tabs.setupWithViewPager(viewPager);
            tabs.setTabMode(MODE_SCROLLABLE);
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("*/*");

                startActivityForResult(intent, FILE_SELECT_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                Intent uploadIntent = new Intent(this, UploadService.class);
                uploadIntent.setData(data.getData());
                startService(uploadIntent);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setCheckedItem(R.id.nav_all);
        setToolbarTitle("All Recent Files");
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {
                            case R.id.nav_all:
                                layoutManager.setNavContext(LayoutManager.NavContext.ALL);
                                setToolbarTitle("All Recent Files");
                                break;
                            case R.id.nav_favorites:
                                layoutManager.setNavContext(LayoutManager.NavContext.FAVORITE);
                                setToolbarTitle("Favorites");
                                break;
                            case R.id.nav_popular:
                                layoutManager.setNavContext(LayoutManager.NavContext.POPULAR);
                                setToolbarTitle("Popular");
                                break;
                            case R.id.nav_trash:
                                layoutManager.setNavContext(LayoutManager.NavContext.TRASH);
                                setToolbarTitle("Trash");
                                break;
                            default:
                                throw new RuntimeException("Unknown type");
                        }
                        setupViewPager(viewPager);
                        tabs.setupWithViewPager(viewPager);
                        return true;
                    }
                });
    }

    private void setToolbarTitle(CharSequence charSequence) {
        getSupportActionBar().setTitle(charSequence);
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(7);
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(FilesFragment.newInstance(ALL), "Recent");
        adapter.addFragment(FilesFragment.newInstance(IMAGE), "Images");
        adapter.addFragment(FilesFragment.newInstance(VIDEO), "Videos");
        adapter.addFragment(FilesFragment.newInstance(ARCHIVE), "Archives");
        adapter.addFragment(FilesFragment.newInstance(TEXT), "Text");
        adapter.addFragment(FilesFragment.newInstance(AUDIO), "Audio");
        adapter.addFragment(FilesFragment.newInstance(BOOKMARK), "Bookmarks");
        adapter.addFragment(FilesFragment.newInstance(UNKNOWN), "Unknown");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_refresh:
                dataManager.getAllItems(ALL, true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(final CloudAppItem data, View view) {
        bottomsheet.setPeekOnDismiss(true);

        if (!bottomsheet.isSheetShowing()) {
            bottomsheet.showWithSheetView(LayoutInflater.from(this).inflate(R.layout.file_info, bottomsheet, false));
            bottomsheet.setFocusableInTouchMode(true);
            bottomsheet.requestFocus();
            bottomsheet.setPeekSheetTranslation((float) (getResources().getDisplayMetrics().heightPixels * .60));
        }

        final TextView bsFileName = ButterKnife.findById(bottomsheet, R.id.bs_file_name);
        TextView bsSizeText = ButterKnife.findById(bottomsheet, R.id.bs_size_text);
        TextView bsCreatedText = ButterKnife.findById(bottomsheet, R.id.bs_created_text);
        TextView bsViewsText = ButterKnife.findById(bottomsheet, R.id.bs_views_text);
        View copyLink = ButterKnife.findById(bottomsheet, R.id.bs_copy_link_container);
        View shareLink = ButterKnife.findById(bottomsheet, R.id.bs_share_link_container);
        View renameFile = ButterKnife.findById(bottomsheet, R.id.bs_rename_container);
        View deleteFile = ButterKnife.findById(bottomsheet, R.id.bs_delete_container);
        final View mainContainer = ButterKnife.findById(bottomsheet, R.id.bs_main_container);
        View expandButton = ButterKnife.findById(bottomsheet, R.id.bs_expand_icon);

        final TouchImageView mainImage = ButterKnife.findById(bottomsheet, R.id.bs_main_icon);
        VaprUtils.setTypedImageView(data, mainImage, false, 120);

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("Please wait")
                .progress(true, 0).build();

        bsFileName.setText(data.getName());
        bsSizeText.setText(VaprUtils.humanReadableByteCount(data.getContentLength(), true));
        bsCreatedText.setText(data.getFormattedCreatedAt(this));
        bsViewsText.setText(String.valueOf(data.getViewCounter()));

        final BottomSheetPresenter sheetPresenter = new BottomSheetPresenterImpl(new BottomSheetView() {
            @Override
            public void showLoading(boolean isLoading) {
                if (isLoading) {
                    dialog.show();
                } else {
                    dialog.dismiss();
                }
            }

            @Override
            public void showError(String message) {
                Toast.makeText(FilesActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void rename(CloudAppItem cloudAppItem) {
                bsFileName.setText(cloudAppItem.getName());
                bus.post(new RenameEvent(cloudAppItem));
            }

            @Override
            public void deleteItem(CloudAppItem cloudAppItem) {
                bus.post(new DeleteEvent(cloudAppItem));
            }

            @Override
            public void hideSheet() {
                bottomsheet.dismissSheet();
            }
        }, data);
        VaprApp.objectGraph(this).inject(sheetPresenter);

        renameFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(FilesActivity.this)
                        .title("Rename file to")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("New name", data.getName(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                sheetPresenter.renameFile(input.toString());
                            }
                        }).show();
            }
        });

        deleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(FilesActivity.this)
                        .title("Delete")
                        .content(data.getName())
                        .positiveText("Yes")

                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                sheetPresenter.deleteFile();
                            }
                        })
                        .show();
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("Uploaded item url", data.getUrl());
                clipboardManager.setPrimaryClip(clip);
                Toast.makeText(FilesActivity.this, "Copied: " + data.getUrl(), Toast.LENGTH_SHORT).show();
            }
        });

        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FilesActivity.this, ImageActivity.class);
                intent.setData(Uri.parse(data.getRemoteUrl()));
                startActivity(intent);
            }
        });

        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, data.getUrl());
        sendIntent.setType("text/plain");
        shareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBottomsheet.showWithSheetView(new IntentPickerSheetView(FilesActivity.this, sendIntent, "Share with...", new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                        FilesActivity.this.startActivity(activityInfo.getConcreteIntent(new Intent()));
                    }
                }));
            }
        });
//        VaprUtils.openLink(getParentActivity(), data.getUrl());
    }


    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Subscribe
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        setEmail();
    }

    private void setEmail() {
        if (UserManager.getUserName() != null) {
            mHeaderEmail.setText(UserManager.getUserName());
        }
    }
}
