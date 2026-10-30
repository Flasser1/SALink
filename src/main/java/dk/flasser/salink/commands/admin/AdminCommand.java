package dk.flasser.salink.commands.admin;

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
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@eu.okaeri.commands.annotation.Command(
        label = "linkadmin"
)
@Async
public class AdminCommand implements CommandService {
    private @Inject LinkManager linkManager;
    private @Inject SALink saLink;

    @Executor
    public void view(
            @Context Player player,
            @Arg("target") String target
    ) {
        if (!player.hasPermission("salink.admin")) {
            player.sendMessage("§7Dette har du ikke adgang til.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
            List<LinkManager.LinkedAccount> accounts = linkManager.getLinkedAll(target);

            if (accounts.isEmpty()) {
                Bukkit.getScheduler().runTask(saLink, () -> {
                    player.sendMessage(new String[]{
                            "§8[ §fLINK §eSYSTEM §8]",
                            " §7Denne bruger har ingen linkede konti."
                    });
                });
                return;
            }

            saLink.getJDA().retrieveUserById(target).queue(
                user -> {
                    String username = user.getName();

                    Bukkit.getScheduler().runTask(saLink, () -> {
                        player.sendMessage(new String[]{
                                "§8[ §fLINK §eSYSTEM §8]",
                                " §7Logs for §f"+username
                        });

                        for (LinkManager.LinkedAccount account : accounts) {
                            player.sendMessage( "§7"+account.ID+" - "+Bukkit.getOfflinePlayer(UUID.fromString(account.ID)).getName()+" - "+new Date(account.timeDate));
                        }
                    });
                },
                error -> {
                    Bukkit.getScheduler().runTask(saLink, () -> {
                        player.sendMessage(new String[]{
                                "§8[ §fLINK §eSYSTEM §8]",
                                " §7Denne bruger har ingen linkede konti."
                        });
                    });
                }
            );
        });
    }
}
