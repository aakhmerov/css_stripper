package de.smava.css.stripper;

import com.phloc.commons.io.IInputStreamProvider;
import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.reader.CSSReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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


    public static void main(String[] args) {

        File file  = new File (ClassLoader.getSystemClassLoader().getResource("test.css").getFile());
        if (file.exists()) {
            CascadingStyleSheet aCSS = CSSReader.readFromFile(file, "utf-8", ECSSVersion.CSS30);

            for (ICSSTopLevelRule rule : aCSS.getAllRules()) {
                logger.debug(rule.toString());
            }
        } else {
            logger.error("css file does not exist");
        }

    }
}
