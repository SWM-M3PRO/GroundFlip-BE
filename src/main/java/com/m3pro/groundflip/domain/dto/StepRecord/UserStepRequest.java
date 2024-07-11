package com.m3pro.groundflip.domain.dto.StepRecord;

import java.util.Date;

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
public class UserStepRequest {

	@Schema(description = "user Id", example = "3")
	private Long userId;

	@Schema(description = "시작 날짜", example = "2023-08-02")
	private Date startDate;

	@Schema(description = "종료 날짜", example = "2023-08-09")
	private Date endDate;

}
