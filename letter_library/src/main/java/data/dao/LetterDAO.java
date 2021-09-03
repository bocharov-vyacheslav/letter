package data.dao;

import data.constants.Constants;
import data.models.LastRegNo;
import data.models.Letter;
import data.models.LetterFile;
import data.models.LetterPagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
@Qualifier("letterDAO")
public class LetterDAO {

    @Autowired
    private JdbcTemplate letterJdbcTemplate;

    public void addOrEditAll(List<Letter> letterRecords) {
        letterJdbcTemplate.batchUpdate("EXEC addOrEditLetterNew @adding_date = ?,\n" +
                        "\t@receipt_date = ?,\n" +
                        "\t@receipt_type = ?,\n" +
                        "\t@letter_type = ?,\n" +
                        "\t@letter_file = ?,\n" +
                        "\t@letter_file_extension = ?,\n" +
                        "\t@letter_file_filename = ?,\n" +
                        "\t@letter_file_size = ?,\n" +
                        "\t@leader_fio = ?,\n" +
                        "\t@responsible_post = ?,\n" +
                        "\t@responsible_fio = ?,\n" +
                        "\t@phone = ?,\n" +
                        "\t@email = ?,\n" +
                        "\t@okud = ?,\n" +
                        "\t@okpo = ?,\n" +
                        "\t@period_year = ?,\n" +
                        "\t@period_number = ?,\n" +
                        "\t@periodicity = ?,\n" +
                        "\t@username = ?,\n" +
                        "\t@reg_no_id = ?,\n" +
                        "\t@reg_no = ?,\n" +
                        "\t@adding_year = ?",
                letterRecords, Constants.BATCH_SIZE, (ps, letter) -> {
                    ps.setTimestamp(1, new Timestamp(letter.getAddingDate().getTime()));
                    ps.setTimestamp(2, new Timestamp(letter.getReceiptDate().getTime()));
                    ps.setByte(3, letter.getReceiptType());
                    ps.setByte(4, letter.getLetterType());
                    ps.setBytes(5, letter.getLetterFile());
                    ps.setString(6, letter.getLetterFileExtension());
                    ps.setString(7, letter.getLetterFileFilename());
                    ps.setLong(8, letter.getLetterFileSize());
                    ps.setString(9, letter.getLeaderFio());
                    ps.setString(10, letter.getResponsiblePost());
                    ps.setString(11, letter.getResponsibleFio());
                    ps.setString(12, letter.getPhone());
                    ps.setString(13, letter.getEmail());
                    ps.setString(14, letter.getOkud());
                    ps.setString(15, letter.getOkpo());
                    ps.setShort(16, letter.getPeriodYear());
                    ps.setString(17, letter.getPeriodNumber());
                    ps.setString(18, letter.getPeriodicity());
                    ps.setString(19, letter.getUsername());
                    ps.setLong(20, letter.getRegNoId());
                    ps.setString(21, letter.getRegNo());
                    ps.setShort(22, letter.getAddingYear());
                });
    }

    public List<Letter> getLetterReport(Date periodFrom, Date periodTo, String okpo, String okud, byte letterType, byte periodicityCode, short year, short periodNumber, String okato, String oktmo, String okved) {
        return letterJdbcTemplate.query("EXEC getLetterReportNew @period_from = ?, @period_to = ?, @okpo = ?, @okud = ?, @letter_type = ?, @periodicity_code = ?, @year = ?, @period_number = ?, @okato = ?, @oktmo = ?, @okved = ?",
                new Object[] { periodFrom, periodTo, okpo, okud, letterType, periodicityCode, year, periodNumber, okato, oktmo, okved }, new BeanPropertyRowMapper<>(Letter.class));
    }

    public List<LetterPagination> getLetters(Date periodFrom, Date periodTo, String okpo, String okud, byte letterType, byte periodicityCode, short year, short periodNumber, byte rowsPerPage, long pageNumber, long totalPage, String okato, String oktmo, String okved) {
        return letterJdbcTemplate.query("EXEC getLettersNew @period_from = ?, @period_to = ?, @okpo = ?, @okud = ?, @letter_type = ?, @periodicity_code = ?, @year = ?, @period_number = ?, @rows_per_page = ?, @page_number = ?, @total_page = ?, @okato = ?, @oktmo = ?, @okved = ?",
                new Object[] { periodFrom, periodTo, okpo, okud, letterType, periodicityCode, year, periodNumber, rowsPerPage, pageNumber, totalPage, okato, oktmo, okved }, new BeanPropertyRowMapper<>(LetterPagination.class));
    }

    public void lockLetters() {
        letterJdbcTemplate.execute("SELECT 1 FROM [letter] with (holdlock, tablockx)");
    }

    public long getLastRegNoId() {
        List<LastRegNo> letterRecords = letterJdbcTemplate.query("EXEC getLastRegNoId", new BeanPropertyRowMapper<>(LastRegNo.class));
        return (letterRecords.isEmpty()) ? 0L : letterRecords.get(0).getRegNoId();
    }

    public String getRegNoPreffix() {
        return letterJdbcTemplate.queryForObject("EXEC getRegNoPreffix", String.class);
    }

    public String getLetterDir() {
        return letterJdbcTemplate.queryForObject("EXEC getLetterDir", String.class);
    }

    public String checkOtch(String okpo, String okud, short periodYear, String periodNumber) {
        List<String> messageRecords = letterJdbcTemplate.queryForList("EXEC checkOtch @okpo = ?, @okud = ?, @period_year = ?, @period_number = ?", new Object[] { okpo, okud, periodYear, periodNumber }, String.class);
        return (messageRecords.isEmpty()) ? "" : messageRecords.get(0);
    }

    public byte[] getLetterFile(long id) {
        List<LetterFile> letterFiles = letterJdbcTemplate.query("SELECT [letter_file] FROM [letter] WHERE [id] = ?", new Object[] { id }, new BeanPropertyRowMapper<>(LetterFile.class));
        return (letterFiles.isEmpty()) ? null : letterFiles.get(0).getLetterFile();
    }

    public void deleteAll() {
        letterJdbcTemplate.update("DELETE FROM [letter]");
    }

}
