import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;

public class Video {
    private final File inputFile;
    private final String baseUrl;

    public Video(File inputFile, String baseUrl) {
        try {
            new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        checkArgument(baseUrl.endsWith("/"));
        checkArgument(inputFile.getAbsolutePath().endsWith(".mov"));
        this.inputFile = inputFile;
        this.baseUrl = baseUrl;
    }

    public String getInputUrl() {
        return baseUrl + "videos/" + getName();
    }

    public String getOutputUrlH264() {
        return getInputUrl().replace(".mov", ".h264");
    }

    public String getOutputUrlWebM() {
        return getInputUrl().replace(".mov", ".webm");
    }

    public String getInputKey() {
        return removeBaseUrl(getInputUrl());
    }

    public String getOutputKeyH264() {
        return removeBaseUrl(getOutputUrlH264());
    }

    public String getOutputKeyWebM() {
        return removeBaseUrl(getOutputUrlWebM());
    }

    public String getName() {
        String absolutePath = inputFile.getAbsolutePath();

        return absolutePath.substring(absolutePath.lastIndexOf(File.separatorChar) + 1, absolutePath.length());
    }

    private String removeBaseUrl(String from) {
        return from.replace(baseUrl, "");
    }
}
