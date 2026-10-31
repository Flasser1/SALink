package dk.flasser.salink.commands.link;

import dk.flasser.salink.API.SuperawesomeAPI;
import dk.flasser.salink.SALink;
import dk.flasser.salink.managers.CacheManager;
import dk.flasser.salink.managers.DiscordManager;
import dk.flasser.salink.managers.LinkManager;

import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.bukkit.annotation.Async;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Component
@eu.okaeri.commands.annotation.Command(
        label = "link"
)
@Async
public class LinkCommand implements CommandService {
    private @Inject CacheManager cacheManager;
    private @Inject SuperawesomeAPI superawesomeAPI;
    private @Inject LinkManager linkManager;
    private @Inject DiscordManager discordManager;
    private @Inject SALink saLink;

    @Executor
    public void _def(
            @Context Player player
    ) {
        String code = cacheManager.genCode();
        cacheManager.removeCodeCache(player.getUniqueId());
        cacheManager.addCodeCache(code, player.getUniqueId());
        player.sendMessage(new String[]{
                "§8[ §f§lLINK §e§lSYSTEM §8]",
                " §7Din link kode er §f"+code,
                " §7Du kan også linke direkte til din SA konto med §f/link sa"
        });
    }

    @Executor
    public void sa(
            @Context Player player
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
            try {
                String discordID = superawesomeAPI.getDiscordID(player.getName());

                if (discordID == null || discordID.isEmpty()) {
                    Bukkit.getScheduler().runTask(saLink, () -> {
                        if (!player.isOnline()) return;

                        player.sendMessage(new String[]{
                                "§8[ §f§lLINK §e§lSYSTEM §8]",
                                " §7Denne konto er ikke linket med SA."
                        });
                    });
                    return;
                }

                linkManager.addLink(discordID, player.getUniqueId());
                discordManager.link(player, Long.valueOf(discordID));

                Bukkit.getScheduler().runTask(saLink, () -> {
                    if (!player.isOnline()) return;

                    player.sendMessage(new String[]{
                            "§8[ §f§lLINK §e§lSYSTEM §8]",
                            " §7Din konto er nu linket med din SA konto!."
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
