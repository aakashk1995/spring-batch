//package com.infybuzz.config;
//
//import com.infybuzz.model.StudentJdbc;
//import com.infybuzz.writer.JdbcWriter;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.core.partition.support.Partitioner;
//import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
//import org.springframework.batch.item.database.JdbcBatchItemWriter;
//import org.springframework.batch.item.database.JdbcPagingItemReader;
//import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class JdbcPartitionJobConfig {
//
//    @Autowired
//    private JobBuilderFactory jobBuilderFactory;
//
//    @Autowired
//    private StepBuilderFactory stepBuilderFactory;
//
//    @Autowired
//    private DataSource datasource;
//    @Autowired
//    private JdbcWriter jdbcWriter;
//
//    @Bean("jdbc-partition-job")
//    public Job jdbcPartitionJob() throws Exception {
//        return  jobBuilderFactory.get("jdbc-partition-job")
//                .incrementer(new RunIdIncrementer())
//                .start(masterStep())
//                .build();
//    }
//
//    @Bean
//    public Step masterStep() throws Exception {
//        return stepBuilderFactory.get("master-step")
//                .partitioner("slaveStep", partitioner())
//                .step(slaveStep())
//                .taskExecutor(taskExecutor())
//                .build();
//    }
//
//    @Bean
//    public Partitioner partitioner() {
//        ColumnRangePartitioner partitioner = new ColumnRangePartitioner();
//        partitioner.setDataSource(datasource);
//        partitioner.setTable("student");
//        partitioner.setColumn("id");
//        return partitioner;
//    }
//
//    @Bean
//    public Step slaveStep() throws Exception {
//        return stepBuilderFactory.get("slave-step")
//                .<StudentJdbc, StudentJdbc>chunk(1000)
//                .reader(jdbcPagingItemReader()) // You can still use PagingReader here
//                .writer(jdbcBatchItemWriter())
//                .build();
//    }
//
//    @Bean
//    public JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter() {
//        JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter =
//                new JdbcBatchItemWriter<StudentJdbc>();
//
//        jdbcBatchItemWriter.setDataSource(datasource);
//        jdbcBatchItemWriter.setSql(
//                "insert into student_bkcp(id, first_name, last_name, email) "
//                        + "values (:id, :firstName, :lastName, :email)");
//
//        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
//                new BeanPropertyItemSqlParameterSourceProvider<StudentJdbc>());
//
//        return jdbcBatchItemWriter;
//    }
//
//    @Bean
//    public JdbcPagingItemReader<StudentJdbc> jdbcPagingItemReader() throws Exception {
//        JdbcPagingItemReader<StudentJdbc> reader = new JdbcPagingItemReader<>();
//        reader.setDataSource(datasource);
//        reader.setPageSize(1000); // Batch size for each page
//
//        // Define query
//        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
//        queryProvider.setDataSource(datasource);
//        queryProvider.setSelectClause("select id, first_name as firstName, last_name as lastName, email");
//        queryProvider.setFromClause("from student");
//        queryProvider.setSortKey("id");
//
//        reader.setQueryProvider(queryProvider.getObject());
//        reader.setRowMapper(new BeanPropertyRowMapper<>(StudentJdbc.class));
//
//        return reader;
//    }
//
//}
