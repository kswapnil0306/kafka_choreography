package com.untitled.service;

import com.untitled.model.Greeting;
import com.untitled.repository.GreetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GreetingService {

    private final GreetingRepository greetingRepository;

    public GreetingService(GreetingRepository greetingRepository) {
        this.greetingRepository = greetingRepository;
    }

    public List<Greeting> findAll() {
        return greetingRepository.findAll();
    }

    @Transactional
    public Greeting create(String message) {
        return greetingRepository.save(new Greeting(message));
    }
}
