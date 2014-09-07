/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.util.logging.osi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_INCLUDED_RESOURCE;
import static hm.binkley.util.logging.osi.SupportLoggers.AUDIT;
import static java.nio.file.Files.lines;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

/**
 * {@code ITSupportLoggers} integration tests {@link SupportLoggers}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley</a>
 */
public final class ITSplunkLoggers {
    @Rule
    public final StandardOutputStreamLog sout = new StandardOutputStreamLog();
    @Rule
    public final StandardErrorStreamLog serr = new StandardErrorStreamLog();
    @Rule
    public final ProvideSystemProperty osi = new ProvideSystemProperty(
            LOGBACK_CONFIGURATION_FILE.key(), "osi-logback.xml");
    @Rule
    public final ProvideSystemProperty included = new ProvideSystemProperty(
            LOGBACK_INCLUDED_RESOURCE.key(), "osi-splunk-included.xml");
    @Rule
    public final TemporaryFolder logDir = new TemporaryFolder();
    @Rule
    public final RestoreSystemProperties auditLog = new RestoreSystemProperties("logback.splunk");

    @Before
    public void setUp() {
        refreshLogback();
    }

    @Test
    public void shouldOverrideAuditDefinition()
            throws IOException {
        final File auditLog = logDir.newFile();
        System.setProperty("logback.splunk", auditLog.getAbsolutePath());

        refreshLogback();
        AUDIT.getLogger("test").warn("Foo!");

        assertThat(sout.getLog(), isEmptyString());
        assertThat(serr.getLog(), isEmptyString());
        lines(auditLog.toPath()).
                forEach(line -> assertThat(line, containsString("AUDIT/WARN")));
        assertThat(auditLog.length(), is(greaterThan(0L)));
    }
}
