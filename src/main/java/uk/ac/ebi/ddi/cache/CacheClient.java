package uk.ac.ebi.ddi.cache;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheClient {

    private static Map<String, Map<String, ImmutablePair<Date, Object>>> cache = new ConcurrentHashMap<>();

    private static final String CACHE_DIR;

    private static final Date DUE_DATE;

    private static final int AUTO_SAVE_EVERY_N_ITEMS = 50;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheClient.class);

    static {
        String cacheDir = System.getenv("CACHE_DIR");
        if (cacheDir == null) {
            cacheDir = System.getProperty("java.io.tmpdir");
        }
        LOGGER.info("Cache dir: {}", cacheDir);
        CACHE_DIR = cacheDir;
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 7);
        DUE_DATE = c.getTime();
    }

    public static <T extends Serializable> void addCache(String service, String key, T value) {
        if (cache.get(service) == null) {
            cache.put(service, new ConcurrentHashMap<>());
        }
        cache.get(service).put(key, new ImmutablePair<>(DUE_DATE, value));
        if (cache.get(service).keySet().size() % AUTO_SAVE_EVERY_N_ITEMS == 0) {
            writeCache(service);
        }
    }

    public static <T> T getCache(String service, String key, Class<T> clazz) {
        if (cache.get(service) != null) {
            ImmutablePair<Date, Object> result = cache.get(service).get(key);
            if (result == null) {
                return null;
            } else if (result.getKey().after(new Date())) {
                return clazz.cast(result.getValue());
            } else {
                cache.get(service).remove(key);
            }
        }
        return null;
    }

    public static <T> T getCache(String service, String key, Class<T> clazz, List<String> remaining) {
        T result = getCache(service, key, clazz);
        if (result == null) {
            remaining.add(key);
        }
        return result;
    }

    private static synchronized void writeCache(String service) {
        try {
            Path cacheFile = Paths.get(CACHE_DIR, service + ".cached");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile.toFile()));
            out.writeObject(cache.get(service));
            out.close();
        } catch (IOException e) {
            LOGGER.error("Exception occurred when trying to write cache, service {}, ", service, e);
        }
    }

    public static void writeCaches() {
        for (String key : cache.keySet()) {
            writeCache(key);
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadCaches(String service) {
        try {
            Path cacheFile = Paths.get(CACHE_DIR, service + ".cached");
            if (!cacheFile.toFile().isFile()) {
                return;
            }
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile.toFile()));
            cache.put(service, (Map<String, ImmutablePair<Date, Object>>) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Exception occurred when trying to read cache, service {}, ", service, e);
        }
    }
}
