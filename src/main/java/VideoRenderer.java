import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoRenderer implements Renderer {
    private final AtomicInteger videoIndex;
    private final VideoService videoService;
    private final String baseUrl;
    private String videoContentTemplate;

    public VideoRenderer(VideoService videoService, String baseUrl, String videoContentTemplate) {
        this.videoIndex = new AtomicInteger(0);
        this.videoService = videoService;
        this.baseUrl = baseUrl;
        this.videoContentTemplate = videoContentTemplate;
    }

    @Override
    public boolean accept(File input) {
        return input.isFile() && (input.getAbsolutePath().toLowerCase().endsWith(".mov") || input.getAbsolutePath().toLowerCase().endsWith(".m4v"));
    }

    @Override
    public void renderTo(StringBuilder destination, File input) {
        Video video = videoService.transcode(input, baseUrl);
        destination.append(String.format(videoContentTemplate, StringShortcuts.makeTitleFrom(video.getName()), videoIndex.incrementAndGet() + "", video.getOutputUrlPoster1(), video.getOutputUrlH264(), video.getOutputUrlWebM(), video.getOutputUrlSDH264(), video.getOutputUrlSDWebM()));
    }
}
