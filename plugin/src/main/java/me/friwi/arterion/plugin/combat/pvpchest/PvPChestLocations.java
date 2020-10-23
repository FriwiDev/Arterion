package me.friwi.arterion.plugin.combat.pvpchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PvPChestLocations {
    private static final List<Location> spawnPositions = new ArrayList<>(128);

    static {
        register(250, 60, -53, 4);
        register(274, 118, -223, 5);
        register(368, 106, -235, 3);
        register(45, 93, -264, 3);
        register(14, 55, -375, 5);
        register(-181, 54, -349, 3);
        register(-252, 50, -341, 2);
        register(-284, 82, -265, 2);
        register(-275, 86, -202, 4);
        register(-207, 115, -190, 4);
        register(-177, 62, -35, 5);
        register(-193, 74, -44, 2);
        register(-174, 69, 14, 5);
        register(-251, 39, 53, 3);
        register(-279, 41, 24, 5);
        register(-262, 42, -24, 4);
        register(-328, 44, -34, 2);
        register(-318, 42, 141, 3);
        register(-155, 35, 209, 4);
        register(-98, 48, 239, 4);
        register(0, 80, 269, 5);
        register(100, 39, 240, 5);
        register(131, 30, 212, 5);
        register(162, 40, 123, 4);
        register(247, 62, 16, 4);
        register(316, 21, -41, 4);
        register(-212, 77, -86, 5);
        register(-88, 91, -267, 3);
        register(-167, 39, -275, 5);
        register(-64, 91, 330, 4);
        register(-207, 90, 289, 5);
        register(-259, 49, 228, 5);
        register(-320, 34, 186, 2);
        register(-171, 36, 142, 2);
        register(-108, 55, 199, 4);
        register(-79, 58, 122, 3);
        register(-135, 36, 107, 4);
        register(-173, 73, -63, 4);
        register(-203, 146, -257, 5);
        register(-197, 72, -347, 5);
        register(-210, 57, -364, 3);
        register(-125, 82, -362, 2);
        register(-61, 67, -382, 5);
        register(266, 132, -246, 5);
        register(291, 129, -293, 3);
        register(322, 129, -231, 4);
        register(306, 112, -203, 3);
        register(287, 77, -176, 5);
        register(196, 74, -106, 3);
        register(180, 71, -72, 5);
        register(152, 70, -58, 3);
        register(202, 65, -2, 4);
        register(241, 66, 22, 2);
        register(308, 52, 66, 3);
        register(327, 63, 44, 3);
        register(377, 37, 18, 4);
        register(-1, 46, 217, 3);
        register(-82, 45, 225, 3);
        register(-110, 40, 231, 2);
        register(-149, 36, 247, 5);
        register(-132, 40, 300, 3);
        register(-119, 41, 300, 4);
        register(-109, 40, 285, 4);
        register(-156, 35, 193, 2);
        register(-350, 55, -179, 3);
        register(-316, 55, -237, 4);
        register(-301, 60, -217, 2);
        register(-276, 81, -238, 4);
        register(-298, 95, -260, 4);
        register(-300, 39, 228, 4);
        register(-358, 34, 115, 2);
        register(-358, 34, 90, 3);
        register(-311, 38, 132, 2);
        register(-297, 40, -52, 3);
        register(-242, 43, -67, 4);
        register(-208, 76, -89, 3);
        register(-177, 62, -35, 5);
        register(-166, 62, -35, 4);
        register(-150, 73, 21, 2);
        register(-177, 68, -6, 5);
        register(-211, 62, -44, 5);
        register(-216, 62, -45, 4);
        register(-140, 41, 181, 5);
        register(2, 80, 279, 2);
        register(4, 80, 275, 4);
        register(4, 80, 273, 4);
        register(0, 80, 268, 5);
        register(0, 80, 275, 5);
        register(0, 80, 235, 5);
        register(1, 59, 202, 2);
        register(8, 47, 255, 3);
        register(98, 39, 238, 2);
        register(171, 32, 212, 5);
        register(191, 36, 183, 2);
        register(197, 39, 154, 5);
        register(6, 79, 148, 4);
        register(-7, 79, 157, 5);
        register(-24, 65, 179, 4);
        register(-21, 65, 171, 2);
        register(36, 64, 141, 5);
        register(41, 58, 139, 5);
        register(43, 58, 137, 3);
        register(97, 27, 185, 2);
        register(88, 25, 188, 5);
        register(83, 23, 199, 2);
        register(157, 41, 112, 4);
        register(224, 73, 91, 5);
        register(232, 77, 79, 3);
        register(284, 74, 47, 5);
        register(275, 77, -185, 3);
        register(288, 75, -187, 3);
        register(299, 78, -175, 5);
        register(296, 75, -185, 5);
        register(273, 80, -176, 5);
        register(364, 102, -258, 3);
        register(327, 129, -202, 2);
        register(323, 129, -206, 5);
        register(266, 132, -246, 5);
        register(266, 188, -245, 5);
        register(64, 93, -253, 3);
        register(33, 85, -251, 2);
        register(-230, 72, -190, 3);
        register(-236, 72, -188, 5);
        register(-216, 71, -162, 4);
        register(-233, 68, -151, 4);
        register(-253, 69, -165, 5);
        register(-349, 55, -178, 3);
        register(-342, 63, -213, 5);
        register(-315, 71, -251, 3);
        register(-272, 90, -191, 3);
        register(-266, 94, -172, 5);
        register(-323, 42, -40, 2);
        register(-303, 40, -71, 3);
        register(-275, 40, -59, 4);
        register(-242, 39, -42, 5);
        register(-244, 41, -38, 3);
        register(-224, 43, -11, 3);
        register(-259, 45, 18, 3);
        register(-283, 48, 17, 5);
        register(-285, 47, 19, 3);
        register(-286, 49, 18, 4);
        register(-290, 42, -4, 2);
        register(-289, 42, -19, 4);
        register(-301, 45, -26, 2);
        register(-262, 42, -24, 4);
        register(-233, 34, 116, 4);
        register(-294, 40, 146, 2);
        register(-279, 34, 153, 4);
    }

    private static void register(int x, int y, int z, int data) {
        spawnPositions.add(new Location(Bukkit.getWorlds().get(0), x, y, z, data, 0));
        Bukkit.getWorlds().get(0).getBlockAt(x, y, z).setType(Material.AIR);
    }

    public static Location getRandomLocation(Location blackListed, List<PvPChest> existing) {
        Collections.shuffle(spawnPositions);
        outer:
        for (int i = 0; i < spawnPositions.size(); i++) {
            Location test = spawnPositions.get(i);
            if (blackListed != null && blackListed.equals(test)) continue;
            for (PvPChest c : existing) {
                if (c.getLoc().getLocation().equals(test)) continue outer;
            }
            return test;
        }
        return null;
    }
}
