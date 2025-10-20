import com.fasterxml.jackson.annotation.JsonIgnore;

public class Book {
    private String title;
    private String author;
    private String nation;
    private String era;
    private int readCount;      // 阅读次数
    private int finishedCount;  // 已读完次数
    private boolean isRead;     // 是否已读（至少读过一次）
    private int maximCount;     // 好词好句数量

    public Book() {
        this.readCount = 0;
        this.finishedCount = 0;
        this.isRead = false;
        this.maximCount = 0;
    }

    public Book(String title, String author, String nation, String era) {
        this.title = title;
        this.author = author;
        this.nation = nation;
        this.era = era;
        this.readCount = 0;
        this.finishedCount = 0;
        this.isRead = false;
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

    public int getFinishedCount() { return finishedCount; }
    public void setFinishedCount(int finishedCount) { this.finishedCount = finishedCount; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getMaximCount() { return maximCount; }
    public void setMaximCount(int maximCount) { this.maximCount = maximCount; }

    // 增加阅读次数的方法
    public void incrementReadCount() {
        this.readCount++;
        this.isRead = true; // 标记为已读
    }

    // 增加已读完次数
    public void incrementFinishedCount() {
        this.finishedCount++;
        this.isRead = true; // 标记为已读
    }

    // 增加好词好句数量
    public void incrementMaximCount() {
        this.maximCount++;
    }

    // 获取完整的显示信息
    @JsonIgnore
    public String getFullDisplayText() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(" - ").append(author)
                .append(" (").append(nation).append(", ").append(era).append(")");

        if (isRead) {
            if (finishedCount > 0) {
                sb.append(" - 已读").append(readCount).append("次")
                        .append(" - 已读完").append(finishedCount).append("次");
            } else {
                sb.append(" - 已读").append(readCount).append("次");
            }
        }

        if (maximCount > 0) {
            sb.append(" - 摘抄").append(maximCount).append("条");
        }

        return sb.toString();
    }

    // 获取简化的显示信息（用于未读列表）
    @JsonIgnore
    public String getSimpleDisplayText() {
        return title + " - " + author + " (" + nation + ", " + era + ")";
    }

    @JsonIgnore
    public String getStatusText() {
        if (finishedCount > 0) {
            return "已读完 " + finishedCount + " 次" + (readCount > finishedCount ? " - 已读 " + readCount + " 次" : "");
        } else if (isRead) {
            return "已读 " + readCount + " 次";
        } else {
            return "未读";
        }
    }

    @Override
    public String toString() {
        return getFullDisplayText();
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