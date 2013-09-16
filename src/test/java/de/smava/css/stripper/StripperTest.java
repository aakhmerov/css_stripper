package de.smava.css.stripper;

import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriterSettings;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: aakhmerov
 * Date: 9/16/13
 * Time: 11:40 AM
 * Basic validation of stripper work
 */
public class StripperTest {
    private Stripper toTest = new Stripper();
    @Test
    public void testStrip() throws Exception {
        File file  = new File (ClassLoader.getSystemClassLoader().getResource("test.css").getFile());
        File analysis = new File (ClassLoader.getSystemClassLoader().getResource("test_analysis.txt").getFile());
        StringBuffer stripped = toTest.strip(FileUtils.readFileToString(file), FileUtils.readFileToString(analysis));
        String strippedString = stripped.toString();
        assertThat("stripped CSS is not empty",strippedString.isEmpty(),is(false));
        BufferedReader r = new BufferedReader(new FileReader(analysis));
        String line = r.readLine();

        while (line != null) {
            assertThat("Stripped css contains selector : [" + line + "]",strippedString.contains(line),is(false));
            line = r.readLine();
        }

//      ensure that all proper styles are still in the file
        File proper = new File (ClassLoader.getSystemClassLoader().getResource("test_result.css").getFile());
        CascadingStyleSheet aCSS = CSSReader.readFromString(FileUtils.readFileToString(proper), Charset.forName("UTF-8"), ECSSVersion.CSS30);
        for (ICSSTopLevelRule rule : aCSS.getAllRules()) {
            if (rule instanceof CSSStyleRule) {
                line = ((CSSStyleRule) rule).getSelectorsAsCSSString(new CSSWriterSettings(ECSSVersion.CSS30),0);
                assertThat("Stripped css contains selector : [" + line + "]",
                        strippedString.contains(line),
                        is(true)
                );
            }
        }
    }

    @Test
    public void testComposeRemovalCss() throws Exception {
        File analysis = new File (ClassLoader.getSystemClassLoader().getResource("test_analysis.txt").getFile());
        String data = FileUtils.readFileToString(analysis);
        CascadingStyleSheet wrapped = toTest.composeRemovalCss(data);
        assertThat(wrapped, is(notNullValue()));
        assertThat(wrapped.getAllRules().size(),is(13));
    }
}
