package com.m3pro.groundflip.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

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

	@Test
	@DisplayName("[isDateInCurrentWeek]")
	public void testIsDateInCurrentWeek() {
		LocalDate today = LocalDate.now();

		// 테스트 케이스 1: 오늘 날짜 (이번 주에 속해야 함)
		assertTrue(DateUtils.isDateInCurrentWeek(today), "Today's date should be in the current week");

		// 테스트 케이스 2: 이번 주의 다른 날짜 (월요일로 설정)
		LocalDate thisMonday = today.with(java.time.DayOfWeek.MONDAY);
		assertTrue(DateUtils.isDateInCurrentWeek(thisMonday), "This Monday should be in the current week");

		// 테스트 케이스 3: 이번 주의 다른 날짜 (일요일로 설정)
		LocalDate thisSunday = today.with(java.time.DayOfWeek.SUNDAY);
		assertTrue(DateUtils.isDateInCurrentWeek(thisSunday), "This Sunday should be in the current week");

		// 테스트 케이스 4: 지난 주의 날짜 (현재 주에 속하지 않아야 함)
		LocalDate lastWeek = today.minusWeeks(1);
		assertFalse(DateUtils.isDateInCurrentWeek(lastWeek), "A date from last week should not be in the current week");

		// 테스트 케이스 5: 다음 주의 날짜 (현재 주에 속하지 않아야 함)
		LocalDate nextWeek = today.plusWeeks(1);
		assertFalse(DateUtils.isDateInCurrentWeek(nextWeek), "A date from next week should not be in the current week");
	}

	@Test
	@DisplayName("[getWeekOfDate]")
	public void testGetWeekOfDate() {
		// 테스트 케이스 1: 오늘 날짜
		LocalDate today = LocalDate.now();
		WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
		int expectedWeekOfYear = today.get(weekFields.weekOfWeekBasedYear());
		assertEquals(expectedWeekOfYear, DateUtils.getWeekOfDate(today), "Week number of today's date should match");

		// 테스트 케이스 2: 특정 날짜 (2024-01-01, 첫 주)
		LocalDate date1 = LocalDate.of(2024, 1, 1);
		int expectedWeek1 = date1.get(weekFields.weekOfWeekBasedYear());
		assertEquals(expectedWeek1, DateUtils.getWeekOfDate(date1), "Week number of 2024-01-01 should match");

		// 테스트 케이스 3: 특정 날짜 (2024-07-15, 중간 주)
		LocalDate date2 = LocalDate.of(2024, 7, 15);
		int expectedWeek2 = date2.get(weekFields.weekOfWeekBasedYear());
		assertEquals(expectedWeek2, DateUtils.getWeekOfDate(date2), "Week number of 2024-07-15 should match");

		// 테스트 케이스 4: 특정 날짜 (2024-12-31, 마지막 주)
		LocalDate date3 = LocalDate.of(2024, 12, 31);
		int expectedWeek3 = date3.get(weekFields.weekOfWeekBasedYear());
		assertEquals(expectedWeek3, DateUtils.getWeekOfDate(date3), "Week number of 2024-12-31 should match");

		// 테스트 케이스 5: 특정 날짜 (윤년인 2020-02-29)
		LocalDate date4 = LocalDate.of(2020, 2, 29);
		int expectedWeek4 = date4.get(weekFields.weekOfWeekBasedYear());
		assertEquals(expectedWeek4, DateUtils.getWeekOfDate(date4), "Week number of 2020-02-29 should match");
	}
}
