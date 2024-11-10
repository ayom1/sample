package com.ali.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service  // Ensure this annotation is present
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        List<UserRole> roleList = this.userRoleRepository.findByUserId(user.getId());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                roleList.stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("user_role"))
                        .collect(Collectors.toList())
        );
    }
}
