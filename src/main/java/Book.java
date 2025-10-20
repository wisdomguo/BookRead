public class Book {
    private String title;
    private String author;
    private String nation;
    private String era;
    
    public Book() {}
    
    public Book(String title, String author, String nation, String era) {
        this.title = title;
        this.author = author;
        this.nation = nation;
        this.era = era;
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
    
    @Override
    public String toString() {
        return String.format("《%s》 - %s (%s, %s)", title, author, nation, era);
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