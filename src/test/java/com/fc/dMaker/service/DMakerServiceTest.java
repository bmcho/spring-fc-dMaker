package com.fc.dMaker.service;

import com.fc.dMaker.constant.DMakerConstant;
import com.fc.dMaker.dto.CreateDeveloper;
import com.fc.dMaker.entity.Developer;
import com.fc.dMaker.exception.DMakerException;
import com.fc.dMaker.repository.DeveloperRepository;
import com.fc.dMaker.type.DeveloperLevel;
import com.fc.dMaker.type.DeveloperSkillType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.fc.dMaker.code.StatusCode.EMPLOYED;
import static com.fc.dMaker.constant.DMakerConstant.MAX_JUNIOR_EXPERIENCE_YEARS;
import static com.fc.dMaker.constant.DMakerConstant.MIN_SENIOR_EXPERIENCE_YEARS;
import static com.fc.dMaker.exception.DMakerErrorCode.DUPLICATED_MEMBER_ID;
import static com.fc.dMaker.exception.DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED;
import static com.fc.dMaker.type.DeveloperLevel.*;
import static com.fc.dMaker.type.DeveloperSkillType.BACK_END;
import static com.fc.dMaker.type.DeveloperSkillType.FRONT_END;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DMakerServiceTest {

    @Mock
    private DeveloperRepository developerRepository;

    @InjectMocks
    private DMakerService dMakerService;

    private final Developer defaultDeveloper = Developer.builder()
            .developerLevel(SENIOR)
            .developerSkillType(FRONT_END)
            .experienceYears(12)
            .statusCode(EMPLOYED)
            .name("name")
            .age(12)
            .build();

    private CreateDeveloper.Request getCreateRequest(
            DeveloperLevel developerLevel, DeveloperSkillType developerSkillType, Integer experienceYears
    ) {
        return CreateDeveloper.Request.builder()
                .developerLevel(developerLevel)
                .developerSkillType(developerSkillType)
                .experienceYears(experienceYears)
                .memberId("member1")
                .name("test")
                .age(32)
                .build();
    }


    @Test
    void createDeveloperTest_success() {
        //given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.empty());
        given(developerRepository.save(any()))
                .willReturn(defaultDeveloper);
        ArgumentCaptor<Developer> captor =
                ArgumentCaptor.forClass(Developer.class);

        //when
        dMakerService.createDeveloper(getCreateRequest(SENIOR, FRONT_END, MIN_SENIOR_EXPERIENCE_YEARS));

        //then
        verify(developerRepository, times(1))
                .save(captor.capture());
        Developer savedDeveloper = captor.getValue();
        assertEquals(SENIOR, savedDeveloper.getDeveloperLevel());
        assertEquals(FRONT_END, savedDeveloper.getDeveloperSkillType());
        assertEquals(12, savedDeveloper.getExperienceYears());
    }

    @Test
    void createDeveloperTest_fail_with_unmatched_level() {
        //given
        //when
        //then
        DMakerException dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(SENIOR, FRONT_END, MIN_SENIOR_EXPERIENCE_YEARS-1)
                ));

        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());
    }

    @Test
    void createDeveloperTest_failed_with_duplication() {
        //given
        //when
        //then
        DMakerException dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(JUNIOR, FRONT_END,
                                MAX_JUNIOR_EXPERIENCE_YEARS + 1)
                )
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED,
                dMakerException.getDMakerErrorCode()
        );

        dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(JUNGNIOR, FRONT_END,
                                MIN_SENIOR_EXPERIENCE_YEARS + 1)
                )
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED,
                dMakerException.getDMakerErrorCode()
        );

        dMakerException = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(SENIOR, FRONT_END,
                                MIN_SENIOR_EXPERIENCE_YEARS - 1)
                )
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED,
                dMakerException.getDMakerErrorCode()
        );

    }
}