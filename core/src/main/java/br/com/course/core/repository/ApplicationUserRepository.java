package br.com.course.core.repository;

import br.com.course.core.model.ApplicationUser;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ApplicationUserRepository extends PagingAndSortingRepository<ApplicationUser, Long> {

    ApplicationUser findByUsername(String username);
}
