package data.models;

import java.util.Arrays;

public class LetterFile {

    private byte[] letterFile;

    public byte[] getLetterFile() {
        return letterFile;
    }

    public void setLetterFile(byte[] letterFile) {
        this.letterFile = letterFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LetterFile that = (LetterFile) o;
        return Arrays.equals(letterFile, that.letterFile);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(letterFile);
    }
}
