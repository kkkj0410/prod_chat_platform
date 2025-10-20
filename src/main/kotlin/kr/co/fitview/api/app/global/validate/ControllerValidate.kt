package kr.co.fitview.api.app.global.validate

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class ControllerValidate(private val validator: LocalValidatorFactoryBean) : Validator {

    override fun supports(clazz: Class<*>): Boolean = true

    override fun validate(target: Any, errors: Errors) {
        if (target is Collection<*>) {
            for (item in target) {
                item?.let {
                    ValidationUtils.invokeValidator(validator, it, errors)
                }
            }
        }
    }
}