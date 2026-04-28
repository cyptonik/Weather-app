package org.weather.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.weather.app.model.UserSession;
import org.weather.app.service.SessionService;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;

    public SessionInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserSession session = sessionService.getSessionFromCookie(request.getCookies());
        if (!sessionService.isSessionValid(session)) {
            response.sendRedirect(request.getPathInfo() + "/login");
            return false;
        }
        request.setAttribute("currentSession", session);
        return true;
    }
}
