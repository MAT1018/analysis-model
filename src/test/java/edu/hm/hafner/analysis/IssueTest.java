package edu.hm.hafner.analysis;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.assertions.SoftAssertions;
import edu.hm.hafner.util.SerializableTest;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import nl.jqno.equalsverifier.EqualsVerifier;

import static edu.hm.hafner.analysis.assertions.Assertions.*;
import static java.util.Collections.*;

/**
 * Unit tests for {@link Issue}.
 *
 * @author Marcel Binder
 */
@SuppressFBWarnings("DMI")
class IssueTest extends SerializableTest<Issue> {
    private static final String SERIALIZATION_NAME = "issue.ser";
    private static final TreeStringBuilder TREE_STRING_BUILDER = new TreeStringBuilder();

    private static final String BASE_NAME = "file.txt";
    static final String FILE_NAME = "some/relative/path/to/" + BASE_NAME;
    static final TreeString FILE_NAME_TS = TREE_STRING_BUILDER.intern(FILE_NAME);
    static final String PATH_NAME = "/path/to/affected/files";

    static final int LINE_START = 1;
    static final int LINE_END = 2;
    static final int COLUMN_START = 3;
    static final int COLUMN_END = 4;
    static final String CATEGORY = "category";
    static final String TYPE = "type";
    static final String PACKAGE_NAME = "package-name";
    static final TreeString PACKAGE_NAME_TS = TREE_STRING_BUILDER.intern(PACKAGE_NAME);
    static final String MODULE_NAME = "module-name";
    static final Severity SEVERITY = Severity.WARNING_HIGH;
    static final String MESSAGE = "message";
    static final TreeString MESSAGE_TS = TREE_STRING_BUILDER.intern(MESSAGE);
    static final String DESCRIPTION = "description";
    static final String EMPTY = "";
    static final TreeString EMPTY_TS = TREE_STRING_BUILDER.intern(EMPTY);
    static final String UNDEFINED = "-";
    static final TreeString UNDEFINED_TS = TREE_STRING_BUILDER.intern(UNDEFINED);
    static final String FINGERPRINT = "fingerprint";
    static final String ORIGIN = "origin";
    static final String ORIGIN_NAME = "Origin";
    static final String REFERENCE = "reference";
    static final String ADDITIONAL_PROPERTIES = "additional";
    static final LineRangeList LINE_RANGES = new LineRangeList(singletonList(new LineRange(5, 6)));
    private static final String WINDOWS_PATH = "C:/Windows";

