package me.friwi.arterion.website.replay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
public class UploadController {
    @Autowired
    ReplayUploadConfig replayUploadConfig;

    @PostMapping("/upload")
    public void handleFileUpload(HttpServletRequest request, HttpServletResponse response, @RequestParam("binaryFile") MultipartFile file) {
        String secret = request.getHeader("ReplayAuthentication");
        String path = request.getHeader("ReplayPath");
        if (secret == null || !secret.equals(replayUploadConfig.REPLAY_SECRET)) {
            try {
                response.sendError(403, "Unauthorized");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            File create = new File(replayUploadConfig.REPLAY_DIR, path + File.separator + file.getOriginalFilename());
            if (!create.exists()) {
                create.getParentFile().mkdirs();
                create.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(create);
            fos.write(file.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(500, "Error saving replay files");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            response.sendError(200, "Ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
