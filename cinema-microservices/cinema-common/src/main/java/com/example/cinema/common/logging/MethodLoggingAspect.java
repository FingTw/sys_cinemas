package com.example.cinema.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP Aspect tu dong ghi log vao dau va cuoi moi phuong thuc.
 * Giup xac dinh:
 * - Phuong thuc nao duoc goi
 * - Tham so truyen vao la gi
 * - Mat bao nhieu thoi gian de hoan thanh
 * - Ket qua tra ve (hoac loi neu co)
 *
 * Output format:
 * >>> [SERVICE] ClassName.methodName() | Args: [arg1, arg2] | STARTED
 * <<< [SERVICE] ClassName.methodName() | Time: 45ms | COMPLETED
 * !!! [SERVICE] ClassName.methodName() | Time: 120ms | FAILED: error message
 */
@Aspect
@Component
public class MethodLoggingAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    // --- Pointcut Definitions ---

    /**
     * Bat tat ca cac class trong package "usecases" (UseCase/Service layer)
     */
    @Pointcut("execution(* com.example.cinema..application.usecases..*(..))")
    public void useCaseLayer() {}

    /**
     * Bat tat ca cac class trong package "controllers" (Presentation layer)
     */
    @Pointcut("execution(* com.example.cinema..presentation.controllers..*(..))")
    public void controllerLayer() {}

    /**
     * Bat tat ca cac class trong package "adapters" (Infrastructure layer)
     */
    @Pointcut("execution(* com.example.cinema..infrastructure..*Adapter*.*(..))")
    public void adapterLayer() {}

    /**
     * Bat tat ca cac class trong package "external" (Feign Client calls)
     */
    @Pointcut("execution(* com.example.cinema..infrastructure.external..*(..))")
    public void externalCallLayer() {}

    // --- Advice ---

    @Around("useCaseLayer() || controllerLayer()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return doLog(joinPoint, "BUSINESS");
    }

    @Around("adapterLayer()")
    public Object logAdapterExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return doLog(joinPoint, "DATABASE");
    }

    @Around("externalCallLayer()")
    public Object logExternalCallExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return doLog(joinPoint, "EXTERNAL");
    }

    /**
     * Core logging logic: ghi log dau vao, do thoi gian, ghi log dau ra hoac loi.
     */
    private Object doLog(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String location = className + "." + methodName + "()";

        Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        // --- Format tham so dau vao (gioi han do dai de tranh log qua dai) ---
        String args = formatArgs(joinPoint.getArgs());

        // --- LOG ENTRY ---
        log.info(">>> [{}] {} | Layer: {} | Args: [{}] | STARTED",
                serviceName.toUpperCase(), location, layer, args);

        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            // --- LOG EXIT (thanh cong) ---
            if (durationMs > 1000) {
                // Canh bao neu method mat hon 1 giay
                log.warn("<<< [{}] {} | Layer: {} | Time: {}ms | SLOW EXECUTION",
                        serviceName.toUpperCase(), location, layer, durationMs);
            } else {
                log.info("<<< [{}] {} | Layer: {} | Time: {}ms | COMPLETED",
                        serviceName.toUpperCase(), location, layer, durationMs);
            }

            return result;

        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            // --- LOG EXIT (that bai) ---
            log.error("!!! [{}] {} | Layer: {} | Time: {}ms | FAILED: {}",
                    serviceName.toUpperCase(), location, layer, durationMs,
                    ex.getClass().getSimpleName() + " - " + ex.getMessage());

            throw ex; // Re-throw de GlobalExceptionHandler xu ly
        }
    }

    /**
     * Format tham so dau vao, gioi han moi tham so toi da 100 ky tu
     * de tranh log qua dai (VD: khi truyen vao 1 list lon).
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String str = arg.toString();
                    return str.length() > 100 ? str.substring(0, 100) + "..." : str;
                })
                .collect(Collectors.joining(", "));
    }
}
