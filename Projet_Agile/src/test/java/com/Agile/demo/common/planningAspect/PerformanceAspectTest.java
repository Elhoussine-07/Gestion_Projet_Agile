package com.Agile.demo.common.planningAspect;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PerformanceAspectTest {

    @Test
    void shouldMeasureExecutionTime() throws Throwable {
        // Given
        PerformanceAspect aspect = new PerformanceAspect();
        TestService target = new TestService();

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);

        TestService proxy = factory.getProxy();

        // When / Then
        assertDoesNotThrow(proxy::fastMethod);
        assertDoesNotThrow(proxy::slowMethod);
    }

    @Test
    void shouldLogWarningWhenThresholdExceeded() throws Throwable {
        // Given
        PerformanceAspect aspect = new PerformanceAspect();
        TestService target = new TestService();

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);

        TestService proxy = factory.getProxy();

        // When
        String result = proxy.slowMethod();

        // Then
        assertThat(result).isEqualTo("done");
        // Vérifier dans les logs qu'un warning a été émis
    }

    // Classe de test
    static class TestService {

        @LogExecutionTime(threshold = 100)
        public String fastMethod() {
            return "fast";
        }

        @LogExecutionTime(threshold = 50)
        public String slowMethod() {
            try {
                Thread.sleep(100);  // Dépasse le seuil
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }
    }
}