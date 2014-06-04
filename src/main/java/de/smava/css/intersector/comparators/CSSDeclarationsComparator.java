package de.smava.css.intersector.comparators;

import com.phloc.css.ECSSVersion;
import com.phloc.css.ICSSWriterSettings;
import com.phloc.css.decl.CSSDeclaration;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.writer.CSSWriterSettings;
import de.smava.css.intersector.Intersector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class CSSDeclarationsComparator implements Comparator<ICSSTopLevelRule> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Intersector.class);
    private static final ECSSVersion CSS_VERSION = ECSSVersion.CSS30;

    public int compare(ICSSTopLevelRule o1, ICSSTopLevelRule o2) {
        int result = 1;

        if (o1 instanceof CSSStyleRule && o2 instanceof CSSStyleRule) {
            CSSStyleRule styleRuleA = (CSSStyleRule) o1;
            CSSStyleRule styleRuleB = (CSSStyleRule) o2;

            ICSSWriterSettings settings = new CSSWriterSettings(CSS_VERSION);

            if (styleRuleA.getAllDeclarations().size() == styleRuleB.getAllDeclarations().size()) {
                if (styleRuleA.getAllDeclarations() != null && styleRuleA.getAllDeclarations().size() > 0) {
                    for (CSSDeclaration cssDeclarationA : styleRuleA.getAllDeclarations()) {
                        boolean declarationAFoundInRuleB = false;

                        for (CSSDeclaration cssDeclarationB : styleRuleB.getAllDeclarations()) {
                            if (cssDeclarationB.getAsCSSString(settings, 0).equals(cssDeclarationA.getAsCSSString(settings, 0))) {
                                declarationAFoundInRuleB = true;
                                break;
                            }
                        }

                        if (!declarationAFoundInRuleB) {
                            result = 1;
                            break;
                        }

                        result = 0;
                    }
                } else {
                    result = 0;
                }
            }
        } else {
            if (o1.equals(o2)) {
                result = 0;
            }
        }

        return result;
    }

}
