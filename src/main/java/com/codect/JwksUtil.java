package com.codect.etl.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.access.AccessDeniedException;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JwksUtil {
    private static final MLogger LOGGER = MLogger.getLogger(JwksUtil.class);


    private static JWKSet getJWKSet(String key) {
        try {
            return new JWKSet(JWK.parse(key));
        } catch (ParseException e) {
            LOGGER.error("can't parse JWK for TokenTransit", e);
            throw new RuntimeException("can't parse JWK for TokenTransit", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String,Object> sub=new HashMap<>();
        sub.put("userId","1234qwe");
        sub.put("email","user@gmail.com");
        KeyPair keyPair = JwksUtil.generateRSAKey();
        JWK jwk = JwksUtil.generateJWK(keyPair);
        System.out.println(jwk);
        System.out.println(jwk.toPublicJWK());
        JWK parse = JWK.parse(jwk.toString());
        String jwt = createRsaJWT(new HashMap<>(),sub,((RSAKey) parse).toKeyPair());
        System.out.println(jwt);
        System.out.println(JwksUtil.checkRsaJWT(jwt, (RSAPublicKey) keyPair.getPublic()));
    }

    public static KeyPair generateRSAKey() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static JWK generateJWK(KeyPair kp){
        return new RSAKey.Builder((RSAPublicKey)kp.getPublic())
                .privateKey((RSAPrivateKey)kp.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .keyID("moovit_masabi")
                .algorithm(new Algorithm("RS256"))
                .build();
    }

    public static String createRsaJWT(Map<String,Object>headerMap,Map<String,Object> claimsMap,KeyPair keyPair) throws ParseException, JOSEException {
        PrivateKey x=keyPair.getPrivate();
        JWSSigner signer = new RSASSASigner(x);
        JWTClaimsSet claimsSet = JWTClaimsSet.parse(new JSONObject(claimsMap));
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).customParams(headerMap).build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    public static Map<String,Object> checkRsaJWT(String jwt, RSAPublicKey pk){
        Exception e1=null;
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            JWSVerifier verifier = new RSASSAVerifier(pk);
            if (signedJWT.verify(verifier))
                return signedJWT.getJWTClaimsSet().getClaims();
        }catch(Exception e){
            e1=e;
        }
        throw new AccessDeniedException("jwt token failed in verification",e1);
    }

    public static String keyToString(Key kp){
        byte[] encoded = kp.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    public static KeyPair retrieveKeyPairFromString(String priv,String pub) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory rsa2 = KeyFactory.getInstance("RSA");
        PrivateKey rsa = rsa2.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priv)));
        PublicKey rsa1 = rsa2.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pub)));
        return new KeyPair(rsa1,rsa);
    }
}
