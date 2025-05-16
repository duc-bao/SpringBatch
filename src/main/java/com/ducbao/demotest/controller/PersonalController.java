package com.ducbao.demotest.controller;

import com.ducbao.demotest.config.dataRandom.DataGenerate;
import com.ducbao.demotest.model.Person;
import com.ducbao.demotest.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/per")
public class PersonalController {
    private final PersonRepository personRepository;
    private final DataGenerate dataGenerate;

    public PersonalController(PersonRepository personRepository, DataGenerate dataGenerate) {
        this.personRepository = personRepository;
        this.dataGenerate = dataGenerate;
    }

    @PostMapping()
    public String saveAll(){
        List<Person> people = dataGenerate.generate(100000);
        personRepository.saveAll(people); // lưu toàn bộ
        return "Saved " + people.size() + " persons!";
    }
}
