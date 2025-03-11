package com.github.lzpdark.boardservice.controller;

import com.github.lzpdark.boardservice.exception.CustomAuthenticationException;
import com.github.lzpdark.boardservice.exception.RegisterException;
import com.github.lzpdark.boardservice.mapper.AnonymousUserMapper;
import com.github.lzpdark.boardservice.mapper.BoardMapper;
import com.github.lzpdark.boardservice.mapper.TaskMapper;
import com.github.lzpdark.boardservice.model.AnonymousUser;
import com.github.lzpdark.boardservice.model.BoardModel;
import com.github.lzpdark.boardservice.model.TaskModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
@CrossOrigin
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsManager userDetailsManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    AnonymousUserMapper anonymousUserMapper;

    private final List<String> taskSamples = List.of(
            "阅读",
            "早睡晚起",
            "吃饭",
            "找工作"
    );

    private void setBasicSessionInfo(HttpSession session) {
        session.setMaxInactiveInterval(60 * 60 * 24 * 30);// 30days
        session.setAttribute("version", "1");
    }

    @Data
    public static class UserInformation {
        private String name;
    }

    @PostMapping("/anonymous")
    public Object anonymous(HttpServletRequest request, HttpServletResponse response) {

        // create anonymous user
        AnonymousUser anonymousUser = new AnonymousUser();
        anonymousUser.setCreatedTime(new Date(System.currentTimeMillis()));
        anonymousUserMapper.createAnonymousUser(anonymousUser);
        // create user bound to anonymous user
        UserDetails user = registerUser("游客-" + anonymousUser.getId(), "******");
        BoardModel boardModel = createBoard(user.getUsername());

        // create fake task as sample in this created board
        for (int idx = 0; idx < taskSamples.size(); idx++) {
            TaskModel model = TaskModel.create(boardModel.getId(), idx, taskSamples.get(idx));
            taskMapper.createModel(model);
        }

        // authenticate anonymous user
        authenticateUser(request, response,
                user.getUsername(), "******");

        UserInformation userInformation = new UserInformation();
        userInformation.setName(user.getUsername());
        return userInformation;
    }

    public record RegisterRequest(String username,
                                  String email,
                                  String password,
                                  String confirmPassword) {
    }

    public record RegisterResponse(String username) {
    }

    @PostMapping("/register")
    public Object register(@RequestBody RegisterRequest payload) throws RegisterException {
        if (!StringUtils.hasLength(payload.username()) ||
                payload.username().startsWith("游客-") ||
                userDetailsManager.userExists(payload.username())) {
            throw new RegisterException("conflict-username", Map.of(
                    "username", "用户名已经被注册"
            ));
        }

        registerUser(payload.username(), payload.password());
        createBoard(payload.username());

        return new RegisterResponse(payload.username());
    }

    private UserDetails registerUser(String username, String password) {
        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER") // Default role
                .build();
        userDetailsManager.createUser(user);
        return user;


    }

    private BoardModel createBoard(String username) {
        // create board for user
        BoardModel boardModel = new BoardModel();
        boardModel.setUsername(username);
        boardModel.setDescription("board for user " + username);
        boardModel.setCreatedTime(new Date(System.currentTimeMillis()));
        boardMapper.createBoard(boardModel);
        return boardModel;
    }

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    @Autowired
    SessionRegistry sessionRegistry;
    @Autowired
    SessionAuthenticationStrategy sessionAuthenticationStrategy;
    @Autowired
    RememberMeServices rememberMeServices;

    public record LoginRequest(String username, String password) {
    }

    @PostMapping("/login")
    public Object login(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestBody LoginRequest payload,
                        @RequestParam(value = "remember-me", defaultValue = "false") boolean rememberMe) {

        String username = payload.username();
        if(!StringUtils.hasLength(username) || username.startsWith("游客-")) {
            throw new CustomAuthenticationException("invalid user");
        }

        Authentication authenticate = authenticateUser(request, response,
                payload.username(), payload.password());

        // remember me logic
        if (rememberMe) {
            rememberMeServices.loginSuccess(request, response, authenticate);
        }

        UserInformation userInformation = new UserInformation();
        userInformation.setName(username);
        return ResponseEntity.ok(userInformation);
    }

    private Authentication authenticateUser(HttpServletRequest request, HttpServletResponse response,
                       String username, String password) {
        UsernamePasswordAuthenticationToken token =
                UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        Authentication authenticate =
                this.authenticationManager.authenticate(token);
        if (authenticate == null || !authenticate.isAuthenticated()) {
            throw new CustomAuthenticationException("not authenticated");
        }

        // authenticated successfully, invalid other session
        sessionAuthenticationStrategy.onAuthentication(authenticate, request, response);

        // save security context
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authenticate);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        HttpSession session = request.getSession();
        sessionRegistry.registerNewSession(session.getId(), authenticate.getPrincipal());
        return authenticate;
    }

    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    @PostMapping("/logout")
    public Object logout(Authentication authentication,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices = (PersistentTokenBasedRememberMeServices) rememberMeServices;
        persistentTokenBasedRememberMeServices.logout(request, response, authentication);
        return List.of();
    }

}
