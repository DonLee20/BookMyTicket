package com.bookmyticket.identity;

import com.bookmyticket.common.ApiException;
import com.bookmyticket.security.AppUserDetails;
import com.bookmyticket.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @Pattern(regexp = "^$|^[0-9+\\- ]{6,20}$", message = "Enter a valid phone number.") String phone
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be at least 8 characters.") String newPassword
    ) {
    }

    public record ProfileResponse(Long id, String name, String email, String phone) {
    }

    @GetMapping
    public ProfileResponse get() {
        User user = currentUser();
        return new ProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }

    @PutMapping
    public ProfileResponse update(@Valid @RequestBody UpdateProfileRequest request) {
        User user = currentUser();
        user.setName(request.name().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        return toResponse(userRepository.save(user));
    }

    @PostMapping("/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("INVALID_PASSWORD", "Your current password is not correct.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User currentUser() {
        AppUserDetails principal = SecurityUtils.requireUser();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.unauthorized("Account not found."));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }
}
