package com.infybuzz.writer;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FirstItemWriter implements ItemWriter<Long> {
  //in item writer you will not get data one-by-one it depends of chunk size
    @Override
    public void write(List list) throws Exception {
        System.out.println("Inside item writer");
      list.stream().forEach(System.out::println);
    }
}
