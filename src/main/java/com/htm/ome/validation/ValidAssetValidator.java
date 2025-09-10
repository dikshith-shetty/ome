package com.htm.ome.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ValidAssetValidator implements ConstraintValidator<ValidAsset, String> {

    private final List<String> allowedAssets;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        boolean isValid = allowedAssets.stream()
                .anyMatch(asset -> asset.equalsIgnoreCase(value));

        if (!isValid) {
            String allowedList = allowedAssets.stream()
                    .sorted()
                    .collect(Collectors.joining(", "));

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid asset '" + value + "'. Allowed values are: " + allowedList
            ).addConstraintViolation();
        }

        return isValid;
    }
}
