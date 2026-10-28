package dk.flasser.salink;

import dk.flasser.salink.managers.DiscordManager;
import dk.flasser.salink.managers.SQLManager;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.plan.ExecutionPhase;
import eu.okaeri.platform.core.plan.Planned;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bstats.bukkit.Metrics;

@Scan(deep = true)
public final class SALink extends OkaeriBukkitPlugin {
    private @Inject SQLManager sqlManager;
    private JDA jda;

    @Planned(ExecutionPhase.STARTUP)
    public void onStartup() {
        getLogger().info("SALINK: STARTING UP");
    }

    @Planned(ExecutionPhase.POST_SETUP)
    public void afterSetup() {
        saveDefaultConfig();

        int pluginId = 34980;
        Metrics metrics = new Metrics(this, pluginId);

        getLogger().info("SALINK: STARTING DISCORD BOT");
        String token = getConfig().getString("discord.token");

        if (token == null || token.isEmpty()
                || token.equals("PUT_YOUR_BOT_TOKEN_HERE")) {
            getLogger().warning("Discord bot token is missing!");
            return;
        }

        jda = JDABuilder.createDefault(token)
                .addEventListeners(new DiscordManager())
                .build();

        this.getLogger().info("SALINK: CONNECTING TO DATABASE");
        sqlManager.connect();

        if (!sqlManager.isSetUp()) {
            this.getLogger().info("SALINK: SETTING UP DATABASE");
            sqlManager.setUp();
        }
    }

    @Planned(ExecutionPhase.SHUTDOWN)
    public void onShutdown() {
        this.getLogger().info("SALINK: DISCONNECTING FROM DATABASE");
        sqlManager.disconnect();

        getLogger().info(String.format(
                "[%s] Disabled Version %s",
                getDescription().getName(),
                getDescription().getVersion()
        ));
    }

    public JDA getJDA() {
        return jda;
    };
}
