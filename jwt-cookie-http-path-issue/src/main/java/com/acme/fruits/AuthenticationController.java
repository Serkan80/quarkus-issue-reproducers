package com.acme.fruits;

import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.*;
import java.time.OffsetDateTime;
import java.util.Optional;

import static io.smallrye.jwt.util.KeyUtils.loadKeyStore;
import static jakarta.ws.rs.core.NewCookie.SameSite.STRICT;

@Path("/auth")
public class AuthenticationController {

    @Inject
    SecurityIdentity identity;

    @Inject
    JWTParser jwtParser;

    @Min(1)
    @ConfigProperty(name = "rt.expiration.days", defaultValue = "7")
    int expirationDays;

    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String keystoreLocation;

    @ConfigProperty(name = "smallrye.jwt.keystore.password")
    String keystorePassword;

    private KeyStore keystore;

    @PostConstruct
    public void init() {
        try {
            this.keystore = loadKeyStore(this.keystoreLocation, this.keystorePassword, Optional.empty(), Optional.empty());
            Log.debugf("keystore loaded with %d entries", this.keystore.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/token")
    @Authenticated
    public Response accessToken() {
        return Response
                .noContent()
                .cookie(cookie("access_token", generateAccessToken()), cookie("refresh_token", generateRefreshToken()))
                .build();
    }

    @POST
    @PermitAll
    @Path("/refresh")
    public Response refreshToken(@NotBlank String refreshToken) {
        try {
            this.jwtParser.verify(refreshToken, this.keystore.getCertificate("rt").getPublicKey());
        } catch (ParseException | GeneralSecurityException e) {
            throw new WebApplicationException("Invalid or expired refreshToken", e, 401);
        }

        return accessToken();
    }

    private NewCookie cookie(String name, String value) {
        return new NewCookie.Builder(name)
//              .secure(true)
                .sameSite(STRICT)
                .httpOnly(true)
                .path("/")
                .value(value)
                .build();
    }

    /*
     * The keypair, issuer, audience & exp. time, are all configured in application.properties.
     */
    private String generateAccessToken() {
        return Jwt.upn(this.identity.getPrincipal().getName())
                .subject(this.identity.getPrincipal().getName())
                .groups(this.identity.getRoles())
                .sign();
    }

    private String generateRefreshToken() {
        try {
            var privateKey = (PrivateKey) this.keystore.getKey("rt", this.keystorePassword.toCharArray());
            return Jwt.upn(this.identity.getPrincipal().getName())
                    .expiresAt(OffsetDateTime.now().plusDays(this.expirationDays).toInstant())
                    .sign(privateKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new WebApplicationException(e);
        }
    }
}
