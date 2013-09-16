package de.smava.css.stripper;

import com.phloc.css.ECSSVersion;
import com.phloc.css.ICSSWriterSettings;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriter;
import com.phloc.css.writer.CSSWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aakhmerov
 * Date: 9/16/13
 * Time: 9:11 AM
 *
 * Actual stripper service class. performs processing of specified files
 * and provides proper output.
 */
public class Stripper {

    private static final String CHARSET = "UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ECSSVersion CSS_VERSION = ECSSVersion.CSS30;

    /**
     * Provide output stream after unused styles have been stripped out of source file based
     * on analysis provided by external tool.
     *
     * @param toStrip
     * @param analyzedData
     * @return
     */
    public StringBuffer strip (String toStrip, String analyzedData) {
        StringBuffer result = new StringBuffer();
        CascadingStyleSheet aCSS = CSSReader.readFromString(toStrip, Charset.forName(CHARSET), CSS_VERSION);
        List<String> analyzedSelectors = listData(analyzedData);

        CascadingStyleSheet resultingCss = new CascadingStyleSheet();
        for (ICSSTopLevelRule rule : aCSS.getAllRules()) {
            String original = ((CSSStyleRule) rule).getSelectorsAsCSSString(new CSSWriterSettings(CSS_VERSION),0);
//          replace all whitespaces and chars like \t, \n from selector signature
            String clean = original.replaceAll("\\s+","");
            if (!analyzedSelectors.contains(clean)){
                resultingCss.addRule(rule);
            }

        }
        CSSWriter writer = new CSSWriter(CSS_VERSION);
        try {
            writer.setHeaderText("");
            result.append(writer.getCSSAsString(resultingCss));
        } catch (IOException e) {
            logger.error("cannot write resulting CSS", e);
        }
        return result;
    }

    /**
     *
     * @param analyzedData
     * @return
     */
    public List<String> listData(String analyzedData) {
        List<String> result = new ArrayList<String>();

        try {
            BufferedReader r = new BufferedReader(new StringReader(analyzedData));
            String toPush = r.readLine();

            while (toPush != null) {
//          replace all whitespaces and chars like \t, \n from selector signature
                toPush = toPush.replaceAll("\\s+","");
                result.add(toPush);
                toPush = r.readLine();
            }

        } catch (IOException e) {
            logger.error("cant read analyzed data",e);
        }

        return result;
    }

    /**
     * Utility method to wrap CSS analysis outcomes into format recognized by parser and
     * wrap it ino common internal data structures
     *
     * @param analyzedData - String containing analyzed data in format of Chromium
     * @return
     */
    public CascadingStyleSheet composeRemovalCss(String analyzedData) {
        BufferedReader r = new BufferedReader(new StringReader(analyzedData));
        String line = null;
        StringBuilder result = new StringBuilder();
        try {
            line = r.readLine();
            while (line != null) {
                result.append(line + "{}\n");
                line = r.readLine();
            }
        } catch (IOException e) {
            logger.error("error while processing analysis data",e);
        }
        return CSSReader.readFromString(result.toString(), Charset.forName(CHARSET), CSS_VERSION);
    }


    /**
     * This is utility comparator, to assert equality of CSS wrapped rules based
     * on their selector values only.
     *
     * if classes don't match or
     */
    private class SelectorComparator implements Comparator< ICSSTopLevelRule> {

        @Override
        public int compare(ICSSTopLevelRule o1, ICSSTopLevelRule o2) {
            int result = 1;
            if (o1 instanceof CSSStyleRule && o2 instanceof CSSStyleRule) {
                CSSStyleRule cast1 = (CSSStyleRule) o1;
                CSSStyleRule cast2 = (CSSStyleRule) o2;
                ICSSWriterSettings settings = new CSSWriterSettings(CSS_VERSION);
                if ((cast1.getAllSelectors().size() == cast2.getAllSelectors().size()) &&
                        cast1.getSelectorsAsCSSString(settings,0).equals(cast2.getSelectorsAsCSSString(settings,0))
                        ) {
                    result = 0;
                }
            } else {
                if (o1.equals(o2)) result = 0;
            }
            return result;
        }
    }
}
