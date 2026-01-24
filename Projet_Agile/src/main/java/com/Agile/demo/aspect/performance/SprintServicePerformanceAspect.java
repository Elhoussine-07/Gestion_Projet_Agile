package com.Agile.demo.aspect.performance;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aspect de performance spécifique pour SprintService
 * Mesure et monitore les performances des opérations de sprint
 */
@Aspect
@Component
@Slf4j
public class SprintServicePerformanceAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_METHOD_THRESHOLD_MS = 3000;

    private final Map<String, MethodPerformanceStats> sprintPerformanceStats = new ConcurrentHashMap<>();

    /**
     * Pointcut pour toutes les méthodes publiques de SprintService
     */
    @Pointcut("execution(public * com.Agile.demo.execution.services.SprintService.*(..))")
    public void sprintServiceMethods() {}

    /**
     * Pointcut pour les méthodes transactionnelles de SprintService
     */
    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional) && " +
            "execution(* com.Agile.demo.execution.services.SprintService.*(..))")
    public void transactionalSprintMethods() {}

    /**
     * Mesure le temps d'exécution de toutes les méthodes de SprintService
     */
    @Around("sprintServiceMethods()")
    public Object measureSprintMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = "SprintService." + methodName;

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            updateSprintPerformanceStats(fullMethodName, executionTimeMs, true);
            logSprintExecutionTime(fullMethodName, executionTimeMs);

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            updateSprintPerformanceStats(fullMethodName, executionTimeMs, false);

            log.warn("[SPRINT-PERFORMANCE] ⚠️ Méthode {} a échoué après {} ms",
                    fullMethodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring spécifique pour les transactions de sprint
     */
    @Around("transactionalSprintMethods()")
    public Object monitorSprintTransactions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = "SprintService." + methodName;

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            if (executionTimeMs > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("[SPRINT-TRANSACTION]  Transaction lente détectée: {} - Durée: {} ms",
                        fullMethodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[SPRINT-TRANSACTION]  Échec de la transaction: {} après {} ms",
                    fullMethodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring spécial pour les opérations critiques de workflow
     */
    @Around("execution(* com.Agile.demo.execution.services.SprintService.startSprint(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.completeSprint(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.cancelSprint(..))")
    public Object monitorSprintWorkflowPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.info("[SPRINT-WORKFLOW-PERFORMANCE] {} exécuté en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 2000) {
                log.warn("[SPRINT-WORKFLOW-PERFORMANCE]  Opération de workflow lente: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[SPRINT-WORKFLOW-PERFORMANCE] ✗ Échec de {} après {} ms",
                    methodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring pour les calculs de métriques
     */
    @Around("execution(* com.Agile.demo.execution.services.SprintService.getSprintMetrics(..)) || " +
            "execution(* com.Agile.demo.execution.services.SprintService.getSprintBurndown(..))")
    public Object monitorMetricsCalculation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[SPRINT-METRICS-PERFORMANCE] Calcul de {} en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 500) {
                log.warn("[SPRINT-METRICS-PERFORMANCE]  Calcul de métriques lent: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Met à jour les statistiques de performance
     */
    private void updateSprintPerformanceStats(String methodName, long executionTimeMs, boolean success) {
        sprintPerformanceStats.computeIfAbsent(methodName, k -> new MethodPerformanceStats())
                .recordExecution(executionTimeMs, success);
    }

    /**
     * Log le temps d'exécution avec le niveau approprié
     */
    private void logSprintExecutionTime(String methodName, long executionTimeMs) {
        if (executionTimeMs >= VERY_SLOW_METHOD_THRESHOLD_MS) {
            log.error("[SPRINT-PERFORMANCE]  TRÈS LENTE: {} - Durée: {} ms", methodName, executionTimeMs);
        } else if (executionTimeMs >= SLOW_METHOD_THRESHOLD_MS) {
            log.warn("[SPRINT-PERFORMANCE]  LENTE: {} - Durée: {} ms", methodName, executionTimeMs);
        } else if (executionTimeMs >= 500) {
            log.info("[SPRINT-PERFORMANCE]  {} exécutée en {} ms", methodName, executionTimeMs);
        } else {
            log.debug("[SPRINT-PERFORMANCE]  {} exécutée en {} ms (rapide)", methodName, executionTimeMs);
        }
    }

    /**
     * Récupère les statistiques de performance
     */
    public Map<String, MethodPerformanceStats> getSprintPerformanceStats() {
        return new ConcurrentHashMap<>(sprintPerformanceStats);
    }

    /**
     * Affiche un rapport de performance pour SprintService
     */
    public void printSprintPerformanceReport() {
        log.info("========================================");
        log.info("  RAPPORT DE PERFORMANCE - SPRINT SERVICE");
        log.info("========================================");

        sprintPerformanceStats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getAverageTimeMs(), e1.getValue().getAverageTimeMs()))
                .forEach(entry -> {
                    String methodName = entry.getKey();
                    MethodPerformanceStats stats = entry.getValue();

                    log.info("Méthode: {}", methodName);
                    log.info("  - Appels: {} (Succès: {}, Échecs: {})",
                            stats.getTotalCalls(), stats.getSuccessfulCalls(), stats.getFailedCalls());
                    log.info("  - Temps moyen: {} ms", stats.getAverageTimeMs());
                    log.info("  - Temps min/max: {} ms / {} ms", stats.getMinTimeMs(), stats.getMaxTimeMs());
                    log.info("  - Taux de succès: {}%", stats.getSuccessRate());
                    log.info("---");
                });

        log.info("========================================");
    }

    /**
     * Réinitialise les statistiques
     */
    public void resetSprintStats() {
        sprintPerformanceStats.clear();
        log.info("[SPRINT-PERFORMANCE] Statistiques réinitialisées");
    }

    /**
     * Classe interne pour les statistiques de performance
     */
    public static class MethodPerformanceStats {
        private final AtomicInteger totalCalls = new AtomicInteger(0);
        private final AtomicInteger successfulCalls = new AtomicInteger(0);
        private final AtomicInteger failedCalls = new AtomicInteger(0);
        private final AtomicLong totalTimeMs = new AtomicLong(0);
        private final AtomicLong minTimeMs = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTimeMs = new AtomicLong(0);

        public void recordExecution(long executionTimeMs, boolean success) {
            totalCalls.incrementAndGet();

            if (success) {
                successfulCalls.incrementAndGet();
            } else {
                failedCalls.incrementAndGet();
            }

            totalTimeMs.addAndGet(executionTimeMs);
            minTimeMs.updateAndGet(current -> Math.min(current, executionTimeMs));
            maxTimeMs.updateAndGet(current -> Math.max(current, executionTimeMs));
        }

        public int getTotalCalls() {
            return totalCalls.get();
        }

        public int getSuccessfulCalls() {
            return successfulCalls.get();
        }

        public int getFailedCalls() {
            return failedCalls.get();
        }

        public long getTotalTimeMs() {
            return totalTimeMs.get();
        }

        public long getMinTimeMs() {
            return minTimeMs.get() == Long.MAX_VALUE ? 0 : minTimeMs.get();
        }

        public long getMaxTimeMs() {
            return maxTimeMs.get();
        }

        public long getAverageTimeMs() {
            int calls = totalCalls.get();
            return calls > 0 ? totalTimeMs.get() / calls : 0;
        }

        public double getSuccessRate() {
            int total = totalCalls.get();
            return total > 0 ? (successfulCalls.get() * 100.0) / total : 0.0;
        }
    }
}