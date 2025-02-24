package edu.hm.hafner.analysis.registry;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.CheckStyleParser;
import edu.hm.hafner.analysis.parser.CheckStyleRules;
import edu.hm.hafner.analysis.util.Deferred;

/**
 * A descriptor for the CheckStyle warnings.
 *
 * @author Lorenz Munsch
 */
class CheckStyleDescriptor extends ParserDescriptor {
    private static final String ID = "checkstyle";
    private static final String NAME = "CheckStyle";

    private final Deferred<CheckStyleRules> messages = new Deferred<>(CheckStyleRules::new);

    CheckStyleDescriptor() {
        super(ID, NAME);
    }

    @Override
    public IssueParser create(final Option... options) {
        return new CheckStyleParser();
    }

    @Override
    public String getPattern() {
        return "**/checkstyle-result.xml";
    }

    @Override
    public String getUrl() {
        return "https://checkstyle.org";
    }

    @Override
    public String getIconUrl() {
        return "https://raw.githubusercontent.com/checkstyle/checkstyle/master/src/site/resources/images/checkstyle_logo_small_64.png";
    }

    @Override
    public String getDescription(final Issue issue) {
        return messages.get().getDescription(issue.getType());
    }
}
