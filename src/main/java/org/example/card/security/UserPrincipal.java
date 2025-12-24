package org.example.card.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    private Long userId;
}