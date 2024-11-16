package com.minecraft.core.proxy.util.antibot.list;

import com.minecraft.core.proxy.util.antibot.AntiBotModule;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.PendingConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class NameBlocker extends AntiBotModule {

    private final CachedConnection cachedConnection = new CachedConnection();
    private final List<String> filter = new ArrayList<>();

    private static final String MCSPAM_WORDS = "(Craft|Beach|Actor|Games|Tower|Elder|Mine|Nitro|Worms|Build|Plays|Hyper|Crazy|Super|_Itz|Slime)";
    private static final String MCSPAM_SUFFIX = "(11|50|69|99|88|HD|LP|XD|YT)";

    private final Pattern MCSPAM_PATTERN = Pattern.compile("^" + MCSPAM_WORDS + MCSPAM_WORDS + MCSPAM_SUFFIX);

    public NameBlocker blockNames(String... words) {
        filter.addAll(Arrays.asList(words));
        return this;
    }

    @Override
    public boolean isViolator(PendingConnection connection) {
        String connectionName = connection.getName();

        if (cachedConnection.isValid()) {
            String cachedName = cachedConnection.getName();
            if (!cachedName.equals(connectionName) && cachedName.length() == connectionName.length())
                return true;
        }

        cachedConnection.setName(connectionName);
        cachedConnection.setExpireAt(System.currentTimeMillis() + 1200);

        if (MCSPAM_PATTERN.matcher(connectionName).find()) {
            return true;
        }


        for (String filter : filter) {
            if (connectionName.toLowerCase().contains(filter)) {
                return true;
            }
        }

        return false;
    }

    @Getter
    @Setter
    public static class CachedConnection {

        private String name;
        private long expireAt;

        public CachedConnection() {
            this.name = "...";
        }

        public boolean isValid() {
            return expireAt > System.currentTimeMillis();
        }
    }
}
