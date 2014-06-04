package de.smava.css.intersector;

import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriterSettings;

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
        
        Map<IntersectDataType, StringBuffer> intersectedData = intersectorToTest.intersect(FileUtils.readFileToString(toIntersectA), FileUtils.readFileToString(toIntersectB));

        String intersectedCSSString = intersectedData.get(IntersectDataType.INTERSECTED).toString();
        String differenceACSSString = intersectedData.get(IntersectDataType.DIFFERENCE_A).toString();
        String differenceBCSSString = intersectedData.get(IntersectDataType.DIFFERENCE_B).toString();
        
        assertThat("intersected CSS is not empty", intersectedCSSString.isEmpty(),is(false));
        assertThat("differenceA CSS is not empty", differenceACSSString.isEmpty(),is(false));
        assertThat("differenceB CSS is not empty", differenceBCSSString.isEmpty(),is(false));
        
        CascadingStyleSheet cssToIntersectA = CSSReader.readFromString(FileUtils.readFileToString(toIntersectA), Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssToIntersectB = CSSReader.readFromString(FileUtils.readFileToString(toIntersectB), Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssIntersected = CSSReader.readFromString(intersectedCSSString, Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssDifferenceA = CSSReader.readFromString(differenceACSSString, Charset.forName(CHARSET), CSS_VERSION);
        CascadingStyleSheet cssDifferenceB = CSSReader.readFromString(differenceBCSSString, Charset.forName(CHARSET), CSS_VERSION);
        
//      ensure that all proper styles are still in the file
        /*File proper = new File (ClassLoader.getSystemClassLoader().getResource("test_result.css").getFile());
        CascadingStyleSheet aCSS = CSSReader.readFromString(FileUtils.readFileToString(proper), Charset.forName("UTF-8"), ECSSVersion.CSS30);
        for (ICSSTopLevelRule rule : aCSS.getAllRules()) {
            if (rule instanceof CSSStyleRule) {
                line = ((CSSStyleRule) rule).getSelectorsAsCSSString(new CSSWriterSettings(ECSSVersion.CSS30),0);
                assertThat("Stripped css contains selector : [" + line + "]",
                        strippedString.contains(line),
                        is(true)
                );
            }
        }*/
    }

}
