package com.infybuzz.config;

import com.infybuzz.model.StudentJdbc;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

@Configuration
public class JdbcToCsvJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource datasource;


    // @Bean("jdbc-csv-job")
    public Job jdbcCsvJob() throws Exception {
        return  jobBuilderFactory.get("jdbc-csv-job")
                .incrementer(new RunIdIncrementer())
                .start(firstJdbcCsvChunkStep())
                .build();
    }

    public Step firstJdbcCsvChunkStep() throws Exception {
        return  stepBuilderFactory.get("first-jdbc-csv-chunk")
                .<StudentJdbc,StudentJdbc>chunk(1000)
                .reader(jdbcPagingItemReader())
                .writer(csvFileItemWriter())
                .taskExecutor(taskExecutor())
                .throttleLimit(10)
                .build();
    }

   // @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10); // Limit to 10 concurrent threads
        return taskExecutor;
    }

   // @Bean
    public FlatFileItemWriter<StudentJdbc> csvFileItemWriter(){
        FlatFileItemWriter<StudentJdbc> flatFileItemWriter = new FlatFileItemWriter<>();
        // Output CSV file location
        flatFileItemWriter.setResource(new FileSystemResource("output/students.csv"));

        // Header of the CSV file
        flatFileItemWriter.setHeaderCallback(writer1 -> writer1.write("ID,First Name,Last Name,Email"));
        flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentJdbc>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<StudentJdbc>() {
                    {
                        setNames(new String[] {"id", "firstName", "lastName", "email"});
                    }
                });
            }
        });
        flatFileItemWriter.setAppendAllowed(true);
//Ensures that multiple threads can append data to the same CSV file without overwriting the content.
        flatFileItemWriter.setFooterCallback(new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                writer.write("Created @ " + new Date());
            }
        });


        return flatFileItemWriter;
    }
   // @Bean
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
}
