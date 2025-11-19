package dev.jotxee.secretsanta.security;

import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipanteUserDetailsService implements UserDetailsService {

    private final ParticipanteRepository participanteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return participanteRepository.findByEmail(email)
            .map(ParticipanteUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}

