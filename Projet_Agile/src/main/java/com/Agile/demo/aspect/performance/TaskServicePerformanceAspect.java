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
 * Aspect de performance spécifique pour TaskService
 * Mesure et monitore les performances des opérations de tâches
 */
@Aspect
@Component
@Slf4j
public class TaskServicePerformanceAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 800;
    private static final long VERY_SLOW_METHOD_THRESHOLD_MS = 2000;

    private final Map<String, MethodPerformanceStats> taskPerformanceStats = new ConcurrentHashMap<>();

    /**
     * Pointcut pour toutes les méthodes publiques de TaskService
     */
    @Pointcut("execution(public * com.Agile.demo.execution.services.TaskService.*(..))")
    public void taskServiceMethods() {}

    /**
     * Pointcut pour les méthodes transactionnelles de TaskService
     */
    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional) && " +
            "execution(* com.Agile.demo.execution.services.TaskService.*(..))")
    public void transactionalTaskMethods() {}

    /**
     * Mesure le temps d'exécution de toutes les méthodes de TaskService
     */
    @Around("taskServiceMethods()")
    public Object measureTaskMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = "TaskService." + methodName;

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            updateTaskPerformanceStats(fullMethodName, executionTimeMs, true);
            logTaskExecutionTime(fullMethodName, executionTimeMs);

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            updateTaskPerformanceStats(fullMethodName, executionTimeMs, false);

            log.warn("[TASK-PERFORMANCE] ⚠️ Méthode {} a échoué après {} ms",
                    fullMethodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring spécifique pour les transactions de tâche
     */
    @Around("transactionalTaskMethods()")
    public Object monitorTaskTransactions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = "TaskService." + methodName;

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            if (executionTimeMs > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("[TASK-TRANSACTION] ⚠️ Transaction lente détectée: {} - Durée: {} ms",
                        fullMethodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[TASK-TRANSACTION] ✗ Échec de la transaction: {} après {} ms",
                    fullMethodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring spécial pour les opérations critiques de workflow
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.startTask(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.completeTask*(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.moveTask*(..))")
    public Object monitorTaskWorkflowPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.info("[TASK-WORKFLOW-PERFORMANCE] {} exécuté en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 1500) {
                log.warn("[TASK-WORKFLOW-PERFORMANCE] ⚠️ Opération de workflow lente: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[TASK-WORKFLOW-PERFORMANCE] ✗ Échec de {} après {} ms",
                    methodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations de blocage
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.blockTask(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.unblockTask(..))")
    public Object monitorBlockingOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[TASK-BLOCKING-PERFORMANCE] {} exécuté en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 500) {
                log.warn("[TASK-BLOCKING-PERFORMANCE] ⚠️ Opération de blocage lente: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Monitoring pour les calculs de métriques
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.*Metrics(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.*Statistics(..))")
    public Object monitorMetricsCalculation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[TASK-METRICS-PERFORMANCE] Calcul de {} en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 400) {
                log.warn("[TASK-METRICS-PERFORMANCE] ⚠️ Calcul de métriques lent: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Monitoring pour les requêtes complexes
     */
    @Around("execution(* com.Agile.demo.execution.services.TaskService.getTasksBySprint*(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.findOverEstimated*(..)) || " +
            "execution(* com.Agile.demo.execution.services.TaskService.getCriticalTasks(..))")
    public Object monitorComplexQueries(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[TASK-QUERY-PERFORMANCE] Requête {} exécutée en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 1000) {
                log.warn("[TASK-QUERY-PERFORMANCE] ⚠️ Requête lente détectée: {} ({} ms)",
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
    private void updateTaskPerformanceStats(String methodName, long executionTimeMs, boolean success) {
        taskPerformanceStats.computeIfAbsent(methodName, k -> new MethodPerformanceStats())
                .recordExecution(executionTimeMs, success);
    }

    /**
     * Log le temps d'exécution avec le niveau approprié
     */
    private void logTaskExecutionTime(String methodName, long executionTimeMs) {
        if (executionTimeMs >= VERY_SLOW_METHOD_THRESHOLD_MS) {
            log.error("[TASK-PERFORMANCE] 🔴 TRÈS LENTE: {} - Durée: {} ms", methodName, executionTimeMs);
        } else if (executionTimeMs >= SLOW_METHOD_THRESHOLD_MS) {
            log.warn("[TASK-PERFORMANCE] 🟡 LENTE: {} - Durée: {} ms", methodName, executionTimeMs);
        } else if (executionTimeMs >= 300) {
            log.info("[TASK-PERFORMANCE] 🟢 {} exécutée en {} ms", methodName, executionTimeMs);
        } else {
            log.debug("[TASK-PERFORMANCE] ⚡ {} exécutée en {} ms (rapide)", methodName, executionTimeMs);
        }
    }

    /**
     * Récupère les statistiques de performance
     */
    public Map<String, MethodPerformanceStats> getTaskPerformanceStats() {
        return new ConcurrentHashMap<>(taskPerformanceStats);
    }

    /**
     * Affiche un rapport de performance pour TaskService
     */
    public void printTaskPerformanceReport() {
        log.info("========================================");
        log.info("  RAPPORT DE PERFORMANCE - TASK SERVICE");
        log.info("========================================");

        taskPerformanceStats.entrySet().stream()
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
    public void resetTaskStats() {
        taskPerformanceStats.clear();
        log.info("[TASK-PERFORMANCE] Statistiques réinitialisées");
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