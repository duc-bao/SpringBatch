package com.ducbao.demotest.config;

import com.ducbao.demotest.config.dataRandom.DataGenerate;
import com.ducbao.demotest.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

public class PersonItemReader implements ItemReader<Person> {
    private final DataGenerate dataGenerate;
    private Iterator<Person> personIterator;
    private final Integer fromId;
    private final Integer toId;

    public PersonItemReader(DataGenerate dataGenerate, Integer fromId, Integer toId) {
        this.dataGenerate = dataGenerate;
        this.fromId = fromId;
        this.toId = toId;
    }

    @Override
    public Person read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (personIterator == null) {
            List<Person> people = dataGenerate.generateRange(fromId, toId);
            personIterator = people.iterator();
            System.out.println("Partition processing range: " + fromId + " to " + toId + " (total: " + people.size() + " records)");
        }

        if (personIterator.hasNext()) {
            return personIterator.next();
        } else {
            return null;
        }
    }

}
