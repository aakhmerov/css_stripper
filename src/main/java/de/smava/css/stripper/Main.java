package de.smava.css.stripper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: aakhmerov
 * Date: 9/13/13
 * Time: 5:34 PM
 *
 * Entry point for stand alone version of CSS stripper.
 * Parse input css file and file containing list of unused styles.
 * Compose output file containing resulting css file without all styles specified in list of unused files.
 *
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String ORIGINAL_SUB_PATH = "/original";
    private static final String ANALYSIS_SUB_PATH = "/analysis";
    private static final String OUTPUT_SUB_PATH = "/stripped";
    private static final String OUT_SUFFIX = "_stripped.css";
    private static final String DEFAULT_CHARSET = "UTF-8";


    /**
     * Main entry point of the tool. based on the attributes create and execute proper implementation
     * of css stripping logic.
     *
     * Arguments expected :
     * 1. path to the folder with following structure
     *  - original - folder with original css files that have to be stripped
     *  - analysis - folder with analysis files taken from any external tool
     *               NOTE: files in that folder should start names same way as files in original
     *               folder for proper mapping
     *
     * Output will be provided in "stripped" folder in specified base path
     *
     * @param args - list of arguments for the stripper.
     */
    public static void main(String[] args) {
//      read provided css file
        if (args.length != 1 || args[0] == null) {
            printInfo();
        } else {
            String basePath = args[0];
            File originalsFolder = new File( basePath + ORIGINAL_SUB_PATH);
            final File analysisFolder = new File( basePath + ANALYSIS_SUB_PATH);
            File outputFolder = new File( basePath + OUTPUT_SUB_PATH);

//          clean out existing files if they exist
            if (outputFolder.exists()) {
                outputFolder.delete();
            }
            outputFolder.mkdirs();
            Stripper stripper = new Stripper ();
            for (final File original : originalsFolder.listFiles()) {
//              JDK 7 specific implementation - TODO: get rid of it
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        boolean result = false;
                        if (name.startsWith(original.getName())) {
                            result = true;
                        }
                        return result;
                    }
                };
                File[] matchingAnalysisFiles = analysisFolder.listFiles(filter);

//
                if (matchingAnalysisFiles == null || matchingAnalysisFiles.length != 1) {
                    logger.error("Matching analysis files should be mappable 1 to 1 with original");
                    logger.error("Current matches : [" + matchingAnalysisFiles + "]");
                } else {
                    File analysis = matchingAnalysisFiles[0];
                    File destination = new File (outputFolder.getAbsolutePath() + "/" + original.getName().split("\\.")[0] + OUT_SUFFIX);
                    StringBuffer data = null;
                    try {
                        data = stripper.strip(FileUtils.readFileToString(original),FileUtils.readFileToString(analysis));
                    } catch (IOException e) {
                        logger.error("error reading CSS files and analysis data",e);
                    }
                    try {
                        FileUtils.write(destination, data);
                    } catch (IOException e) {
                        logger.error("Cant write destination file [" + destination.getAbsolutePath() + "]",e);
                    }
                }
            }

        }
    }

    private static void printInfo() {
        logger.info("please provide path to the folder with proper structure as an argument");
        logger.info("folder structure : ");
        logger.info("./base");
        logger.info("   ./original");
        logger.info("   ./analysis");
    }
}
