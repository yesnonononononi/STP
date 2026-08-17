package com.summit.stp.operationlog.annotation;

import com.summit.stp.operationlog.domain.model.Operation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    String description() default "";

    Operation.OperationType type() default Operation.OperationType.OTHER;

    String entityId() default "";
}
