package org.weather.app;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import jakarta.servlet.SessionTrackingMode;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Collections;

public class WebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(final ServletContext sc) {
        AnnotationConfigWebApplicationContext ctx =
                new AnnotationConfigWebApplicationContext();
        ctx.register(WebConfig.class, HibernateConfig.class);

        ServletRegistration.Dynamic appServlet =
		        sc.addServlet("mvc", new DispatcherServlet(ctx));
        appServlet.setLoadOnStartup(1);
        appServlet.addMapping("/");

        // нужно для передачи атрибутов
        sc.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
    }
}
