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
        checkArgument(inputFile.getAbsolutePath().endsWith(".mov") || inputFile.getAbsolutePath().endsWith(".m4v"));
        this.inputFile = inputFile;
        this.baseUrl = baseUrl;
    }

    public String getInputUrl() {
        return baseUrl + "videos/" + getName();
    }

    public String getOutputUrlH264() {
        return getInputUrl().replace(".mov", ".h264").replace(".m4v", ".h264");
    }

    public String getOutputUrlWebM() {
        return getInputUrl().replace(".mov", ".webm").replace(".m4v", ".webm");
    }

    public String getOutputUrlSDWebM() {
        return getInputUrl().replace(".mov", ".sd.webm").replace(".m4v", ".sd.webm");
    }

    public String getOutputUrlSDH264() {
        return getInputUrl().replace(".mov", ".sd.h264").replace(".m4v", ".sd.h264");
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

    public String getOutputKeySDH264() {
        return removeBaseUrl(getOutputUrlSDH264());
    }

    public String getOutputKeySDWebM() {
        return removeBaseUrl(getOutputUrlSDWebM());
    }

    public String getOutputUrlPoster1() {
        return getOutputUrlH264() + "00002.png";
    }

    public String getName() {
        String absolutePath = inputFile.getAbsolutePath();

        return absolutePath.substring(absolutePath.lastIndexOf(File.separatorChar) + 1, absolutePath.length());
    }

    private String removeBaseUrl(String from) {
        return from.replace(baseUrl, "");
    }
}
