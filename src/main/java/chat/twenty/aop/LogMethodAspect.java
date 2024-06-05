package chat.twenty.aop;

import chat.twenty.log.trace.LogTrace;
import chat.twenty.log.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@ConditionalOnExpression("${aopLogging:true}") // properties 값 읽어서 끔 (테스트에서 끄려고)
@RequiredArgsConstructor
public class LogMethodAspect {

    private final LogTrace logTrace;

    // Pointcut 표현식 분리
    @Pointcut("execution(* chat.twenty.controller..*(..))")
    public void allController() {}

    @Pointcut("execution(* chat.twenty.service..*(..))")
    public void allService() {}

    @Pointcut("execution(* chat.twenty.repository..*(..))")
    public void allRepository() {}

    @Around("allController() || allService() || allRepository()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;
        Object[] params = null;
        try {
            String message = joinPoint.getSignature().toShortString();
            params = joinPoint.getArgs();

            status = logTrace.begin(message);

            Object result = joinPoint.proceed();

            logTrace.end(status);
            return result;
        } catch (Exception e) {
            // Exception 발생시, end 가 아닌 exception 호출
            logTrace.exception(status, e, params);
            throw e;
        }
    }

}
