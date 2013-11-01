import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoRenderer implements Renderer {
    private final AtomicInteger videoIndex;
    private final VideoService videoService;
    private final String baseUrl;
    private final MustacheFactory mustacheFactory;

    public VideoRenderer(VideoService videoService, String baseUrl, MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
        this.videoIndex = new AtomicInteger(0);
        this.videoService = videoService;
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean accept(File input) {
        return input.isFile() && (input.getAbsolutePath().toLowerCase().endsWith(".mov") || input.getAbsolutePath().toLowerCase().endsWith(".m4v"));
    }

    @Override
    public void renderTo(StringBuilder destination, File input) {
        Video video = videoService.transcode(input, baseUrl);

        Map<String, Object> context = ImmutableMap.<String, Object>builder()
                .put("title", StringShortcuts.makeTitleFrom(video.getName()))
                .put("uid", videoIndex.incrementAndGet())
                .put("poster", video.getOutputUrlPoster1())
                .put("hdMp4", video.getOutputUrlH264())
                .put("hdWebm", video.getOutputUrlWebM())
                .put("sdMp4", video.getOutputUrlSDH264())
                .put("sdWebm", video.getOutputUrlSDWebM())
                .build();

        StringWriter writer = new StringWriter();
        try {
            mustacheFactory.compile("video.mustache").execute(writer, context).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        destination.append(writer.toString());
    }
}
