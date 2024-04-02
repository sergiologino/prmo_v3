package ru.prmo.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class UrlAuthenticationSuccessHandler: AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val authorityNames = authentication!!.authorities.map { it.authority }
        if (authorityNames.contains("ROLE_ADMIN")) {
            response!!.sendRedirect("/admin/panel")
        } else {
            response!!.sendRedirect("/user/workspace")
        }
    }
}