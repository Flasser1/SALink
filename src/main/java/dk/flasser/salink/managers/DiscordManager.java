package dk.flasser.salink.managers;

import dk.flasser.salink.SALink;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class DiscordManager extends ListenerAdapter {
    private @Inject SALink saLink;
    private @Inject LinkManager linkManager;
    private @Inject CacheManager cacheManager;

    List<String> adminRoleIds;

    @Override
    public void onReady(ReadyEvent event) {
        Guild guild = null;

        try {
            String guildId = saLink.getConfig()
                    .getString("discord.guild-id");

            guild = event.getJDA().getGuildById(guildId);
            adminRoleIds = saLink.getConfig().getStringList("discord.admin-roles-ids");

            if (guild == null) {
                saLink.getLogger().warning(
                        "Server id is missing."
                );
                return;
            }
        } catch (NumberFormatException e) {
            saLink.getLogger().severe("Invalid discord server ID, either server ID is not valid or bot is not in the server!");
            return;
        }

        guild.upsertCommand(
                "link",
                "Link din konto."
        ).addOption(
                OptionType.STRING,
                "kode",
                "Din link-kode",
                true
        ).queue(
                command -> saLink.getLogger().info(
                        "Registered /link command!"
                ),
                error -> saLink.getLogger().severe(
                        "Failed to register /link: "
                                + error.getMessage()
                )
        );

        guild.upsertCommand(
                "unlink",
                "Unlink din konto."
        ).addOption(
                OptionType.STRING,
                "minecraft-navn",
                "Dit minecraft-navn",
                true
        ).queue(
            command -> saLink.getLogger().info(
                    "Registered /unlink command!"
            ),
            error -> saLink.getLogger().severe(
                    "Failed to register /unlink: "
                            + error.getMessage()
            )
        );

        guild.upsertCommand(
                "logsdiscord",
                "Tjek link historie for en bruger."
        ).addOption(
                OptionType.STRING,
                "discord-id",
                "Brugerens discord-id",
                true
        ).queue(
                command -> saLink.getLogger().info(
                        "Registered /logsdiscord command!"
                ),
                error -> saLink.getLogger().severe(
                        "Failed to register /logsdiscord: "
                                + error.getMessage()
                )
        );

        saLink.getLogger().info("DISCORD BOT SUCCESSFULLY STARTED");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("link")) {
            event.deferReply(true).queue();

            Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
                String code = event.getOption("kode").getAsString();

                if (!cacheManager.containsCodeCache(code)) {
                    event.getHook().editOriginal("Denne kode findes ikke, prøv igen.").queue();
                    return;
                }

                UUID uuid = cacheManager.getCode(code);
                Member member = event.getMember();

                assert member != null;
                linkManager.addLink(member.getId(), uuid);

                Bukkit.getScheduler().runTask(saLink, () -> {
                    try {
                        event.getGuild().modifyNickname(
                                member,
                                Bukkit.getOfflinePlayer(uuid).getName()
                        ).queue();
                        event.getHook().editOriginal("Du er nu linket med din konto.").queue();
                    } catch (HierarchyException e) {
                        saLink.getLogger().info("Tried to modify a nickname after a user linked, but ran into a HierarchyException. Make sure that the bot has a higher role than any user. User ID is: "+member.getId());

                        event.getHook().editOriginal(
                                "Du er nu linket med din konto."
                        ).queue();
                    }
                });
            });
        }

        if (event.getName().equals("unlink")) {
            event.deferReply(true).queue();

            String name = event.getOption("minecraft-navn").getAsString();
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);

            Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
                Member member = event.getMember();

                if (!linkManager.isLinked(member.getId(), player.getUniqueId())) {
                    event.getHook().editOriginal("Du er ikke linket med denne konto.").queue();
                    return;
                }

                linkManager.removeLink(member.getId(), player.getUniqueId());
                assert member != null;
                try {
                    event.getGuild().modifyNickname(
                            member,
                            null
                    ).queue();
                    event.getHook().editOriginal("Du er nu unlinket.").queue();
                } catch (HierarchyException e) {
                    saLink.getLogger().info("Tried to modify a nickname after a user linked, but ran into a HierarchyException. Make sure that the bot has a higher role than any user. User ID is: "+member.getId());

                    event.getHook().editOriginal(
                            "Du er nu unlinket."
                    ).queue();
                }
            });
        }

        if (event.getName().equals("logsdiscord")) {
            event.deferReply(true).queue();

            Bukkit.getScheduler().runTaskAsynchronously(saLink, () -> {
                Member member = event.getMember();

                if (member == null || adminRoleIds == null || member.getRoles().stream().noneMatch(role -> adminRoleIds.contains(role.getId()))) {
                    event.getHook().editOriginal("Du har ikke adgang til dette").queue();
                    return;
                }

                String target = event.getOption("discord-id").getAsString();

                List<LinkManager.LinkedAccount> accounts = linkManager.getLinkedAll(target);

                if (accounts.isEmpty()) {
                    event.getHook().editOriginal("Denne bruger har ingen linkede konti").queue();
                    return;
                }

                saLink.getJDA().retrieveUserById(target).queue(
                    user -> {
                        String username = user.getName();
                        StringBuilder msg = new StringBuilder("Logs for " + username + "\n");

                        Bukkit.getScheduler().runTask(saLink, () -> {
                            for (LinkManager.LinkedAccount account : accounts) {
                                if (account.active) msg.append("- ").append(account.ID).append(" - ").append(Bukkit.getOfflinePlayer(UUID.fromString(account.ID)).getName()).append(" - ").append(new Date(account.timeDate)).append(" - (Active)").append("\n");
                                else msg.append("- ").append(account.ID).append(" - ").append(Bukkit.getOfflinePlayer(UUID.fromString(account.ID)).getName()).append(" - ").append(new Date(account.timeDate)).append("\n");
                            }

                            event.getHook().editOriginal(msg.toString()).queue();
                        });
                    },
                    error -> {
                        event.getHook().editOriginal("Denne bruger har ingen linkede konti.").queue();
                    }
                );
            });
        }
    }

    public void reload() {
        adminRoleIds = saLink.getConfig().getStringList("discord.admin-roles-ids");
    }
}
