package com.m3pro.groundflip.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.DailyPixel;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.scheduler.dto.DailyPixelResponse;
import com.m3pro.groundflip.scheduler.repository.SchedulerDailyPixelRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerUserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyPixelService {
	private final SchedulerDailyPixelRepository schedulerDailyPixelRepository;
	private final SchedulerUserRepository userRepository;

	@Transactional
	@Scheduled(cron = "0 1 0 * * ?")
	public void saveDailyPixelCount() {

		List<DailyPixelResponse> dailyPixelResponseList = schedulerDailyPixelRepository.findDailyPixelByDate(
			LocalDate.now());

		List<DailyPixel> dailyPixelList = new ArrayList<DailyPixel>();

		for (DailyPixelResponse dailyPixelResponse : dailyPixelResponseList) {
			User user = userRepository.getReferenceById(dailyPixelResponse.getUserId());

			DailyPixel dailyPixel = DailyPixel.builder()
				.user(user)
				.dailyPixelCount(dailyPixelResponse.getDailyPixelCount())
				.createdAt(LocalDateTime.now().minusDays(1))
				.build();

			dailyPixelList.add(dailyPixel);
		}

		schedulerDailyPixelRepository.saveAll(dailyPixelList);
		log.info("daily pixel save complete");
	}
}
