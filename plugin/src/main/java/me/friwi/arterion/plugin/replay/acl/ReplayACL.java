package me.friwi.arterion.plugin.replay.acl;

import me.friwi.arterion.plugin.permissions.Rank;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ReplayACL {
    private List<ReplayACLEntry> aclEntryList = new LinkedList<>();

    public static ReplayACL read(InputStream stream) throws IOException {
        ByteBuffer read = ByteBuffer.allocate(65536);
        int r;
        while ((r = stream.read(read.array(), read.position(), read.limit() - read.position())) > 0) {
            read.position(read.position() + r);
        }
        read.position(0);
        short len = read.getShort();
        ReplayACL acl = new ReplayACL();
        for (int i = 0; i < len; i++) {
            ReplayACLEntryType type = ReplayACLEntryType.values()[read.get()];
            UUID object = null;
            if (read.get() == (byte) 1) {
                object = new UUID(read.getLong(), read.getLong());
            }
            boolean allowed = read.get() == (byte) 1;
            acl.addEntry(type, object, allowed);
        }
        return acl;
    }

    public void addEntry(ReplayACLEntryType type, UUID object, boolean allowed) {
        aclEntryList.add(new ReplayACLEntry(type, object, allowed));
    }

    public boolean hasAccess(UUID player, UUID guild, Rank rank) {
        if (rank.isHigherTeam()) return true;
        for (ReplayACLEntry ent : aclEntryList) {
            if (ent.getType() == ReplayACLEntryType.ALL) {
                return ent.isAllowed();
            } else if (ent.getType() == ReplayACLEntryType.PLAYER && player != null && player.equals(ent.getObject())) {
                return ent.isAllowed();
            } else if (ent.getType() == ReplayACLEntryType.GUILD && guild != null && guild.equals(ent.getObject())) {
                return ent.isAllowed();
            }
        }
        return false; //Default to no-access
    }

    public void write(OutputStream stream) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.allocate(65536);
        writeBuffer.putShort((short) aclEntryList.size());
        for (ReplayACLEntry entry : aclEntryList) {
            writeBuffer.put((byte) entry.getType().ordinal());
            if (entry.getObject() == null) {
                writeBuffer.put((byte) 0);
            } else {
                writeBuffer.put((byte) 1);
                writeBuffer.putLong(entry.getObject().getMostSignificantBits());
                writeBuffer.putLong(entry.getObject().getLeastSignificantBits());
            }
            writeBuffer.put((byte) (entry.isAllowed() ? 1 : 0));
        }
        stream.write(writeBuffer.array(), 0, writeBuffer.position());
    }
}
