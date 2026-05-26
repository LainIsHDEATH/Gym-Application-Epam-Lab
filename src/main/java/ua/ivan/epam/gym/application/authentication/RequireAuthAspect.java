package ua.ivan.epam.gym.application.authentication;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireAuthAspect {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final HttpServletRequest request;
    private final AuthService authService;
    private final BasicAuthParser basicAuthParser;

    @Before("@annotation(ua.ivan.epam.gym.application.authentication.RequireAuth)")
    public void authenticate() {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        BasicAuthCredentials credentials = basicAuthParser.parse(authorizationHeader);

        authService.authenticate(
                credentials.username(),
                credentials.password()
        );
    }
}
