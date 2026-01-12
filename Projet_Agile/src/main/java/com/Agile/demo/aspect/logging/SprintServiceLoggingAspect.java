package com.Agile.demo.aspect.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
@Slf4j
public class SprintServiceLoggingAspect {

    /**
     * Pointcut pour toutes les méthodes publiques de SprintService
     */
    @Pointcut("execution(public * com.Agile.demo.execution.services.SprintService.*(..))")
    public void sprintServiceMethods() {}

    /**
     * Pointcut pour les méthodes de création de sprint
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.SprintService.create*(..))")
    public void createSprintMethods() {}

    /**
     * Pointcut pour les méthodes de mise à jour de sprint
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.SprintService.update*(..))")
    public void updateSprintMethods() {}

    /**
     * Pointcut pour les méthodes de suppression de sprint
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.SprintService.delete*(..))")
    public void deleteSprintMethods() {}

    /**
     * Pointcut pour les méthodes de lecture de sprint
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.SprintService.get*(..))")
    public void getSprintMethods() {}

    /**
     * Pointcut pour les méthodes de workflow de sprint (start, complete, cancel)
     */
    @Pointcut("execution(* com.Agile.demo.execution.services.SprintService.startSprint(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.completeSprint(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.cancelSprint(..))")
    public void sprintWorkflowMethods() {}

    /**
     * Log avant l'exécution de toute méthode de SprintService
     */
    @Before("sprintServiceMethods()")
    public void logBeforeSprintMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("[SPRINT-SERVICE] ==> Appel de {} avec paramètres: {}",
                methodName, Arrays.toString(args));
    }

    /**
     * Log après l'exécution réussie d'une méthode de SprintService
     */
    @AfterReturning(pointcut = "sprintServiceMethods()", returning = "result")
    public void logAfterReturningSprintMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[SPRINT-SERVICE] <== Retour de {} avec résultat: {}",
                methodName, result);
    }

    /**
     * Log en cas d'exception dans SprintService
     */
    @AfterThrowing(pointcut = "sprintServiceMethods()", throwing = "exception")
    public void logAfterThrowingSprintMethod(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.error("[SPRINT-SERVICE] <!> Exception dans {} avec paramètres: {}",
                methodName, Arrays.toString(args));
        log.error("[SPRINT-SERVICE] <!> Erreur: {} - {}",
                exception.getClass().getSimpleName(), exception.getMessage());
    }

    /**
     * Log détaillé pour la création de sprints
     */
    @Around("createSprintMethods()")
    public Object logAroundCreateSprint(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[SPRINT-SERVICE] [CRÉATION] 📅 Tentative de création de sprint");
        log.debug("[SPRINT-SERVICE] [CRÉATION] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.info("[SPRINT-SERVICE] [CRÉATION] ✓ Sprint créé avec succès");
            log.debug("[SPRINT-SERVICE] [CRÉATION] Résultat: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [CRÉATION] ✗ Échec de la création du sprint - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log détaillé pour la mise à jour de sprints
     */
    @Around("updateSprintMethods()")
    public Object logAroundUpdateSprint(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[SPRINT-SERVICE] [MISE À JOUR] 🔄 Tentative de mise à jour du sprint");
        log.debug("[SPRINT-SERVICE] [MISE À JOUR] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.info("[SPRINT-SERVICE] [MISE À JOUR] ✓ Sprint mis à jour avec succès");
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [MISE À JOUR] ✗ Échec de la mise à jour - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log détaillé pour la suppression de sprints
     */
    @Around("deleteSprintMethods()")
    public Object logAroundDeleteSprint(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.warn("[SPRINT-SERVICE] [SUPPRESSION] 🗑️ Tentative de suppression de sprint");
        log.debug("[SPRINT-SERVICE] [SUPPRESSION] Paramètres: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.warn("[SPRINT-SERVICE] [SUPPRESSION] ✓ Sprint supprimé avec succès");
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [SUPPRESSION] ✗ Échec de la suppression - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log optimisé pour les opérations de lecture
     */
    @Around("getSprintMethods()")
    public Object logAroundGetSprint(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[SPRINT-SERVICE] [LECTURE] 📖 Récupération de données: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[SPRINT-SERVICE] [LECTURE] ✓ Données récupérées avec succès");
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [LECTURE] ✗ Échec de la récupération - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Log spécifique pour les opérations de workflow (start, complete, cancel)
     */
    @Around("sprintWorkflowMethods()")
    public Object logAroundSprintWorkflow(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String emoji = switch (methodName) {
            case "startSprint" -> "▶️";
            case "completeSprint" -> "✅";
            case "cancelSprint" -> "❌";
            default -> "🔄";
        };

        log.info("[SPRINT-SERVICE] [WORKFLOW] {} Exécution de {} pour sprint ID: {}",
                emoji, methodName, args.length > 0 ? args[0] : "N/A");

        try {
            Object result = joinPoint.proceed();
            log.info("[SPRINT-SERVICE] [WORKFLOW] ✓ Opération {} réussie", methodName);
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [WORKFLOW] ✗ Échec de {} - Erreur: {}",
                    methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Log pour l'ajout/retrait de User Stories
     */
    @Around("execution(* com.Agile.demo.execution.services.SprintService.add*UserStor*(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.remove*UserStor*(..))")
    public Object logAroundUserStoryManagement(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        boolean isAdd = methodName.contains("add");
        String operation = isAdd ? "AJOUT" : "RETRAIT";
        String emoji = isAdd ? "➕" : "➖";

        log.info("[SPRINT-SERVICE] [{}] {} User Story dans sprint", operation, emoji);
        log.debug("[SPRINT-SERVICE] [{}] Paramètres: {}", operation, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.info("[SPRINT-SERVICE] [{}] ✓ Opération réussie", operation);
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [{}] ✗ Échec - Erreur: {}", operation, e.getMessage());
            throw e;
        }
    }

    /**
     * Log pour les métriques et statistiques
     */
    @Around("execution(* com.Agile.demo.execution.services.SprintService.get*Metrics(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.get*Burndown(..))")
    public Object logAroundMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[SPRINT-SERVICE] [METRICS] 📊 Calcul des métriques: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[SPRINT-SERVICE] [METRICS] ✓ Métriques calculées");
            return result;
        } catch (Exception e) {
            log.error("[SPRINT-SERVICE] [METRICS] ✗ Échec du calcul - Erreur: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Log final après chaque exécution
     */
    @After("sprintServiceMethods()")
    public void logAfterSprintMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.trace("[SPRINT-SERVICE] --- Fin de {}", methodName);
    }
}