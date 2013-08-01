import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AlbumRenderer implements Renderer {
    private final PhotoService photoService;

    private final String photoPairTemplate;
    private final String photoSingleTemplate;

    private final String photoSeparatorTemplate;
    private final String albumStartTemplate;
    private final String albumEndTemplate;

    public AlbumRenderer(PhotoService photoService, String albumStartTemplate, String albumEndTemplate, String photoPairTemplate, String photoSingleTemplate, String photoSeparatorTemplate) {
        this.albumStartTemplate = albumStartTemplate;
        this.albumEndTemplate = albumEndTemplate;
        this.photoPairTemplate = photoPairTemplate;
        this.photoSingleTemplate = photoSingleTemplate;
        this.photoSeparatorTemplate = photoSeparatorTemplate;
        this.photoService = photoService;
    }

    @Override
    public boolean accept(File input) {
        return input.isDirectory();
    }

    @Override
    public void renderTo(StringBuilder destination, File input) {
        List<String> photos = new ArrayList<String>();
        Iterator<File> it = getJpgIterator(input);

        destination.append(String.format(albumStartTemplate, StringShortcuts.makeTitleFrom(input.getAbsolutePath())));

        while (it.hasNext()) {
            File photo1 = it.next();
            if (it.hasNext()) {
                addPhotoPair(photos, photo1, it.next());
            } else {
                addSinglePhoto(photos, photo1);
            }
        }

        destination.append(Joiner.on(photoSeparatorTemplate).join(photos));
        destination.append(albumEndTemplate);
    }

    private Iterator<File> getJpgIterator(File input) {
        File[] files = input.listFiles();
        if (files != null) {
            return FluentIterable.from(Arrays.asList(files)).filter(new Predicate<File>() {
                @Override
                public boolean apply(File input) {
                    return input != null && input.isFile() && input.getAbsolutePath().endsWith(".jpg");
                }
            }).iterator();
        }
        return Iterators.empty();
    }

    private void addSinglePhoto(List<String> photos, File photo1) {
        Photo photoPng = photoService.transcode(photo1);

        photos.add(String.format(photoSingleTemplate, photoPng.getPngUrl()));
    }

    private void addPhotoPair(List<String> photos, File photo1, File photo2) {
        Photo photoPng1 = photoService.transcode(photo1);
        Photo photoPng2 = photoService.transcode(photo2);

        photos.add(String.format(photoPairTemplate, photoPng1.getPngUrl(), photoPng2.getPngUrl()));
    }

}
