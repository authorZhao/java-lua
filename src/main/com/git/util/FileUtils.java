package com.git.util;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * @author authorZhao
 * @since 2024-03-30
 */
public class FileUtils {
    public static final int BUFFER_SIZE = 8192;
    public static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private static byte[] readAllBytes(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (; ; ) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = source.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            capacity = Math.max(newLength(capacity,
                            1,       /* minimum growth */
                            capacity /* preferred growth */),
                    BUFFER_SIZE);
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte) n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    public static String readString(InputStream source) throws IOException {
        if (source == null) {
            return null;
        }
        int available = source.available();
        byte[] ba = readAllBytes(source, available);
        return new String(ba, StandardCharsets.UTF_8);
    }

    public static String readFromPath(String path){
        try {
            URL url = ResourceUtils.getURL(path);
            return readString(url.openStream());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void copy(InputStream inputStream, Path out) throws IOException {
        Files.copy(inputStream,out, StandardCopyOption.REPLACE_EXISTING);
    }


    private static String read(InputStream inputStream) throws IOException {
        StringBuilder buf = new StringBuilder();
        try (inputStream; BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                buf.append(line);
            }
        }
        return buf.toString();
    }

    public static int newLength(int oldLength, int minGrowth, int prefGrowth) {
        // preconditions not checked because of inlining
        // assert oldLength >= 0
        // assert minGrowth > 0

        int prefLength = oldLength + Math.max(minGrowth, prefGrowth); // might overflow
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            // put code cold in a separate method
            return hugeLength(oldLength, minGrowth);
        }
    }

    private static int hugeLength(int oldLength, int minGrowth) {
        int minLength = oldLength + minGrowth;
        if (minLength < 0) { // overflow
            throw new OutOfMemoryError("Required array length " + oldLength + " + " + minGrowth + " is too large");
        } else {
            return Math.max(minLength, SOFT_MAX_ARRAY_LENGTH);
        }
    }
}
