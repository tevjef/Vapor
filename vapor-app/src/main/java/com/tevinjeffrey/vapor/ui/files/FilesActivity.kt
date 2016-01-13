package com.tevinjeffrey.vapor.ui.files

import android.content.ClipboardManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.flipboard.bottomsheet.BottomSheetLayout
import com.flipboard.bottomsheet.commons.IntentPickerSheetView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.VaporApp
import com.tevinjeffrey.vapor.customviews.TouchImageView
import com.tevinjeffrey.vapor.events.DeleteEvent
import com.tevinjeffrey.vapor.events.RenameEvent
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.UserManager
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.services.IntentBridge
import com.tevinjeffrey.vapor.ui.ImageActivity
import com.tevinjeffrey.vapor.ui.SettingsActivity
import com.tevinjeffrey.vapor.ui.files.fragments.FilesFragment
import com.tevinjeffrey.vapor.ui.login.LoginActivity
import com.tevinjeffrey.vapor.ui.utils.ItemClickListener
import com.tevinjeffrey.vapor.utils.ScrimUtil
import com.tevinjeffrey.vapor.utils.VaporUtils

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import butterknife.Bind
import butterknife.ButterKnife
import icepick.Icepick
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import timber.log.Timber

import android.support.design.widget.TabLayout.MODE_SCROLLABLE
import android.support.v4.view.PagerAdapter
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ALL
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ARCHIVE
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.AUDIO
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.BOOKMARK
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.IMAGE
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.TEXT
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.UNKNOWN
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.VIDEO


class FilesActivity : AppCompatActivity(), ItemClickListener<CloudAppItem, View>, FilesActivityView {

    @Inject
    lateinit var userManager: UserManager
    @Inject
    lateinit var bus: Bus

    @Inject
    var presenter: FilesActivityPresenter? = null

    lateinit var tabs: TabLayout
    lateinit var viewpager: ViewPager
    lateinit var bottomsheet: BottomSheetLayout
    lateinit var nav_view: NavigationView
    lateinit var fab: FloatingActionButton
    lateinit var drawer_layout: DrawerLayout
    lateinit var share_bottomsheet: BottomSheetLayout

    lateinit  var mHeaderEmail: TextView

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
        VaporApp.uiComponent(this).inject(this)
        setContentView(R.layout.activity_main)

        tabs = findViewById(R.id.tabs) as TabLayout
        viewpager = findViewById(R.id.viewpager) as ViewPager
        bottomsheet = findViewById(R.id.bottomsheet) as BottomSheetLayout
        nav_view = findViewById(R.id.nav_view) as NavigationView
        fab = findViewById(R.id.fab) as FloatingActionButton
        drawer_layout = findViewById(R.id.drawer_layout) as DrawerLayout
        share_bottomsheet =  findViewById(R.id.share_bottomsheet) as BottomSheetLayout

        bus.register(this)

