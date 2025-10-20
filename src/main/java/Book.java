import com.fasterxml.jackson.annotation.JsonIgnore;

public class Book {
    private String title;
    private String author;
    private String nation;
    private String era;
    private int readCount;
    private int maximCount; // 新增：好词好句数量

    public Book() {
        this.readCount = 0;
        this.maximCount = 0;
    }

    public Book(String title, String author, String nation, String era) {
        this.title = title;
        this.author = author;
        this.nation = nation;
        this.era = era;
        this.readCount = 0;
        this.maximCount = 0;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getNation() { return nation; }
    public void setNation(String nation) { this.nation = nation; }

    public String getEra() { return era; }
    public void setEra(String era) { this.era = era; }

    public int getReadCount() { return readCount; }
    public void setReadCount(int readCount) { this.readCount = readCount; }

    public int getMaximCount() { return maximCount; }
    public void setMaximCount(int maximCount) { this.maximCount = maximCount; }

    // 增加阅读次数的方法
    public void incrementReadCount() {
        this.readCount++;
    }

    // 增加好词好句数量
    public void incrementMaximCount() {
        this.maximCount++;
    }

    @JsonIgnore
    public String getDisplayText() {
        return String.format("《%s》 - %s (%s, %s) - 已读%d次 - 摘抄%d条",
                title, author, nation, era, readCount, maximCount);
    }

    @Override
    public String toString() {
        return getDisplayText();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book book = (Book) obj;
        return title.equals(book.title) && author.equals(book.author);
    }

    @Override
    public int hashCode() {
        return title.hashCode() + author.hashCode();
    }
}