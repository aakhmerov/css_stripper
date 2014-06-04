package de.smava.css.intersector;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

/**
 * Intersect takes two sets (folders): oryginals_a, originals_b of CSS files and intersect them. As a result produce three folders with CSS files: intersected, differences_a, differences_b.
 * Remember that all files from set_a must have theirs equivalent in name in set_b.
 *
 * @author Daniel Suszczynski
 */

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String ORIGINALS_A_SUB_PATH = "/originals_a";
    private static final String ORIGINALS_B_SUB_PATH = "/originals_b";
    private static final String INTERSECTED_SUB_PATH = "/intersected";
    private static final String DIFFERENCES_A_SUB_PATH = "/differences_a";
    private static final String DIFFERENCES_B_SUB_PATH = "/differences_b";
    private static final String INTERSECTED_OUT_SUFFIX = "_intersected.css";
    private static final String DEFAULT_CHARSET = "UTF-8";

    public static void main(String[] args) {
        if (args.length != 1 || args[0] == null) {
            printInfo();
        } else {
            String basePath = args[0];
            File originalsAFolder = new File(basePath + ORIGINALS_A_SUB_PATH);
            File originalsBFolder = new File(basePath + ORIGINALS_B_SUB_PATH);
            File intersectedFolder = new File(basePath + INTERSECTED_SUB_PATH);
            File differencesAFolder = new File(basePath + DIFFERENCES_A_SUB_PATH);
            File differencesBFolder = new File(basePath + DIFFERENCES_B_SUB_PATH);

//          clean outgoing files if they exists
            if (intersectedFolder.exists()) {
                intersectedFolder.delete();
            }
            intersectedFolder.mkdirs();

            if (differencesAFolder.exists()) {
                differencesAFolder.delete();
            }
            differencesAFolder.mkdirs();

            if (differencesBFolder.exists()) {
                differencesBFolder.delete();
            }
            differencesBFolder.mkdirs();

            Intersector intersect = new Intersector();

            for (final File originalAFolderFile : originalsAFolder.listFiles()) {
                FilenameFilter originalsABFoldersFilenameFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        boolean result = false;
                        if (name.startsWith(originalAFolderFile.getName())) {
                            result = true;
                        }
                        return result;
                    }
                };
                File[] matchingOriginalBFolderFiles = originalsBFolder.listFiles(originalsABFoldersFilenameFilter);

                if (matchingOriginalBFolderFiles == null || matchingOriginalBFolderFiles.length != 1) {
                    LOGGER.error("Matching oryginals A files should be mappable 1 to 1 with oryginals B");
                    LOGGER.error("Current matches : [" + matchingOriginalBFolderFiles + "]");
                } else {
                    File originalBFolderFile = matchingOriginalBFolderFiles[0];
                    File intersectedFolderFile = new File(intersectedFolder.getAbsolutePath() + "/" + originalAFolderFile.getName().split("\\.")[0] + INTERSECTED_OUT_SUFFIX);
                    File differencesAFolderFile = new File(differencesAFolder.getAbsolutePath() + "/" + originalAFolderFile.getName());
                    File differencesBFolderFile = new File(differencesBFolder.getAbsolutePath() + "/" + originalAFolderFile.getName());

                    Map<IntersectDataType, StringBuffer> intersectedData = null;

                    try {
                        intersectedData = intersect.intersect(FileUtils.readFileToString(originalAFolderFile), FileUtils.readFileToString(originalBFolderFile));
                    } catch (IOException e) {
                        LOGGER.error("Rrror during reading CSS files and intersect data", e);
                    }

                    try {
                        FileUtils.write(intersectedFolderFile, intersectedData.get(IntersectDataType.INTERSECTED));
                    } catch (IOException e) {
                        LOGGER.error("Can't write intersected file [" + intersectedFolderFile.getAbsolutePath() + "]", e);
                    }
                    try {
                        FileUtils.write(differencesAFolderFile, intersectedData.get(IntersectDataType.DIFFERENCE_A));
                    } catch (IOException e) {
                        LOGGER.error("Can't write differences A file [" + differencesAFolderFile.getAbsolutePath() + "]", e);
                    }
                    try {
                        FileUtils.write(differencesBFolderFile, intersectedData.get(IntersectDataType.DIFFERENCE_B));
                    } catch (IOException e) {
                        LOGGER.error("Can't write differences B file [" + differencesBFolderFile.getAbsolutePath() + "]", e);
                    }
                }
            }

        }
    }

    private static void printInfo() {
        LOGGER.info("Please provide path to the folder with proper structure as an argument.");
        LOGGER.info("Folder structure:");
        LOGGER.info("./oryginals_a");
        LOGGER.info("./originals_b");
        LOGGER.info("./intersected");
        LOGGER.info("./differences_a");
        LOGGER.info("./differences_b");
    }

}
