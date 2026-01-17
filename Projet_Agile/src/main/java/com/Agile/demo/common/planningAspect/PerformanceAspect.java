package com.Agile.demo.common.planningAspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect pour mesurer le temps d'exécution des méthodes annotées
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    /**
     * Mesure le temps d'exécution des méthodes annotées @LogExecutionTime
     */
    @Around("@annotation(com.Agile.demo.common.planningAspect.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Récupérer l'annotation et son seuil
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
        long threshold = annotation.threshold();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        // Mesurer le temps
        long startTime = System.currentTimeMillis();

        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // Logger selon le seuil
            if (executionTime > threshold) {
                log.warn("[PERFORMANCE] {}.{} took {}ms (threshold: {}ms) ",
                        className, methodName, executionTime, threshold);
            } else {
                log.info("[PERFORMANCE] {}.{} took {}ms",
                        className, methodName, executionTime);
            }
        }

        return result;
    }
}