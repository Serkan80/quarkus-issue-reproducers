package com.acme.gateway;

//import io.quarkus.security.identity.IdentityProviderManager;
//import io.quarkus.security.identity.SecurityIdentity;
//import io.quarkus.security.identity.request.AuthenticationRequest;
//import io.quarkus.smallrye.jwt.runtime.auth.JWTAuthMechanism;
//import io.quarkus.vertx.http.runtime.security.BasicAuthenticationMechanism;
//import io.quarkus.vertx.http.runtime.security.ChallengeData;
//import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
//import io.smallrye.mutiny.Uni;
//import io.vertx.ext.web.RoutingContext;
//import jakarta.annotation.Priority;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.enterprise.inject.Alternative;
//import jakarta.inject.Inject;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Alternative
//@Priority(1)
//@ApplicationScoped
//public class AuthenticationSelector implements HttpAuthenticationMechanism {
//
//    @Inject
//    JWTAuthMechanism jwt;
//
//    @Inject
//    BasicAuthenticationMechanism basic;
//
//    @Override
//    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
//        return selectAuthMechanism(context).authenticate(context, identityProviderManager);
//    }
//
//    @Override
//    public Uni<ChallengeData> getChallenge(RoutingContext context) {
//        return selectAuthMechanism(context).getChallenge(context);
//    }
//
//    @Override
//    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
//        var credentialTypes = new HashSet<Class<? extends AuthenticationRequest>>();
//        credentialTypes.addAll(this.jwt.getCredentialTypes());
//        credentialTypes.addAll(this.basic.getCredentialTypes());
//        return credentialTypes;
//    }
//
//    private HttpAuthenticationMechanism selectAuthMechanism(RoutingContext context) {
//        var uri = context.request().uri();
//        if (uri.contains("/auth/")) {
//            return this.basic;
//        }
//        return this.jwt;
//    }
//}
