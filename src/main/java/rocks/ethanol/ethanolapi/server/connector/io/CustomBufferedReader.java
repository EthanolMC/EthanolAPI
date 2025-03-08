package rocks.ethanol.ethanolapi.server.connector.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

public class CustomBufferedReader extends BufferedReader {
    public CustomBufferedReader(final Reader in) {
        super(in);
    }

    @Override
    public String readLine() throws IOException {
        int read = this.read();
        if (read < 0)
            throw new EOFException();

        final String string = super.readLine();
        if (string == null)
            return null;

        final char[] line = string.toCharArray();

        final char[] output = new char[line.length + 1];
        output[0] = (char) read;
        System.arraycopy(line, 0, output, 1, line.length);

        return new String(output);
    }
}
