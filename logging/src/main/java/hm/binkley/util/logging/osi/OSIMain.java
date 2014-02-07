/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import ch.qos.logback.core.joran.spi.JoranException;

import java.net.MalformedURLException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@code OSIMain} demonstrates OSI logging.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class OSIMain {
    public static void main(final String... args)
            throws JoranException, MalformedURLException {
        // This is the only configuration needed:
        System.setProperty("logback.configurationFile", "osi-logback.xml");

        getLogger("example").error("Hi, mom!");

        System.setProperty("logback.debug", "true");
        OSI.reload();

        getLogger("example").error("Hi, mom!");
    }
}