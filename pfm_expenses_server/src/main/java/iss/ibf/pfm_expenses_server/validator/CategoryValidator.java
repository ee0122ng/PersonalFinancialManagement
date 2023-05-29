package iss.ibf.pfm_expenses_server.validator;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CategoryValidator implements ConstraintValidator<Category, String> {

    private final List<String> CATEGORIES = List.of("income", "saving", "spending", "investment", "protection");

    public boolean isValid(String item, ConstraintValidatorContext context) {

        if (item != null) {
            if (CATEGORIES.contains(item.toLowerCase())) {
                return false;
            }
        }

        return true;

    }
    
}
