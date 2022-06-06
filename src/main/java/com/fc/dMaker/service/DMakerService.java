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
import com.fc.dMaker.type.DMakerErrorCode;
import com.fc.dMaker.type.DeveloperLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DMakerService {
    private final DeveloperRepository developerRepository;
    private final RetiredDeveloperRepository retiredDeveloper;
//    private final EntityManager em;


    private void validateDeveloperLevel(DeveloperLevel developerLevel, Integer experienceYears) {
        if (developerLevel == DeveloperLevel.SENIOR
                && experienceYears < 10) {
            throw new DMakerException(DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
        }
        if (developerLevel == DeveloperLevel.JUNGNIOR
                && (experienceYears < 4 || experienceYears > 10)) {
            throw new DMakerException(DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
        }
        if (developerLevel == DeveloperLevel.JUNIOR && experienceYears > 4) {
            throw new DMakerException(DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
        }
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

//        EntityTransaction transaction = em.getTransaction();
//        try {
//            transaction.begin();
//            transaction.commit();
//        } catch (Exception ex) {
//
//        }

        Developer developer = Developer.builder()
                .developerLevel(request.getDeveloperLevel())
                .developerSkillType(request.getDeveloperSkillType())
                .experienceYears(request.getExperienceYears())
                .memberId(request.getMemberId())
                .name(request.getName())
                .age(request.getAge())
                .statusCode(StatusCode.EMPLOYED)
                .build();

        developerRepository.save(developer);
        return CreateDeveloper.Response.fromEntity(developer);
    }

    private void validateCreateDeveloperRequest(CreateDeveloper.Request request) {
        developerRepository.findByMemberId(request.getMemberId())
                .ifPresent((developer) -> {
                    throw new DMakerException(DMakerErrorCode.DUPLICATED_MEMBER_ID);
                });

        validateDeveloperLevel(
                request.getDeveloperLevel(),
                request.getExperienceYears());
    }


    @Transactional
    public DeveloperDetailDto editDeveloper(String memberId, EditDeveloper.Request request) {
        validateEditDeveloperRequest(request, memberId);

        Developer developer =  developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(DMakerErrorCode.NO_DEVELOPER));

        developer.setDeveloperLevel(request.getDeveloperLevel());
        developer.setDeveloperSkillType(request.getDeveloperSkillType());
        developer.setExperienceYears(request.getExperienceYears());

        return DeveloperDetailDto.fromEntity(developer);
    }

    private void validateEditDeveloperRequest(EditDeveloper.Request request, String memberId) {
        validateDeveloperLevel(
                request.getDeveloperLevel(),
                request.getExperienceYears());
     }


    /*
    Get Method
     */
    public List<DeveloperDto> getAllEmployedDevelopers() {
        return developerRepository.findDevelopersByStatusCodeEquals(StatusCode.EMPLOYED)
                .stream().map(DeveloperDto::fromEntity)
                .collect(Collectors.toList());
    }

    public DeveloperDetailDto getDeveloperDetail(String memberId) {
        return developerRepository.findByMemberId(memberId)
                .map(DeveloperDetailDto::fromEntity)
                .orElseThrow(() -> new DMakerException(DMakerErrorCode.NO_DEVELOPER));
    }

    /*
    Delete
     */
    @Transactional
    public DeveloperDetailDto deleteDeveloper(String memberId) {
        Developer developer = developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(DMakerErrorCode.NO_DEVELOPER));

        developer.setStatusCode(StatusCode.RETIRED);

        retiredDeveloper.save(RetiredDeveloper.builder()
                .memberId(developer.getMemberId())
                .name(developer.getName())
                .build());

        return DeveloperDetailDto.fromEntity(developer);
    }
}

