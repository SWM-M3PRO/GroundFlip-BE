package com.m3pro.groundflip.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateUtilsTest {
	@Test
	@DisplayName("[getThisWeekStartDate] 현재 날짜가 속한 주의 시작 날짜를 반환한다.")
	void getThisWeekStartDateTest() {
		LocalDate today = LocalDate.now();

		LocalDate expectedMonday = today.with(DayOfWeek.MONDAY);
		LocalDate actualMonday = DateUtils.getThisWeekStartDate();

		assertEquals(expectedMonday, actualMonday);
	}
}
