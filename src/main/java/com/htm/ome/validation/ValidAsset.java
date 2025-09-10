package com.htm.ome.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidAssetValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAsset {

    String message() default "Invalid asset. Please use value from allowed assets list";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
