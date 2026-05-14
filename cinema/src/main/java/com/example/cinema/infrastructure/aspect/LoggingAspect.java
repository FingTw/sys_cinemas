package com.example.cinema.infrastructure.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.example.cinema.presentation.controllers..*)")
    public void controllerPointcut() {}

    @Pointcut("within(com.example.cinema.application.usecases..*)")
    public void useCasePointcut() {}

    @Pointcut("within(com.example.cinema.infrastructure..*) && !within(com.example.cinema.infrastructure.aspect..*) && !within(com.example.cinema.infrastructure.security..*)")
    public void infrastructurePointcut() {}

    @Around("controllerPointcut() || useCasePointcut() || infrastructurePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // 1. Log khi đi vào hàm
        if (log.isDebugEnabled() || log.isInfoEnabled()) {
            Object[] args = joinPoint.getArgs();
            String maskedArgs = maskSensitiveData(Arrays.toString(args));
            log.info("Enter: {}.{}() with argument[s] = {}", className, methodName, maskedArgs);
        }

        long start = System.currentTimeMillis();
        try {
            // 2. Chạy hàm thực tế
            Object result = joinPoint.proceed();
            
            // 3. Log khi kết thúc hàm thành công
            long elapsedTime = System.currentTimeMillis() - start;
            if (log.isDebugEnabled() || log.isInfoEnabled()) {
                String maskedResult = maskSensitiveData(String.valueOf(result));
                log.info("Exit: {}.{}() with result = {} (Time: {} ms)", className, methodName, maskedResult, elapsedTime);
            }
            return result;
        } catch (IllegalArgumentException e) {
            // 4. Log khi có ngoại lệ
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()), className, methodName);
            throw e;
        } catch (Exception e) {
            log.error("Exception in {}.{}() with cause = {}", className, methodName, e.getCause() != null ? e.getCause() : "NULL");
            throw e;
        }
    }

    /**
     * Hàm che giấu thông tin nhạy cảm trước khi in ra log
     */
    private String maskSensitiveData(String data) {
        if (data == null) return "null";
        
        // Che dấu token, password, secret (Rất cơ bản, có thể dùng Regex nâng cao hơn)
        String masked = data.replaceAll("(?i)(password|token|secret|jwt)([\\s\"':=]+)[^\\s,\\]\\}]+", "$1$2***MASKED***");
        
        // Cắt bớt nếu chuỗi quá dài (Tránh log rác)
        if (masked.length() > 500) {
            return masked.substring(0, 500) + "... (truncated)";
        }
        return masked;
    }
}
