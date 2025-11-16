package com.immo.service;

import com.immo.model.Utilisateur;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Une implémentation personnalisée de UserDetails qui encapsule notre entité Utilisateur.
 * Cela nous permet d'accéder à des informations supplémentaires, comme l'ID de l'utilisateur,
 * directement depuis l'objet Principal de Spring Security.
 */
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final Utilisateur utilisateur;

    /**
     * La méthode clé qui nous donne accès à l'ID de l'utilisateur.
     * @return L'ID de l'entité Utilisateur.
     */
    public Long getId() {
        return this.utilisateur.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assurez-vous que le rôle dans la base de données est bien préfixé par "ROLE_"
        // ou ajoutez le préfixe ici si ce n'est pas le cas.
        return Collections.singletonList(new SimpleGrantedAuthority(utilisateur.getRole()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getPassword();
    }
    

    @Override
    public String getUsername() {
        // Dans notre application, l'email sert de nom d'utilisateur.
        return utilisateur.getEmail();
    }

    // Pour cet exemple, nous retournons true. En production, vous pourriez
    // avoir des colonnes en base de données pour gérer ces états.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}