    @Test
    void shouldSplitFileNameElements() {
        Issue issue = new Issue(PATH_NAME, FILE_NAME_TS, 2, 1, 2, 1, LINE_RANGES, CATEGORY,
                TYPE, PACKAGE_NAME_TS, MODULE_NAME, SEVERITY,
                MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE, FINGERPRINT, ADDITIONAL_PROPERTIES,
                UUID.randomUUID());

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue)
                    .hasFileName(FILE_NAME)
                    .hasPath(PATH_NAME)
                    .hasAbsolutePath(PATH_NAME + "/" + FILE_NAME)
                    .hasBaseName(BASE_NAME);
        }

        TreeString newName = TreeString.valueOf("new.txt");
        issue.setFileName("/new", newName);
        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue)
                    .hasFileName("new.txt")
                    .hasPath("/new")
                    .hasAbsolutePath("/new/new.txt")
                    .hasBaseName("new.txt");
        }

        Issue other = new Issue(PATH_NAME, newName, 2, 1, 2, 1, LINE_RANGES, CATEGORY,
                TYPE, PACKAGE_NAME_TS, MODULE_NAME, SEVERITY,
                MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE, FINGERPRINT, ADDITIONAL_PROPERTIES,
                UUID.randomUUID());
        assertThat(issue).as("Equals should not consider pathName in computation").isEqualTo(other);

        Issue emptyPath = new Issue("", FILE_NAME_TS, 2, 1, 2, 1, LINE_RANGES, CATEGORY,
                TYPE, PACKAGE_NAME_TS, MODULE_NAME, SEVERITY,
                MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE, FINGERPRINT, ADDITIONAL_PROPERTIES,
                UUID.randomUUID());

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(emptyPath)
                    .hasFileName(FILE_NAME)
                    .hasPath(UNDEFINED)
                    .hasAbsolutePath(FILE_NAME)
                    .hasBaseName(BASE_NAME);
        }
    }

    @Test
    void shouldConvertWindowsNames() {
        Issue issue = new Issue("C:\\Windows", FILE_NAME_TS, 2, 1, 2, 1, LINE_RANGES, CATEGORY,
                TYPE, PACKAGE_NAME_TS, MODULE_NAME, SEVERITY,
                MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE, FINGERPRINT, ADDITIONAL_PROPERTIES,
                UUID.randomUUID());

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue)
                    .hasFileName(FILE_NAME)
                    .hasPath(WINDOWS_PATH)
                    .hasAbsolutePath(WINDOWS_PATH + "/" + FILE_NAME)
                    .hasBaseName(BASE_NAME);
        }

        issue.setFileName("c:\\Another\\Path", FILE_NAME_TS);
        assertThat(issue).hasPath("C:/Another/Path");
    }

    @Test
    void shouldExpandPath() {
        try (var builder = new IssueBuilder()
                .setPathName("/jenkins-data/jenkins/workspace/root/trunk/sw/build")
                .setFileName("../../component/app/_src/file.c")) {
            var issue = builder.build();
            assertThat(issue.getAbsolutePath()).isEqualTo(
                    "/jenkins-data/jenkins/workspace/root/trunk/component/app/_src/file.c");
        }
    }

    @Test
    void shouldEnsureThatEndIsGreaterOrEqualStart() {
        Issue issue = new Issue(PATH_NAME, FILE_NAME_TS, 2, 1, 2, 1, LINE_RANGES, CATEGORY,
                TYPE, PACKAGE_NAME_TS, MODULE_NAME, SEVERITY,
                MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE, FINGERPRINT, ADDITIONAL_PROPERTIES,
                UUID.randomUUID());
        assertThat(issue).hasLineStart(1).hasLineEnd(2);
        assertThat(issue).hasColumnStart(1).hasColumnEnd(2);

        assertThat(issue.affectsLine(0)).isTrue();
        assertThat(issue.affectsLine(1)).isTrue();
        assertThat(issue.affectsLine(2)).isTrue();
        assertThat(issue.affectsLine(3)).isFalse();
        assertThat(issue.affectsLine(4)).isFalse();
        assertThat(issue.affectsLine(5)).isTrue();
        assertThat(issue.affectsLine(6)).isTrue();
        assertThat(issue.affectsLine(7)).isFalse();
    }

    /**
     * Creates an issue with all properties set to a specific value. Verifies that each getter returns the correct
     * result.
     */
    @Test
    void shouldSetAllPropertiesInConstructor() {
        Issue issue = createFilledIssue();

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue.getId()).isNotNull();
            softly.assertThat(issue)
                    .hasFileName(FILE_NAME)
                    .hasCategory(CATEGORY)
                    .hasLineStart(LINE_START)
                    .hasLineEnd(LINE_END)
                    .hasColumnStart(COLUMN_START)
                    .hasColumnEnd(COLUMN_END)
                    .hasType(TYPE)
                    .hasPackageName(PACKAGE_NAME)
                    .hasModuleName(MODULE_NAME)
                    .hasSeverity(SEVERITY)
                    .hasMessage(MESSAGE)
                    .hasOrigin(ORIGIN)
                    .hasDescription(DESCRIPTION)
                    .hasFingerprint(FINGERPRINT);
            softly.assertThat(issue.hasFingerprint()).isTrue();
            softly.assertThat(issue.hasPackageName()).isTrue();
            softly.assertThat(issue.hasFileName()).isTrue();
            softly.assertThat(issue.hasModuleName()).isTrue();
        }

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(Issue.getPropertyValueAsString(issue, "fileName"))
                    .isEqualTo(issue.getFileName());
            softly.assertThat(Issue.getPropertyValueAsString(issue, "category"))
                    .isEqualTo(issue.getCategory());
            softly.assertThat(Issue.getPropertyValueAsString(issue, "lineStart"))
                    .isEqualTo(String.valueOf(issue.getLineStart()));
            softly.assertThat(Issue.getPropertyValueAsString(issue, "severity"))
                    .isEqualTo(issue.getSeverity().toString());
        }
    }

    @Test
    void shouldChangeMutableProperties() {
        Issue issue = createFilledIssue();

        String origin = "new-origin";
        issue.setOrigin(origin);
        String reference = "new-reference";
        issue.setReference(reference);
        String moduleName = "new-module";
        issue.setModuleName(moduleName);
        String packageName = "new-package";
        issue.setPackageName(TREE_STRING_BUILDER.intern(packageName));
        TreeString fileName = TREE_STRING_BUILDER.intern("new-file");
        String pathName = "new-path";
        issue.setFileName(pathName, fileName);
        String fingerprint = "new-fingerprint";
        issue.setFingerprint(fingerprint);

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue)
                    .hasOrigin(origin)
                    .hasReference(reference)
                    .hasModuleName(moduleName)
                    .hasPackageName(packageName)
                    .hasFileName(fileName.toString())
                    .hasPath(pathName)
                    .hasAbsolutePath(pathName + "/" + fileName)
                    .hasFingerprint(fingerprint);
        }

        String originName = "new-origin-name";
        issue.setOrigin(origin, originName);

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue)
                    .hasOrigin(origin)
                    .hasOriginName(originName)
                    .hasReference(reference)
                    .hasModuleName(moduleName)
                    .hasPackageName(packageName)
                    .hasFileName(fileName.toString())
                    .hasPath(pathName)
                    .hasAbsolutePath(pathName + "/" + fileName)
                    .hasFingerprint(fingerprint);
        }
    }

    @Test
    @SuppressWarnings("NullAway")
    void testDefaultIssueNullStringsNegativeIntegers() {
        Issue issue = new Issue(null, UNDEFINED_TS, 0, 0, 0, 0, LINE_RANGES, null, null,
                UNDEFINED_TS, null, SEVERITY, EMPTY_TS, EMPTY, null, ORIGIN_NAME, null, null,
                null, UUID.randomUUID());

        assertIsDefaultIssue(issue);
    }

    @Test
    void testDefaultIssueEmptyStringsNegativeIntegers() {
        Issue issue = new Issue(EMPTY, UNDEFINED_TS, -1, -1, -1, -1, LINE_RANGES, EMPTY, EMPTY,
                UNDEFINED_TS, EMPTY, SEVERITY, EMPTY_TS, EMPTY, EMPTY, ORIGIN_NAME, EMPTY, EMPTY,
                EMPTY, UUID.randomUUID());

        assertIsDefaultIssue(issue);
    }

    private void assertIsDefaultIssue(final Issue issue) {
        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue.getId()).isNotNull();
            softly.assertThat(issue)
                    .hasFileName(UNDEFINED)
                    .hasCategory(EMPTY)
                    .hasLineStart(0)
                    .hasLineEnd(0)
                    .hasColumnStart(0)
                    .hasColumnEnd(0)
                    .hasType(UNDEFINED)
                    .hasPackageName(UNDEFINED)
                    .hasMessage(EMPTY)
                    .hasDescription(EMPTY)
                    .hasFingerprint(UNDEFINED);
            softly.assertThat(issue.hasFingerprint()).isFalse();
            softly.assertThat(issue.hasPackageName()).isFalse();
            softly.assertThat(issue.hasFileName()).isFalse();
            softly.assertThat(issue.hasModuleName()).isFalse();
        }
    }

    @Test
    void testZeroLineColumnEndsDefaultToLineColumnStarts() {
        Issue issue = new Issue(PATH_NAME, FILE_NAME_TS, LINE_START, 0, COLUMN_START, 0, LINE_RANGES, CATEGORY, TYPE,
                PACKAGE_NAME_TS, MODULE_NAME, SEVERITY, MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE,
                FINGERPRINT,
                ADDITIONAL_PROPERTIES, UUID.randomUUID());

        try (SoftAssertions softly = new SoftAssertions()) {
            softly.assertThat(issue)
                    .hasLineStart(LINE_START)
                    .hasLineEnd(LINE_START)
                    .hasColumnStart(COLUMN_START)
                    .hasColumnEnd(COLUMN_START);
        }
    }

    @Test
    void testNullPriorityDefaultsToNormal() {
        Issue issue = new Issue(PATH_NAME, FILE_NAME_TS, LINE_START, LINE_END, COLUMN_START, COLUMN_END, LINE_RANGES,
                CATEGORY, TYPE,
                PACKAGE_NAME_TS, MODULE_NAME, null, MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE,
                FINGERPRINT,
                ADDITIONAL_PROPERTIES, UUID.randomUUID());

        assertThat(issue.getSeverity()).isEqualTo(Severity.WARNING_NORMAL);
    }

    @Test
    void testIdRandomlyGenerated() {
        Issue one = createFilledIssue();
        Issue another = createFilledIssue();

        assertThat(one.getId()).isNotEqualTo(another.getId());
    }

    /**
     * Creates an issue that contains valid properties.
     *
     * @return a correctly filled issue
     */
    protected Issue createFilledIssue() {
        return new Issue(PATH_NAME, FILE_NAME_TS, LINE_START, LINE_END, COLUMN_START, COLUMN_END, LINE_RANGES, CATEGORY,
                TYPE,
                PACKAGE_NAME_TS, MODULE_NAME, SEVERITY, MESSAGE_TS, DESCRIPTION, ORIGIN, ORIGIN_NAME, REFERENCE,
                FINGERPRINT,
                ADDITIONAL_PROPERTIES, UUID.randomUUID());
    }

    @Test
    void shouldObeyEqualsContract() {
        LineRangeList filled = new LineRangeList(15);
        filled.add(new LineRange(15));

        EqualsVerifier.simple()
                .withPrefabValues(TreeString.class,
                        TREE_STRING_BUILDER.intern("One"),
                        TREE_STRING_BUILDER.intern("Two"))
                .withPrefabValues(LineRangeList.class, new LineRangeList(10), filled)
                .forClass(Issue.class)
                .withIgnoredFields("id", "reference", "pathName", "fingerprint").verify();
    }

    @Override
    protected Issue createSerializable() {
        return createFilledIssue();
    }

    @Override
    protected void assertThatRestoredInstanceEqualsOriginalInstance(final Issue original, final Issue restored) {
        assertThat(original).isEqualTo(restored);
    }

    /**
     * Verifies that saved serialized format (from a previous release) still can be resolved with the current
     * implementation of {@link Issue}.
     */
    @Test
    void shouldReadIssueFromOldSerialization() {
        byte[] restored = readAllBytes(SERIALIZATION_NAME);

        assertThatSerializableCanBeRestoredFrom(restored);
    }

    static final class IssueWriter {
        private IssueWriter() {
            // prevents instantiation
        }

        /**
         * Serializes an {@link Issue} to a file. Use this method in case the issue properties have been changed and the
         * readResolve method has been adapted accordingly so that the old serialization still can be read.
         *
         * @param args
         *         not used
         *
         * @throws IOException
         *         if the file could not be written
         */
        public static void main(final String... args) throws IOException {
            new IssueTest().createSerializationFile();
        }
    }
}
