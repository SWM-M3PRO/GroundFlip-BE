package com.m3pro.groundflip.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static boolean isDateInCurrentWeek(LocalDate date) {
		return getWeekOfDate(LocalDate.now()) == getWeekOfDate(date);
	}

	public static int getWeekOfDate(LocalDate date) {
		WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
		return date.get(weekFields.weekOfWeekBasedYear());
	}

	public static LocalDate getThisWeekStartDate() {
		LocalDate today = LocalDate.now();
		return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}

	/*
	 * 시, 분, 초를 0으로 하고 년,월,일 만 구하기
	 * @Param input Date
	 * @return result Date
	 * */
	public static Date truncateTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
