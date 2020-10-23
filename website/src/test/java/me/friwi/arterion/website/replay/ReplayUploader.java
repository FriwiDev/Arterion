package me.friwi.arterion.website.replay;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class ReplayUploader {
    private static final String SECRET = "VFYpCgrbqg6Gzh2WxQSL8jNwWBMQbWDf";
    private static final String UPLOAD_URL = "http://localhost:9090/upload";

    public static void main(String args[]) {
        scanForReplays(new File("replays_test"), new File("replays_test"));
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

                URLConnection connection = new URL(UPLOAD_URL).openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.addRequestProperty("ReplayAuthentication", SECRET);
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
            deleteFolder(parent);
            System.out.println("Replay upload completed: " + replayPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function recursively deletes all the sub folder and files from sourceFolder
     */
    public static void deleteFolder(File sourceFolder) throws IOException {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then delete the file directly
        if (sourceFolder.isDirectory()) {
            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and delete them one by one
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);

                //Recursive function call
                deleteFolder(srcFile);
            }
            //Delete the directory
            sourceFolder.delete();
        } else {
            //Delete the file
            sourceFolder.delete();
        }
    }
}
