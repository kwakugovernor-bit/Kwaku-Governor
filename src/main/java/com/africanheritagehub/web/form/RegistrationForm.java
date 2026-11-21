package com.africanheritagehub.web.form;

import com.africanheritagehub.domain.Category;
import com.africanheritagehub.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistrationForm {
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.too.long}")
    private String fullName;

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.email}")
    private String email;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "^(\\+233|0)\\d{9}$", message = "{validation.phone}")
    private String phone;

    @NotBlank(message = "{validation.required}")
    @Size(min = 8, max = 72, message = "{validation.password}")
    private String password;

    @NotBlank(message = "{validation.required}")
    private String region;

    @NotNull(message = "{validation.required}")
    private Role role = Role.CLIENT;

    @AssertTrue(message = "{validation.terms}")
    private boolean acceptTerms;

    /** Only for workers. */
    private Category category;
}
