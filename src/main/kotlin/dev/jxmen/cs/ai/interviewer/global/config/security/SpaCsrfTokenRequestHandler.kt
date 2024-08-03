package dev.jxmen.cs.ai.interviewer.global.config.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.csrf.CsrfTokenRequestHandler
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler
import org.springframework.util.StringUtils
import java.util.function.Supplier

class SpaCsrfTokenRequestHandler : CsrfTokenRequestAttributeHandler() {
    private val delegate: CsrfTokenRequestHandler = XorCsrfTokenRequestAttributeHandler()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        csrfToken: Supplier<CsrfToken>,
    ) {
        /*
         * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
         * the CsrfToken when it is rendered in the response body.
         */
        delegate.handle(request, response, csrfToken)
    }

    override fun resolveCsrfTokenValue(
        request: HttpServletRequest,
        csrfToken: CsrfToken,
    ): String? {
        /*
         * If the request contains a request header, use CsrfTokenRequestAttributeHandler
         * to resolve the CsrfToken. This applies when a single-page application includes
         * the header value automatically, which was obtained via a cookie containing the
         * raw CsrfToken.
         */
        return if (StringUtils.hasText(request.getHeader(csrfToken.headerName))) {
            super.resolveCsrfTokenValue(request, csrfToken)
        } else {
            /*
             * In all other cases (e.g. if the request contains a request parameter), use
             * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
             * when a server-side rendered form includes the _csrf request parameter as a
             * hidden input.
             */
            delegate.resolveCsrfTokenValue(request, csrfToken)
        }
    }
}
