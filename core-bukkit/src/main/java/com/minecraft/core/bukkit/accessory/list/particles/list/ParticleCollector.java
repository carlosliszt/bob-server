package com.minecraft.core.bukkit.accessory.list.particles.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.particles.ParticlesAccessory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum ParticleCollector {

    NOTE("Notas_de_Música", EnumParticle.NOTE,
            new ParticlesAccessory("91k2a",
                    "Notas_de_Música",
                    "particle.notes.accessory", Rank.VIP, new ItemStack(Material.NOTE_BLOCK),
                    "Fly me to the moon, and let me play among the stars...",
                    true,
                    AccessoryType.PARTICLES,
                    AccessoryRarity.LEGENDARY)
    ),

    CLOUD("Núvem", EnumParticle.EXPLOSION_NORMAL,
            new ParticlesAccessory("91k2b",
                    "Núvem",
                    "particle.cloud.accessory", Rank.VIP, new ItemStack(Material.CLAY),
                    "Crie núvens ao seu redor!",
                    true,
                    AccessoryType.PARTICLES,
                    AccessoryRarity.UNCOMMON)
    ),

    FLAME("Fogo_ao_Redor", EnumParticle.FLAME,
            new ParticlesAccessory("ewf23",
                    "Fogo_ao_Redor",
                    "particle.flame.accessory", Rank.VIP, new ItemStack(Material.BLAZE_POWDER),
                    "Crie fogo ao seu redor!",
                    true,
                    AccessoryType.PARTICLES,
                    AccessoryRarity.RARE)
    ),

    HEART("Corações", EnumParticle.HEART,
            new ParticlesAccessory("e2ees",
                    "Corações",
                    "particle.heart.accessory", Rank.VIP, new ItemStack(Material.APPLE),
                    "Crie corações em sua volta!",
                    true,
                    AccessoryType.PARTICLES,
                    AccessoryRarity.UNCOMMON)
    ),

    FIREWORK("Foguetes", EnumParticle.FIREWORKS_SPARK,
            new ParticlesAccessory("51kn9",
                    "Foguetes",
                    "particle.firework.accessory", Rank.VIP, new ItemStack(Material.FIREWORK),
                    "Crie foguetes em seu redor ao andar!",
                    true,
                    AccessoryType.PARTICLES,
                    AccessoryRarity.COMMON)
    );

    private String display;
    private EnumParticle particle;
    private ParticlesAccessory accessory;

    public static ParticleCollector  getParticleByDisplay(String value) {
        for (ParticleCollector particle : ParticleCollector.values())
            if (particle.getDisplay().equalsIgnoreCase(value))
                return particle;
        return null;
    }
}
