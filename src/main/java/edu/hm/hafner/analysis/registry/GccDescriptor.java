package edu.hm.hafner.analysis.registry;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.GccParser;

/**
 * A descriptor for GNU C Compiler 3 (gcc).
 *
 * @author Lorenz Munsch
 */
class GccDescriptor extends ParserDescriptor {
    private static final String ID = "gcc3";
    private static final String NAME = "GNU C Compiler 3 (gcc)";

    GccDescriptor() {
        super(ID, NAME);
    }

    @Override
    public IssueParser create(final Option... options) {
        return new GccParser();
    }
}
