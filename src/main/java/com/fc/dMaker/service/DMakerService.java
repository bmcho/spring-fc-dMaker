package com.fc.dMaker.service;

import com.fc.dMaker.entity.Developer;
import com.fc.dMaker.repository.DeveloperRepository;
import com.fc.dMaker.type.DeveloperLevel;
import com.fc.dMaker.type.DeveloperSkillType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DMakerService {
    private final DeveloperRepository developerRepository;

    /*
        ACID
        Atomic - 원자
        Consistency - 일괄
        Isolation -  고립
        Durability - 영속
     */
    @Transactional
    public void createDeveloper() {
        Developer developer = Developer.builder()
                .developerLevel(DeveloperLevel.JUNIOR)
                .developerSkillType(DeveloperSkillType.FRONT_END)
                .experienceYears(2)
                .name("Olaf")
                .age(5)
                .build();

        developerRepository.save(developer);
    }
}
