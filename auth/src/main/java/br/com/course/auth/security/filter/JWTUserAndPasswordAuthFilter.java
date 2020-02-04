package br.com.course.auth.security.filter;

import br.com.course.core.model.ApplicationUser;
import br.com.course.core.property.JWTConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.shared.Application;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class JWTUserAndPasswordAuthFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authManager;
    private final JWTConfiguration jwtConfiguration;

    @Override
    @SneakyThrows
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response){
        log.info("Attemping authentication . . .");
       ApplicationUser user = new ObjectMapper().readValue(request.getInputStream(), ApplicationUser.class);
        if (user == null)
            throw new UsernameNotFoundException("Unable to retrieve the username or password");
        log.info("Creating the authentication object for the user '{}' and calling  UserDetailServiceImpl loadByUsername", user);
        UsernamePasswordAuthenticationToken usernameAuthToken = new UsernamePasswordAuthenticationToken(
                                                                        user.getUsername(),
                                                                        user.getPassword(),
                                                                        Collections.emptyList());
        usernameAuthToken.setDetails(user);
        return authManager.authenticate(usernameAuthToken);
    }

    @Override
    @SneakyThrows
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        log.info("Authentications was successful for the user '{}' , generating JWE token", authentication.getName());
        SignedJWT signedJWT = createSignedJWT(authentication);
        String encryptedToken = encryptToken(signedJWT);
        log.info("Token generated successfully, adding it to the response header");
        response.addHeader("Access-Control-Expose-Headers","XSRF-TOKEN, "+ jwtConfiguration.getHeader().getName());
        response.addHeader(jwtConfiguration.getHeader().getName(), jwtConfiguration.getHeader().getPrefix() + encryptedToken);
    }

    @SneakyThrows
    private SignedJWT createSignedJWT (Authentication authentication ){
        log.info("Starting to create the signed JWT");
        ApplicationUser applicationUser =  (ApplicationUser) authentication.getPrincipal();
        JWTClaimsSet jwtClaimSet = createJWTClaimSet(authentication,applicationUser);
        KeyPair rsaKeys = generatedKeyPair();
        log.info("Building JWK from the RSA Keys");
        JWK jwk = new RSAKey
                .Builder((RSAPublicKey)rsaKeys
                    .getPublic())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        SignedJWT signedJWT= new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).jwk(jwk).type(JOSEObjectType.JWT).build(), jwtClaimSet);
        log.info("Signing the token with the private RSA key");
        RSASSASigner signer =new RSASSASigner(rsaKeys.getPrivate());
        signedJWT.sign(signer);
        log.info("Serialized token '{}'", signedJWT.serialize());
        return signedJWT;
    }

    private JWTClaimsSet createJWTClaimSet(Authentication authentication, ApplicationUser applicationUser ) {
        log.info("Creating the JWTClaimSet for '{}'", applicationUser);
        return new JWTClaimsSet.Builder()
                    .subject(applicationUser.getUsername())
                    .claim("authorities", authentication.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .issuer("http://academy.devdojo")
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + (jwtConfiguration.getExpiration() * 1000)) )
                    .build();
    }


    @SneakyThrows
    private KeyPair generatedKeyPair() {
            log.info("Generating RSA 2048 bits key");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.genKeyPair();
    }


    private String encryptToken(SignedJWT signedJWT) throws JOSEException {
        log.info("Starting the encryptToken method");
        DirectEncrypter encrypter= new DirectEncrypter(jwtConfiguration.getPrivateKey().getBytes());
        JWEObject jweObject= new JWEObject(new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
        .contentType("JWT")
        .build(),new Payload(signedJWT));
        log.info("Encrypting token with system's private key");
        jweObject.encrypt(encrypter);
        log.info("Token encrypted");
        return jweObject.serialize();

    }
}
