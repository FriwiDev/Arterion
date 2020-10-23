package me.friwi.arterion.plugin.guild;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerList;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerListProvider;
import me.friwi.arterion.plugin.combat.friendlies.GuildFriendlyPlayerList;
import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.combat.gamemode.artefact.ArtefactDrops;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.economy.GuildMoneyBearer;
import me.friwi.arterion.plugin.guild.fight.GuildFight;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.stats.object.StatObject;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatObjectTracker;
import me.friwi.arterion.plugin.ui.mod.packet.Packet05FriendlyRemove;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.language.translateables.NumberTranslateable;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.block.nonbtblocks.GuildBlock;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import me.friwi.arterion.plugin.world.item.GoldItem;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Guild implements FriendlyPlayerListProvider, StatObject {
    public static final Pattern GUILD_NAME_PATTERN = Pattern.compile("[A-Z][a-z]{4,9}");
    public static final Pattern GUILD_TAG_PATTERN = Pattern.compile("[A-Z0-9]{3}");

    private DatabaseGuild persistenceHolder;
    private GuildMoneyBearer moneyBearer;
    private Location cachedHomeLocation;
    private List<ArterionPlayer> onlineMembers = new CopyOnWriteArrayList<>();
    private GuildRegion region;
    private GuildVault vault;

    private GuildFight localFight;

    private GuildBlock block;

    private FriendlyPlayerList friendlyPlayerList;
    private StatObjectTracker<Guild> statObjectTracker;

    public Guild(DatabaseGuild persistenceHolder) {
        this.persistenceHolder = persistenceHolder;
        if (persistenceHolder.getClaimWorld() != null) {
            cachedHomeLocation = new Location(Bukkit.getWorld(persistenceHolder.getClaimWorld()), persistenceHolder.getHomeX(), persistenceHolder.getHomeY(), persistenceHolder.getHomeZ());
        }
        this.moneyBearer = new GuildMoneyBearer(this, persistenceHolder.getGold());
        this.friendlyPlayerList = new GuildFriendlyPlayerList(this);
        try {
            this.vault = new GuildVault(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.statObjectTracker = new StatObjectTracker<>(this);
        if (hasArtefact()) beginDroppingArtefactItems();
    }

    /*
     * DB stuff below
     */

    public DatabaseGuild getPersistenceHolder() {
        return persistenceHolder;
    }

    public UUID getUuid() {
        return persistenceHolder.getUuid();
    }

    public String getName() {
        return persistenceHolder.getName();
    }

    public void setName(String name, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setName(name), successCallBack);
    }

    public String getTag() {
        return persistenceHolder.getTag();
    }

    public void setTag(String tag, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setTag(tag), success -> {
            if (success) {
                ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        for (ArterionPlayer p : onlineMembers) updateInterfaceGuildAffiliation(p);
                    }
                });
            }
            successCallBack.accept(success);
        });
    }

    public long getCreated() {
        return persistenceHolder.getCreated();
    }

    public long getDeleted() {
        return persistenceHolder.getDeleted();
    }

    public void setDeleted(long deleted, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setDeleted(deleted), successCallBack);
    }

    public long getMinusBankBalance() {
        return persistenceHolder.getMinusBankBalance();
    }

    public void setMinusBankBalance(long minusBankBalance, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setMinusBankBalance(minusBankBalance), successCallBack);
    }

    public int getObsidian() {
        return persistenceHolder.getObsidian();
    }

    public void setObsidian(int obsidian, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setObsidian(obsidian), successCallBack);
    }

    public boolean isXpShare() {
        return persistenceHolder.isXpShare();
    }

    public void setXpShare(boolean xpShare, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setXpShare(xpShare), successCallBack);
    }

    public boolean isGoldShare() {
        return persistenceHolder.isGoldShare();
    }

    public void setGoldShare(boolean goldShare, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setGoldShare(goldShare), successCallBack);
    }

    public Location getHomeLocation() {
        return cachedHomeLocation == null ? null : cachedHomeLocation.clone();
    }

    public void setHomeLocation(Location homeLocation, Consumer<Boolean> successCallBack) {
        Location finalHomeLocation = homeLocation == null ? null : homeLocation.clone();
        updateInDB(g -> {
            if (finalHomeLocation == null) {
                g.setClaimWorld(null);
                g.setHomeX(0);
                g.setHomeY(0);
                g.setHomeZ(0);
            } else {
                g.setClaimWorld(finalHomeLocation.getWorld().getName());
                g.setHomeX(finalHomeLocation.getBlockX());
                g.setHomeY(finalHomeLocation.getBlockY());
                g.setHomeZ(finalHomeLocation.getBlockZ());
            }
        }, success -> {
            if (success) cachedHomeLocation = finalHomeLocation;
            successCallBack.accept(success);
        });
    }

    public long getLastReclaim() {
        return persistenceHolder.getLastReclaim();
    }

    public void setLastReclaim(long lastReclaim, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setLastReclaim(lastReclaim), successCallBack);
    }

    public GuildMoneyBearer getMoneyBearer() {
        return moneyBearer;
    }

    public DatabasePlayer getLeader() {
        return persistenceHolder.getLeader();
    }

    public Set<DatabasePlayer> getOfficers() {
        return persistenceHolder.getOfficers();
    }

    public Set<DatabasePlayer> getMembers() {
        return persistenceHolder.getMembers();
    }

    public void setLeader(DatabasePlayer newLeader, Consumer<Boolean> successCallBack) {
        updateInDB(g -> {
            g.getMembers().remove(newLeader);
            g.getOfficers().remove(newLeader);
            g.getMembers().add(g.getLeader());
            g.setLeader(newLeader);
        }, successCallBack);
    }

    public void promoteOfficer(DatabasePlayer newOfficer, Consumer<Boolean> successCallBack) {
        updateInDB(g -> {
            g.getMembers().remove(newOfficer);
            g.getOfficers().add(newOfficer);
        }, successCallBack);
    }

    public void demoteOfficer(DatabasePlayer oldOfficer, Consumer<Boolean> successCallBack) {
        updateInDB(g -> {
            g.getOfficers().remove(oldOfficer);
            g.getMembers().add(oldOfficer);
        }, successCallBack);
    }

    public void addMember(DatabasePlayer newMember, Consumer<Boolean> successCallBack) {
        updateInDB(g -> {
            g.getMembers().add(newMember);
        }, successCallBack);
    }

    public void removeMember(DatabasePlayer oldMember, Consumer<Boolean> successCallBack) {
        updateInDB(g -> {
            boolean del = g.getMembers().remove(oldMember);
            if (!del) del = g.getOfficers().remove(oldMember);
            if (del) {
                g.setClanKills(g.getClanKills() - oldMember.getClanKills());
                if (g.getClanKills() < 0) g.setClanKills(0);
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        Player p = Bukkit.getPlayer(oldMember.getUuid());
                        if (p != null && p.isOnline()) {
                            ArterionPlayerUtil.get(p).setClanKills(0, succ -> {
                            });
                        } else {
                            new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, oldMember.getUuid()) {

                                @Override
                                public void updateObject(DatabasePlayer removed) {
                                    removed.setClanKills(0);
                                }

                                @Override
                                public void success() {

                                }

                                @Override
                                public void fail() {

                                }
                            }.execute();
                        }
                    }
                });
            }
        }, successCallBack);
    }

    public int getMemberCount() {
        return 1 + getOfficers().size() + getMembers().size();
    }

    public int getOnlineMemberCount() {
        return onlineMembers.size();
    }

    private void updateInDB(Consumer<DatabaseGuild> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabaseGuild>(DatabaseGuild.class, persistenceHolder.getUuid()) {
            DatabaseGuild updatedHolder;

            @Override
            public void updateObject(DatabaseGuild databaseGuild) {
                updatedHolder = databaseGuild;
                apply.accept(databaseGuild);
            }

            @Override
            public void success() {
                Guild.this.persistenceHolder = updatedHolder;
                successCallBack.accept(true);
            }

            @Override
            public void fail() {
                successCallBack.accept(false);
            }
        }.execute();
    }

    public Set<DatabasePlayer> getAllMembersIncludingOfficersAndLeader() {
        Set<DatabasePlayer> ret = new HashSet<>();
        ret.add(getLeader());
        ret.addAll(getOfficers());
        ret.addAll(getMembers());
        return ret;
    }

    /*
     * Live server stuff below
     */

    public List<ArterionPlayer> getOnlineMembers() {
        return onlineMembers;
    }

    public void sendTranslation(String key, Object... values) {
        for (ArterionPlayer p : onlineMembers) p.sendTranslation(key, values);
    }

    public void sendMessage(String msg) {
        for (ArterionPlayer p : onlineMembers) p.sendMessage(msg);
    }

    public boolean isInGuild(ArterionPlayer player) {
        return isInGuild(player.getBukkitPlayer().getUniqueId());
    }

    public boolean isInGuild(UUID u) {
        return getMember(u) != null;
    }

    public DatabasePlayer getMember(UUID u) {
        if (getLeader().getUuid().equals(u)) return getLeader();
        for (DatabasePlayer p : getOfficers()) {
            if (p.getUuid().equals(u)) return p;
        }
        for (DatabasePlayer p : getMembers()) {
            if (p.getUuid().equals(u)) return p;
        }
        return null;
    }

    public DatabasePlayer getOfficer(UUID u) {
        if (getLeader().getUuid().equals(u)) return getLeader();
        for (DatabasePlayer p : getOfficers()) {
            if (p.getUuid().equals(u)) return p;
        }
        return null;
    }

    public void updateMember(DatabasePlayer databasePlayer) {
        //DatabasePlayer compares by uuid only!
        if (getLeader().equals(databasePlayer)) {
            persistenceHolder.setLeader(databasePlayer);
            return;
        }
        if (getOfficers().remove(databasePlayer)) {
            getOfficers().add(databasePlayer);
            return;
        }
        if (getMembers().remove(databasePlayer)) {
            getMembers().add(databasePlayer);
            return;
        }
    }

    public void onPlayerJoin(ArterionPlayer player) {
        this.onPlayerJoin(player, false);
    }

    public void onPlayerJoin(ArterionPlayer player, boolean silent) {
        this.onlineMembers.add(player);
        if (!silent) this.sendTranslation("guild.member.joined", player);
        if (this.getBlock() != null) this.getBlock().updateGui(0);
        updateInterfaceGuildAffiliation(player);
    }

    public void onPlayerLeave(ArterionPlayer player) {
        this.onPlayerLeave(player, false);
    }

    public void onPlayerLeave(ArterionPlayer player, boolean silent) {
        this.onlineMembers.remove(player);
        if (!silent) this.sendTranslation("guild.member.left", player);
        if (this.getBlock() != null) this.getBlock().updateGui(0);
        updateInterfaceGuildAffiliation(player);
        for (ArterionPlayer p : onlineMembers) {
            if (p.usesMod()) {
                ModConnection.sendModPacket(p, new Packet05FriendlyRemove(player.getBukkitPlayer().getUniqueId()));
            }
        }
    }

    public GuildRegion getRegion() {
        return region;
    }

    public void setRegion(GuildRegion region) {
        this.region = region;
    }

    public boolean isInLocalFight() {
        return localFight != null;
    }

    public GuildFight getLocalFight() {
        return localFight;
    }

    public void setLocalFight(GuildFight localFight) {
        this.localFight = localFight;
    }

    public boolean isAllowedOnLand(ArterionPlayer other) {
        if (other.getGuild() != null) {
            if (this.equals(other.getGuild())) return true;
            if (localFight != null) {
                if (localFight.getAttackerWhitelist().contains(other.getBukkitPlayer().getUniqueId())) return true;
                localFight.signup(other);
                if (localFight.getAttackerWhitelist().contains(other.getBukkitPlayer().getUniqueId())) return true;
            }
        }
        return false;
    }

    public void deleteGuild(boolean dueToTax) {
        //Artefact
        if (hasArtefact()) Artefact.reset();
        //DB Stuff
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                GlobalStats.getContext().getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), Guild.this);
                GlobalStats.getTopContext().getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), Guild.this);
            }
        });
        updateInDB(g -> {
            g.setDeleted(System.currentTimeMillis());
            g.setMinusBankBalance(DatabaseGuild.NOT_IN_MINUS_BALANCE);
            getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
        }, success -> {
            if (success) {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        if (Guild.this.getRegion() != null) {
                            ArterionPlugin.getInstance().getRegionManager().unRegisterRegionParallel(region, GuildRegion.REGISTER_BATCH_SIZE, () -> {
                            });
                            setRegion(null);
                            ArterionPlugin.getInstance().getSpecialBlockManager().remove(getHomeLocation());
                            getHomeLocation().getBlock().setType(Material.AIR);
                            setHomeLocation(null, success -> {
                            });
                        }
                        for (ArterionPlayer p : getOnlineMembers()) {
                            p.setGuild(null);
                        }
                        LanguageAPI.broadcastMessage("line");
                        LanguageAPI.broadcastMessage(dueToTax ? "guild.disband.tax" : "guild.disband.normal", Guild.this);
                        LanguageAPI.broadcastMessage("line");
                        for (ArterionPlayer p : onlineMembers) updateInterfaceGuildAffiliation(p);
                        getOnlineMembers().clear();
                        getVault().drop(1f);
                    }
                });

            }
        });
    }

    public void calculateAndSubtractGuildTax(List<Guild> nextGuilds) {
        if (getRegion() == null || getDeleted() != DatabaseGuild.NOT_DELETED) {
            if (nextGuilds.size() > 0) {
                Guild next = nextGuilds.remove(0);
                next.calculateAndSubtractGuildTax(nextGuilds);
            }
            return;
        }
        List<Chunk> chunks = new LinkedList<>();
        getRegion().forEachChunkParallel(c -> chunks.add(c), 3, () -> {
            new Thread("Guild-Tax-Calc-" + this.getName()) {
                public void run() {
                    int[] obsifound = new int[1];
                    //Fetch one chunk per tick to allow for smooth calculation
                    List<ChunkSnapshot> snapshots = new LinkedList<>();
                    Object lock = new Object();
                    for (Chunk c : chunks) {
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                if (!c.isLoaded()) c.load(true);
                                snapshots.add(c.getChunkSnapshot(true, false, false));
                                synchronized (lock) {
                                    lock.notifyAll();
                                }
                            }
                        });
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //Now iterate over fetched snapshots
                    for (ChunkSnapshot snapshot : snapshots) {
                        int obsi = 0;
                        int id = 0;
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                int yh = snapshot.getHighestBlockYAt(x, z);
                                for (int y = 0; y < yh; y++) {
                                    id = snapshot.getBlockTypeId(x, y, z);
                                    if (id == Material.OBSIDIAN.getId() || id == Material.ENDER_STONE.getId()) {
                                        obsi++;
                                    }
                                }
                            }
                        }
                        obsifound[0] += obsi;
                    }
                    Guild.this.setObsidian(obsifound[0], success -> {
                    });
                    //Calculate gold
                    int amount = ArterionPlugin.getInstance().getFormulaManager().GUILD_TAX.evaluateInt(obsifound[0]);
                    //Artefact
                    if (hasArtefact()) {
                        amount *= ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_GUILD_TAX_MULTIPLIER.evaluateDouble();
                        trackStatistic(StatType.ARTEFACT_HOURS, 0, v -> v + 1);
                    }
                    //Subtract from guild inv
                    int finalAmount = amount;
                    Guild.this.getMoneyBearer().addMoney(-amount, success -> {
                        if (success) {
                            Guild.this.sendTranslation("line");
                            Guild.this.sendTranslation("guild.tax", obsifound[0], finalAmount / 100f);
                            Guild.this.sendTranslation("line");
                            //Check if guild negative balance (and disband it)
                            if (Guild.this.getMoneyBearer().getCachedMoney() < 0) {
                                long mb = 0;
                                if (Guild.this.getMinusBankBalance() == DatabaseGuild.NOT_IN_MINUS_BALANCE) {
                                    Guild.this.setMinusBankBalance(System.currentTimeMillis(), s -> {
                                    });
                                    mb = System.currentTimeMillis();
                                } else {
                                    mb = Guild.this.getMinusBankBalance();
                                }
                                int days = ArterionPlugin.getInstance().getFormulaManager().GUILD_DISBAND_TIME.evaluateInt(Guild.this);
                                if (mb < System.currentTimeMillis() - days * 1000 * 60 * 60 * 24) {
                                    Guild.this.forceAllGuisClose();
                                    //Guild is x days at minus balance
                                    Guild.this.deleteGuild(true);
                                } else {
                                    int hoursLeft = (int) ((mb + days * 1000 * 60 * 60 * 24 - System.currentTimeMillis() + 1000) / 3600000);
                                    int daysLeft = hoursLeft / 24;
                                    hoursLeft %= 24;
                                    Guild.this.sendTranslation("guild.tax.negative", daysLeft, hoursLeft);
                                }
                            } else if (Guild.this.getMinusBankBalance() != DatabaseGuild.NOT_IN_MINUS_BALANCE) {
                                Guild.this.setMinusBankBalance(DatabaseGuild.NOT_IN_MINUS_BALANCE, s -> {
                                });
                            }
                        }//else hmm, shit happens. We'll get 'em next time
                    }, true);
                    //Start calculation for next guild
                    if (nextGuilds.size() > 0) {
                        Guild next = nextGuilds.remove(0);
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                next.calculateAndSubtractGuildTax(nextGuilds);
                            }
                        });
                    }
                    //Save guild vault once per hour
                    getVault().save();
                }
            }.start();
        });
    }

    public void forceAllGuisClose() {
        for (ArterionPlayer p : onlineMembers) p.closeGui();
        if (getVault() != null) getVault().hideAll();
    }

    public void playNote(Instrument instrument, Note note) {
        for (ArterionPlayer p : onlineMembers) {
            p.getBukkitPlayer().playNote(p.getBukkitPlayer().getLocation(), instrument, note);
        }
    }

    public GuildBlock getBlock() {
        return block;
    }

    public void setBlock(GuildBlock block) {
        this.block = block;
    }

    public void updateInterfaceGuildAffiliation(ArterionPlayer p) {
        ArterionPlugin.getInstance().getTablistManager().updatePlayerListName(p);
        p.getPlayerScoreboard().updateAllPlayerRelations();
        if (this.getLocalFight() != null) {
            p.getSkillSlots().setObjective(this.getLocalFight().getObjective(p), SkillSlots.GUILD_OBJECTIVE_PRIORITY);
        }
    }

    public long getProtection() {
        return persistenceHolder.getProtection();
    }

    public void setProtection(long protection, Consumer<Boolean> successCallBack) {
        updateInDB(g -> g.setProtection(protection), successCallBack);
    }

    public boolean isProtected() {
        return this.getProtection() > System.currentTimeMillis();
    }

    @Override
    public FriendlyPlayerList getFriendlyPlayerList() {
        return friendlyPlayerList;
    }

    public boolean hasArtefact() {
        return persistenceHolder.isHasArtefact();
    }

    public void setHasArtefact(boolean hasArtefact, Consumer<Boolean> successCallBack) {
        if (getHomeLocation() != null) {
            if (hasArtefact) {
                LanguageAPI.broadcastMessage("artefact.captured", this.getName());
                Artefact.appendTranslationToRecordingChat("artefact.replay.captured", this.getName());
                this.getHomeLocation().getBlock().setData((byte) (this.getHomeLocation().getBlock().getData() % 4 + 4));
                ParticleEffect.FLAME.display(1.5f, 1.5f, 1.5f, 0, 20, this.getHomeLocation(), Skill.PARTICLE_RANGE);
                this.getHomeLocation().getWorld().playSound(this.getHomeLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
                DatabasePlayer capturer = Artefact.getCarrier().getPersistenceHolder();
                Artefact.setCarrier(null);
                Artefact.setOwner(this);
                if (Artefact.getFight() != null) Artefact.getFight().end(this.getPersistenceHolder(), capturer, false);
            } else {
                this.getHomeLocation().getBlock().setData((byte) (this.getHomeLocation().getBlock().getData() % 4));
            }
        }
        updateInDB(g -> g.setHasArtefact(hasArtefact), succ -> {
            if (succ) {
                beginDroppingArtefactItems();
            }
            successCallBack.accept(succ);
        });
    }

    private void beginDroppingArtefactItems() {
        int tickInterval = ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_DROPS_INTERVAL.evaluateInt();
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (!hasArtefact()) {
                    cancel();
                    return;
                }
                tick += 20;
                if (tick % tickInterval == 0 && getOnlineMembers().size() > 0) {
                    //Drop stuff
                    ItemStack stack = ArtefactDrops.INSTANCE.getRandomDrop();
                    CustomItem item = CustomItemUtil.getCustomItem(stack);
                    for (ArterionPlayer player : getOnlineMembers()) {
                        String itemName;
                        String itemAmount;
                        if (item instanceof GoldItem) {
                            itemName = player.getTranslation("money");
                            itemAmount = NumberTranslateable.formatNumber(((GoldItem) item).getAmount() / 100d);
                        } else {
                            itemName = player.getLanguage().translateObject(stack);
                            itemAmount = stack.getAmount() + "x";
                        }
                        player.sendTranslation("artefact.itemgain", itemName, itemAmount);
                    }
                    getHomeLocation().getChunk().load();
                    getHomeLocation().getWorld().dropItemNaturally(getHomeLocation().clone().add(0.5, 1, 0.5), stack);
                }
            }
        }, 20, 20);
    }

    public GuildUpgradeLevel getUpgradeLevel(GuildUpgradeEnum upgradeEnum) {
        GuildUpgradeLevel level = persistenceHolder.getGuildUpgradeLevel().get(upgradeEnum);
        if (level == null) {
            return GuildUpgradeLevel.LEVEL_1;
        } else {
            return level;
        }
    }

    public void upgrade(GuildUpgradeEnum upgradeEnum, GuildUpgradeLevel level, Consumer<Boolean> callback) {
        GuildUpgradeLevel current = getUpgradeLevel(upgradeEnum);
        if (current.ordinal() >= level.ordinal() || level.ordinal() > upgradeEnum.getMaxLevel().ordinal()) {
            callback.accept(false);
            return;
        }
        this.updateInDB(dbg -> dbg.getGuildUpgradeLevel().put(upgradeEnum, level), succ -> {
            if (succ) {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        upgradeEnum.onUpgrade(Guild.this, level);
                    }
                });
            }
            callback.accept(succ);
        });
    }

    public GuildVault getVault() {
        return vault;
    }

    public void reclaimRegion() {
        if (region != null) {
            ArterionPlugin.getInstance().getRegionManager().all().remove(region);
        }
        if (this.getDeleted() == DatabaseGuild.NOT_DELETED && this.getHomeLocation() != null) {
            int dist = this.getRegionDistance();
            GuildRegion region = new GuildRegion(this, this.getHomeLocation().getWorld(), (this.getHomeLocation().getBlockX() >> 4) - dist, (this.getHomeLocation().getBlockX() >> 4) + dist, (this.getHomeLocation().getBlockZ() >> 4) - dist, (this.getHomeLocation().getBlockZ() >> 4) + dist);
            ArterionPlugin.getInstance().getRegionManager().registerRegion(region);
            this.setRegion(region);
        }
    }

    public int getRegionDistance() {
        return GuildUpgradeEnum.REGION.getValue(getUpgradeLevel(GuildUpgradeEnum.REGION));
    }

    public int getMaxOfficers() {
        return GuildUpgradeEnum.OFFICER.getValue(getUpgradeLevel(GuildUpgradeEnum.OFFICER));
    }

    public int getVaultRows() {
        return GuildUpgradeEnum.VAULT.getValue(getUpgradeLevel(GuildUpgradeEnum.VAULT));
    }

    public int getClanKills() {
        return persistenceHolder.getClanKills();
    }

    public void setClanKills(int clanKills, Consumer<Boolean> callback) {
        this.updateInDB(dbg -> dbg.setClanKills(clanKills), callback);
    }

    public ChatColor getClanTagColor() {
        int kills = getClanKills();
        if (kills < 100) {
            return ChatColor.GRAY;
        } else if (kills < 400) {
            return ChatColor.GREEN;
        } else if (kills < 1000) {
            return ChatColor.DARK_GREEN;
        } else if (kills < 2000) {
            return ChatColor.RED;
        } else if (kills < 3500) {
            return ChatColor.GOLD;
        } else {
            return ChatColor.DARK_PURPLE;
        }
    }

    @Override
    public StatObjectType getType() {
        return StatObjectType.GUILD;
    }

    @Override
    public UUID getUUID() {
        return persistenceHolder.getUuid();
    }

    @Override
    public StatObjectTracker getStatTracker() {
        return statObjectTracker;
    }

    public void trackStatistic(StatType type, int statData, Function<Long, Long> valueMapper) {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().trackStatistic(ArterionPlugin.getInstance().getExternalDatabase(), type, statData, valueMapper);
            }
        });
    }
}
