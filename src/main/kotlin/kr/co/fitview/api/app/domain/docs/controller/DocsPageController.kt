package kr.co.fitview.api.app.domain.docs.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/api/v1/docs")
class DocsPageController {

    @GetMapping("/login-page")
    fun loginPage(): String {
        return "login"
    }
}