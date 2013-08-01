import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.IOException;

public class TexteRenderer implements Renderer {
    private final String template;

    public TexteRenderer(String template) {
        this.template = template;
    }

    @Override
    public boolean accept(File input) {
        return input.isFile() && input.getAbsolutePath().toLowerCase().endsWith(".texte");
    }

    @Override
    public void renderTo(StringBuilder destination, File input) {
        try {
            String content = FileUtils.readFileToString(input);

            destination.append(String.format(template, StringShortcuts.makeTitleFrom(input.getAbsolutePath()), StringEscapeUtils.escapeHtml(content).replace("\n", "<br/>")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
