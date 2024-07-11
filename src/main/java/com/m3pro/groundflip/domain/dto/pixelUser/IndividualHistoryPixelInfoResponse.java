package com.m3pro.groundflip.domain.dto.pixelUser;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndividualHistoryPixelInfoResponse {
	private String address;
	private Integer addressNumber;
	private Integer visitCount;
	private List<LocalDateTime> visitList;
}
