package me.friwi.arterion.plugin.util.file;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    /**
     * This function recursively copy all the sub folder and files from sourceFolder to destinationFolder
     */
    public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                //Recursive function call
                copyFolder(srcFile, destFile);
            }
        } else {
            //Copy the file content from one place to another
            Files.copy(sourceFolder, destinationFolder);
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
