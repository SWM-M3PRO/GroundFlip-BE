package com.m3pro.groundflip.domain.dto.StepRecord;

import com.m3pro.groundflip.domain.entity.StepRecord;
import com.m3pro.groundflip.domain.entity.User;

import java.sql.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "개인 걸음수 정보")
public class UserStepInfo {

	@Schema(description = "걸음수 id", example = "4")
	Long id;

	@Schema(description = "걸음수 기록 날짜", example = "2024-07-05 12:53:11")
	private Date date;

	@Schema(description = "걸음수", example = "1557")
	private Integer steps;

	@Schema(description = "유저id", example = "3")
	private User user;


	public static StepRecord of(UserStepInfo userStepInfo) {
		return StepRecord.builder()
			.date(userStepInfo.getDate())
			.steps(userStepInfo.getSteps())
			.user(userStepInfo.getUser())
			.build();
	}
}
