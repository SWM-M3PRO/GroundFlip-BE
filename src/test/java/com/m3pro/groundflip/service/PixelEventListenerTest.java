package com.m3pro.groundflip.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RecordApplicationEvents
public class PixelEventListenerTest {
	@Autowired
	private ApplicationContext applicationContext;
	@MockBean
	private PixelEventListener pixelEventListener;

	@Test
	@Transactional
	@DisplayName("[insertPixelUserHistory] 픽셀 방문 기록 이벤트가 정상적으로 수신되는지 확인")
	void insertPixelUserEventPublisher() {
		PixelUserInsertEvent event = new PixelUserInsertEvent(1L, 1L, null);
		applicationContext.publishEvent(event);

		Mockito.verify(pixelEventListener).insertPixelUserHistory(event);
	}
}
