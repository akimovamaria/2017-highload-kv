package ru.mail.polis.akimovamaria;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maria on 09.10.2017.
 */
public class MyStore {

    private final File path;

    public MyStore(File dir){
        path = dir;
    }

    public byte[] get(String key) throws NoSuchElementException {
        try {
            final File file = new File(path, key);
            final FileInputStream stream = new FileInputStream(file);

            final byte value[] = Util.getData(stream);

            stream.close();
            return value;

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        throw new NoSuchElementException("No such element");
    }

    public void put(String key, byte[] value) throws IOException {
        final File file = new File(path, checkKey(key));
        final FileOutputStream stream = new FileOutputStream(file);
        stream.write(value);
        stream.close();
    }

    public void delete(String key) {
        final File file = new File(path, key);
        file.delete();
    }

    private String checkKey(String key) throws IOException {
        final Pattern pattern = Pattern.compile("\\w*");
        final Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) return key;
        else throw new IOException("Incorrect key: " + key);
    }
}