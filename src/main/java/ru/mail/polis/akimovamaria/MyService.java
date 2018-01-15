package ru.mail.polis.akimovamaria;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maria on 09.10.2017.
 */
public class MyService implements KVService {

    private final static Pattern ID = Pattern.compile("id=([\\w]*)");

    private final HttpServer server;
    private final MyStore store;


    public MyService(MyStore myStore, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        store = myStore;

        server.createContext("/v0/entity", http -> {
            if (http.getRequestMethod().equals("GET")) process(http);
            else {
                Thread thread = new Thread(() -> process(http));
                thread.start();
            }
        });

        server.createContext("/v0/status", http -> {
            try {
                final String response = "ONLINE";
                http.sendResponseHeaders(200, response.length());
                http.getResponseBody().write(response.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                http.close();
            }
        });
    }

    private void process(HttpExchange http) {
        try {
            final String id = getID(http.getRequestURI().getQuery());
            if (id == null) http.sendResponseHeaders(404, 0);
            else if ("".equals(id)) http.sendResponseHeaders(400, 0);
            else {
                switch (http.getRequestMethod()) {
                    case "GET":
                        final byte getValue[] = store.get(id);
                        if (getValue != null) {
                            http.sendResponseHeaders(200, getValue.length);
                            http.getResponseBody().write(getValue);
                        } else {
                            http.sendResponseHeaders(404, 0);
                        }
                        break;

                    case "PUT":
                        final InputStream is = http.getRequestBody();
                        final byte putValue[] = Util.getData(is);
                        is.close();
                        store.put(id, putValue);
                        http.sendResponseHeaders(201, 0);
                        break;

                    case "DELETE":
                        store.delete(id);
                        http.sendResponseHeaders(202, 0);
                        break;

                    default:
                        http.sendResponseHeaders(405, 0);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            http.close();
        }
    }

    private static String getID(String uri) {
        final Matcher matcher = ID.matcher(uri);
        if (matcher.matches()) {
            return matcher.group(1);
        } else return null;
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }
}
