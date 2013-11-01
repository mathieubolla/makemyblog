import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

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

        VideoService videoService = new VideoService(transcoder, storage, properties.getProperty("PRESET_ID_H264"), properties.getProperty("PRESET_ID_WEBM"), properties.getProperty("PRESET_ID_SD_H264"), properties.getProperty("PRESET_ID_SD_WEBM"), properties.getProperty("PIPELINE_TRANSCODE_FOR_MP4"));
        String baseUrl = properties.getProperty("BASE_URL");
        PhotoService photoService = new PhotoService(storage, baseUrl);
        DefaultMustacheFactory mustacheFactory = new DefaultMustacheFactory("templates");

        final List<Renderer> renderers = Arrays.asList(
                new AlbumRenderer(photoService, mustacheFactory),
                new VideoRenderer(videoService, baseUrl, mustacheFactory),
                new TexteRenderer(loadResource("templates/texte.txt")));

        final List<List<File>> sortedFiles = Lists.partition(FluentIterable.from(listSortedFiles(args[0])).filter(willRender(renderers)).toList(), 4);

        Map<String, Object> context = buildContext(properties);
        
        sendPageFile(storage, makeContent(renderers, sortedFiles.get(0), 0, sortedFiles.size() > 1, mustacheFactory, context), "index.html");
        for (int i = 1; i < sortedFiles.size(); i++) {
            sendPageFile(storage, makeContent(renderers, sortedFiles.get(i), i, i != sortedFiles.size() - 1, mustacheFactory, context), "page-" + i + ".html");
        }

        sendSupportFiles(storage);
    }

    private static Predicate<File> willRender(final List<Renderer> renderers) {
        return new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                for (Renderer renderer : renderers) {
                    if (renderer.accept(input)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private static String loadResource(String name) {
        try {
            return IOUtils.toString(MakeMyBlog.class.getClassLoader().getResourceAsStream(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String makeContent(List<Renderer> renderers, List<File> sortedFiles, int pageNum, boolean hasNext, MustacheFactory mustacheFactory, Map<String, Object> context) {
        StringBuilder html = new StringBuilder(renderContext(mustacheFactory, "start.mustache", context));

        String pagination = buildPagination(pageNum, hasNext, mustacheFactory);

        html.append(pagination);
        html.append("<div class=\"row\"><div class=\"spacer\">&nbsp;</div></div>");
        for (File element : sortedFiles) {
            for (Renderer renderer : renderers) {
                if (renderer.accept(element)) {
                    System.err.println("Rendering " + element + " with " + renderer);
                    renderer.renderTo(html, element);
                }
            }
        }
        html.append(pagination);

        return html.append(loadResource("templates/end.txt")).toString();
    }

    private static Map<String, Object> buildContext(Properties properties) {
        Map<String, Object> context = Maps.newHashMap();
        if (properties.containsKey("GA_ID") && properties.containsKey("GA_DN")) {
            context.put("ga", ImmutableMap.builder().put("id", properties.getProperty("GA_ID")).put("dn", properties.getProperty("GA_DN")).build());
        }
        if (properties.containsKey("TITLE")) {
            context.put("title", properties.getProperty("TITLE"));
        }
        return context;
    }

    private static String buildPagination(int pageNum, boolean hasNext, MustacheFactory mustacheFactory) {
        Map<String, Object> context = Maps.newHashMap();
        if (pageNum >= 2) {
            context.put("previous", "page-" + (pageNum - 1) + ".html");
        }
        if (pageNum == 1) {
            context.put("previous", "index.html");
        }
        if (hasNext) {
            context.put("next", "page-" + (pageNum + 1) + ".html");
        }

        return renderContext(mustacheFactory, "pagination.mustache", context);
    }

    private static String renderContext(MustacheFactory mustacheFactory, String name, Map<String, Object> context) {
        StringWriter writer = new StringWriter();
        try {
            mustacheFactory.compile(name).execute(writer, context).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    private static List<File> listSortedFiles(String pathname) {
        File sources = new File(pathname);

        File[] files = sources.listFiles();
        if (files == null) {
            throw new RuntimeException("No content");
        }

        List<File> sortedFiles = Lists.newArrayList(files);
        Collections.sort(sortedFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getAbsolutePath().compareTo(o1.getAbsolutePath());
            }
        });
        return sortedFiles;
    }

    private static void sendPageFile(StorageService storage, String content, String pageName) {
        storage.sendPublic(content, pageName, "text/html");
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
