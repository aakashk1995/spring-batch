package com.infybuzz.config;

import com.infybuzz.model.StudentJdbc;
import com.infybuzz.writer.JdbcWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Configuration
public class JdbcJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource datasource;
    @Autowired
    private JdbcWriter jdbcWriter;

   // @Bean("jdbc-job")
    public Job jdbcJob() throws Exception {
      return  jobBuilderFactory.get("jdbc-job")
                .incrementer(new RunIdIncrementer())
                .start(firstJdbcChunkStep())
                .build();
    }

    public Step firstJdbcChunkStep() throws Exception {
      return  stepBuilderFactory.get("first-jdbc-chunk")
                .<StudentJdbc,StudentJdbc>chunk(1000)
              .reader(jdbcPagingItemReader())
             //.reader(jdbcCursorItemReader())
                .writer(jdbcBatchItemWriter())
            //  .writer(jdbcWriter)
              .taskExecutor(taskExecutor())
              .throttleLimit(10)
                .build();
    }


  //  @Bean
    public JdbcPagingItemReader<StudentJdbc> jdbcPagingItemReader() throws Exception {
        JdbcPagingItemReader<StudentJdbc> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(datasource);
        reader.setPageSize(1000); // Batch size for each page

        // Define query
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(datasource);
        queryProvider.setSelectClause("select id, first_name as firstName, last_name as lastName, email");
        queryProvider.setFromClause("from student");
        queryProvider.setSortKey("id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setRowMapper(new BeanPropertyRowMapper<>(StudentJdbc.class));

        return reader;
    }


    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader() {
       // reads records sequentially,
        JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader =
                new JdbcCursorItemReader<StudentJdbc>();

        jdbcCursorItemReader.setDataSource(datasource);
        jdbcCursorItemReader.setSql(
                "select id, first_name as firstName, last_name as lastName,"
                        + "email from student");

        jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<StudentJdbc>() {
            {
                setMappedClass(StudentJdbc.class);
            }
        });

      //  jdbcCursorItemReader.setCurrentItemCount(3); // to skip first 3 records
      //  jdbcCursorItemReader.setMaxItemCount(10);  // max records to read
        return jdbcCursorItemReader;
    }

   // @Bean
    public JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentJdbc>();

        jdbcBatchItemWriter.setDataSource(datasource);
        jdbcBatchItemWriter.setSql(
                "insert into student_bkcp(id, first_name, last_name, email) "
                        + "values (:id, :firstName, :lastName, :email)");

        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<StudentJdbc>());

        return jdbcBatchItemWriter;
    }

   // @Bean
    public JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter1() {
        JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentJdbc>();

        jdbcBatchItemWriter.setDataSource(datasource);
        jdbcBatchItemWriter.setSql(
                "insert into student(id, first_name, last_name, email) "
                        + "values (?,?,?,?)");

        jdbcBatchItemWriter.setItemPreparedStatementSetter(
                new ItemPreparedStatementSetter<StudentJdbc>() {

                    @Override
                    public void setValues(StudentJdbc item, PreparedStatement ps) throws SQLException {
                        ps.setLong(1, item.getId());
                        ps.setString(2, item.getFirstName());
                        ps.setString(3, item.getLastName());
                        ps.setString(4, item.getEmail());
                    }
                });

        return jdbcBatchItemWriter;
    }


  //  @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10); // Limit to 10 concurrent threads
        return taskExecutor;
    }


}
