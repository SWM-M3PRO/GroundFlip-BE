package com.m3pro.groundflip.repository;


import com.m3pro.groundflip.domain.entity.Pixel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PixelRepositoryTest {
    @Autowired
    PixelRepository pixelRepository;

    @Test
    void save() {
        Pixel save = pixelRepository.save(Pixel.builder()
                .x(1L)
                .y(1L)
                .build());
    }
}
