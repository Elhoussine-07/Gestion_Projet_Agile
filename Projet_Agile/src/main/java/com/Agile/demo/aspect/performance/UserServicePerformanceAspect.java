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
 * Aspect de performance spécifique pour UserService
 * Mesure et monitore les performances des opérations utilisateur
 */
@Aspect
@Component
@Slf4j
public class UserServicePerformanceAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 700;
    private static final long VERY_SLOW_METHOD_THRESHOLD_MS = 1500;

    private final Map<String, MethodPerformanceStats> userPerformanceStats = new ConcurrentHashMap<>();

    /**
     * Pointcut pour toutes les méthodes publiques de UserService
     */
    @Pointcut("execution(public * com.Agile.demo.execution.services.UserService.*(..))")
    public void userServiceMethods() {}

    /**
     * Pointcut pour les méthodes transactionnelles de UserService
     */
    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional) && " +
            "execution(* com.Agile.demo.execution.services.UserService.*(..))")
    public void transactionalUserMethods() {}

    /**
     * Mesure le temps d'exécution de toutes les méthodes de UserService
     */
    @Around("userServiceMethods()")
    public Object measureUserMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = "UserService." + methodName;

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            updateUserPerformanceStats(fullMethodName, executionTimeMs, true);
            logUserExecutionTime(fullMethodName, executionTimeMs);

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            updateUserPerformanceStats(fullMethodName, executionTimeMs, false);

            log.warn("[USER-PERFORMANCE] ⚠️ Méthode {} a échoué après {} ms",
                    fullMethodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring spécifique pour les transactions utilisateur
     */
    @Around("transactionalUserMethods()")
    public Object monitorUserTransactions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = "UserService." + methodName;

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            if (executionTimeMs > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("[USER-TRANSACTION] ⚠️ Transaction lente détectée: {} - Durée: {} ms",
                        fullMethodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[USER-TRANSACTION] ✗ Échec de la transaction: {} après {} ms",
                    fullMethodName, executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations de création d'utilisateur
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.createUser(..))")
    public Object monitorUserCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.info("[USER-CREATION-PERFORMANCE] Création d'utilisateur en {} ms", executionTimeMs);

            if (executionTimeMs > 1000) {
                log.warn("[USER-CREATION-PERFORMANCE] ⚠️ Création lente: {} ms", executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[USER-CREATION-PERFORMANCE] ✗ Échec de la création après {} ms", executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations de mot de passe (critiques pour la sécurité)
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.*Password(..))")
    public Object monitorPasswordOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.info("[USER-PASSWORD-PERFORMANCE] Opération de mot de passe en {} ms", executionTimeMs);

            // Les opérations de mot de passe ne doivent pas être trop lentes (hashing)
            if (executionTimeMs > 2000) {
                log.warn("[USER-PASSWORD-PERFORMANCE] ⚠️ Opération de hashing très lente: {} ms",
                        executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.error("[USER-PASSWORD-PERFORMANCE] ✗ Échec de l'opération après {} ms", executionTimeMs);

            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations de recherche d'utilisateurs
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.searchUsers(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.getUsersByRole(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.getUsersByProject(..))")
    public Object monitorUserSearchOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[USER-SEARCH-PERFORMANCE] Recherche {} en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 800) {
                log.warn("[USER-SEARCH-PERFORMANCE] ⚠️ Recherche lente: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Monitoring pour les calculs de statistiques
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.*Statistics(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.*Workload(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.*Performance(..))")
    public Object monitorUserStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[USER-STATS-PERFORMANCE] Calcul de {} en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 500) {
                log.warn("[USER-STATS-PERFORMANCE] ⚠️ Calcul de statistiques lent: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations de validation (doivent être rapides)
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.*Exists(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.isUser*(..))")
    public Object monitorValidationOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            // Les validations doivent être très rapides
            if (executionTimeMs > 200) {
                log.warn("[USER-VALIDATION-PERFORMANCE] ⚠️ Validation lente: {} ({} ms)",
                        methodName, executionTimeMs);
            } else {
                log.trace("[USER-VALIDATION-PERFORMANCE] Validation {} en {} ms",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations d'activation/désactivation
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.activateUser(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.deactivateUser(..))")
    public Object monitorActivationOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[USER-ACTIVATION-PERFORMANCE] {} en {} ms", methodName, executionTimeMs);

            if (executionTimeMs > 500) {
                log.warn("[USER-ACTIVATION-PERFORMANCE] ⚠️ Opération lente: {} ({} ms)",
                        methodName, executionTimeMs);
            }

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * Monitoring pour les opérations de workload (charge de travail)
     */
    @Around("execution(* com.Agile.demo.execution.services.UserService.getTeamWorkload(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.getMostLoadedUsers(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.getLeastLoadedUserByRole(..))")
    public Object monitorWorkloadCalculations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long executionTimeMs = Duration.between(start, end).toMillis();

            log.debug("[USER-WORKLOAD-PERFORMANCE] Calcul de workload {} en {} ms",
                    methodName, executionTimeMs);

            if (executionTimeMs > 1000) {
                log.warn("[USER-WORKLOAD-PERFORMANCE] ⚠️ Calcul de charge lent: {} ({} ms)",
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
    private void updateUserPerformanceStats(String methodName, long executionTimeMs, boolean success) {
        userPerformanceStats.computeIfAbsent(methodName, k -> new MethodPerformanceStats())
                .recordExecution(executionTimeMs, success);
    }

    /**
     * Log le temps d'exécution avec le niveau approprié
     */
    private void logUserExecutionTime(String methodName, long executionTimeMs) {
        if (executionTimeMs >= VERY_SLOW_METHOD_THRESHOLD_MS) {
            log.error("[USER-PERFORMANCE] 🔴 TRÈS LENTE: {} - Durée: {} ms", methodName, executionTimeMs);
        } else if (executionTimeMs >= SLOW_METHOD_THRESHOLD_MS) {
            log.warn("[USER-PERFORMANCE] 🟡 LENTE: {} - Durée: {} ms", methodName, executionTimeMs);
        } else if (executionTimeMs >= 250) {
            log.info("[USER-PERFORMANCE] 🟢 {} exécutée en {} ms", methodName, executionTimeMs);
        } else {
            log.debug("[USER-PERFORMANCE] ⚡ {} exécutée en {} ms (rapide)", methodName, executionTimeMs);
        }
    }

    /**
     * Récupère les statistiques de performance
     */
    public Map<String, MethodPerformanceStats> getUserPerformanceStats() {
        return new ConcurrentHashMap<>(userPerformanceStats);
    }

    /**
     * Affiche un rapport de performance pour UserService
     */
    public void printUserPerformanceReport() {
        log.info("========================================");
        log.info("  RAPPORT DE PERFORMANCE - USER SERVICE");
        log.info("========================================");

        userPerformanceStats.entrySet().stream()
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
    public void resetUserStats() {
        userPerformanceStats.clear();
        log.info("[USER-PERFORMANCE] Statistiques réinitialisées");
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