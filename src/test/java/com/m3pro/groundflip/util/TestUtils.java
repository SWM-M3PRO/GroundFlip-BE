package com.m3pro.groundflip.util;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;

public class TestUtils {
	public static void setCreatedAtOfPixelUser(PixelUser pixelUser, LocalDateTime createdAt) {
		try {
			Field createdAtField = BaseTimeEntity.class.getDeclaredField("createdAt");
			createdAtField.setAccessible(true);
			createdAtField.set(pixelUser, createdAt);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
