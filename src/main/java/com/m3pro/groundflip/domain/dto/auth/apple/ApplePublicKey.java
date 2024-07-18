package com.m3pro.groundflip.domain.dto.auth.apple;

public record ApplePublicKey(String kty, String kid, String alg, String n, String e) {
}
