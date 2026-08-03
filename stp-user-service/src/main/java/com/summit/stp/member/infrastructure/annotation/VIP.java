package com.summit.stp.member.infrastructure.annotation;

import com.summit.stp.member.domain.model.MemberType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VIP {
    MemberType type() default MemberType.REGULAR;
}
