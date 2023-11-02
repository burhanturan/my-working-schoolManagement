package com.project.schoolmanagment.security.service;

import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return new UserDetailsImpl(
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    false,
                    user.getPassword(),
                    user.getSsn(),
                    user.getUserRole().getRoleType().name()
            );
        }
        throw new UsernameNotFoundException("User: " + username + " not found");
    }
}
