package me.friwi.arterion.plugin.world.region;

import com.google.common.collect.ImmutableList;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Objects;

public class GuildRegion extends Region {
    public static final int REGISTER_BATCH_SIZE = 3;
    public static final List<Material> ALLOWED_DESTROY_TYPES = ImmutableList.of(
            Material.DIAMOND_BLOCK,
            Material.DIAMOND_ORE,
            Material.EMERALD_BLOCK,
            Material.EMERALD_ORE,
            Material.GOLD_BLOCK,
            Material.GOLD_ORE,
            Material.IRON_BLOCK,
            Material.IRON_ORE,
            Material.LAPIS_BLOCK,
            Material.LAPIS_ORE,
            Material.COAL_BLOCK,
            Material.COAL_ORE,
            Material.REDSTONE_ORE
    );
    private Guild guild;

    public GuildRegion(Guild guild, World world, int x1, int x2, int z1, int z2) {
        super("region.guild", world, x1, x2, z1, z2, true, true, false, true, true, false);
        this.guild = guild;
    }

    @Override
    public boolean canPlayerBuild(ArterionPlayer p) {
        return p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE || (p.getGuild() != null && p.getGuild().equals(guild) && !guild.isInLocalFight());
    }

    @Override
    public boolean canPlayerDestroy(ArterionPlayer p, Block b) {
        if (ALLOWED_DESTROY_TYPES.contains(b.getType())) return true;
        return p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE || (p.getGuild() != null && p.getGuild().equals(guild));
    }

    @Override
    public boolean isNoEnter(ArterionPlayer player) {
        if (player.getGuild() == null) return true;
        if (!guild.isAllowedOnLand(player)) return true;
        if (guild.isInLocalFight() && player.getGuild().equals(guild) && guild.getLocalFight().getDefender().equals(guild)) {
            if (!guild.getLocalFight().getDefenderWhitelist().contains(player.getUUID())) {
                player.sendTranslation("fight.guild.login.ended");
                return true;
            }
        }
        return false;
    }

    public String getName(Language lang) {
        return lang.getTranslation(this.getRawName()).translate(guild.getName()).getMessage();
    }

    @Override
    public void greetMsg(ArterionPlayer player) {
        TitleAPI.send(player, player.getTranslation(this.getRawName() + ".greettitle", guild.getName()), player.getTranslation(this.getRawName() + ".greetsubtitle", guild.getName()));
    }

    @Override
    public void denyMsg(ArterionPlayer player) {
        TitleAPI.send(player, player.getTranslation(this.getRawName() + ".denytitle", guild.getName()), player.getTranslation(this.getRawName() + ".denysubtitle", guild.getName()));
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public boolean belongsToPlayer(ArterionPlayer player) {
        return getGuild().getMember(player.getBukkitPlayer().getUniqueId()) != null;
    }

    @Override
    public boolean administeredByPlayer(ArterionPlayer player) {
        return getGuild().getLeader().getUuid().equals(player.getBukkitPlayer().getUniqueId());
    }

    @Override
    public void onEnter(ArterionPlayer player) {
        if (getGuild().isInLocalFight() && getGuild().getLocalFight().getDefender().equals(getGuild()))
            getGuild().getLocalFight().enterRegion(player);
    }

    @Override
    public void onLeave(ArterionPlayer player) {
        if (getGuild().isInLocalFight() && getGuild().getLocalFight().getDefender().equals(getGuild()))
            getGuild().getLocalFight().leaveRegion(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuildRegion)) return false;
        GuildRegion region = (GuildRegion) o;
        return region.getGuild().equals(getGuild());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGuild());
    }
}
