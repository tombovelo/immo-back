package com.immo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// import java.util.Collections;

// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.User;

import com.immo.model.Utilisateur;
import com.immo.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // oblige injection par constructeur
    private final UtilisateurRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("utilisateur non trouver : " + email);
        }
        // return new User(
        //     user.getEmail(), 
        //     user.getPassword(), 
        //     Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        // );
        return new UserDetailsImpl(user);
    } 
    
}
