package com.ejemplo.notasapp.aspecto;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.ejemplo.notasapp.excepcion.EstudianteNoEncontrado;

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

    @AfterThrowing(pointcut = "puntoDeCorte()", throwing = "ex")
    public void despuesDeExcepcion(JoinPoint joinPoint, EstudianteNoEncontrado ex) {
        System.out.println("================================");
        System.out.println("Excepción capturada en: " + joinPoint.getSignature().getName());
        System.out.println("Mensaje: " + ex.getMessage());
        System.out.println("Código: " + ex.getCodigoError());
        System.out.println("Causa: " + ex.getCause());
        System.out.println("Redirigiendo al ErrorController...");
        System.out.println("================================");
    }

    @After("puntoDeCorte()")
    public void despuesDeEjecutar(JoinPoint joinPoint) {
        System.out.println("--------------------------------");
        System.out.println("Después de ejecutar: " + joinPoint.getSignature().getName());
        System.out.println("--------------------------------");
    }

    @Around("execution(* com.ejemplo.notasapp.controlador.NotaController.editar(..))")
    public Object logAlrededorEditarNota(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("-------Iniciando Editar Nota-------");

        Object[] args = joinPoint.getArgs();
        System.out.println("Número de argumentos: " + args.length);

        // Buscar el ID de la nota (parámetro Long)
        Long idNota = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                idNota = (Long) arg;
                break;
            }
        }

        System.out.println("ID de la nota a editar: " + idNota);
        System.out.println("Argumentos: " + Arrays.toString(args));

        Long inicio = System.currentTimeMillis();

        Object resultado;
        try {
            resultado = joinPoint.proceed();
            System.out.println("Nota editada correctamente");
        } catch (Exception e) {
            System.out.println("Error al editar la nota: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("-------Finalizando Editar Nota-------");
            Long fin = System.currentTimeMillis();
            System.out.println("Tiempo de ejecución: " + (fin - inicio) + "ms");
        }
        return resultado;
    }
}
