package com.motorplus.motorplus.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.motorplus.motorplus.mapper")
public class MyBatisConfig {
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception {
        var f = new SqlSessionFactoryBean();
        f.setDataSource(ds);

        var cfg = new org.apache.ibatis.session.Configuration();
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.setDefaultScriptingLanguage(org.apache.ibatis.scripting.xmltags.XMLLanguageDriver.class);
        cfg.getTypeHandlerRegistry().register(com.motorplus.motorplus.mybatis.UUIDTypeHandler.class);

        f.setConfiguration(cfg);
        return f.getObject();
    }
}
