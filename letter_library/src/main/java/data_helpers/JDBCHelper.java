package data_helpers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@ComponentScan(basePackages = "data")
public class JDBCHelper {

    @Bean
    public DataSource pasportDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(AppConfig.getPasportUrl());
        dataSource.setCatalog(AppConfig.getPasportCatalog());
        dataSource.setUsername(pasportSecurity.getPasportUsername());
        dataSource.setPassword(pasportSecurity.getPasportPassword());
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager pasportTsransactionManager() {
        return new DataSourceTransactionManager(pasportDataSource());
    }

    @Bean
    public JdbcTemplate pasportJdbcTemplate(DataSource pasportDataSource) {
        return createJdbcTemplate(pasportDataSource);
    }

    private JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setResultsMapCaseInsensitive(true);
        return jdbcTemplate;
    }

    public static Connection getPasportConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(AppConfig.getPasportUrl(), pasportSecurity.getPasportUsername(), pasportSecurity.getPasportPassword());
        connection.setCatalog(AppConfig.getPasportCatalog());
        return connection;
    }

    public static PasportSecurity pasportSecurity = new PasportSecurity();

    public static class PasportSecurity {

        private String pasportUsername;
        private String pasportPassword;

        private String getPasportUsername() {
            return pasportUsername;
        }

        private String getPasportPassword() {
            return pasportPassword;
        }

        public void setPasportUsername(String pasportUsername) {
            this.pasportUsername = pasportUsername;
        }

        public void setPasportPassword(String pasportPassword) {
            this.pasportPassword = pasportPassword;
        }
    }
}