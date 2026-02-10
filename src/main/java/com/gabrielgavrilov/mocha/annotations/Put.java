package com.gabrielgavrilov.mocha.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Put {
    public String value() default "";
}

