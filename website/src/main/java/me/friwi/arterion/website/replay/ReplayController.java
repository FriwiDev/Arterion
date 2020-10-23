package me.friwi.arterion.website.replay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

@Controller
public class ReplayController {
    public static final Pattern PATH_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    @Autowired
    ReplayUploadConfig replayUploadConfig;

    @RequestMapping(value = "/replay/{replayType}/{replayId}", method = RequestMethod.GET)
    public String replay(Model model, @PathVariable String replayType, @PathVariable String replayId) throws IOException {
        if (!PATH_PATTERN.matcher(replayType).matches() || !PATH_PATTERN.matcher(replayId).matches()) {
            return "replay/notfound";
        }
        File replayDir = new File(replayUploadConfig.REPLAY_DIR, replayType + File.separator + replayId);
        if (!replayDir.exists()) return "replay/notfound";
        ReplayACLFile acl = new ReplayACLFile(new File(replayDir, "replayacl"));
        boolean allowed = ReplayACLChecker.hasAccess(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), acl);
        if (!allowed) {
            return "replay/notallowed";
        }
        File download = new File(replayDir, replayDir.getName() + ".mcpr");
        if (!download.exists()) download = new File(replayDir, replayDir.getName() + ".zip");
        if (!download.exists()) return "replay/notfound";
        model.addAttribute("downloadName", download.getName());
        model.addAttribute("downloadLink", "/download/replay/" + replayType + "/" + replayId);
        return "replay/download";
    }
}
