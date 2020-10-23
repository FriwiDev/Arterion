package me.friwi.arterion.plugin.chat;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.reward.RewardUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public enum AnnouncementMessages {
    TUTORIAL(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.hasNewbieProtection();
        }
    },
    VOTE(ClickEvent.Action.OPEN_URL) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            long lastAction = player.getPersistenceHolder().getLastVote();
            long newAction = System.currentTimeMillis();
            RewardUtil.StreakContinueEnum streakContinueEnum = RewardUtil.getStreakContinue(lastAction, newAction);
            return streakContinueEnum == RewardUtil.StreakContinueEnum.STREAK_LOOSE
                    || streakContinueEnum == RewardUtil.StreakContinueEnum.NEXT_DAY;
        }
    },
    SUPPORT(ClickEvent.Action.RUN_COMMAND),
    GROUP(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() == null && player.getGroup() == null;
        }
    },
    CLIENT(ClickEvent.Action.OPEN_URL) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return !player.usesMod();
        }
    },
    SKILL_BIND(ClickEvent.Action.SUGGEST_COMMAND),
    WAR(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() != null;
        }
    },
    CHANNEL(ClickEvent.Action.SUGGEST_COMMAND),
    ARENA(ClickEvent.Action.RUN_COMMAND),
    PVPCHEST(ClickEvent.Action.RUN_COMMAND),
    RECIPES(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() != null;
        }
    },
    GINFO(ClickEvent.Action.SUGGEST_COMMAND),
    WHO(ClickEvent.Action.SUGGEST_COMMAND),
    HOMEBLOCK(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() == null && player.getHomeLocation() == null;
        }
    },
    ARTEFACT(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() != null;
        }
    },
    SIEGE_POINTS(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() != null;
        }
    },
    WEBSITE(ClickEvent.Action.OPEN_URL),
    LEGENDARY(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return !player.getRank().isHigherOrEqualThan(Rank.LEGENDARY);
        }
    },
    CLAN_KILLS(ClickEvent.Action.RUN_COMMAND) {
        @Override
        public boolean qualifies(ArterionPlayer player) {
            return player.getGuild() != null;
        }
    };
    private String key;
    private ClickEvent.Action action;

    private AnnouncementMessages(ClickEvent.Action action) {
        this.key = "announcements." + name().toLowerCase();
        this.action = action;
    }

    public static void sendNextMessage(ArterionPlayer player) {
        int ai = player.getAnnouncementIndex() + 1;
        ai %= values().length;
        player.setAnnouncementIndex(ai);
        AnnouncementMessages message = values()[ai];
        if (message.qualifies(player)) {
            message.sendToPlayer(player);
        } else {
            sendNextMessage(player);
        }
    }

    public static void startMessageScheduler() {
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInMyCircleTimer(new InternalTask() {
            @Override
            public void run() {
                for (Player player : ArterionPlugin.getOnlinePlayers()) {
                    sendNextMessage(ArterionPlayerUtil.get(player));
                }
            }
        }, 5 * 60 * 20, 5 * 60 * 20);
    }

    public boolean qualifies(ArterionPlayer player) {
        return true;
    }

    public Object[] getTextValues(ArterionPlayer player) {
        return new Object[0];
    }

    public Object[] getHoverValues(ArterionPlayer player) {
        return new Object[0];
    }

    public Object[] getActionValues(ArterionPlayer player) {
        return new Object[0];
    }

    public void sendToPlayer(ArterionPlayer player) {
        BaseComponent[] components = TextComponent.fromLegacyText(player.getTranslation(key + ".text", getTextValues(player)));
        TextComponent comp = new TextComponent(components);
        String hover = player.getTranslation(key + ".hover", getHoverValues(player));
        String actionValue = player.getTranslation(key + ".action", getActionValues(player));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        comp.setClickEvent(new ClickEvent(action, actionValue));
        player.getBukkitPlayer().spigot().sendMessage(comp);
    }
}
