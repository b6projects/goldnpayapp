package com.gps.cs.security;

import com.gps.cs.entity.Role;

/** The authenticated caller, taken from a validated access token. */
public record AppPrincipal(Long id, String email, Role role) {}
