package dev.jotxee.secretsanta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class SecretsantaApplicationTests {

	@Autowired
	private ErrorViewResolver errorViewResolver;

	@Test
	void contextLoads() {
	}

	@Test
	void notFoundResolvesCustomJteView() {
		ModelAndView view = errorViewResolver.resolveErrorView(
				new MockHttpServletRequest(),
				HttpStatus.NOT_FOUND,
				Map.of()
		);

		assertNotNull(view);
		assertEquals("error/404", view.getViewName());
		assertEquals(HttpStatus.NOT_FOUND, view.getStatus());
	}

}
