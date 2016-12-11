package com.stolser.javatraining.webproject.controller.validator.periodical;

import com.stolser.javatraining.webproject.controller.validator.ValidationResult;
import com.stolser.javatraining.webproject.controller.validator.Validator;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class PeriodicalPublisherValidator implements Validator {

    @Override
    public ValidationResult validate(String paramValue, HttpServletRequest request) {
        int statusCode;
        String messageKey;

        if (Pattern.matches(PERIODICAL_PUBLISHER_PATTERN_REGEX, paramValue)) {
            statusCode = STATUS_CODE_SUCCESS;
            messageKey = MESSAGE_KEY_SUCCESS;
        } else {
            statusCode = STATUS_CODE_VALIDATION_FAILED;
            messageKey = MESSAGE_KEY_PERIODICAL_PUBLISHER_ERROR;
        }

        return new ValidationResult(statusCode, messageKey);
    }
}