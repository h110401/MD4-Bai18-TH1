package rikkei.academy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rikkei.academy.dto.request.SignInDTO;
import rikkei.academy.dto.request.SignUpDTO;
import rikkei.academy.dto.response.JwtResponse;
import rikkei.academy.dto.response.ResponseMessage;
import rikkei.academy.model.Role;
import rikkei.academy.model.RoleName;
import rikkei.academy.model.User;
import rikkei.academy.security.jwt.JwtProvider;
import rikkei.academy.security.userprincipal.UserPrincipal;
import rikkei.academy.service.IRoleService;
import rikkei.academy.service.IUserService;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> register(
            @Valid
            @RequestBody
            SignUpDTO signUpDTO
    ) {
        if (userService.existsByUsername(signUpDTO.getUsername())) {
            return new ResponseEntity<>(new ResponseMessage("Username existed!"), HttpStatus.OK);
        }
        if (userService.existsByEmail(signUpDTO.getEmail())) {
            return new ResponseEntity<>(new ResponseMessage("Email existed!"), HttpStatus.OK);
        }
        User user = new User(
                signUpDTO.getName(),
                signUpDTO.getUsername(),
                signUpDTO.getEmail(),
                passwordEncoder.encode(signUpDTO.getPassword())
        );
        Set<Role> roles = signUpDTO.getRoles().stream().map(role -> roleService.findByName(RoleName.valueOf(role.toUpperCase())).orElseThrow(
                () -> new RuntimeException("Role not valid!")
        )).collect(Collectors.toSet());

        user.setRoles(roles);

        userService.save(user);
        return new ResponseEntity<>(new ResponseMessage("Create user success"), HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(
            @Valid
            @RequestBody
            SignInDTO signInDTO
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInDTO.getUsername(), signInDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.createToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(token, userPrincipal.getName(), userPrincipal.getAuthorities()));
    }
}
