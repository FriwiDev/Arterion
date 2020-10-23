package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.chat.ChatChannel;
import me.friwi.arterion.plugin.combat.skill.SkillEnum;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.permissions.TimeUnit;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.villager.VillagerType;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommandManager {
    private static final String[] commandBlacklist = new String[]{
            "org.bukkit.command.defaults",
            "org.spigotmc.RestartCommand",
            "org.spigotmc.TicksPerSecondCommand",
            "org.bukkit.craftbukkit." + ArterionPlugin.REFLECTION_VERSION + ".command.VanillaCommandWrapper"
    };
    private static final String[] commandVanillaNameBlacklist = new String[]{
            "defaultgamemode",
            "spreadplayers",
            "achievement",
            "tell",
            "kick",
            "ban",
            "help",
            "stats",
            "scoreboard",
            "whitelist",
            "seed",
            //"worldborder",
            "execute",
            "title",
            "list",
            "particle",
            "tellraw",
            "spawnpoint",
            "ban-ip",
            "setidletimeout",
            "testforblock",
            "replaceitem",
            "trigger",
            "kill",
            "pardon-ip",
            "playsound",
            "me",
            "setworldspawn",
            //"setblock",
            "pardon",
            "blockdata",
            "testfor",
            "debug",
            "say"
    };
    private BaseCommand[] allCommands = new BaseCommand[]{
            new HelpCommand(),
            new RankCommand(this),
            new SetPosCommand(this),
            new ResetCommand(this),
            new SpawnCommand(this),
            new BanCommand(this),
            new UnbanCommand(this),
            new MuteCommand(this),
            new UnmuteCommand(this),
            new KickCommand(this),
            new MaintenanceCommand(this),
            new ChannelCommand(this),
            new TellCommand(this),
            new ReplyCommand(this),
            new PayCommand(this),
            new AddMoneyCommand(this),
            new AdCommand(this),
            new FormulaCommand(this),
            new VillagerCommand(this),
            new InviteCommand(this),
            new HomeCommand(this),
            new GroupCommand(this),
            new WarCommand(this),
            new StealCommand(this),
            new AddXPCommand(this),
            new ArenaCommand(this),
            new LevelCommand(),
            new DamageDebugCommand(),
            new HealCommand(),
            new SkillCommand(),
            new SkillDiscCommand(),
            new SkillsCommand(),
            new SkillSlotCommand(),
            new BindCommand(),
            new UnbindCommand(),
            new KillAllCommand(),
            new ResetCooldownCommand(),
            new StopCommand(),
            new JobsCommand(),
            new ArtefactCommand(this),
            new GraveruinCommand(this),
            new DeserttempleCommand(this),
            new MorgothCommand(this),
            new EchestCommand(this),
            new GinfoCommand(this),
            new GlistCommand(this),
            new IgnoreCommand(this),
            new InfoCommand(this),
            new InvseeCommand(this),
            new ListCommand(this),
            new RecipesCommand(this),
            new TpaCommand(this),
            new VanishCommand(this),
            new VoteCommand(this),
            new WebsiteCommand(this),
            new TutorialCommand(this),
            new HologramCreateCommand(this),
            new HologramDeleteCommand(this),
            new KillCommand(this),
            new TellposCommand(this),
            new CreateTokenCommand(this),
            new QuestsCommand(),
            new WorldGenCommand(this),
            new GamemodeCommand(this),
            new TpCommand(this),
            new TpNextCommand(this),
            new TpPrevCommand(this),
            new BoosterCommand(this),
            new AntiCheatDebugCommand(),
            new HudCommand(this),
            new SkillpointsCommand(),
            new GuildManageCommand(this)
    };
    private ArterionPlugin plugin;
    private co.aikar.commands.CommandManager manager;
    private Map<String, Command> commandMap;

    public CommandManager(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        new BukkitRunnable() {
            @Override
            public void run() {
                CommandManager.this.unregisterAllBukkitCommands();
            }
        }.runTaskLater(plugin, 1L);
        manager = new PaperCommandManager(plugin);
        for (BaseCommand cmd : allCommands) manager.registerCommand(cmd);
        manager.getCommandCompletions().registerCompletion("rank", c -> {
            List<String> values = new LinkedList<>();
            for (Rank r : Rank.values()) {
                values.add(r.name());
            }
            return values;
        });
        manager.getCommandCompletions().registerCompletion("gamemode", c -> {
            List<String> values = new LinkedList<>();
            for (GameMode r : GameMode.values()) {
                values.add(r.name());
            }
            return values;
        });
        manager.getCommandCompletions().registerCompletion("timeunit", c -> {
            List<String> values = new LinkedList<>();
            for (TimeUnit r : TimeUnit.values()) {
                values.add(r.name());
            }
            return values;
        });
        manager.getCommandCompletions().registerCompletion("state", c -> {
            List<String> values = new LinkedList<>();
            for (StateEnum r : StateEnum.values()) {
                values.add(r.name());
            }
            return values;
        });
        manager.getCommandCompletions().registerCompletion("channel", c -> {
            List<String> values = new LinkedList<>();
            for (ChatChannel r : ChatChannel.values()) {
                values.add(r.name());
            }
            return values;
        });
        manager.getCommandCompletions().registerCompletion("villagertype", c -> {
            List<String> values = new LinkedList<>();
            for (VillagerType r : VillagerType.values()) {
                values.add(r.name());
            }
            return values;
        });
        manager.getCommandCompletions().registerCompletion("formula", c -> plugin.getFormulaManager().getAvailableFormulas());
        manager.getCommandCompletions().registerCompletion("guild", c -> StreamSupport.stream(plugin.getGuildManager().getGuilds().spliterator(), false).map(g -> g.getName()).collect(Collectors.toList()));
        manager.getCommandCompletions().registerCompletion("skill", c -> {
            List<String> values = new LinkedList<>();
            if (c.getIssuer().getIssuer() instanceof Player) {
                ArterionPlayer ap = ArterionPlayerUtil.get(c.getIssuer().getIssuer());
                for (SkillEnum s : SkillEnum.values()) {
                    values.add(s.getSkill().getName(ap).replace(" ", "_"));
                }
            }
            return values;
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                manager.registerCommand(new PluginsCommand(CommandManager.this));
            }
        }.runTaskLater(plugin, 5L);
    }

    private void unregisterAllBukkitCommands() {
        System.out.println("Removing all standard commands");
        try {
            Object result = getPrivateField(plugin.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            this.commandMap = knownCommands;
            for (Object c : new HashSet<>(knownCommands.values()).toArray()) {
                for (String prefix : commandBlacklist) {
                    if (c.getClass().getCanonicalName().startsWith(prefix)) {
                        List<String> problematicKeys = new LinkedList<>();
                        for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                            if (entry.getValue().getClass().equals(c.getClass())) problematicKeys.add(entry.getKey());
                        }
                        boolean vanilla = c.getClass().getName().endsWith("VanillaCommandWrapper");
                        for (String key : problematicKeys) {
                            if (vanilla) {
                                if (key.startsWith("minecraft:")) {
                                    //System.out.println("Unregistering command /"+key);
                                    knownCommands.remove(key);
                                }
                                for (String b : commandVanillaNameBlacklist) {
                                    if (b.equalsIgnoreCase(key)) {
                                        //System.out.println("Unregistering command /"+key);
                                        knownCommands.remove(key);
                                        continue;
                                    }
                                }
                            } else {
                                //System.out.println("Unregistering command /" + key);
                                knownCommands.remove(key);
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getPrivateField(Object object, String field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    public Command getCommand(String command) {
        return commandMap.get(command);
    }

    public ArterionPlugin getPlugin() {
        return plugin;
    }

    public enum StateEnum {
        ON(true),
        OFF(false);
        private boolean booleanMapping;

        StateEnum(boolean booleanMapping) {
            this.booleanMapping = booleanMapping;
        }

        public boolean getBooleanMapping() {
            return booleanMapping;
        }
    }
}
