package dev.jotxee.secretsanta.security;

import dev.jotxee.secretsanta.entity.Participante;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ParticipanteUserDetails implements UserDetails {

    private final Participante participante;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + participante.getRole()));
    }

    @Override
    public String getPassword() {
        return participante.getPassword();
    }

    @Override
    public String getUsername() {
        return participante.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return participante.getId();
    }

    public Participante getParticipante() {
        return participante;
    }
}

