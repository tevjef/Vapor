package deadpixel.app.vapor.ui.factory;

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

    private AllFilesFragment allFilesFragment;
    private ImageFragment imageFragment;
    private VideoFragment videoFragment;
    private AudioFragment audioFragment;
    private TextFragment textFragment;
    private ArchiveFragment archiveFragment;
    private BookmarkFragment bookMarkFragment;
    private OtherFragment otherFragment;
    private TrashFragment trashFragment;

    public FilesFragmentFactory() {

        allFilesFragment = new AllFilesFragment();
        imageFragment = new ImageFragment();
        videoFragment = new VideoFragment();
        audioFragment = new AudioFragment();
        textFragment = new TextFragment();
        archiveFragment = new ArchiveFragment();
        bookMarkFragment = new BookmarkFragment();
        otherFragment = new OtherFragment();
        trashFragment = new TrashFragment();
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
}
