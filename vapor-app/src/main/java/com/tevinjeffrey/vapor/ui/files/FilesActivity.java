package com.tevinjeffrey.vapor.ui.files;

import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.IntentPickerSheetView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.customviews.TouchImageView;
import com.tevinjeffrey.vapor.events.DeleteEvent;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.events.LogoutEvent;
import com.tevinjeffrey.vapor.events.RenameEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.services.IntentBridge;
import com.tevinjeffrey.vapor.ui.ImageActivity;
import com.tevinjeffrey.vapor.ui.SettingsActivity;
import com.tevinjeffrey.vapor.ui.files.fragments.BottomSheetView;
import com.tevinjeffrey.vapor.ui.files.fragments.FilesFragment;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BottomSheetPresenterImpl;
import com.tevinjeffrey.vapor.ui.login.LoginActivity;
import com.tevinjeffrey.vapor.ui.utils.ItemClickListener;
import com.tevinjeffrey.vapor.utils.ScrimUtil;
import com.tevinjeffrey.vapor.utils.VaporUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.Icicle;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ALL;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ARCHIVE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.AUDIO;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.BOOKMARK;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.IMAGE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.TEXT;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.UNKNOWN;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.VIDEO;

public class FilesActivity extends AppCompatActivity implements ItemClickListener<CloudAppItem, View>, ActivityCompat.OnRequestPermissionsResultCallback {

    @Inject UserManager userManager;
    @Inject DataManager dataManager;
    @Inject Bus bus;
    @Inject LayoutManager layoutManager;
    @Inject ClipboardManager clipboardManager;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.tabs) TabLayout tabs;
    @Bind(R.id.appbar) AppBarLayout appbar;
    @Bind(R.id.viewpager) ViewPager viewPager;
    @Bind(R.id.main_content) CoordinatorLayout mainContent;
    @Bind(R.id.nav_view) NavigationView navView;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Icicle LayoutManager.NavContext navContext;
    @Bind(R.id.bottomsheet) BottomSheetLayout bottomsheet;
    @Bind(R.id.share_bottomsheet) BottomSheetLayout shareBottomsheet;

    TextView mHeaderEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        VaporApp.uiComponent(this).inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bus.register(this);

        if (!userManager.isLoggedIn()) {
            Timber.d("User not logged in.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        if (navContext == null) {
            navContext = LayoutManager.NavContext.ALL;
        }

        View mHeader = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
        mHeaderEmail = ButterKnife.findById(mHeader, R.id.nav_view_email);
        setEmail();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        if (navView != null) {
            setupDrawerContent(navView);
            navView.addHeaderView(mHeader);
        }

        if (viewPager != null) {
            setupViewPager(viewPager);
            tabs.setupWithViewPager(viewPager);
            tabs.setTabMode(MODE_SCROLLABLE);
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntentBridge.FILE_SELECT, null, FilesActivity.this, IntentBridge.class));
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setCheckedItem(R.id.nav_all);
        setToolbarTitle(layoutManager.getTitle());
        drawerLayout.setScrimColor(ContextCompat.getColor(this, R.color.drawer_scrim));
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        LayoutManager.NavContext oldContext = layoutManager.getNavContext();

                        switch (menuItem.getItemId()) {
                            case R.id.nav_all:
                                layoutManager.setNavContext(LayoutManager.NavContext.ALL);
                                break;
                            case R.id.nav_favorites:
                                layoutManager.setNavContext(LayoutManager.NavContext.FAVORITE);
                                break;
                            case R.id.nav_popular:
                                layoutManager.setNavContext(LayoutManager.NavContext.POPULAR);
                                break;
                            case R.id.nav_trash:
                                layoutManager.setNavContext(LayoutManager.NavContext.TRASH);
                                break;
                            default:
                                throw new RuntimeException("Unknown type");
                        }
                        setToolbarTitle(layoutManager.getTitle());

                        //Smoother animation when closing drawer.
                        if (oldContext != layoutManager.getNavContext()) {
                            Observable.just(new Object()).delay(250, TimeUnit.MILLISECONDS)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Object>() {
                                        @Override
                                        public void call(Object o) {
                                            setupViewPager(viewPager);
                                            tabs.setupWithViewPager(viewPager);
                                        }
                                    });
                        }
                        return true;
                    }
                });

    }

    private void setToolbarTitle(CharSequence charSequence) {
        getSupportActionBar().setTitle(charSequence);
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(5);
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
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                //TODO fix this shit
                //dataManager.getAllItems(ALL, true, 0);
                return true;
            case R.id.action_logout:
                new MaterialDialog.Builder(this)
                        .title(R.string.are_you_sure)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                userManager.logout();
                                finish();
                                Intent intent = new Intent(FilesActivity.this, FilesActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
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

        Drawable previousImage = ButterKnife.<ImageView>findById(view, R.id.files_list_image).getDrawable();

        final TextView bsFileName = ButterKnife.findById(bottomsheet, R.id.bs_file_name);
        TextView bsSizeText = ButterKnife.findById(bottomsheet, R.id.bs_size_text);
        TextView bsCreatedText = ButterKnife.findById(bottomsheet, R.id.bs_created_text);
        TextView bsViewsText = ButterKnife.findById(bottomsheet, R.id.bs_views_text);
        View downloadLink = ButterKnife.findById(bottomsheet, R.id.bs_download_link_container);
        View shareLink = ButterKnife.findById(bottomsheet, R.id.bs_share_link_container);
        View renameFile = ButterKnife.findById(bottomsheet, R.id.bs_rename_container);
        View deleteFile = ButterKnife.findById(bottomsheet, R.id.bs_delete_container);
        View expandButton = ButterKnife.findById(bottomsheet, R.id.bs_expand_icon);
        View scrim = ButterKnife.findById(bottomsheet, R.id.bs_main_scrim);
        if (data.getItemType() != IMAGE) {
            expandButton.setVisibility(View.GONE);
        }

        scrim.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
                0xaa000000, 8, Gravity.BOTTOM));
        final TouchImageView mainImage = ButterKnife.findById(bottomsheet, R.id.bs_main_icon);
        mainImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        VaporUtils.setTypedImageView(data, mainImage, previousImage, false, 120);

        bsFileName.setText(data.getName());
        bsSizeText.setText(VaporUtils.humanReadableByteCount(data.getContentLength(), true));
        bsCreatedText.setText(data.getFormattedCreatedAt(this));
        bsViewsText.setText(String.valueOf(data.getViewCounter()));

        final BottomSheetPresenterImpl sheetPresenter = new BottomSheetPresenterImpl(new BottomSheetView() {

           /* final MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .content("Please wait")
                    .progress(true, 0).build();*/

            @Override
            public void showLoading(boolean isLoading) {
                /*if (isLoading) {
                    dialog.show();
                } else {
                    dialog.dismiss();
                }*/
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
        VaporApp.uiComponent(this).inject(sheetPresenter);

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

        downloadLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetPresenter.downloadFile();
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
        if (userManager.isLoggedIn()) {
            Icepick.saveInstanceState(this, outState);
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        setEmail();
    }

    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {

    }


    private void setEmail() {
        if (UserManager.getUserName() != null) {
            mHeaderEmail.setText(UserManager.getUserName());
        }
    }
}
