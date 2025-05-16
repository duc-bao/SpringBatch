package com.ducbao.demotest.config;

import com.ducbao.demotest.config.dataRandom.DataGenerate;
import com.ducbao.demotest.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class PersonItemReader implements ItemReader<Person> {
    private final DataGenerate dataGenerate;
    private Iterator<Person> personIterator;
    private final int count = 10000;

    public PersonItemReader(DataGenerate dataGenerate) {
        this.dataGenerate = dataGenerate;
    }

    @Override
    public Person read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (personIterator == null) {
            List<Person> people = dataGenerate.generate(count);
            personIterator = people.iterator();
        }

        if (personIterator.hasNext()) {
            return personIterator.next();
        } else {
            return null;
        }
    }

}
