package dev.jotxee.secretsanta.config;

import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

@Configuration
public class ErrorConfig {

    @Bean
    public ErrorViewResolver customErrorViewResolver() {
        return (request, status, model) -> {
            if (status != HttpStatus.NOT_FOUND) {
                return null;
            }

            ModelAndView view = new ModelAndView("error/404");
            view.addAllObjects(model);
            view.setStatus(status);
            return view;
        };
    }
}
