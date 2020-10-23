package me.friwi.arterion.plugin.player.reward;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RewardUtilTest {
    public static final ZoneId TIME_ZONE = ZoneId.of("CET");
    public static final ZoneId SERVER_TIME_ZONE = ZoneId.of("Z");

    public static void main(String args[]) {
        long lastVote = 1596280187907L;
        long day = 24 * 60 * 60 * 1000;
        System.out.println(getStreakContinue(lastVote, lastVote + day * 0));
    }

    public static RewardUtil.StreakContinueEnum getStreakContinue(long lastAction, long newAction) {
        ZonedDateTime last = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastAction), SERVER_TIME_ZONE).atZone(SERVER_TIME_ZONE).withZoneSameInstant(TIME_ZONE);
        ZonedDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(newAction), SERVER_TIME_ZONE).atZone(SERVER_TIME_ZONE).withZoneSameInstant(TIME_ZONE);
        System.out.println("last: " + last + ", current: " + now);
        System.out.println("lastSec: " + Instant.from(last).getEpochSecond() + ", currentSec: " + Instant.from(now).getEpochSecond());
        long lastDay = Instant.from(last).getEpochSecond() / (24 * 60 * 60);
        long currentDay = Instant.from(now).getEpochSecond() / (24 * 60 * 60);
        System.out.println("lastDay: " + lastDay + ", currentDay: " + currentDay);
        if (lastDay >= currentDay) {
            return RewardUtil.StreakContinueEnum.SAME_DAY;
        } else if (lastDay + 1 == currentDay) {
            return RewardUtil.StreakContinueEnum.NEXT_DAY;
        } else {
            return RewardUtil.StreakContinueEnum.STREAK_LOOSE;
        }
    }
}
