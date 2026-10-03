package za.co.lab.service;

import za.co.lab.dao.AuditLogDAO;
import za.co.lab.model.AuditLog;
import za.co.lab.model.User;

public class AuditService {

    private static final AuditLogDAO dao = new AuditLogDAO();

    public static void log(User user,
                           String actionType,
                           String entityType,
                           Integer entityId,
                           String details) {

        AuditLog log = new AuditLog();

        if (user != null) {
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setRole(user.getRole());
        }

        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setActionDetails(details);

        dao.log(log);
    }
}