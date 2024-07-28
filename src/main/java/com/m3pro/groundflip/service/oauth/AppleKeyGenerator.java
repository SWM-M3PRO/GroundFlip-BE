package com.m3pro.groundflip.service.oauth;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKey;
import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKeyResponse;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppleKeyGenerator {
	private final RestClient restClient;
	@Value("${oauth.apple.url.public-keys}")
	private String applePublicKeysUrl;
	@Value("${oauth.apple.app.keyId}")
	private String kid;
	@Value("${oauth.apple.app.teamId}")
	private String teamId;
	@Value("${oauth.apple.app.id}")
	private String appId;
	@Value("${oauth.apple.app.privateKey}")
	private String privateKey;

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

	/**
	 * apple client secret 을 생성한다.
	 * @return
	 * @throws IOException
	 */
	public String getClientSecret() {
		Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

		return Jwts.builder()
			.setHeaderParam("kid", kid)
			.setHeaderParam("alg", "ES256")
			.setIssuer(teamId)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(expirationDate)
			.setAudience("https://appleid.apple.com")
			.setSubject(appId)
			.signWith(SignatureAlgorithm.ES256, getPrivateKey())
			.compact();
	}

	/**
	 * apple private 키를 반환한다.
	 * @return
	 * @throws IOException
	 */
	private PrivateKey getPrivateKey() {
		try {
			Reader pemReader = new StringReader(privateKey.replace("\\n", "\n"));
			PEMParser pemParser = new PEMParser(pemReader);
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			PrivateKeyInfo object = (PrivateKeyInfo)pemParser.readObject();
			return converter.getPrivateKey(object);
		} catch (IOException e) {
			throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}
}
