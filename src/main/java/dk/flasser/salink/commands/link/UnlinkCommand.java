package dk.flasser.salink.commands.link;

import dk.flasser.salink.SALink;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@eu.okaeri.commands.annotation.Command(
        label = "unlink"
)
@Async
public class UnlinkCommand implements CommandService {
    private @Inject LinkManager linkManager;
    private @Inject DiscordManager discordManager;
    private @Inject SALink saLink;

    @Executor
    public void _def(
            @Context Player player
    ) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
            List<String> discordIDs = linkManager.getLinkedActive(uuid);

            Guild guild = saLink.getJDA().getGuildById(discordManager.getGuild());
            Map<String, Long> roles = discordManager.getRoles();

            for (String discordID : discordIDs) {
                linkManager.removeLink(discordID, uuid);
                Member member = guild.getMemberById(discordID);

                roles.forEach((name, id) -> {
                    Role role = guild.getRoleById(id);

                    if (player.hasPermission("group."+name)) {
                        if (role != null) guild.removeRoleFromMember(member, role).queue();
                        else saLink.getLogger().info("No role was found with ID: "+id);
                    }
                });

                Role linkRole = guild.getRoleById(saLink.getConfig().getLong("discord.link-role"));
                if (linkRole != null) guild.removeRoleFromMember(member, linkRole).queue();
                else saLink.getLogger().info("No role was found with ID: "+linkRole);

                guild.modifyNickname(
                        member,
                        null
                ).queue();
            }

            Bukkit.getScheduler().runTask(saLink, () -> {
                player.sendMessage(new String[]{
                        "§8[ §f§lLINK §e§lSYSTEM §8]",
                        " §7Du er nu unlinket."
                });
            });
        });
    }
}
