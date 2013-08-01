import java.io.File;

public interface Renderer {
    boolean accept(File input);
    void renderTo(StringBuilder destination, File input);
}