        if (!userManager.isLoggedIn) {
            Timber.d("User not logged in.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        presenter!!.attachView(this)

        val mHeader = LayoutInflater.from(this).inflate(R.layout.activity_main_nav_header, null)
        mHeaderEmail = ButterKnife.findById<TextView>(mHeader, R.id.nav_view_email)
        presenter!!.loadEmail()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val ab = supportActionBar
        ab.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
        if (nav_view != null) {
            setupDrawerContent(nav_view)
            nav_view!!.addHeaderView(mHeader)
        }

        if (viewpager != null) {
            setupViewPager(viewpager)
            tabs.setupWithViewPager(viewpager)
            tabs.tabMode = MODE_SCROLLABLE
        }

        fab.setOnClickListener { startActivity(Intent(IntentBridge.FILE_SELECT, null, this@FilesActivity, IntentBridge::class.java)) }

        presenter!!.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (presenter != null) {
            presenter!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (presenter != null) {
            presenter!!.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setCheckedItem(R.id.nav_all)
        setToolbarTitle(presenter!!.navContext.toString())
        drawer_layout.setScrimColor(ContextCompat.getColor(this, R.color.drawer_scrim))
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.setChecked(true)
            drawer_layout.closeDrawers()
            val oldContext = presenter!!.navContext

            when (menuItem.itemId) {
                R.id.nav_all -> presenter!!.navContext = FilesActivityPresenter.NavContext.ALL
                R.id.nav_favorites -> presenter!!.navContext = FilesActivityPresenter.NavContext.FAVORITE
                R.id.nav_popular -> presenter!!.navContext = FilesActivityPresenter.NavContext.POPULAR
                R.id.nav_trash -> presenter!!.navContext = FilesActivityPresenter.NavContext.TRASH
                else -> throw RuntimeException("Unknown type")
            }
            setToolbarTitle(presenter!!.navContext.toString())

            //Smoother animation when closing drawer.
            if (oldContext !== presenter!!.navContext) {
                Observable.just<Any>(Object()).delay(250, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    setupViewPager(viewpager)
                    tabs.setupWithViewPager(viewpager)
                }
            }
            true
        }

    }

    private fun setToolbarTitle(charSequence: CharSequence) {
        supportActionBar!!.title = charSequence
    }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.offscreenPageLimit = 5
        val adapter = Adapter(supportFragmentManager)
        adapter.addFragment(FilesFragment.newInstance(ALL), "Recent")
        adapter.addFragment(FilesFragment.newInstance(IMAGE), "Images")
        adapter.addFragment(FilesFragment.newInstance(VIDEO), "Videos")
        adapter.addFragment(FilesFragment.newInstance(ARCHIVE), "Archives")
        adapter.addFragment(FilesFragment.newInstance(TEXT), "Text")
        adapter.addFragment(FilesFragment.newInstance(AUDIO), "Audio")
        adapter.addFragment(FilesFragment.newInstance(BOOKMARK), "Bookmarks")
        adapter.addFragment(FilesFragment.newInstance(UNKNOWN), "Unknown")
        viewPager.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_refresh -> {
                presenter!!.refreshClicked()
                return true
            }
            R.id.action_logout -> {
                MaterialDialog.Builder(this).title(R.string.are_you_sure).positiveText(R.string.yes).negativeText(R.string.no).callback(object : MaterialDialog.ButtonCallback() {
                    override fun onPositive(dialog: MaterialDialog?) {
                        super.onPositive(dialog)
                        userManager.logout()
                        finish()
                        val intent = Intent(this@FilesActivity, FilesActivity::class.java)
                        startActivity(intent)
                    }
                }).show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onItemClicked(data: CloudAppItem, view: View) {
        bottomsheet.peekOnDismiss = true

        if (!bottomsheet.isSheetShowing) {
            bottomsheet.showWithSheetView(LayoutInflater.from(this).inflate(R.layout.fragment_files_info, bottomsheet, false))
            bottomsheet.isFocusableInTouchMode = true
            bottomsheet.requestFocus()
            bottomsheet.peekSheetTranslation = (resources.displayMetrics.heightPixels * .60).toFloat()
        }

        val previousImage = ButterKnife.findById<ImageView>(view, R.id.files_list_image).drawable

        val bsFileName = ButterKnife.findById<TextView>(bottomsheet, R.id.bs_file_name)
        val bsSizeText = ButterKnife.findById<TextView>(bottomsheet, R.id.bs_size_text)
        val bsCreatedText = ButterKnife.findById<TextView>(bottomsheet, R.id.bs_created_text)
        val bsViewsText = ButterKnife.findById<TextView>(bottomsheet, R.id.bs_views_text)
        val downloadLink = ButterKnife.findById<View>(bottomsheet, R.id.bs_download_link_container)
        val shareLink = ButterKnife.findById<View>(bottomsheet, R.id.bs_share_link_container)
        val renameFile = ButterKnife.findById<View>(bottomsheet, R.id.bs_rename_container)
        val deleteFile = ButterKnife.findById<View>(bottomsheet, R.id.bs_delete_container)
        val expandButton = ButterKnife.findById<View>(bottomsheet, R.id.bs_expand_icon)
        val scrim = ButterKnife.findById<View>(bottomsheet, R.id.bs_main_scrim)
        if (data.getItemType() !== IMAGE) {
            expandButton.visibility = View.GONE
        }

        scrim.background = ScrimUtil.makeCubicGradientScrimDrawable(
                -1442840576, 8, Gravity.BOTTOM)
        val mainImage = ButterKnife.findById<TouchImageView>(bottomsheet, R.id.bs_main_icon)
        VaporUtils.setTypedImageView(data, mainImage, previousImage, false)

        bsFileName.text = data.name
        bsSizeText.text = VaporUtils.humanReadableByteCount(data.contentLength, true)
        bsCreatedText.text = data.getFormattedCreatedAt(this)
        bsViewsText.text = data.viewCounter.toString()

        val sheetPresenter = BottomSheetPresenterImpl(object : BottomSheetView {

            override fun showLoading(isLoading: Boolean) {
                /*if (isLoading) {
                    dialog.show();
                } else {
                    dialog.dismiss();
                }*/
            }

            override fun showError(message: String) {
                Toast.makeText(this@FilesActivity, message, Toast.LENGTH_LONG).show()
            }

            override fun rename(cloudAppItem: CloudAppItem) {
                bsFileName.text = cloudAppItem.name
                bus.post(RenameEvent(cloudAppItem))
            }

            override fun deleteItem(cloudAppItem: CloudAppItem) {
                bus.post(DeleteEvent(cloudAppItem))
            }

            override fun hideSheet() {
                bottomsheet.dismissSheet()
            }
        }, data)
        VaporApp.uiComponent(this).inject(sheetPresenter)

        if (data.isTrashed) {
            renameFile.isEnabled = false
            deleteFile.isEnabled = false
            downloadLink.isEnabled = false
            renameFile.alpha = .6f
            deleteFile.alpha = .6f
            downloadLink.alpha = .6f
        } else {
            renameFile.setOnClickListener { MaterialDialog.Builder(this@FilesActivity).title("Rename file to").inputType(InputType.TYPE_CLASS_TEXT).input("New name", data.name) { dialog, input -> sheetPresenter.renameFile(input.toString()) }.show() }

            deleteFile.setOnClickListener {
                MaterialDialog.Builder(this@FilesActivity).title("Delete").content(data.name).positiveText("Yes").negativeText("Cancel").callback(object : MaterialDialog.ButtonCallback() {
                    override fun onPositive(dialog: MaterialDialog?) {
                        super.onPositive(dialog)
                        sheetPresenter.deleteFile()
                    }
                }).show()
            }

            downloadLink.setOnClickListener { sheetPresenter.downloadFile() }
        }

        expandButton.setOnClickListener {
            val intent = Intent(this@FilesActivity, ImageActivity::class.java)
            intent.setData(Uri.parse(data.remoteUrl))
            startActivity(intent)
        }

        val sendIntent = Intent()
        sendIntent.setAction(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TEXT, data.getUrl())
        sendIntent.setType("text/plain")
        shareLink.setOnClickListener {
            val intentPickerSheetView = IntentPickerSheetView(this@FilesActivity, sendIntent, "Share with...", IntentPickerSheetView.OnIntentPickedListener { activityInfo ->
                share_bottomsheet.dismissSheet()
                this@FilesActivity.startActivity(activityInfo.getConcreteIntent(sendIntent))
            })
            share_bottomsheet.showWithSheetView(intentPickerSheetView)
        }
    }

    override fun setEmailInHeader(email: String) {
        mHeaderEmail.text = email
    }

    internal class Adapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val mFragments = ArrayList<Fragment>()
        private val mFragmentTitles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            mFragments.add(fragment)
            mFragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getItemPosition(`object`: Any?): Int {
            return PagerAdapter.POSITION_NONE
        }

        @Subscribe
        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitles[position]
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        if (presenter != null) {
            presenter!!.onSaveInstanceState(bundle)
        }
        Icepick.saveInstanceState(this, bundle)
    }
}
