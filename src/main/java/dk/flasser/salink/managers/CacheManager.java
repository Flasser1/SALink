package dk.flasser.salink.managers;

import eu.okaeri.platform.core.annotation.Component;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CacheManager {
    private static final Map<String, UUID> codes = new HashMap<>();

    public Map<String, UUID> getCodeCache() {
        return codes;
    }

    public void addCodeCache(String code, UUID uuid) {
        codes.put(code, uuid);
    }

    public void removeCodeCache(String code) {
        codes.remove(code);
    }

    public boolean containsCodeCache(String code) {
        return  codes.containsKey(code);
    }

    public UUID getCode(String code) {
        return codes.get(code);
    }

    public String genCode() {
        return RandomStringUtils.randomAlphanumeric(4).toUpperCase();
    }
}
