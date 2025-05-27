package com.ejemplo.notasapp.aspecto;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Aspectos {

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.*(..))")
    public void puntoDeCorte() {
    }

    @Before("puntoDeCorte()")
    public void antesDeEjecutar(JoinPoint joinPoint) {
        System.out.println("--------------------------------");
        System.out.println("Antes de ejecutar: " + joinPoint.getSignature().getName());
        System.out.println("Parámetros: " + Arrays.toString(joinPoint.getArgs()));
        System.out.println("--------------------------------");
    }

    @AfterReturning(pointcut = "puntoDeCorte()", returning = "resultado")
    public void despuesDeEjecutar(JoinPoint joinPoint, Object resultado) {
        System.out.println("--------------------------------");
        System.out.println("Después de ejecutar: " + joinPoint.getSignature().getName());
        System.out.println("Resultado: " + resultado);
        System.out.println("--------------------------------");
    }
}
