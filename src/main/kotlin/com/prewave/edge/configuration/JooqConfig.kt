package com.prewave.edge.configuration

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.jooq.impl.DefaultExecuteListenerProvider
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource

@Configuration
class JooqConfig {

    @Autowired
    private val environment: Environment? = null

    @Bean
    fun dataSource(): DataSource {
        val dataSource = PGSimpleDataSource()
        dataSource.setUrl(environment!!.getRequiredProperty("spring.datasource.url"))
        dataSource.setUser(environment.getRequiredProperty("spring.datasource.username"))
        dataSource.setPassword(environment.getRequiredProperty("spring.datasource.password"))
        return dataSource
    }

    @Bean
    fun transactionAwareDataSource(): TransactionAwareDataSourceProxy {
        return TransactionAwareDataSourceProxy(dataSource())
    }

    @Bean
    fun connectionProvider(): DataSourceConnectionProvider {
        return DataSourceConnectionProvider(transactionAwareDataSource())
    }

    @Bean
    fun exceptionTransformer(): ExceptionTranslator {
        return ExceptionTranslator()
    }

    @Bean
    fun configuration(): DefaultConfiguration {
        val jooqConfiguration = DefaultConfiguration()
        jooqConfiguration.set(connectionProvider())
        jooqConfiguration.set(DefaultExecuteListenerProvider(exceptionTransformer()))
        val sqlDialectName: String = environment!!.getRequiredProperty("spring.jooq.sql-dialect")
        val dialect = SQLDialect.valueOf(sqlDialectName)
        jooqConfiguration.set(dialect)
        return jooqConfiguration
    }

    @Bean
    fun dslContext(): DSLContext {
        return DefaultDSLContext(configuration())
    }
}