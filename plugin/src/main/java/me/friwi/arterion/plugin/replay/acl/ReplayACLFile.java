package me.friwi.arterion.plugin.replay.acl;

import me.friwi.arterion.plugin.permissions.Rank;

import java.io.*;
import java.util.UUID;

public class ReplayACLFile {
    private File file;
    private ReplayACL acl;

    public ReplayACLFile(File file) throws IOException {
        this.file = file;
        if (file.exists() && file.isFile()) {
            InputStream in = new FileInputStream(file);
            acl = ReplayACL.read(in);
            in.close();
        } else {
            acl = new ReplayACL();
        }
    }

    public void addEntry(ReplayACLEntryType type, UUID object, boolean allowed) {
        acl.addEntry(type, object, allowed);
    }

    public boolean hasAccess(UUID player, UUID guild, Rank rank) {
        return acl.hasAccess(player, guild, rank);
    }

    public void save() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        OutputStream out = new FileOutputStream(file);
        acl.write(out);
        out.flush();
        out.close();
    }
}
