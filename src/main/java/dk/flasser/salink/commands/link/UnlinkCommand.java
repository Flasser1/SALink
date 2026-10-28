package dk.flasser.salink.commands.link;

import dk.flasser.salink.SALink;
import dk.flasser.salink.managers.LinkManager;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.bukkit.annotation.Async;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Component
@eu.okaeri.commands.annotation.Command(
        label = "unlink"
)
@Async
public class UnlinkCommand implements CommandService {
    private @Inject LinkManager linkManager;
    private @Inject SALink saLink;

    @Executor
    public void _def(
            @Context Player player
    ) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
            List<String> discordIDs = linkManager.getLinkedActive(uuid);

            for (String discordID : discordIDs) {
                linkManager.removeLink(discordID, uuid);
            }

            Bukkit.getScheduler().runTask(saLink, () -> {
                player.sendMessage(new String[]{
                        "§8[ §fLINK §eSYSTEM §8]",
                        " §7Du er nu unlinket."
                });
            });
        });
    }
}
