package com.ducbao.demotest.config;

import com.ducbao.demotest.model.Person;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PersonItemProcess implements ItemProcessor<Person, Person> {
    @Override
    public Person process(Person item) throws Exception {
        item.setName(item.getName().toLowerCase());
        return item;
    }
}
