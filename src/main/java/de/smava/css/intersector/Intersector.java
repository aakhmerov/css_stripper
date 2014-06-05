package de.smava.css.intersector;

import com.phloc.css.ECSSVersion;
import com.phloc.css.ICSSWriterSettings;
import com.phloc.css.decl.*;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriter;
import com.phloc.css.writer.CSSWriterSettings;
import de.smava.css.intersector.comparators.CSSDeclarationsComparator;
import de.smava.css.intersector.comparators.CSSSelectorsComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Suszczynski
 */

public class Intersector {

    private static final String CHARSET = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(Intersector.class);
    private static final ECSSVersion CSS_VERSION = ECSSVersion.CSS30;

    /**
     * Intersect two CSS styles sets
     *
     * @param toIntersectA CSS content to intersect
     * @param toIntersectB CSS content to intersect
     * @return Map<String key, StringBuffer CSScode> keys: intersected, differenceA, differenceB
     */
    public Map<IntersectDataType, StringBuffer> intersect(String toIntersectA, String toIntersectB) {
        Map<IntersectDataType, StringBuffer> result = new HashMap<IntersectDataType, StringBuffer>();

        StringBuffer intersected = new StringBuffer();
        StringBuffer differenceA = new StringBuffer();
        StringBuffer differenceB = new StringBuffer();

        CascadingStyleSheet cssToIntersectA = CSSReader.readFromString(toIntersectA, Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssToIntersectB = CSSReader.readFromString(toIntersectB, Charset.forName(CHARSET), CSS_VERSION);

        CascadingStyleSheet intersectedCSS = new CascadingStyleSheet();
        CascadingStyleSheet differenceACSS = new CascadingStyleSheet();
        CascadingStyleSheet differenceBCSS = new CascadingStyleSheet();

        List<ICSSTopLevelRule> allRulesA = cssToIntersectA.getAllRules();
//        List<ICSSTopLevelRule> allRulesB = cssToIntersectB.getAllRules();

        CSSSelectorsComparator cssSelectorComparator = new CSSSelectorsComparator();
        CSSDeclarationsComparator cssDeclarationsComparator = new CSSDeclarationsComparator();

        for (ICSSTopLevelRule ruleCSSA : allRulesA) {
            int selectorAppearanceInRuleCSSB = 0;

            for (ICSSTopLevelRule ruleCSSB : cssToIntersectB.getAllRules()) {
                // search for adequate selector in B
                if (cssSelectorComparator.compare(ruleCSSA, ruleCSSB) == 0) {
                    selectorAppearanceInRuleCSSB++;

                    // if selector appears more than once in B, then throw exception (developer should check CSS, and make improvements there)
                    // therefore there is no break; in the end of this method
                    /*if(selectorAppearanceInRuleCSSB > 1) {
        				throw new CSSSelectorMultipleAppearanceException("Multiple Appearance of CSSSelector : " + ((CSSStyleRule) ruleCSSB).getSelectorsAsCSSString(new CSSWriterSettings(CSS_VERSION), 0));
        			}*/

                    // compare rule declarations
                    if (cssDeclarationsComparator.compare(ruleCSSA, ruleCSSB) == 0) {
                        intersectedCSS.addRule(ruleCSSA);
                    } else {
                        // otherwise pull out common declarations and placed them to intersected/${file_name}_intersected.css,
                        // rest of declarations put accordingly to differences_a/${file_name}.css and differences_b/${file_name}.css
                        Map<IntersectDataType, ICSSTopLevelRule> cssDeclarationsMerge = this.cssDeclarationsMerge(ruleCSSA, ruleCSSB);
                        intersectedCSS.addRule(cssDeclarationsMerge.get(IntersectDataType.INTERSECTED));
                        differenceACSS.addRule(cssDeclarationsMerge.get(IntersectDataType.DIFFERENCE_A));
                        differenceBCSS.addRule(cssDeclarationsMerge.get(IntersectDataType.DIFFERENCE_B));
                    }

                    // remove rule A and B from lists
                    cssToIntersectA.removeRule(ruleCSSA);
                    cssToIntersectB.removeRule(ruleCSSB);

                    break;
                }
            }

            // if selector from CSS A didn't appear in CSS B, then move it to differences_a/${file_name}.css
            if (selectorAppearanceInRuleCSSB == 0) {
                differenceACSS.addRule(ruleCSSA);
            }

//          TODO: fix ClassCastException: com.phloc.css.decl.CSSMediaRule cannot be cast to com.phloc.css.decl.CSSStyleRule
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("selectorsA:");
                for (CSSSelector cssSelectorA : ((CSSStyleRule) ruleCSSA).getAllSelectors()) {
                    LOGGER.trace("\t" + cssSelectorA.getAsCSSString(new CSSWriterSettings(CSS_VERSION), 0));
                }

                if (((CSSStyleRule) ruleCSSA).getAllDeclarations() != null && ((CSSStyleRule) ruleCSSA).getAllDeclarations().size() > 0) {
                    for (CSSDeclaration cssDeclarationA : ((CSSStyleRule) ruleCSSA).getAllDeclarations()) {
                        LOGGER.trace("\t\t" + cssDeclarationA.getAsCSSString(new CSSWriterSettings(CSS_VERSION), 0) /*+ " : " + cssDeclarationA.getExpression().getAsCSSString(new CSSWriterSettings(CSS_VERSION), 0)*/);
                    }
                } else {
                    LOGGER.trace("\t\t-");
                }
                LOGGER.trace("");
            }


        }

        // move all rules which left in CSS B to differences_b/${file_name}.css
        for (ICSSTopLevelRule leftRuleCSSB : cssToIntersectB.getAllRules()) {
            differenceBCSS.addRule(leftRuleCSSB);
        }

        CSSWriter cssIntersectedWriter = new CSSWriter(CSS_VERSION);
        try {
            cssIntersectedWriter.setHeaderText("");
            intersected.append(cssIntersectedWriter.getCSSAsString(intersectedCSS));
        } catch (Exception e) {
            LOGGER.error("Can't write resulting differenceACSS CSS to intersected!", e);
        }

        CSSWriter cssDifferenceAWriter = new CSSWriter(CSS_VERSION);
        try {
            cssDifferenceAWriter.setHeaderText("");
            differenceA.append(cssDifferenceAWriter.getCSSAsString(differenceACSS));
        } catch (Exception e) {
            LOGGER.error("Can't write resulting differenceACSS CSS to differenceA!", e);
        }

        CSSWriter cssDifferenceBWriter = new CSSWriter(CSS_VERSION);
        try {
            cssDifferenceBWriter.setHeaderText("");
            differenceB.append(cssDifferenceBWriter.getCSSAsString(differenceBCSS));
        } catch (Exception e) {
            LOGGER.error("Can't write resulting differenceBCSS CSS to differenceB!", e);
        }

        result.put(IntersectDataType.INTERSECTED, intersected);
        result.put(IntersectDataType.DIFFERENCE_A, differenceA);
        result.put(IntersectDataType.DIFFERENCE_B, differenceB);
        return result;
    }

