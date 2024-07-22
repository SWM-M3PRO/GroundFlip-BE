package com.m3pro.groundflip.service.oauth;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKey;
import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKeyResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplePublicKeyGenerator {
	private final RestClient restClient;
	@Value("${oauth.apple.url.public-keys}")
	private String applePublicKeysUrl;

	/**
	 * identity token을 검증할 public key를 반환한다.
	 * @param tokenHeaders identity token 헤더
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public PublicKey getPublicKey(Map<String, String> tokenHeaders) throws
		NoSuchAlgorithmException,
		InvalidKeySpecException {
		ApplePublicKeyResponse applePublicKeys = getAppleAuthPublicKey();
		ApplePublicKey publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"),
			tokenHeaders.get("alg"));

		return generatePublicKey(publicKey);
	}

	/**
	 * 애플의 퍼블릭키를 가져온다.
	 * @return 애플의 퍼블릭키
	 */
	private ApplePublicKeyResponse getAppleAuthPublicKey() {
		return restClient.get()
			.uri(applePublicKeysUrl)
			.retrieve()
			.body(ApplePublicKeyResponse.class);
	}

	/**
	 * jwk 로 퍼블릭키를 생성한다.
	 * @param publicKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private PublicKey generatePublicKey(ApplePublicKey publicKey)
		throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
		byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes),
			new BigInteger(1, eBytes));

		KeyFactory keyFactory = KeyFactory.getInstance(publicKey.kty());
		return keyFactory.generatePublic(publicKeySpec);
	}
}
