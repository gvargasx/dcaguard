package com.dcaguard.security;

import com.dcaguard.entity.PlanType;
import com.dcaguard.entity.User;
import com.dcaguard.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenFilter(UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Attempting to verify Firebase token (length={})", token.length());

            try {
                FirebaseToken decoded = firebaseAuth.verifyIdToken(token);

                String uid = decoded.getUid();
                String email = decoded.getEmail();
                String name = decoded.getName();

                log.debug("Firebase token verified for uid={}, email={}", uid, email);

                PlanType planType = resolvePlanType(decoded);

                User user = userRepository.findByFirebaseUid(uid)
                        .map(existing -> updateUser(existing, email, name, planType))
                        .orElseGet(() -> createUser(uid, email, name, planType));

                AuthenticatedUser authUser = new AuthenticatedUser(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authUser, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("SecurityContext set for user id={}, plan={}", user.getId(), planType);

            } catch (Exception e) {
                log.error("Firebase token verification FAILED: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private PlanType resolvePlanType(FirebaseToken decoded) {
        // 1. Check custom claim first (for Pro users set via admin SDK)
        Object planClaim = decoded.getClaims().get("plan");
        if (planClaim != null) {
            try {
                return PlanType.valueOf(planClaim.toString().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // invalid claim value, fall through
            }
        }

        // 2. Check sign_in_provider to distinguish anonymous vs authenticated
        String signInProvider = getSignInProvider(decoded);
        if ("anonymous".equals(signInProvider)) {
            return PlanType.ANONYMOUS;
        }

        // 3. Default: authenticated user without Pro claim = FREE
        return PlanType.FREE;
    }

    @SuppressWarnings("unchecked")
    private String getSignInProvider(FirebaseToken decoded) {
        try {
            Object firebaseClaim = decoded.getClaims().get("firebase");
            if (firebaseClaim instanceof Map) {
                Object provider = ((Map<String, Object>) firebaseClaim).get("sign_in_provider");
                return provider != null ? provider.toString() : null;
            }
        } catch (Exception e) {
            log.debug("Could not read sign_in_provider from token: {}", e.getMessage());
        }
        return null;
    }

    private User createUser(String uid, String email, String name, PlanType planType) {
        User user = new User(uid, email, name, planType);
        return userRepository.save(user);
    }

    private User updateUser(User user, String email, String name, PlanType planType) {
        boolean changed = false;

        if (!Objects.equals(user.getEmail(), email)) {
            user.setEmail(email);
            changed = true;
        }

        if (!Objects.equals(user.getDisplayName(), name)) {
            user.setDisplayName(name);
            changed = true;
        }

        if (user.getPlanType() != planType) {
            user.setPlanType(planType);
            changed = true;
        }

        return changed ? userRepository.save(user) : user;
    }
}