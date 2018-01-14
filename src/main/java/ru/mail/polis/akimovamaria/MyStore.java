package ru.mail.polis.akimovamaria;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

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
            e.printStackTrace();
        }

        throw new NoSuchElementException("No such element");
    }

    public void put(String key, byte[] value) {
        try {
            final File file = new File(path, checkKey(key));
            final FileOutputStream stream = new FileOutputStream(file);
            stream.write(value);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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