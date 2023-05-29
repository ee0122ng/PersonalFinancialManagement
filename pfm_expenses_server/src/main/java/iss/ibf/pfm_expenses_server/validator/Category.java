package iss.ibf.pfm_expenses_server.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryValidator.class)
public @interface Category {
    
    String message() default "Invalid data";
    // to be included together with a @Constraint annotation
	Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
