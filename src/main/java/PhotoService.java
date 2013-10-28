import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PhotoService {
    private final StorageService storage;
    private final String baseUrl;

    public PhotoService(StorageService storage, String baseUrl) {
        this.storage = storage;
        this.baseUrl = baseUrl;
    }

    public Photo transcode(File inputFile) {
        Photo photo = new Photo(inputFile, baseUrl);
        try {
            if (!storage.exists(photo.getKey())) {
                storage.sendPublic(compressPhotoFile(inputFile), photo.getKey(), "image/jpg");
            }

            return photo;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File compressPhotoFile(File input) throws InterruptedException, IOException {
        File output = File.createTempFile("tempPhoto", ".jpg");

        List<String> commandLine = Arrays.asList("/usr/local/bin/gm", "convert", "-size", "600x600", input.getAbsolutePath(), "-resize", "600x600", "-background", "white", "-compose", "Copy", "-gravity", "center", "-extent", "600x600", "-quality", "60", output.getAbsolutePath());

        ProcessBuilder builder = new ProcessBuilder(commandLine).directory(new File("/usr/local/bin"));

        Process process = builder.start();
        process.waitFor();
        process.destroy();

        return output;
    }
}
