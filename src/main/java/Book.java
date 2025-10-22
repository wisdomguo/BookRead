import com.fasterxml.jackson.annotation.JsonIgnore;

public class Book {
    private String title;
    private String author;
    private String nation;
    private String era;
    private int readCount;      // 阅读次数
    private int finishedCount;  // 新增：已读完次数
    private boolean isRead;     // 新增：是否已读（至少读过一次）
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
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public int getFinishedCount() {
        return finishedCount;
    }

    public void setFinishedCount(int finishedCount) {
        this.finishedCount = finishedCount;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getMaximCount() {
        return maximCount;
    }

    public void setMaximCount(int maximCount) {
        this.maximCount = maximCount;
    }

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

    @JsonIgnore
    public String getDisplayText() {
        // 修复Bug3：正确显示已读次数和已读完次数
        StringBuilder sb = new StringBuilder();
        sb.append("《").append(title).append("》");
        if (author != null) {
            sb.append(" - ").append(author);
        }else {
            sb.append(" - ").append("佚名");
        }
        if ((nation != null && !nation.isEmpty()) || (era != null && !era.isEmpty())) {

            if (nation != null && !nation.isEmpty()) {
                sb.append(" (").append(nation);
                if (era != null && !era.isEmpty()) {
                    sb.append(", ").append(era);
                }
                sb.append(")");
            } else {
                sb.append(" (").append(era).append(")");
            }
        }

        if (finishedCount > 0) {
            sb.append(" - 已读完").append(finishedCount).append("次");
        }
        if (isRead) {
            sb.append(" - 已读").append(readCount).append("次");
        }

        if (maximCount > 0) {
            sb.append(" - 摘抄").append(maximCount).append("条");
        }

        return sb.toString();
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