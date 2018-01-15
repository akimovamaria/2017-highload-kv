package ru.mail.polis.akimovamaria;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maria on 09.10.2017.
 */
public class MyStore {

    private final static Pattern WORD = Pattern.compile("\\w*");
    private final File path;

    public MyStore(File dir) {
        path = dir;
    }

    public byte[] get(final String key) throws IOException {
        final File file = new File(path, key);
        if (file.exists()) {
            try (final FileInputStream stream = new FileInputStream(file)) {
                return Util.getData(stream);
            }
        } else return null;
    }

    public void put(final String key, final byte[] value) throws IOException {
        final File file = new File(path, checkKey(key));
        try (final FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(value);
        }
    }

    public void delete(final String key) {
        final File file = new File(path, key);
        file.delete();
    }

    private String checkKey(String key) throws IOException {
        final Matcher matcher = WORD.matcher(key);
        if (matcher.matches()) return key;
        else throw new IOException("Incorrect key: " + key);
    }
}