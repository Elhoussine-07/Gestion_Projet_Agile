package com.Agile.demo.common.planningAspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect pour logger automatiquement les appels aux services
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut pour tous les services du domaine planning
     */
    @Pointcut("execution(* com.Agile.demo.planning.service.*.*(..))")
    public void planningServiceMethods() {}

    /**
     * Log avant l'exécution d'une méthode de service
     */
    @Before("planningServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("[BEFORE] {}.{} - Args: {}",
                className, methodName, Arrays.toString(args));
    }

    /**
     * Log après l'exécution réussie
     */
    @AfterReturning(
            pointcut = "planningServiceMethods()",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("[SUCCESS] {}.{} - Result: {}",
                className, methodName,
                result != null ? result.getClass().getSimpleName() : "void");
    }

    /**
     * Log en cas d'exception
     */
    @AfterThrowing(
            pointcut = "planningServiceMethods()",
            throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[ERROR] {}.{} - Exception: {} - Message: {}",
                className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }
}