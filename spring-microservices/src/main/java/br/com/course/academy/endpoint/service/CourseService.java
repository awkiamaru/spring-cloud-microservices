package br.com.course.academy.endpoint.service;

import br.com.course.core.model.Course;
import br.com.course.core.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourseService {

    private final CourseRepository repository;

    public Iterable<Course> listAll(Pageable pageable) {
        log.info("List All courses");
        return repository.findAll(pageable);
    }
}
