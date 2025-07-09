package com.emobile.springtodo.core.service.aspect;

import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.exception.AccessRightsException;
import com.emobile.springtodo.core.service.TaskService;
import com.emobile.springtodo.core.service.aspect.annotation.RightsVerification;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class VerifyingUserRights {

    @Autowired
    private TaskService taskService;

    @Before("@annotation(rightsVerification)")
    public void checkRightsVerification(JoinPoint joinPoint, RightsVerification rightsVerification) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Long userId = (Long) args[rightsVerification.user()];
            Long id = (Long) args[0];
            TaskDto task = taskService.getTaskById(id);
            if (!Objects.equals(userId, task.getUserId())) {
                throw new AccessRightsException("Ошибка прав доступа пользователя!");
            }
        }
    }
}
