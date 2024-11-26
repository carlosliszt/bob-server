package com.minecraft.core.proxy.util.antibot.list;

import com.minecraft.core.proxy.util.antibot.AntiBotModule;
import com.minecraft.core.util.geodata.AddressData;
import com.minecraft.core.util.geodata.DataResolver;
import net.md_5.bungee.api.connection.PendingConnection;

import java.util.Arrays;
import java.util.List;

public class CountryBlocker extends AntiBotModule {

    private final List<String> blockedCountries;

    public CountryBlocker(String... blockedCountries) {
        this.blockedCountries = Arrays.asList(blockedCountries);
    }

    @Override
    public boolean isViolator(PendingConnection connection) {
        String hostString = connection.getAddress().getHostString();
        AddressData dataResolver = DataResolver.getInstance().getData(connection.getAddress().getHostString());
        return dataResolver.getCountry() == null || blockedCountries.contains(dataResolver.getCountry());
    }
}
