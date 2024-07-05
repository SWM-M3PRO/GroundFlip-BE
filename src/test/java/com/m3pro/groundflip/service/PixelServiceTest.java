package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelCount;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;

@ExtendWith(MockitoExtension.class)
class PixelServiceTest {
    @Mock
    private PixelRepository pixelRepository;
    @Mock
    private PixelUserRepository pixelUserRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private PixelService pixelService;

    @BeforeEach
    void init() {
        reset(pixelRepository);
        reset(pixelUserRepository);
        reset(userRepository);
    }

    @Test
    @DisplayName("[getIndividualPixelInfo] 없는 pixelId 를 넣을 경우 PIXEL_NOT_FOUND 에러")
    void getIndividualPixelInfoPixelNotFound() {
        // Given
        Long pixelId = 1L;
        when(pixelRepository.findById(pixelId)).thenReturn(Optional.empty());

        // When
        AppException exception = assertThrows(AppException.class, () -> pixelService.getIndividualPixelInfo(pixelId));

        // Then
        assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("[getIndividualPixelInfo] 정상적으로 픽셀에 대한 정보가 있는 경우")
    void getIndividualPixelInfoSuccess() {
        // Given
        Long pixelId = 1L;
        String address = "서울";
        int addressNumber = 1;

        Pixel pixel = Pixel.builder()
                .id(pixelId)
                .address(address)
                .addressNumber(addressNumber)
                .build();

        List<VisitedUser> visitedUsers = List.of(
                new VisitedUser() {
                    @Override
                    public Long getPixelId() {
                        return pixelId;
                    }

                    @Override
                    public Long getUserId() {
                        return 100L;
                    }

                    @Override
                    public String getNickname() {
                        return "JohnDoe";
                    }

                    @Override
                    public String getProfileImage() {
                        return "http://profileImage.png";
                    }
                }
        );
        PixelOwnerUser pixelOwnerUser = new PixelOwnerUser() {
            @Override
            public Long getUserId() {
                return 100L;
            }

            @Override
            public String getNickname() {
                return "JohnDoe";
            }

            @Override
            public String getProfileImage() {
                return "profileImage.png";
            }
        };
        PixelCount accumulatePixelCount = new PixelCount() {
            @Override
            public int getCount() {
                return 10;
            }
        };
        PixelCount currentPixelCount = new PixelCount() {
            @Override
            public int getCount() {
                return 5;
            }
        };

        when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
        when(pixelUserRepository.findAllVisitedUserByPixelId(pixelId)).thenReturn(visitedUsers);
        when(pixelUserRepository.findCurrentOwnerByPixelId(pixelId)).thenReturn(pixelOwnerUser);
        when(pixelUserRepository.findAccumulatePixelCountByUserId(pixelOwnerUser.getUserId())).thenReturn(
                accumulatePixelCount);
        when(pixelUserRepository.findCurrentPixelCountByUserId(pixelOwnerUser.getUserId())).thenReturn(
                currentPixelCount);

        // When
        IndividualPixelInfoResponse response = pixelService.getIndividualPixelInfo(pixelId);

        // Then
        assertThat(response.getAddress()).isEqualTo(address);
        assertThat(response.getAddressNumber()).isEqualTo(addressNumber);
        assertThat(response.getVisitCount()).isEqualTo(visitedUsers.size());
        assertThat(response.getVisitList().get(0).getNickname()).isEqualTo("JohnDoe");
        assertThat(response.getPixelOwnerUser().getCurrentPixelCount()).isEqualTo(currentPixelCount.getCount());
        assertThat(response.getPixelOwnerUser().getNickname()).isEqualTo(pixelOwnerUser.getNickname());
    }

    @Test
    @DisplayName("[getIndividualPixelInfo] pixelId에 해당하는 픽셀에 방문한 사람이 없는 경우")
    void getIndividualPixelInfoNoVisitedUser() {
        // Given
        Long pixelId = 1L;
        String address = "서울";
        int addressNumber = 1;

        Pixel pixel = Pixel.builder()
                .id(pixelId)
                .address(address)
                .addressNumber(addressNumber)
                .build();

        List<VisitedUser> visitedUsers = List.of();
        PixelOwnerUser pixelOwnerUser = null;

        when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
        when(pixelUserRepository.findAllVisitedUserByPixelId(pixelId)).thenReturn(visitedUsers);
        when(pixelUserRepository.findCurrentOwnerByPixelId(pixelId)).thenReturn(pixelOwnerUser);

        // When
        IndividualPixelInfoResponse response = pixelService.getIndividualPixelInfo(pixelId);

        // Then
        assertThat(response.getAddress()).isEqualTo(address);
        assertThat(response.getAddressNumber()).isEqualTo(addressNumber);
        assertThat(response.getVisitCount()).isEqualTo(0);
        assertThat(response.getPixelOwnerUser()).isNull();
    }

    @Test
    @DisplayName("[getIndividualHistoryPixelInfo] pixel history들이 정렬되어 오는지 확인")
    void getIndividualHistoryPixelInfoOrderBy() {
        final int NUMBER_OF_HISTORY = 3;
        // Given
        Long pixelId = 10000L;
        String address = "은평구";
        int addressNumber = 1;

        Pixel pixel = Pixel.builder()
                .id(pixelId)
                .address(address)
                .addressNumber(addressNumber)
                .build();

        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        List<PixelUser> visitHistory = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_HISTORY; i++) {
            PixelUser pixelUser = PixelUser.builder()
                    .user(user)
                    .pixel(pixel)
                    .build();
            TestUtils.setCreatedAtOfPixelUser(pixelUser, LocalDateTime.now().minusSeconds(i));
            visitHistory.add(pixelUser);
        }

        // When
        when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
        when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user)).thenReturn(visitHistory);
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        // Then
        IndividualHistoryPixelInfoResponse response = pixelService.getIndividualHistoryPixelInfo(pixelId, userId);

        assertEquals(visitHistory.size(), response.getVisitList().size());
        for (int i = 0; i < NUMBER_OF_HISTORY; i++) {
            assertEquals(visitHistory.get(i).getCreatedAt(), response.getVisitList().get(i));
        }
    }

    @Test
    @DisplayName("[getIndividualHistoryPixelInfo] 여러 유저들 중 요청을 보낸 유저만 반환하는지 확인")
    void getIndividualHistoryPixelInfoMultipleUser() {
        final int NUMBER_OF_HISTORY_PER_USER = 2;

        // Given
        Long pixelId = 10000L;
        String address = "은평구";
        int addressNumber = 1;

        Pixel pixel = Pixel.builder()
                .id(pixelId)
                .address(address)
                .addressNumber(addressNumber)
                .build();

        Long userId1 = 1L;
        Long userId2 = 2L;

        User user1 = User.builder()
                .id(userId1)
                .build();

        User user2 = User.builder()
                .id(userId2)
                .build();

        List<PixelUser> visitHistoryUser1 = new ArrayList<>();
        List<PixelUser> visitHistoryUser2 = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_HISTORY_PER_USER; i++) {
            PixelUser pixelUser1 = PixelUser.builder()
                    .user(user1)
                    .pixel(pixel)
                    .build();
            TestUtils.setCreatedAtOfPixelUser(pixelUser1, LocalDateTime.now().minusSeconds(i));
            visitHistoryUser1.add(pixelUser1);

            PixelUser pixelUser2 = PixelUser.builder()
                    .user(user2)
                    .pixel(pixel)
                    .build();
            TestUtils.setCreatedAtOfPixelUser(pixelUser2, LocalDateTime.now().minusDays(i));
            visitHistoryUser2.add(pixelUser2);
        }

        // When
        when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
        when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user1)).thenReturn(visitHistoryUser1);
        when(userRepository.getReferenceById(userId1)).thenReturn(user1);

        // Then
        IndividualHistoryPixelInfoResponse response = pixelService.getIndividualHistoryPixelInfo(pixelId, userId1);

        assertEquals(visitHistoryUser1.size(), response.getVisitList().size());
        for (int i = 0; i < NUMBER_OF_HISTORY_PER_USER; i++) {
            assertEquals(visitHistoryUser1.get(i).getCreatedAt(), response.getVisitList().get(i));
        }
    }

    @Test
    @DisplayName("[getIndividualHistoryPixelInfo] 없는 pixelId 를 넣을 경우 PIXEL_NOT_FOUND 에러")
    void getIndividualHistoryPixelInfoNotFound() {
        // Given
        Long pixelId = 1L;
        when(pixelRepository.findById(pixelId)).thenReturn(Optional.empty());

        // When
        AppException exception = assertThrows(AppException.class, () -> pixelService.getIndividualPixelInfo(pixelId));

        // Then
        assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());
    }
}
