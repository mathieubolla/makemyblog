import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.partition;
import static java.util.Arrays.asList;

public class AlbumRenderer implements Renderer {
    public static final Predicate<File> IS_JPG = new Predicate<File>() {
        @Override
        public boolean apply(File input) {
            return input.getAbsolutePath().toLowerCase().endsWith(".jpg") || input.getAbsolutePath().toLowerCase().endsWith(".jpeg");
        }
    };

    public static final Predicate<File> IS_FILE = new Predicate<File>() {
        @Override
        public boolean apply(File input) {
            return input.isFile();
        }
    };

    private final PhotoService photoService;
    private Mustache albumMustache;

    public AlbumRenderer(PhotoService photoService, MustacheFactory mustacheFactory) {
        this.photoService = photoService;
        this.albumMustache = mustacheFactory.compile("album.mustache");
    }

    @Override
    public boolean accept(File input) {
        return input.isDirectory();
    }

    @Override
    public void renderTo(StringBuilder destination, File input) {

        ImmutableMap<Object, Object> context = ImmutableMap.builder()
                .put("title", StringShortcuts.makeTitleFrom(input.getAbsolutePath()))
                .put("picturePairs", partition(from(listFiles(input)).filter(IS_FILE).filter(IS_JPG).transform(JPG_FILE_TO_URL), 2))
                .build();

        StringWriter writer = new StringWriter();
        try {
            albumMustache.execute(writer,context).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        destination.append(writer.toString());
    }

    public final Function<File, Callable<String>> JPG_FILE_TO_URL = new Function<File, Callable<String>>() {
        @Override
        public Callable<String> apply(final File input) {
            return new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Photo photoPng = photoService.transcode(input);

                    return photoPng.getUrl();
                }
            };
        }
    };

    public static Iterable<File> listFiles(final File input) {
        File[] files = input.listFiles();

        if (files == null) {
            return Lists.newArrayList();
        }

        return asList(files);
    }
}
