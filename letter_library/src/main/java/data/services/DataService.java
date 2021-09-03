package data.services;

import data.dao.LetterDAO;
import data.dao.OkpoDAO;
import data.dao.OkudDAO;
import data.models.Letter;
import data.models.LetterPagination;
import data.models.Okpo;
import data.models.Okud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.List;

@Service("dataService")
public class DataService {

    @Autowired
    private OkpoDAO okpoDAO;

    @Autowired
    private OkudDAO okudDAO;

    @Autowired
    private LetterDAO letterDAO;

    @Autowired
    private PlatformTransactionManager pasportTransactionManager;

    public PlatformTransactionManager getPasportTransactionManager(){
        return pasportTransactionManager;
    }

    public List<Okpo> getLetterOkpo() {
        return okpoDAO.getLetterOkpo();
    }

    public List<Okud> getLetterOkud() {
        return okudDAO.getLetterOkud();
    }

    public List<Okud> getLetterShortOkud() {
        return okudDAO.getLetterShortOkud();
    }

    public void addOrEditLetters(List<Letter> letters) {
        letterDAO.addOrEditAll(letters);
    }

    public long getLastRegNoId() {
        return letterDAO.getLastRegNoId();
    }

    public String getLetterDir() {
        return letterDAO.getLetterDir();
    }

    public String getRegNoPreffix() {
        return letterDAO.getRegNoPreffix();
    }

    public void lockLetters() {
        letterDAO.lockLetters();
    }

    public byte[] getLetterFile(long id) {
        return letterDAO.getLetterFile(id);
    }

    public String checkOtch(String okpo, String okud, short periodYear, String periodNumber) {
        return letterDAO.checkOtch(okpo, okud, periodYear, periodNumber);
    }

    public List<LetterPagination> getLetters(Date periodFrom, Date periodTo, String okpo, String okud, byte letterType, byte periodicityCode, short year, short periodNumber, byte rowsPerPage, long pageNumber, long totalPage, String okato, String oktmo, String okved) {
        return letterDAO.getLetters(periodFrom, periodTo, okpo, okud, letterType, periodicityCode, year, periodNumber, rowsPerPage, pageNumber, totalPage, okato, oktmo, okved);
    }

    public List<Letter> getLetterReport(Date periodFrom, Date periodTo, String okpo, String okud, byte letterType, byte periodicityCode, short year, short periodNumber, String okato, String oktmo, String okved) {
        return letterDAO.getLetterReport(periodFrom, periodTo, okpo, okud, letterType, periodicityCode, year, periodNumber, okato, oktmo, okved);
    }
}
