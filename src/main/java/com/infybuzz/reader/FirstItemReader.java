package com.infybuzz.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class FirstItemReader implements ItemReader<Integer> {

    List<Integer> list = Arrays.asList(1,2,4,1,12,6,4,2,2342,1,6,2,12);
    int i=0;
    @Override
    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
       //this method will works like loop
        System.out.println("Inside item reader");
        if(i < list.size())
        {
            return list.get(i++);
        }
        i=0;
        return null; //if return null then end of source
        //it's indicating to item reader to stop reading value from source if we return null
    }
}
