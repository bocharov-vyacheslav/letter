package data.dao;

import data.models.Okud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("okudDAO")
public class OkudDAO {

    @Autowired
    private JdbcTemplate letterJdbcTemplate;

    public List<Okud> getLetterOkud() {
        return letterJdbcTemplate.query("SELECT DISTINCT [okud], ([okud] + ' - ' + [index]) as [name], [periodicity], [periodicity_code]\n" +
                "FROM [pasport_resp].[dbo].[okud]\n" +
                "WHERE okud <> '' AND [index] <> '' AND okud_int <> 0 AND okud_int <> 1" +
                "ORDER BY okud", new BeanPropertyRowMapper<>(Okud.class));
    }

    public List<Okud> getLetterShortOkud() {
        return letterJdbcTemplate.query("SELECT DISTINCT [okud], ([okud] + ' - ' + [index]) as [name], '' as [periodicity], -1 as [periodicity_code]\n" +
                "FROM [pasport_resp].[dbo].[okud]\n" +
                "WHERE okud <> '' AND [index] <> '' AND okud_int <> 0 AND okud_int <> 1" +
                "ORDER BY okud", new BeanPropertyRowMapper<>(Okud.class));
    }
}
