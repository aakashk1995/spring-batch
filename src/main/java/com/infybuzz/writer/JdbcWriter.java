package com.infybuzz.writer;

import com.infybuzz.model.StudentJdbc;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcWriter implements ItemWriter<StudentJdbc> {
  //in item writer you will not get data one-by-one it depends of chunk size
    @Override
    public void write(List<?  extends StudentJdbc> list) throws Exception {
        System.out.println("Inside item writer");
      list.stream().forEach(System.out::println);
    }
}
