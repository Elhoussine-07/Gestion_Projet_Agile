package com.Agile.demo.aspect.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect de logging spécifique pour TaskService
 * Gère la journalisation des opérations de gestion des tâches
 */
@Aspect
@Component
@Slf4j
public class TaskServiceLoggingAspect {

    /**
     * Pointcut pour toutes les méthodes publiques de TaskService
     */
    @Pointcut("execution(public * com.Agile.demo.execution.services.TaskService.*(..))")
    public void taskServiceMethods() {}

    /**
     * Pointcut pour les méthodes de création de tâche
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.TaskService.create*(..))")
    public void createTaskMethods() {}

    /**
     * Pointcut pour les méthodes de mise à jour de tâche
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.TaskService.update*(..))")
    public void updateTaskMethods() {}

    /**
     * Pointcut pour les méthodes de suppression de tâche
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.TaskService.delete*(..))")
    public void deleteTaskMethods() {}

    /**
     * Pointcut pour les méthodes de lecture de tâche
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.TaskService.get*(..))")
    public void getTaskMethods() {}

    /**
     * Pointcut pour les méthodes de workflow de tâche
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.TaskService.startTask(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.completeTask*(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.moveTask*(..))")
    public void taskWorkflowMethods() {}

    /**
     * Pointcut pour les opérations de blocage/déblocage
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.TaskService.blockTask(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.unblockTask(..))")
    public void taskBlockingMethods() {}

    /**
     * Log avant l'exécution de toute méthode de TaskService
     */
    @Before("taskServiceMethods()")
    public void logBeforeTaskMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("[TASK-SERVICE] ==> Appel de {} avec paramètres: {}",
                methodName, Arrays.toString(args));
    }

    /**
     * Log après l'exécution réussie d'une méthode de TaskService
     */
    @AfterReturning(pointcut = "taskServiceMethods()", returning = "result")
    public void logAfterReturningTaskMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[TASK-SERVICE] <== Retour de {} avec résultat: {}",
                methodName, result);
    }

    /**
     * Log en cas d'exception dans TaskService
     */
    @AfterThrowing(pointcut = "taskServiceMethods()", throwing = "exception")
    public void logAfterThrowingTaskMethod(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.error("[TASK-SERVICE] <!> Exception dans {} avec paramètres: {}",
                methodName, Arrays.toString(args));
        log.error("[TASK-SERVICE] <!> Erreur: {} - {}",
                exception.getClass().getSimpleName(), exception.getMessage());
    }

    /**
     * Log détaillé pour la création de tâches
     */
    @Around("createTaskMethods()")
    public Object logAroundCreateTask(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[TASK-SERVICE] [CRÉATION] 📝 Tentative de création de tâche");
        log.debug("[TASK-SERVICE] [CRÉATION] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.info("[TASK-SERVICE] [CRÉATION] ✓ Tâche créée avec succès");
            log.debug("[TASK-SERVICE] [CRÉATION] Résultat: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [CRÉATION] ✗ Échec de la création de la tâche - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log détaillé pour la mise à jour de tâches
     */
    @Around("updateTaskMethods()")
    public Object logAroundUpdateTask(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[TASK-SERVICE] [MISE À JOUR] 🔄 Tentative de mise à jour de tâche");
        log.debug("[TASK-SERVICE] [MISE À JOUR] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.info("[TASK-SERVICE] [MISE À JOUR] ✓ Tâche mise à jour avec succès");
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [MISE À JOUR] ✗ Échec de la mise à jour - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log détaillé pour la suppression de tâches
     */
    @Around("deleteTaskMethods()")
    public Object logAroundDeleteTask(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.warn("[TASK-SERVICE] [SUPPRESSION] 🗑️ Tentative de suppression de tâche");
        log.debug("[TASK-SERVICE] [SUPPRESSION] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.warn("[TASK-SERVICE] [SUPPRESSION] ✓ Tâche supprimée avec succès");
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [SUPPRESSION] ✗ Échec de la suppression - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log optimisé pour les opérations de lecture
     */
    @Around("getTaskMethods()")
    public Object logAroundGetTask(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[TASK-SERVICE] [LECTURE] 📖 Récupération de données: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[TASK-SERVICE] [LECTURE] ✓ Données récupérées avec succès");
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [LECTURE] ✗ Échec de la récupération - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log spécifique pour les opérations de workflow
     */
    @Around("taskWorkflowMethods()")
    public Object logAroundTaskWorkflow(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String emoji = switch (methodName) {
            case "startTask" -> "▶️";
            case "completeTask", "completeTaskWorkflow" -> "✅";
            case "moveTaskToReview", "moveToReview" -> "👀";
            case "moveTaskToTesting", "moveToTesting" -> "🧪";
            default -> "🔄";
        };

        log.info("[TASK-SERVICE] [WORKFLOW] {} Exécution de {} pour tâche ID: {}",
                emoji, methodName, args.length > 0 ? args[0] : "N/A");

        try {
            Object result = joinPoint.proceed();
            log.info("[TASK-SERVICE] [WORKFLOW] ✓ Opération {} réussie", methodName);
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [WORKFLOW] ✗ Échec de {} - Erreur: {}",
                    methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Log pour les opérations de blocage/déblocage
     */
    @Around("taskBlockingMethods()")
    public Object logAroundTaskBlocking(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        boolean isBlock = methodName.equals("blockTask");
        String operation = isBlock ? "BLOCAGE" : "DÉBLOCAGE";
        String emoji = isBlock ? "🚫" : "🔓";

        log.warn("[TASK-SERVICE] [{}] {} Tâche ID: {}", operation, emoji,
                args.length > 0 ? args[0] : "N/A");

        if (isBlock && args.length > 1) {
            log.warn("[TASK-SERVICE] [{}] Raison: {}", operation, args[1]);
        }

        try {
            Object result = joinPoint.proceed();
            log.warn("[TASK-SERVICE] [{}] ✓ Opération réussie", operation);
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [{}] ✗ Échec - Erreur: {}", operation, e.getMessage());
            throw e;
        }
    }

    /**
     * Log pour l'assignation/désassignation de tâches
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.assignTask(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.unassignTask(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.reassignTask(..))")
    public Object logAroundTaskAssignment(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String emoji = switch (methodName) {
            case "assignTask" -> "👤";
            case "unassignTask" -> "👻";
            case "reassignTask" -> "🔄";
            default -> "👥";
        };

        log.info("[TASK-SERVICE] [ASSIGNATION] {} Opération: {}", emoji, methodName);
        log.debug("[TASK-SERVICE] [ASSIGNATION] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.info("[TASK-SERVICE] [ASSIGNATION] ✓ Opération réussie");
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [ASSIGNATION] ✗ Échec - Erreur: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Log pour l'enregistrement des heures
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.logHours(..))")
    public Object logAroundLogHours(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        log.info("[TASK-SERVICE] [HEURES] ⏱️ Enregistrement d'heures - Tâche ID: {}, Heures: {}",
                args.length > 0 ? args[0] : "N/A",
                args.length > 1 ? args[1] : "N/A");

        try {
            Object result = joinPoint.proceed();
            log.info("[TASK-SERVICE] [HEURES] ✓ Heures enregistrées avec succès");
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [HEURES] ✗ Échec de l'enregistrement - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log pour les métriques et statistiques
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.*Metrics(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.*Statistics(..))")
    public Object logAroundTaskMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[TASK-SERVICE] [METRICS] 📊 Calcul des métriques: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[TASK-SERVICE] [METRICS] ✓ Métriques calculées");
            return result;
        } catch (Exception e) {
            log.error("[TASK-SERVICE] [METRICS] ✗ Échec du calcul - Erreur: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Log final après chaque exécution
     */
    @After("taskServiceMethods()")
    public void logAfterTaskMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.trace("[TASK-SERVICE] --- Fin de {}", methodName);
    }
}