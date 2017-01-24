package com.stolser.javatraining.webproject.controller.form.validator;

import com.stolser.javatraining.webproject.controller.form.validator.exception.ValidationProcessorException;
import com.stolser.javatraining.webproject.controller.form.validator.periodical.PeriodicalCategoryValidator;
import com.stolser.javatraining.webproject.controller.form.validator.periodical.PeriodicalCostValidator;
import com.stolser.javatraining.webproject.controller.form.validator.periodical.PeriodicalNameValidator;
import com.stolser.javatraining.webproject.controller.form.validator.periodical.PeriodicalPublisherValidator;
import com.stolser.javatraining.webproject.controller.form.validator.user.UserEmailValidator;
import com.stolser.javatraining.webproject.controller.form.validator.user.UserPasswordValidator;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.*;

/**
 * Produces validators for different parameter names.
 */
public class ValidatorFactory {
    private static final String THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM =
            "There is no validator for such a parameter!";

    public static Validator getPeriodicalNameValidator() {
        return new PeriodicalNameValidator();
    }

    public static Validator getPeriodicalCategoryValidator() {
        return new PeriodicalCategoryValidator();
    }

    public static Validator getPeriodicalPublisherValidator() {
        return new PeriodicalPublisherValidator();
    }

    public static Validator getPeriodicalCostValidator() {
        return new PeriodicalCostValidator();
    }

    /**
     * Returns a concrete validator for this specific parameter.
     *
     * @param paramName a http parameter name that need to be validated
     */
    public static Validator newValidator(String paramName) {
        switch (paramName) {
            case PERIODICAL_NAME_PARAM_NAME:
                return new PeriodicalNameValidator();

            case PERIODICAL_CATEGORY_PARAM_NAME:
                return new PeriodicalCategoryValidator();

            case PERIODICAL_PUBLISHER_PARAM_NAME:
                return new PeriodicalPublisherValidator();

            case PERIODICAL_COST_PARAM_NAME:
                return new PeriodicalCostValidator();

            case USER_EMAIL_PARAM_NAME:
                return new UserEmailValidator();

            case USER_PASSWORD_PARAM_NAME:
                return new UserPasswordValidator();

            default:
                throw new ValidationProcessorException(THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM);
        }
    }
}