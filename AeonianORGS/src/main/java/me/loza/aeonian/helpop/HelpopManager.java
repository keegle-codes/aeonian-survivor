package me.loza.aeonian.helpop;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HelpopManager {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final Map<Integer, UUID> HELPOP_REQUESTS = new ConcurrentHashMap<>();

    private HelpopManager() {}

    public static int createHelpop(UUID playerId) {
        int id = COUNTER.incrementAndGet();
        HELPOP_REQUESTS.put(id, playerId);
        return id;
    }

    public static UUID getHelpopOwner(int id) {
        return HELPOP_REQUESTS.get(id);
    }

    public static void removeHelpop(int id) {
        HELPOP_REQUESTS.remove(id);
    }
}
