package me.friwi.arterion.plugin.permissions;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.sanctions.SanctionType;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabaseSanction;

import java.util.List;
import java.util.UUID;

public class SanctionManager {
    private ArterionPlugin plugin;

    public SanctionManager(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public DatabaseSanction getCurrentSanction(UUID playerUuid, SanctionType type) {
        List<DatabaseSanction> sanctions = plugin.getExternalDatabase().findAllByColumn(DatabaseSanction.class, "affected", new DatabasePlayer(playerUuid));
        for (DatabaseSanction sanction : sanctions) {
            if (sanction.getSanctionType() == type && sanction.isSanctionActive()) {
                return sanction;
            }
        }
        return null;
    }

    public boolean pardon(UUID playerUuid, SanctionType type) {
        boolean updated = false;
        List<DatabaseSanction> sanctions = plugin.getExternalDatabase().findAllByColumn(DatabaseSanction.class, "affected", new DatabasePlayer(playerUuid));
        for (DatabaseSanction sanction : sanctions) {
            if (sanction.getSanctionType() == type && sanction.isSanctionActive()) {
                sanction.setExpires(System.currentTimeMillis());
                plugin.getExternalDatabase().save(sanction);
                updated = true;
            }
        }
        return updated;
    }

    public void sanction(DatabasePlayer affected, DatabasePlayer issuer, SanctionType type, long issued, long expires, String reason) {
        DatabaseSanction sanction = new DatabaseSanction(issued, expires, type, reason, affected, issuer);
        plugin.getExternalDatabase().save(sanction);
    }
}
