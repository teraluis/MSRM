package pdf.pdfdocument;

import java.io.ByteArrayOutputStream;

public class File {
    private final ByteArrayOutputStream content;
    private final String name;

    public File(ByteArrayOutputStream content, String name) {
        this.content = content;
        this.name = name;
    }

    public ByteArrayOutputStream getContent() {
        return content;
    }

    public String getName() {
        return name;
    }
}
