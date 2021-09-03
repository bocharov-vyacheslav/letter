package data.dao;

import data.models.Okpo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("okpoDAO")
public class OkpoDAO {

    @Autowired
    private JdbcTemplate letterJdbcTemplate;

    public List<Okpo> getLetterOkpo() {
        return letterJdbcTemplate.query("SELECT [okpo], ([okpo] + ' - ' + [name]) as [name]\n" +
                "FROM [pasport_resp].[dbo].[gs_okpo]\n" +
                "WHERE okpo <> '' AND name <> '' AND okpo_bigint <> 0" +
                "ORDER BY okpo", new BeanPropertyRowMapper<>(Okpo.class));
    }
}
