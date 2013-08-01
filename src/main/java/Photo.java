import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;

public class Photo {
    private final String key;
    private final String baseUrl;

    public Photo(File inputFile, String baseUrl) {
        try {
            new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        checkArgument(baseUrl.endsWith("/"));
        checkArgument(inputFile.getAbsolutePath().endsWith(".jpg"));
        this.baseUrl = baseUrl;
        String parentAbsolutePath = inputFile.getParentFile().getAbsolutePath();
        String parentName = parentAbsolutePath.substring(parentAbsolutePath.lastIndexOf(File.separatorChar) + 1, parentAbsolutePath.length());

        String inputAbsolutePath = inputFile.getAbsolutePath();
        String inputName = inputAbsolutePath.substring(inputAbsolutePath.lastIndexOf(File.separatorChar) + 1, inputAbsolutePath.length());

        key = parentName + '/' + inputName;
    }

    public String getPngUrl() {
        return baseUrl + getPngKey();
    }

    public String getPngKey() {
        return key.replace(".jpg", ".png");
    }
}
