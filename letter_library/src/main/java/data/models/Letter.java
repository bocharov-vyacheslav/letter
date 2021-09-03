package data.models;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class Letter {

    private long id;

    private Date addingDate;

    private short addingYear;

    private Date receiptDate;

    private byte receiptType;

    private byte letterType;

    private byte[] letterFile;

    private String letterFileExtension;

    private String letterFileFilename;

    private long letterFileSize;

    private String leaderFio;

    private String responsiblePost;

    private String responsibleFio;

    private String phone;

    private String email;

    private String okud;

    private short periodYear;

    private String periodNumber;

    private String periodicity;

    private String okpo;

    private String okato;

    private String oktmo;

    private String username;

    private String regNo;

    private String name;

    private long regNoId;

    private byte tippred;

    private String index;

    private String okved;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getAddingDate() {
        return addingDate;
    }

    public void setAddingDate(Date addingDate) {
        this.addingDate = addingDate;
    }

    public short getAddingYear() {
        return addingYear;
    }

    public void setAddingYear(short addingYear) {
        this.addingYear = addingYear;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public byte getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(byte receiptType) {
        this.receiptType = receiptType;
    }

    public byte getLetterType() {
        return letterType;
    }

    public void setLetterType(byte letterType) {
        this.letterType = letterType;
    }

    public byte[] getLetterFile() {
        return letterFile;
    }

    public void setLetterFile(byte[] letterFile) {
        this.letterFile = letterFile;
    }

    public String getLetterFileExtension() {
        return letterFileExtension;
    }

    public void setLetterFileExtension(String letterFileExtension) {
        this.letterFileExtension = letterFileExtension;
    }

    public String getLetterFileFilename() {
        return letterFileFilename;
    }

    public void setLetterFileFilename(String letterFileFilename) {
        this.letterFileFilename = letterFileFilename;
    }

    public long getLetterFileSize() {
        return letterFileSize;
    }

    public void setLetterFileSize(long letterFileSize) {
        this.letterFileSize = letterFileSize;
    }

    public String getLeaderFio() {
        return leaderFio;
    }

    public void setLeaderFio(String leaderFio) {
        this.leaderFio = leaderFio;
    }

    public String getResponsiblePost() {
        return responsiblePost;
    }

    public void setResponsiblePost(String responsiblePost) {
        this.responsiblePost = responsiblePost;
    }

    public String getResponsibleFio() {
        return responsibleFio;
    }

    public void setResponsibleFio(String responsibleFio) {
        this.responsibleFio = responsibleFio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOkud() {
        return okud;
    }

    public void setOkud(String okud) {
        this.okud = okud;
    }

    public short getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(short periodYear) {
        this.periodYear = periodYear;
    }

    public String getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(String periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getOkpo() {
        return okpo;
    }

    public void setOkpo(String okpo) {
        this.okpo = okpo;
    }

    public String getOkato() {
        return okato;
    }

    public void setOkato(String okato) {
        this.okato = okato;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRegNoId() {
        return regNoId;
    }

    public void setRegNoId(long regNoId) {
        this.regNoId = regNoId;
    }

    public byte getTippred() {
        return tippred;
    }

    public void setTippred(byte tippred) {
        this.tippred = tippred;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getOkved() {
        return okved;
    }

    public void setOkved(String okved) {
        this.okved = okved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Letter letter = (Letter) o;
        return id == letter.id &&
                addingYear == letter.addingYear &&
                receiptType == letter.receiptType &&
                letterType == letter.letterType &&
                letterFileSize == letter.letterFileSize &&
                periodYear == letter.periodYear &&
                regNoId == letter.regNoId &&
                tippred == letter.tippred &&
                Objects.equals(addingDate, letter.addingDate) &&
                Objects.equals(receiptDate, letter.receiptDate) &&
                Arrays.equals(letterFile, letter.letterFile) &&
                Objects.equals(letterFileExtension, letter.letterFileExtension) &&
                Objects.equals(letterFileFilename, letter.letterFileFilename) &&
                Objects.equals(leaderFio, letter.leaderFio) &&
                Objects.equals(responsiblePost, letter.responsiblePost) &&
                Objects.equals(responsibleFio, letter.responsibleFio) &&
                Objects.equals(phone, letter.phone) &&
                Objects.equals(email, letter.email) &&
                Objects.equals(okud, letter.okud) &&
                Objects.equals(periodNumber, letter.periodNumber) &&
                Objects.equals(periodicity, letter.periodicity) &&
                Objects.equals(okpo, letter.okpo) &&
                Objects.equals(okato, letter.okato) &&
                Objects.equals(oktmo, letter.oktmo) &&
                Objects.equals(username, letter.username) &&
                Objects.equals(regNo, letter.regNo) &&
                Objects.equals(name, letter.name) &&
                Objects.equals(index, letter.index) &&
                Objects.equals(okved, letter.okved);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, addingDate, addingYear, receiptDate, receiptType, letterType, letterFileExtension, letterFileFilename, letterFileSize, leaderFio, responsiblePost, responsibleFio, phone, email, okud, periodYear, periodNumber, periodicity, okpo, okato, oktmo, username, regNo, name, regNoId, tippred, index, okved);
        result = 31 * result + Arrays.hashCode(letterFile);
        return result;
    }
}
