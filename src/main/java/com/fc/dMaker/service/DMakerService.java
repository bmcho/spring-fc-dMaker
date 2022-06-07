package com.fc.dMaker.service;

import com.fc.dMaker.code.StatusCode;
import com.fc.dMaker.dto.CreateDeveloper;
import com.fc.dMaker.dto.DeveloperDetailDto;
import com.fc.dMaker.dto.DeveloperDto;
import com.fc.dMaker.dto.EditDeveloper;
import com.fc.dMaker.entity.Developer;
import com.fc.dMaker.entity.RetiredDeveloper;
import com.fc.dMaker.exception.DMakerException;
import com.fc.dMaker.repository.DeveloperRepository;
import com.fc.dMaker.repository.RetiredDeveloperRepository;
import com.fc.dMaker.exception.DMakerErrorCode;
import com.fc.dMaker.type.DeveloperLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.fc.dMaker.constant.DMakerConstant.MAX_JUNIOR_EXPERIENCE_YEARS;
import static com.fc.dMaker.constant.DMakerConstant.MIN_SENIOR_EXPERIENCE_YEARS;
import static com.fc.dMaker.exception.DMakerErrorCode.*;
import static com.fc.dMaker.exception.DMakerErrorCode.NO_DEVELOPER;
import static com.fc.dMaker.type.DeveloperLevel.*;
import static com.fc.dMaker.type.DeveloperLevel.SENIOR;

@Service
@RequiredArgsConstructor
@Slf4j
public class DMakerService {
    private final DeveloperRepository developerRepository;
    private final RetiredDeveloperRepository retiredDeveloper;

    //    private final EntityManager em;
    private Developer createDeveloperFromRequest(CreateDeveloper.Request request) {
        return Developer.builder()
                .developerLevel(request.getDeveloperLevel())
                .developerSkillType(request.getDeveloperSkillType())
                .experienceYears(request.getExperienceYears())
                .memberId(request.getMemberId())
                .name(request.getName())
                .age(request.getAge())
                .statusCode(StatusCode.EMPLOYED)
                .build();
    }

    private Developer getUpdatedDeveloperFromRequest(EditDeveloper.Request request, Developer developer) {
        developer.setDeveloperLevel(request.getDeveloperLevel());
        developer.setDeveloperSkillType(request.getDeveloperSkillType());
        developer.setExperienceYears(request.getExperienceYears());

        return developer;
    }

    private void validateCreateDeveloperRequest(@NonNull CreateDeveloper.Request request) {

        request.getDeveloperLevel()
                .validateExperienceYears(request.getExperienceYears());

        developerRepository.findByMemberId(request.getMemberId())
                .ifPresent((developer) -> {
                    throw new DMakerException(DUPLICATED_MEMBER_ID);
                });
    }

    private Developer getDeveloperByMemberId(String memberId) {
        return developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
    }

    /*
       ACID
       Atomic - 원자
       Consistency - 일괄
       Isolation -  고립
       Durability - 영속
    */
    @Transactional
    public CreateDeveloper.Response createDeveloper(CreateDeveloper.Request request) {
        validateCreateDeveloperRequest(request);
        return CreateDeveloper.Response.fromEntity(
                developerRepository.save(createDeveloperFromRequest(request))
        );
    }
//        EntityTransaction transaction = em.getTransaction();
//        try {
//            transaction.begin();
//            transaction.commit();
//        } catch (Exception ex) {
//
//        }

    @Transactional
    public DeveloperDetailDto editDeveloper(String memberId, EditDeveloper.Request request) {
        request.getDeveloperLevel()
                .validateExperienceYears(request.getExperienceYears());

        return DeveloperDetailDto.fromEntity(
                getUpdatedDeveloperFromRequest(request, getDeveloperByMemberId(memberId))
        );
    }


    /*
    Get Method
     */
    @Transactional(readOnly = true)
    public List<DeveloperDto> getAllEmployedDevelopers() {
        return developerRepository.findDevelopersByStatusCodeEquals(StatusCode.EMPLOYED)
                .stream().map(DeveloperDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeveloperDetailDto getDeveloperDetail(String memberId) {
        return DeveloperDetailDto.fromEntity(getDeveloperByMemberId(memberId));
    }

    /*
    Delete
     */
    @Transactional
    public DeveloperDetailDto deleteDeveloper(String memberId) {
        Developer developer = getDeveloperByMemberId(memberId);

        developer.setStatusCode(StatusCode.RETIRED);

        retiredDeveloper.save(RetiredDeveloper.builder()
                .memberId(developer.getMemberId())
                .name(developer.getName())
                .build());

        return DeveloperDetailDto.fromEntity(developer);
    }
}
