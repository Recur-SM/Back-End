package com.seolstudy.backend.domain.subject;

import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SubjectInitializer implements ApplicationRunner {

    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (subjectRepository.count() > 0) {
            return;
        }

        subjectRepository.save(Subject.builder()
                .subjectName("국어")
                .subjectCode("KOR")
                .build());

        subjectRepository.save(Subject.builder()
                .subjectName("영어")
                .subjectCode("ENG")
                .build());

        subjectRepository.save(Subject.builder()
                .subjectName("수학")
                .subjectCode("MATH")
                .build());
    }
}
