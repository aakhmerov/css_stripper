package de.smava.css.intersector.comparators;

import com.phloc.css.ECSSVersion;
import com.phloc.css.ICSSWriterSettings;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.writer.CSSWriterSettings;
import de.smava.css.intersector.Intersector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class CSSSelectorsComparator implements Comparator<ICSSTopLevelRule> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Intersector.class);
    private static final ECSSVersion CSS_VERSION = ECSSVersion.CSS30;

    public int compare(ICSSTopLevelRule o1, ICSSTopLevelRule o2) {
        int result = 1;

        if (o1 instanceof CSSStyleRule && o2 instanceof CSSStyleRule) {
            CSSStyleRule styleRuleA = (CSSStyleRule) o1;
            CSSStyleRule styleRuleB = (CSSStyleRule) o2;

            ICSSWriterSettings settings = new CSSWriterSettings(CSS_VERSION);

            if ((styleRuleA.getAllSelectors().size() == styleRuleB.getAllSelectors().size()) &&
                    styleRuleA.getSelectorsAsCSSString(settings, 0).equals(styleRuleB.getSelectorsAsCSSString(settings, 0))) {
                result = 0;
            }
        } else {
            if (o1.equals(o2)) {
                result = 0;
            }
        }

        return result;
    }

}
