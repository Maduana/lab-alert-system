package za.co.lab.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides database connections for the deployed application and
 * for automated tests.
 *
 * The deployed application uses the GlassFish JNDI datasource.
 * Automated tests use direct JDBC when test mode is enabled.
 *
 * @author USER
 */
public final class DBUtil {

    /**
     * GlassFish JDBC resource used by the deployed application.
     */
    private static final String JNDI_NAME =
            "jdbc/lab_alert_db";

    /**
     * System property that enables direct JDBC test mode.
     */
    private static final String TEST_MODE_PROPERTY =
            "labalert.testMode";

    /**
     * Test-database configuration system properties.
     */
    private static final String TEST_DB_URL_PROPERTY =
            "labalert.testDbUrl";

    private static final String TEST_DB_USER_PROPERTY =
            "labalert.testDbUser";

    private static final String TEST_DB_PASSWORD_PROPERTY =
            "labalert.testDbPassword";

    /**
     * Safe development defaults.
     *
     * The password has no default and must be supplied through a
     * system property or environment variable.
     */
    private static final String DEFAULT_TEST_DB_URL =
            "jdbc:mysql://localhost:3306/lab_alert_db"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Africa/Johannesburg";

    private static final String DEFAULT_TEST_DB_USER =
            "root";

    /**
     * Prevents utility-class construction.
     */
    private DBUtil() {
    }

    /**
     * Returns a database connection.
     *
     * The deployed application uses the GlassFish JNDI datasource.
     * JUnit tests running outside GlassFish use direct JDBC when
     * labalert.testMode is true.
     *
     * @return database connection
     * @throws SQLException when a connection cannot be obtained
     */
    public static Connection getConnection()
            throws SQLException {

        if (isTestMode()) {
            return getTestConnection();
        }

        return getJndiConnection();
    }

    /**
     * Checks whether direct JDBC test mode is enabled.
     *
     * Test mode may be enabled using:
     *
     * -Dlabalert.testMode=true
     *
     * or:
     *
     * LABALERT_TEST_MODE=true
     *
     * @return true when test mode is enabled
     */
    private static boolean isTestMode() {

        String configuredValue =
                System.getProperty(
                        TEST_MODE_PROPERTY
                );

        if (configuredValue == null
                || configuredValue.isBlank()) {

            configuredValue =
                    System.getenv(
                            "LABALERT_TEST_MODE"
                    );
        }

        return Boolean.parseBoolean(
                configuredValue
        );
    }

    /**
     * Obtains the normal application connection through the
     * GlassFish JDBC resource.
     *
     * @return JNDI database connection
     * @throws SQLException when JNDI lookup or connection fails
     */
    private static Connection getJndiConnection()
            throws SQLException {

        try {
            Context context =
                    new InitialContext();

            Object resource =
                    context.lookup(
                            JNDI_NAME
                    );

            if (!(resource instanceof DataSource)) {

                throw new SQLException(
                        "The JNDI resource "
                        + JNDI_NAME
                        + " is not a DataSource."
                );
            }

            DataSource dataSource =
                    (DataSource) resource;

            return dataSource.getConnection();

        } catch (NamingException exception) {

            throw new SQLException(
                    "Could not obtain database connection "
                    + "from JNDI resource "
                    + JNDI_NAME
                    + ".",
                    exception
            );
        }
    }

    /**
     * Creates a direct JDBC connection for automated tests.
     *
     * The test runner executes outside GlassFish and cannot access
     * the application server's JNDI datasource.
     *
     * @return direct JDBC connection
     * @throws SQLException when the test database cannot be reached
     */
    private static Connection getTestConnection()
            throws SQLException {

        String databaseUrl =
                getConfigurationValue(
                        TEST_DB_URL_PROPERTY,
                        "LABALERT_TEST_DB_URL",
                        DEFAULT_TEST_DB_URL
                );

        String databaseUser =
                getConfigurationValue(
                        TEST_DB_USER_PROPERTY,
                        "LABALERT_TEST_DB_USER",
                        DEFAULT_TEST_DB_USER
                );

        String databasePassword =
                getConfigurationValue(
                        TEST_DB_PASSWORD_PROPERTY,
                        "LABALERT_TEST_DB_PASSWORD",
                        ""
                );

        if (databaseUrl.isBlank()) {

            throw new SQLException(
                    "The test database URL is not configured."
            );
        }

        if (databaseUser.isBlank()) {

            throw new SQLException(
                    "The test database username is not configured."
            );
        }

        try {
            return DriverManager.getConnection(
                    databaseUrl,
                    databaseUser,
                    databasePassword
            );

        } catch (SQLException exception) {

            throw new SQLException(
                    "Could not obtain a direct JDBC connection "
                    + "to the test database. Verify the database URL, "
                    + "username, password and MySQL Connector/J library.",
                    exception
            );
        }
    }

    /**
     * Reads configuration from a Java system property first,
     * then from an environment variable, and finally uses the
     * supplied default.
     *
     * @param systemProperty system-property name
     * @param environmentVariable environment-variable name
     * @param defaultValue fallback value
     * @return configured value
     */
    private static String getConfigurationValue(
            String systemProperty,
            String environmentVariable,
            String defaultValue) {

        String value =
                System.getProperty(
                        systemProperty
                );

        if (value == null || value.isBlank()) {

            value =
                    System.getenv(
                            environmentVariable
                    );
        }

        if (value == null) {
            value = defaultValue;
        }

        return value.trim();
    }
}