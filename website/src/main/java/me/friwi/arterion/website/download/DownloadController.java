package me.friwi.arterion.website.download;

import me.friwi.arterion.website.replay.ReplayACLChecker;
import me.friwi.arterion.website.replay.ReplayACLFile;
import me.friwi.arterion.website.replay.ReplayUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

@Controller
public class DownloadController {
    public static final String CLIENTMOD_VERSION = "0.21";
    public static final Pattern PATH_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    @Autowired
    ReplayUploadConfig replayUploadConfig;

    @RequestMapping(value = "/download/mod", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Resource downloadMod(HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"ArterionClientMod-" + CLIENTMOD_VERSION + ".jar\"");
        return new ClassPathResource("download/ArterionClientMod-" + CLIENTMOD_VERSION + ".jar");
    }

    @RequestMapping(value = "/download/replay/{replayType}/{replayId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Resource downloadReplay(HttpServletResponse response, @PathVariable String replayType, @PathVariable String replayId) throws IOException {
        if (!PATH_PATTERN.matcher(replayType).matches() || !PATH_PATTERN.matcher(replayId).matches()) {
            return null;
        }
        File replayDir = new File(replayUploadConfig.REPLAY_DIR, replayType + File.separator + replayId);
        if (!replayDir.exists()) return null;
        ReplayACLFile acl = new ReplayACLFile(new File(replayDir, "replayacl"));
        boolean allowed = ReplayACLChecker.hasAccess(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), acl);
        if (!allowed) {
            return null;
        }
        File download = new File(replayDir, replayDir.getName() + ".mcpr");
        if (!download.exists()) download = new File(replayDir, replayDir.getName() + ".zip");
        if (!download.exists()) return null;
        response.setHeader("Content-Disposition", "attachment; filename=\"" + download.getName() + "\"");
        return new FileSystemResource(download);
    }
}
