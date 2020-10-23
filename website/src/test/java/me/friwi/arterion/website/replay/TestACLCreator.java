package me.friwi.arterion.website.replay;

import me.friwi.arterion.plugin.util.database.enums.Rank;

import java.io.File;
import java.io.IOException;

public class TestACLCreator {
    public static void main(String args[]) throws IOException {
        ReplayACLFile acl = new ReplayACLFile(new File("replays_test/replayacl"));
        acl.addEntry(ReplayACLEntryType.ALL, null, true);
        acl.save();
        System.out.println(acl.hasAccess(null, null, Rank.NORMAL));
    }
}
