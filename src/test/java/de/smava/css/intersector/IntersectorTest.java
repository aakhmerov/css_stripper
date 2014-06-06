package de.smava.css.intersector;

import com.phloc.css.ECSSVersion;
import com.phloc.css.ICSSWriterSettings;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriterSettings;

import de.smava.css.intersector.comparators.CSSSelectorsComparator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 
 * @author Daniel Suszczynski
 *
 */
public class IntersectorTest {

	private Intersector intersectorToTest = new Intersector();
    
    private static final String CHARSET = "UTF-8";
    private static final ECSSVersion CSS_VERSION = ECSSVersion.CSS30;
    
    @Test
    public void testIntersector() throws Exception {
        File toIntersectA  = new File(ClassLoader.getSystemClassLoader().getResource("to_intersect_a.css").getFile());
        File toIntersectB  = new File(ClassLoader.getSystemClassLoader().getResource("to_intersect_b.css").getFile());
        
        ICSSWriterSettings settings = new CSSWriterSettings(CSS_VERSION);
        
        Map<IntersectDataType, StringBuffer> intersectedData = intersectorToTest.intersect(FileUtils.readFileToString(toIntersectA), FileUtils.readFileToString(toIntersectB));

        String intersectedCSSString = intersectedData.get(IntersectDataType.INTERSECTED).toString();
        String differenceACSSString = intersectedData.get(IntersectDataType.DIFFERENCE_A).toString();
        String differenceBCSSString = intersectedData.get(IntersectDataType.DIFFERENCE_B).toString();
        
        assertThat("intersected CSS is not empty", intersectedCSSString.isEmpty(), is(false));
        assertThat("differenceA CSS is not empty", differenceACSSString.isEmpty(), is(false));
        assertThat("differenceB CSS is not empty", differenceBCSSString.isEmpty(), is(false));
        
        CascadingStyleSheet cssToIntersectA = CSSReader.readFromString(FileUtils.readFileToString(toIntersectA), Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssToIntersectB = CSSReader.readFromString(FileUtils.readFileToString(toIntersectB), Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssIntersected = CSSReader.readFromString(intersectedCSSString, Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssDifferenceA = CSSReader.readFromString(differenceACSSString, Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssDifferenceB = CSSReader.readFromString(differenceBCSSString, Charset.forName(CHARSET), CSS_VERSION);
        
        // check if at least one CSS style sheet generated as a result of intersector contains rules selectors from CSS A
        for (ICSSTopLevelRule cssToIntersectLevelRuleA : cssToIntersectA.getAllRules()) {
        	assertThat("At least one CSS style sheet generated as a result of intersector contains rules selectors from CSS A : [" + ((CSSStyleRule) cssToIntersectLevelRuleA).getSelectorsAsCSSString(settings, 0) + "]",
        			this.isIntersectedDataContainsRuleSelectors(cssIntersected, cssDifferenceA, cssDifferenceB, cssToIntersectLevelRuleA),
        			is(true));
		}
        
        // check if at least one CSS style sheet generated as a result of intersector contains rules selectors from CSS B
        for (ICSSTopLevelRule cssToIntersectLevelRuleB : cssToIntersectB.getAllRules()) {
        	assertThat("At least one CSS style sheet generated as a result of intersector contains rules selectors from CSS B : [" + ((CSSStyleRule) cssToIntersectLevelRuleB).getSelectorsAsCSSString(settings, 0) + "]",
        			this.isIntersectedDataContainsRuleSelectors(cssIntersected, cssDifferenceA, cssDifferenceB, cssToIntersectLevelRuleB),
        			is(true));
		}
    }
    
    @Test
    public void testCSSDeclarationsMerge() {
    	ICSSWriterSettings settings = new CSSWriterSettings(CSS_VERSION);
    	
    	CascadingStyleSheet cssRuleToMergeA = CSSReader.readFromString(".result .questions {"
    			+ "color:#333;"
    			+ "font-size:16px;"
    			+ "font-weight:bold;"
    			+ "margin-bottom:10px;"
    			+ "}", Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssRuleToMergeB = CSSReader.readFromString(".result .questions {"
    			+ "color:#333;"
    			+ "font-size:18px;"
    			+ "font-weight:bold;"
    			+ "margin-bottom:10px;"
    			+ "}", Charset.forName(CHARSET), CSS_VERSION);
    	
    	Map<IntersectDataType, ICSSTopLevelRule> cssDeclarationsMerge = intersectorToTest.cssDeclarationsMerge(cssRuleToMergeA.getRuleAtIndex(0), cssRuleToMergeB.getRuleAtIndex(0));
    	assertThat("",
    			((CSSStyleRule) (cssDeclarationsMerge.get(IntersectDataType.INTERSECTED))).getAllDeclarations().size() == 3,
    			is(true));
    	assertThat("",
    			((CSSStyleRule) (cssDeclarationsMerge.get(IntersectDataType.DIFFERENCE_A))).getAllDeclarations().size() == 1,
    			is(true));
    	assertThat("",
    			((CSSStyleRule) (cssDeclarationsMerge.get(IntersectDataType.DIFFERENCE_B))).getAllDeclarations().size() == 1,
    			is(true));
    	assertThat("",
    			cssDeclarationsMerge.get(IntersectDataType.DIFFERENCE_A).getAsCSSString(settings, 0).contains("font-size:16px;"),
    			is(true));
    	assertThat("",
    			cssDeclarationsMerge.get(IntersectDataType.DIFFERENCE_B).getAsCSSString(settings, 0).contains("font-size:18px;"),
    			is(true));
    }

    private boolean isIntersectedDataContainsRuleSelectors(CascadingStyleSheet cssIntersected, CascadingStyleSheet cssDifferenceA, CascadingStyleSheet cssDifferenceB, ICSSTopLevelRule topLevelRule) {
    	CSSSelectorsComparator cssSelectorComparator = new CSSSelectorsComparator();
    	
    	for (ICSSTopLevelRule intersectedTopLevelRule : cssIntersected.getAllRules()) {
    		if(cssSelectorComparator.compare(topLevelRule, intersectedTopLevelRule) == 0) {
    			return true;
    		}
    	}
    	
    	for (ICSSTopLevelRule differenceATopLevelRule : cssDifferenceA.getAllRules()) {
    		if(cssSelectorComparator.compare(topLevelRule, differenceATopLevelRule) == 0) {
    			return true;
    		}
    	}
    	
    	for (ICSSTopLevelRule differenceBTopLevelRule : cssDifferenceB.getAllRules()) {
    		if(cssSelectorComparator.compare(topLevelRule, differenceBTopLevelRule) == 0) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
}
