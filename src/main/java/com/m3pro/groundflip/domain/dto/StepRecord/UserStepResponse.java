package com.m3pro.groundflip.domain.dto.StepRecord;

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
@Schema(title = "개인 걸음수 정보 response")
public class UserStepResponse {

	@Schema(description = "불러온 걸음수", example = "1557")
	private Integer steps;

	public UserStepResponse from(StepRecord stepRecord) {
		return UserStepResponse.builder()
			.steps(stepRecord.getSteps())
			.build();
	}

}
