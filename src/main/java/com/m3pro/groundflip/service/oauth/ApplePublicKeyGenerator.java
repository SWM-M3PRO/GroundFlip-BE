package com.m3pro.groundflip.service.oauth;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKey;
import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKeyResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplePublicKeyGenerator {
	public PublicKey generatePublicKey(Map<String, String> tokenHeaders,
		ApplePublicKeyResponse applePublicKeys) throws NoSuchAlgorithmException, InvalidKeySpecException {
		ApplePublicKey publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"),
			tokenHeaders.get("alg"));

		return getPublicKey(publicKey);
	}

	private PublicKey getPublicKey(ApplePublicKey publicKey)
		throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
		byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes),
			new BigInteger(1, eBytes));

		KeyFactory keyFactory = KeyFactory.getInstance(publicKey.kty());
		return keyFactory.generatePublic(publicKeySpec);
	}
}