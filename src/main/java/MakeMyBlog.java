import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MakeMyBlog {
    public static void main(String... args) throws IOException, InterruptedException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(args[0], "configuration.properties")));

        PropertiesCredentials awsCredentials = new PropertiesCredentials(new File(properties.getProperty("CREDENTIALS_FILE")));

        AmazonElasticTranscoder transcoder = new AmazonElasticTranscoderClient(awsCredentials);
        AmazonS3 s3 = new AmazonS3Client(awsCredentials);

        transcoder.setRegion(Region.getRegion(Regions.EU_WEST_1));
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

        StorageService storage = new StorageService(s3, properties.getProperty("BUCKET_NAME"));

        VideoService videoService = new VideoService(transcoder, storage, properties.getProperty("PRESET_ID_H264"), properties.getProperty("PRESET_ID_WEBM"), properties.getProperty("PIPELINE_TRANSCODE_FOR_MP4"));
        String baseUrl = properties.getProperty("BASE_URL");
        PhotoService photoService = new PhotoService(storage, baseUrl);
        List<Renderer> renderers = Arrays.asList(
                new AlbumRenderer(photoService, loadResource("templates/album-start.txt"), loadResource("templates/album-end.txt"), loadResource("templates/photo-pair.txt"), loadResource("templates/photo-single.txt"), loadResource("templates/photo-separator.txt")),
                new VideoRenderer(videoService, baseUrl, loadResource("templates/video.txt")),
                new TexteRenderer(loadResource("templates/texte.txt")));

        sendIndexFile(storage, makeContent(renderers, args[0]));
        sendSupportFiles(storage);
    }

    private static String loadResource(String name) {
        try {
            return IOUtils.toString(MakeMyBlog.class.getClassLoader().getResourceAsStream(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String makeContent(List<Renderer> renderers, String pathname) {
        File sources = new File(pathname);
        StringBuilder html = new StringBuilder(loadResource("templates/start.txt"));

        for (File element : sources.listFiles()) {
            for (Renderer renderer : renderers) {
                if (renderer.accept(element)) {
                    renderer.renderTo(html, element);
                }
            }
        }

        return html.append(loadResource("templates/end.txt")).toString();
    }

    private static void sendIndexFile(StorageService storage, String content) {
        storage.sendPublic(content, "index.html", "text/html");
    }

    private static void sendSupportFiles(StorageService storage) throws IOException {
        sendSupportFile(storage, "video-js.min.css", "text/css");
        sendSupportFile(storage, "video-js.png", "image/png");
        sendSupportFile(storage, "video-js.swf", "application/x-shockwave-flash");
        sendSupportFile(storage, "video.js", "application/x-javascript");

        sendSupportFile(storage, "font/vjs.eot", "application/octet-stream");
        sendSupportFile(storage, "font/vjs.svg", "image/svg+xml");
        sendSupportFile(storage, "font/vjs.ttf", "application/octet-stream");
        sendSupportFile(storage, "font/vjs.woff", "application/octet-stream");
    }

    private static void sendSupportFile(StorageService storage, String name, String contentType) throws IOException {
        String resourceName = "support/" + name;
        System.out.println(resourceName);
        byte[] data = IOUtils.toByteArray(MakeMyBlog.class.getClassLoader().getResourceAsStream(resourceName));

        if (storage.exists(name, data)) {
            return;
        }

        storage.sendPublic(data, name, contentType);
    }

}
