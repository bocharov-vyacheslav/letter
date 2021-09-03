package data.models;

import java.util.Objects;

public class LetterPagination extends Letter {

    private long recordsCount;

    private long pageNumber;

    private long totalPage;

    public long getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(long recordsCount) {
        this.recordsCount = recordsCount;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LetterPagination that = (LetterPagination) o;
        return recordsCount == that.recordsCount &&
                pageNumber == that.pageNumber &&
                totalPage == that.totalPage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recordsCount, pageNumber, totalPage);
    }
}
