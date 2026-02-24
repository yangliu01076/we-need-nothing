package org.example.minidubbo.framework;


/**
 * @author duoyian
 * @date 2026/2/14
 */
public class MapService {
    private static final java.util.Map<String, Object> SERVICE_MAP = new java.util.HashMap<>();

    public static void register(String interfaceName, Object serviceImpl) {
        SERVICE_MAP.put(interfaceName, serviceImpl);
    }

    public static Object get(String interfaceName) {
        return SERVICE_MAP.get(interfaceName);
    }
}
