package rocks.ethanol.ethanolapi.server.connector.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class CustomBufferedWriter extends BufferedWriter {
    public CustomBufferedWriter(final Writer out) {
        super(out);
    }

    public final void writeLine(final String line) throws IOException {
        this.write(line + System.lineSeparator());
        this.flush();
    }
}
