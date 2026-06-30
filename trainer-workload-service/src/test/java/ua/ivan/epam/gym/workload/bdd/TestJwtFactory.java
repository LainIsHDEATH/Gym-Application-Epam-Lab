package ua.ivan.epam.gym.workload.bdd;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestJwtFactory {

    private final SecretKey jwtSecretKey;

    public String createServiceToken() {
        return createToken(List.of("ROLE_SERVICE"));
    }

    public String createUserToken() {
        return createToken(List.of("ROLE_USER"));
    }

    private String createToken(List<String> authorities) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("bdd-test")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(300)))
                    .claim("authorities", authorities)
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            jwt.sign(new MACSigner(jwtSecretKey.getEncoded()));

            return jwt.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to generate test JWT",
                    exception
            );
        }
    }
}