    /**
     * Compare and merge declaration form given rule. Declaration are spited to three types: intersected, differenceA, differenceB
     *
     * @param icssTopLevelRuleA rule A to compare and merge
     * @param icssTopLevelRuleB rule B to compare and merge
     * @return Map<IntersectDataType key, ICSSTopLevelRule rule> keys: intersected, differenceA, differenceB
     */
    public Map<IntersectDataType, ICSSTopLevelRule> cssDeclarationsMerge(ICSSTopLevelRule icssTopLevelRuleA, ICSSTopLevelRule icssTopLevelRuleB) {
        Map<IntersectDataType, ICSSTopLevelRule> result = new HashMap<IntersectDataType, ICSSTopLevelRule>();

        CSSStyleRule intersectedRule = new CSSStyleRule();
        CSSStyleRule differenceRuleA = new CSSStyleRule();
        CSSStyleRule differenceRuleB = new CSSStyleRule();

        ICSSWriterSettings settings = new CSSWriterSettings(CSS_VERSION);

        if (icssTopLevelRuleA instanceof CSSStyleRule && icssTopLevelRuleB instanceof CSSStyleRule) {
            CSSStyleRule styleRuleA = (CSSStyleRule) icssTopLevelRuleA;
            CSSStyleRule styleRuleB = (CSSStyleRule) icssTopLevelRuleB;

            // add selectors to new rules
            for (CSSSelector cssSelector : styleRuleA.getAllSelectors()) {
                intersectedRule.addSelector(cssSelector);
                differenceRuleA.addSelector(cssSelector);
                differenceRuleB.addSelector(cssSelector);
            }

            for (CSSDeclaration cssDeclarationA : styleRuleA.getAllDeclarations()) {
                boolean declarationAFoundInRuleB = false;

                // check if current declaration from rule A is in rule B
                for (CSSDeclaration cssDeclarationB : styleRuleB.getAllDeclarations()) {
                    if (cssDeclarationB.getAsCSSString(settings, 0).equals(cssDeclarationA.getAsCSSString(settings, 0))) {
                        declarationAFoundInRuleB = true;

                        // when declaration from rule A is in rule B, then put it to intersected
                        intersectedRule.addDeclaration(cssDeclarationA);
                        styleRuleA.removeDeclaration(cssDeclarationA);
                        styleRuleB.removeDeclaration(cssDeclarationB);

                        break;
                    }
                }

                // when declaration from rule A is not in rule B, then remove it only from rule A
                // declarations from rule B which ware not found, will be moved to differenceB
                if (!declarationAFoundInRuleB) {
                    differenceRuleA.addDeclaration(cssDeclarationA);
                }
            }

            // move others declarations from B to differenceB
            if (styleRuleB.getAllDeclarations().size() > 0) {
                for (CSSDeclaration cssDeclarationB : styleRuleB.getAllDeclarations()) {
                    differenceRuleB.addDeclaration(cssDeclarationB);
                }
            }
        }

        result.put(IntersectDataType.INTERSECTED, intersectedRule);
        result.put(IntersectDataType.DIFFERENCE_A, differenceRuleA);
        result.put(IntersectDataType.DIFFERENCE_B, differenceRuleB);
        return result;
    }

}
