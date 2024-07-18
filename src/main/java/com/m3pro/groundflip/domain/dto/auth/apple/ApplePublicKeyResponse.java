package com.m3pro.groundflip.domain.dto.auth.apple;

import java.util.List;

import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

public record ApplePublicKeyResponse(List<ApplePublicKey> keys) {

	public ApplePublicKey getMatchedKey(String kid, String alg) throws AppException {
		return keys.stream()
			.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
			.findAny()
			.orElseThrow(() -> new AppException(ErrorCode.INVALID_JWT));
	}
}
