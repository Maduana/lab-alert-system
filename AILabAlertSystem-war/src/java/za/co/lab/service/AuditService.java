package za.co.lab.service;

import za.co.lab.dao.AuditLogDAO;
import za.co.lab.model.AuditLog;
import za.co.lab.model.User;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central service for recording application audit events.
 *
 * The service supports:
 *
 * 1. Existing five-parameter calls that provide a User object.
 * 2. Six-parameter calls that may provide an explicit user ID.
 *
 * @author USER
 */
public final class AuditService {

    private static final Logger LOGGER =
            Logger.getLogger(
                    AuditService.class.getName()
            );

    private static final AuditLogDAO DAO =
            new AuditLogDAO();

    /**
     * Prevents construction of this utility class.
     */
    private AuditService() {
    }

    /**
     * Backward-compatible audit method for application components
     * that do not supply a separate user identifier.
     *
     * This method preserves all existing calls such as:
     *
     * AuditService.log(
     *     user,
     *     "ALERT_ACKNOWLEDGED",
     *     "ALERT",
     *     alertId,
     *     "Alert acknowledged by Pathologist"
     * );
     *
     * @param user authenticated user, or null for a system event
     * @param action action performed
     * @param entityType affected entity type
     * @param entityId affected entity identifier
     * @param details event details
     */
    public static void log(
            User user,
            String action,
            String entityType,
            Integer entityId,
            String details) {

        log(
                user,
                null,
                action,
                entityType,
                entityId,
                details
        );
    }

    /**
     * Records an audit event.
     *
     * When a User object is available, the user ID, username, and
     * role are obtained from that object.
     *
     * When the User object is null but an explicit user ID is
     * supplied, the explicit ID is stored. This is useful for
     * background or service operations where the complete User
     * object is unavailable.
     *
     * @param user authenticated user, or null
     * @param explicitUserId optional explicit user identifier
     * @param action action performed
     * @param entityType affected entity type
     * @param entityId affected entity identifier
     * @param details event details
     */
    public static void log(
            User user,
            Integer explicitUserId,
            String action,
            String entityType,
            Integer entityId,
            String details) {

        if (isBlank(action)) {

            LOGGER.log(
                    Level.WARNING,
                    "Audit event rejected because the action "
                    + "was missing."
            );

            return;
        }

        AuditLog auditLog =
                new AuditLog();

        /*
         * Prefer the complete authenticated User object because it
         * supplies the user ID, username, and role.
         */
        if (user != null) {

            auditLog.setUserId(
                    user.getId()
            );

            auditLog.setUsername(
                    user.getUsername()
            );

            auditLog.setRole(
                    user.getRole()
            );

        } else if (explicitUserId != null
                && explicitUserId > 0) {

            /*
             * Use the explicit identifier only when the complete
             * User object is unavailable.
             */
            auditLog.setUserId(
                    explicitUserId
            );
        }

        auditLog.setActionType(
                normaliseRequiredText(
                        action
                )
        );

        auditLog.setEntityType(
                normaliseOptionalText(
                        entityType
                )
        );

        auditLog.setEntityId(
                entityId
        );

        auditLog.setActionDetails(
                normaliseOptionalText(
                        details
                )
        );

        try {
            DAO.log(
                    auditLog
            );

        } catch (Exception exception) {

            /*
             * Audit failure is logged to the server, but it is not
             * rethrown because audit logging should not normally
             * interrupt the user's primary operation.
             */
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to persist audit event "
                    + action
                    + " for entity type "
                    + entityType
                    + " and entity ID "
                    + entityId,
                    exception
            );
        }
    }

    /**
     * Returns trimmed required text.
     *
     * @param value source text
     * @return trimmed value
     */
    private static String normaliseRequiredText(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    /**
     * Returns trimmed optional text.
     *
     * @param value source text
     * @return trimmed value or null
     */
    private static String normaliseOptionalText(
            String value) {

        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    /**
     * Checks whether text is null or blank.
     *
     * @param value text value
     * @return true when missing
     */
    private static boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}