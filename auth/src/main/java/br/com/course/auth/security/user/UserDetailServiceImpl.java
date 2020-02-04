package br.com.course.auth.security.user;

import br.com.course.core.model.ApplicationUser;
import br.com.course.core.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;

import static org.springframework.security.core.authority.AuthorityUtils.*;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService {

    private final ApplicationUserRepository applicationUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username){
        log.info("Searching Data Base the user by username '{}'", username);
        ApplicationUser applicationUser = applicationUserRepository.findByUsername(username);
        log.info("Application user found '{}'", applicationUser);

        if(applicationUser == null ){
        throw new UsernameNotFoundException(String.format("Application user '%s' not found", username));
        }
        return new CustomUserDetails(applicationUser);
    }

    private static final class CustomUserDetails extends ApplicationUser implements UserDetails {

        CustomUserDetails(@NotNull ApplicationUser user) {
            super(user);
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return commaSeparatedStringToAuthorityList("ROLE_" + this.getRole());
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
