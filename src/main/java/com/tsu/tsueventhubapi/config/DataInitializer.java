package com.tsu.tsueventhubapi.config;

import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmail("dean_office@gmail.com").isEmpty()) {
                User deanOffice = User.builder()
                        .name("Деканат")
                        .email("dean_office@gmail.com")
                        .password(new BCryptPasswordEncoder().encode("dean_office123"))
                        .role(Role.DEAN)
                        .status(Status.APPROVED)
                        .build();
                userRepository.save(deanOffice);
            }
        };
    }
}
