package kr.co.fitview.api.app.global.advice

import kr.co.fitview.api.app.global.validate.ControllerValidate
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.InitBinder

@ControllerAdvice
class CustomControllerAdvice(
    val validator: LocalValidatorFactoryBean,
) {

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.addValidators(ControllerValidate(validator))
    }
}