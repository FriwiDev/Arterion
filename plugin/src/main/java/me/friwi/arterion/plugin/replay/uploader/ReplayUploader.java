package me.friwi.arterion.plugin.replay.uploader;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.file.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class ReplayUploader {
    public static void scheduleReplayUpload() {
        new Thread() {
            public void run() {
                setName("ReplayUploader");
                long tick = 0;
                while (ArterionPlugin.getInstance().isEnabled()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tick++;
                    if (tick % 20 == 0) {
                        scanForReplays();
                    }
                }
            }
        }.start();
    }

    public static void scanForReplays() {
        scanForReplays(ArterionPlugin.REPLAY_DIR, ArterionPlugin.REPLAY_DIR);
    }

    public static void scanForReplays(File base, File folder) {
        for (File content : folder.listFiles()) {
            if (content.isFile() && content.getName().equals("replayacl")) {
                try {
                    Thread.sleep(1000); //Make sure that replay has to be completely written
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doUpload(folder, base.toPath().relativize(folder.toPath()).toString());
                return;
            }
        }
        for (File content : folder.listFiles()) {
            if (content.isDirectory()) {
                scanForReplays(base, content);
            }
        }
    }

    public static void doUpload(File parent, String replayPath) {
        try {
            System.out.println("Replay upload begin: " + replayPath);
            for (File binaryFile : parent.listFiles()) {
                if (binaryFile.isDirectory()) continue;
                String charset = "UTF-8";
                String param = "value";
                String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
                String CRLF = "\r\n"; // Line separator required by multipart/form-data.

                URLConnection connection = new URL(ArterionPlugin.getInstance().getArterionConfig().replay_upload_url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.addRequestProperty("ReplayAuthentication", ArterionPlugin.getInstance().getArterionConfig().replay_upload_secret);
                connection.addRequestProperty("ReplayPath", replayPath);

                try (
                        OutputStream output = connection.getOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
                ) {
                    // Send binary file.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
                    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                    writer.append(CRLF).flush();
                    Files.copy(binaryFile.toPath(), output);
                    output.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                    // End of multipart/form-data.
                    writer.append("--" + boundary + "--").append(CRLF).flush();
                }

                // Request is lazily fired whenever you need to obtain information about response.
                int responseCode = ((HttpURLConnection) connection).getResponseCode();
                if (responseCode != 200)
                    throw new IllegalArgumentException("Replay upload response code was " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error uploading replay, will try again later: " + e.getClass().getName() + " " + e.getMessage());
            return;
        }
        try {
            FileUtils.deleteFolder(parent);
            System.out.println("Replay upload completed: " + replayPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
