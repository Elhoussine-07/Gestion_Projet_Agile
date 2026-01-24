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
public class UserServiceLoggingAspect {


    @Pointcut("execution(public * com.Agile.demo.execution.services.UserService.*(..))")
    public void userServiceMethods() {}


    @Pointcut("execution(* com.Agile.demo.execution.services.UserService.create*(..))")
    public void createUserMethods() {}


    @Pointcut("execution(* com.Agile.demo.execution.services.UserService.update*(..))")
    public void updateUserMethods() {}


    @Pointcut("execution(* com.Agile.demo.execution.services.UserService.delete*(..))")
    public void deleteUserMethods() {}


    @Pointcut("execution(* com.Agile.demo.execution.services.UserService.get*(..))")
    public void getUserMethods() {}


    @Pointcut("execution(* com.Agile.demo.execution.services.UserService.activateUser(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.deactivateUser(..))")
    public void userActivationMethods() {}


    @Pointcut("execution(* com.Agile.demo.execution.services.UserService.*Password(..))")
    public void passwordMethods() {}


    @Before("userServiceMethods()")
    public void logBeforeUserMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Masquer les mots de passe dans les logs
        Object[] sanitizedArgs = sanitizeArgs(args, methodName);

        log.debug("[USER-SERVICE] ==> Appel de {} avec paramètres: {}",
                methodName, Arrays.toString(sanitizedArgs));
    }


    @AfterReturning(pointcut = "userServiceMethods()", returning = "result")
    public void logAfterReturningUserMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[USER-SERVICE] <== Retour de {} avec résultat: {}",
                methodName, result);
    }


    @AfterThrowing(pointcut = "userServiceMethods()", throwing = "exception")
    public void logAfterThrowingUserMethod(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.error("[USER-SERVICE] <!> Exception dans {} avec paramètres: {}",
                methodName, Arrays.toString(sanitizeArgs(args, methodName)));
        log.error("[USER-SERVICE] <!> Erreur: {} - {}",
                exception.getClass().getSimpleName(), exception.getMessage());
    }


    @Around("createUserMethods()")
    public Object logAroundCreateUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Extraire le nom d'utilisateur (premier paramètre)
        String username = args.length > 0 ? String.valueOf(args[0]) : "N/A";

        log.info("[USER-SERVICE] [CRÉATION] 👤 Tentative de création d'utilisateur: {}", username);
        log.debug("[USER-SERVICE] [CRÉATION] Paramètres (sanitizés): {}",
                Arrays.toString(sanitizeArgs(args, methodName)));

        try {
            Object result = joinPoint.proceed();
            log.info("[USER-SERVICE] [CRÉATION] ✓ Utilisateur créé avec succès: {}", username);
            log.debug("[USER-SERVICE] [CRÉATION] Résultat: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [CRÉATION] ✗ Échec de la création de l'utilisateur {} - Erreur: {}",
                    username, e.getMessage());
            throw e;
        }
    }


    @Around("updateUserMethods()")
    public Object logAroundUpdateUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Long userId = args.length > 0 ? (Long) args[0] : null;

        log.info("[USER-SERVICE] [MISE À JOUR]  Tentative de mise à jour utilisateur ID: {}", userId);
        log.debug("[USER-SERVICE] [MISE À JOUR] Paramètres (sanitizés): {}",
                Arrays.toString(sanitizeArgs(args, methodName)));

        try {
            Object result = joinPoint.proceed();
            log.info("[USER-SERVICE] [MISE À JOUR] ✓ Utilisateur mis à jour avec succès: {}", userId);
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [MISE À JOUR] ✗ Échec de la mise à jour - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }


    @Around("deleteUserMethods()")
    public Object logAroundDeleteUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Long userId = args.length > 0 ? (Long) args[0] : null;

        log.warn("[USER-SERVICE] [SUPPRESSION]  Tentative de suppression utilisateur ID: {}", userId);

        try {
            Object result = joinPoint.proceed();
            log.warn("[USER-SERVICE] [SUPPRESSION] ✓ Utilisateur supprimé avec succès: {}", userId);
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [SUPPRESSION] ✗ Échec de la suppression - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }


    @Around("getUserMethods()")
    public Object logAroundGetUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[USER-SERVICE] [LECTURE] Récupération de données: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[USER-SERVICE] [LECTURE] ✓ Données récupérées avec succès");
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [LECTURE] ✗ Échec de la récupération - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }


    @Around("userActivationMethods()")
    public Object logAroundUserActivation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Long userId = args.length > 0 ? (Long) args[0] : null;
        boolean isActivation = methodName.equals("activateUser");
        String operation = isActivation ? "ACTIVATION" : "DÉSACTIVATION";
        String emoji = isActivation ? "✅" : "⏸️";

        log.info("[USER-SERVICE] [{}] {} Utilisateur ID: {}", operation, emoji, userId);

        try {
            Object result = joinPoint.proceed();
            log.info("[USER-SERVICE] [{}] ✓ Opération réussie pour utilisateur: {}", operation, userId);
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [{}] ✗ Échec - Erreur: {}", operation, e.getMessage());
            throw e;
        }
    }


    @Around("passwordMethods()")
    public Object logAroundPasswordOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Long userId = args.length > 0 ? (Long) args[0] : null;

        log.info("[USER-SERVICE] [PASSWORD] 🔐 Opération de mot de passe: {} pour utilisateur ID: {}",
                methodName, userId);


        try {
            Object result = joinPoint.proceed();
            log.info("[USER-SERVICE] [PASSWORD] ✓ Opération de mot de passe réussie pour utilisateur: {}",
                    userId);
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [PASSWORD] ✗ Échec de l'opération de mot de passe - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }


    @Around("execution(* com.Agile.demo.execution.services.UserService.*Statistics(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.*Workload(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.*Performance(..))")
    public Object logAroundUserMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.debug("[USER-SERVICE] [METRICS] 📊 Calcul des métriques: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("[USER-SERVICE] [METRICS] ✓ Métriques calculées");
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [METRICS] ✗ Échec du calcul - Erreur: {}", e.getMessage());
            throw e;
        }
    }


    @Around("execution(* com.Agile.demo.execution.services.UserService.searchUsers(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.getAvailable*(..))")
    public Object logAroundUserSearch(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("[USER-SERVICE] [RECHERCHE]  Recherche d'utilisateurs: {}", methodName);
        log.debug("[USER-SERVICE] [RECHERCHE] Critères: {}", Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            log.debug("[USER-SERVICE] [RECHERCHE] ✓ Recherche terminée");
            return result;
        } catch (Exception e) {
            log.error("[USER-SERVICE] [RECHERCHE] ✗ Échec de la recherche - Erreur: {}",
                    e.getMessage());
            throw e;
        }
    }


    @Around("execution(* com.Agile.demo.execution.services.UserService.*Exists(..)) || " +
            "execution(* com.Agile.demo.execution.services.UserService.isUser*(..))")
    public Object logAroundUserValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.trace("[USER-SERVICE] [VALIDATION]  Vérification: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.trace("[USER-SERVICE] [VALIDATION] ✓ Vérification terminée: {}", result);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @After("userServiceMethods()")
    public void logAfterUserMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.trace("[USER-SERVICE] --- Fin de {}", methodName);
    }


    private Object[] sanitizeArgs(Object[] args, String methodName) {
        if (args == null || args.length == 0) {
            return args;
        }

        Object[] sanitized = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            // Masquer les mots de passe
            if (methodName.toLowerCase().contains("password") &&
                    (i == 1 || i == 2)) { // Position typique des mots de passe
                sanitized[i] = "***MASKED***";
            } else {
                sanitized[i] = args[i];
            }
        }

        return sanitized;
    }
}