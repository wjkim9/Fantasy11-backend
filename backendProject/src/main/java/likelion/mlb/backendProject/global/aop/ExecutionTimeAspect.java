package likelion.mlb.backendProject.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Around("@annotation(SchedulerLog)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, SchedulerLog SchedulerLog) throws Throwable {
        String action = SchedulerLog.action();
        log.info("======================= {} 액션 실행======================", action);

        long start = System.currentTimeMillis();
        // 실제 메소드 실행
        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - start;
        log.info("{} executed in {} ms",
                joinPoint.getSignature().toShortString(),
                duration);

        log.info("======================= {} 액션 종료======================", action);

        return result;
    }

}
