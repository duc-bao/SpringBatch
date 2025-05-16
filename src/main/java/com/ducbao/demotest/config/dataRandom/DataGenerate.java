package com.ducbao.demotest.config.dataRandom;

import com.ducbao.demotest.model.Person;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataGenerate {

    public List<Person> generate(int count) {
        List<Person> persons = new ArrayList<Person>();
        for (int i = 0; i < count; i++) {
            persons.add(new Person(null, "Person_" + i));
        }
        return persons;
    }

}
