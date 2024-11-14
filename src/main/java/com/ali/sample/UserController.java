package com.ali.sample;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.security.PermitAll;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService registerService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return registerService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (Exception e) {
            throw new Exception("Invalid credentials", e);
        }
        return createLoginToken(authRequest.getUsername());
    }

    private String createLoginToken(String userName) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        Map<String, Object> claims = new HashMap<>();
        //org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(userDetails.getUsername(),userDetails.getPassword(),getAuthorities(userDetails));

        Collection<? extends GrantedAuthority> authorities = getAuthorities(userDetails.getAuthorities());
        authorities.forEach(authority -> System.out.println(authority.getAuthority())); // Debugging line

        claims.put("roles", authorities);
        return jwtTokenUtil.createToken(claims, userDetails.getUsername());
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<? extends GrantedAuthority> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }

    @PostMapping("/facebook")
    @PermitAll
    public ResponseEntity<?> registerWithFacebook(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        // Verify the access token with Facebook
        String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        try {
            JSONObject userInfo = new JSONObject(response.getBody());
            String facebookId = userInfo.getString("id");
            String email = userInfo.optString("email", "");
            // Implement your registration/login logic here
            Optional<User> userOptional = this.registerService.findByUsername(facebookId);
            User user = null;
            if(userOptional.isEmpty()){
                user = new User();
                user.setUsername(facebookId);
                user.setEmail(email);
                user.setPassword("none");
                user = this.registerService.registerUser(user);
            }else{
                user = userOptional.get();
            }
            return ResponseEntity.ok(this.createLoginToken(user.getUsername()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid Facebook token");
        }
    }

}

class AuthRequest {
    private String username;
    private String password;
    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
