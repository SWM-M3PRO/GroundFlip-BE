package com.m3pro.groundflip.domain.dto.StepRecord;

import java.sql.Date;

import com.m3pro.groundflip.domain.entity.StepRecord;

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

	@Schema(description = "걸음수 기록 날짜", example = "2024-07-05")
	private Date date;

	@Schema(description = "걸음수", example = "1557")
	private Integer steps;

	@Schema(description = "유저id", example = "3")
	private Long userId;

}
