package deadpixel.app.vapor.ui.factory;

import android.os.Bundle;

import deadpixel.app.vapor.ui.fragments.AllFilesFragment;
import deadpixel.app.vapor.ui.fragments.ArchiveFragment;
import deadpixel.app.vapor.ui.fragments.AudioFragment;
import deadpixel.app.vapor.ui.fragments.BookmarkFragment;
import deadpixel.app.vapor.ui.fragments.ImageFragment;
import deadpixel.app.vapor.ui.fragments.OtherFragment;
import deadpixel.app.vapor.ui.fragments.RecentFragment;
import deadpixel.app.vapor.ui.fragments.TextFragment;
import deadpixel.app.vapor.ui.fragments.TrashFragment;
import deadpixel.app.vapor.ui.fragments.VideoFragment;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;

/**
 * Created by Tevin on 1/3/2015.
 */
public class FilesFragmentFactory {

    private RecentFragment allFilesFragment;
    private RecentFragment imageFragment;
    private RecentFragment videoFragment;
    private RecentFragment audioFragment;
    private RecentFragment textFragment;
    private RecentFragment archiveFragment;
    private RecentFragment bookMarkFragment;
    private RecentFragment otherFragment;
    private RecentFragment trashFragment;

    public FilesFragmentFactory() {

        allFilesFragment = new RecentFragment();
        imageFragment = new RecentFragment();
        videoFragment = new RecentFragment();
        audioFragment = new RecentFragment();
        textFragment = new RecentFragment();
        archiveFragment = new RecentFragment();
        bookMarkFragment = new RecentFragment();
        otherFragment = new RecentFragment();
        trashFragment = new RecentFragment();

        setFragmentArguments();
    }

    public RecentFragment makeFragment(String fragmentType) throws Exception {
        if (fragmentType.equals(FilesFragment.ALL_FRAGMENT)) {
            return allFilesFragment;
        }
        else if (fragmentType.equals(FilesFragment.IMAGE_FRAGMENT)) {           
            return imageFragment;
        }
        else if (fragmentType.equals(FilesFragment.VIDEO_FRAGMENT)) {
            return videoFragment;
        }
        else if (fragmentType.equals(FilesFragment.AUDIO_FRAGMENT)) {
            return audioFragment;
        }
        else if (fragmentType.equals(FilesFragment.TEXT_FRAGMENT)) {
            return textFragment;
        }
        else if (fragmentType.equals(FilesFragment.ARCHIVE_FRAGMENT)) {
            return archiveFragment;
        }
        else if (fragmentType.equals(FilesFragment.BOOKMARK_FRAGMENT)) {
            return bookMarkFragment;
        }
        else if (fragmentType.equals(FilesFragment.OTHER_FRAGMENT)) {
            return otherFragment;
        }
        else if (fragmentType.equals(FilesFragment.TRASH_FRAGMENT)) {
            return trashFragment;
        } else {
            throw new Exception("Invalid file type");
        }
    }

    private Bundle createFragmentArguments(boolean b, String fragmentType) {
        Bundle args = new Bundle();
        args.putBoolean(FilesFragment.AUTOLOAD, b);
        args.putString(FilesFragment.FRAGMENT_TYPE, fragmentType);
        return args;
    }
    private void setFragmentArguments() {
        allFilesFragment.setArguments(createFragmentArguments(true, FilesFragment.ALL_FRAGMENT));
        imageFragment.setArguments(createFragmentArguments(true, FilesFragment.IMAGE_FRAGMENT));
        videoFragment.setArguments(createFragmentArguments(true, FilesFragment.VIDEO_FRAGMENT));
        audioFragment.setArguments(createFragmentArguments(true, FilesFragment.AUDIO_FRAGMENT));
        textFragment.setArguments(createFragmentArguments(true, FilesFragment.TEXT_FRAGMENT));
        archiveFragment.setArguments(createFragmentArguments(true, FilesFragment.ARCHIVE_FRAGMENT));
        bookMarkFragment.setArguments(createFragmentArguments(true, FilesFragment.BOOKMARK_FRAGMENT));
        otherFragment.setArguments(createFragmentArguments(true, FilesFragment.OTHER_FRAGMENT));
        trashFragment.setArguments(createFragmentArguments(false, FilesFragment.TRASH_FRAGMENT));
    }
    
    
}
