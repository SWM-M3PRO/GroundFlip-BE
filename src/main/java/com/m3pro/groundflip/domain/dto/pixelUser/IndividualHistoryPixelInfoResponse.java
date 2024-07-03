package com.m3pro.groundflip.domain.dto.pixelUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndividualHistoryPixelInfoResponse {
    private String address;
    private int addressNumber;
    private int visitCount;
    private List<LocalDateTime> visitList;
}
