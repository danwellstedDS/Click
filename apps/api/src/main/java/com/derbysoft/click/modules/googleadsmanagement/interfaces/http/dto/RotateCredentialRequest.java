package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;

public record RotateCredentialRequest(@NotBlank String newCredentialPath) {}
