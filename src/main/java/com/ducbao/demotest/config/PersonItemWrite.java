package com.ducbao.demotest.config;

import com.ducbao.demotest.model.Person;
import com.ducbao.demotest.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class PersonItemWrite implements ItemWriter<Person> {

    private final PersonRepository personRepository;

    public PersonItemWrite(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public void write(Chunk<? extends Person> chunk) throws Exception {
        System.out.println("Writing " + chunk.size() + " items"); // Log kiểm tra
        personRepository.saveAll(chunk.getItems());
    }
}
