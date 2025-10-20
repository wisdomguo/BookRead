import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BookManager {
    private static final String MENU_FILE = "menu.json";
    private static final String READING_FILE = "reading.json";
    private static final String READ_FILE = "read.json";

    private List<Book> allBooks;
    private List<Book> readBooks;
    private Book currentBook;
    private final ObjectMapper objectMapper;

    public BookManager() {
        this.objectMapper = new ObjectMapper();
        loadBooks();
    }

    private void loadBooks() {
        try {
            // 加载所有书籍
            File menuFile = new File(MENU_FILE);
            if (menuFile.exists() && menuFile.length() > 0) {
                System.out.println("正在加载菜单文件...");
                allBooks = objectMapper.readValue(menuFile, new TypeReference<List<Book>>() {});
                System.out.println("成功加载 " + allBooks.size() + " 本书籍");
            } else {
                System.out.println("菜单文件不存在或为空: " + MENU_FILE);
                allBooks = new ArrayList<>();
                createSampleData();
            }

            // 加载已读书籍
            File readFile = new File(READ_FILE);
            if (readFile.exists() && readFile.length() > 0) {
                try {
                    readBooks = objectMapper.readValue(readFile, new TypeReference<List<Book>>() {});
                    System.out.println("已读书籍: " + readBooks.size() + " 本");
                } catch (Exception e) {
                    System.err.println("读取已读书籍文件出错，重置为空列表");
                    readBooks = new ArrayList<>();
                }
            } else {
                readBooks = new ArrayList<>();
                System.out.println("已读书籍文件不存在，创建新文件");
            }

            // 加载当前阅读书籍
            File readingFile = new File(READING_FILE);
            if (readingFile.exists() && readingFile.length() > 0) {
                try {
                    currentBook = objectMapper.readValue(readingFile, Book.class);
                    System.out.println("当前阅读书籍: " + currentBook.getTitle());
                } catch (Exception e1) {
                    try {
                        List<Book> readingList = objectMapper.readValue(readingFile, new TypeReference<List<Book>>() {});
                        if (!readingList.isEmpty()) {
                            currentBook = readingList.get(0);
                            System.out.println("当前阅读书籍(从列表中): " + currentBook.getTitle());
                            saveCurrentBook();
                        } else {
                            currentBook = null;
                        }
                    } catch (Exception e2) {
                        System.err.println("无法解析阅读文件，重置为空");
                        currentBook = null;
                    }
                }
            } else {
                currentBook = null;
                System.out.println("没有正在阅读的书籍");
            }

        } catch (Exception e) {
            System.err.println("加载书籍数据时出错: " + e.getMessage());
            e.printStackTrace();
            allBooks = new ArrayList<>();
            readBooks = new ArrayList<>();
            currentBook = null;
            createSampleData();
        }
    }

    private void createSampleData() {
        System.out.println("创建示例数据...");
        allBooks.add(new Book("示例书籍", "示例作者", "中国", "现代"));
        saveAllBooks();
    }

    // 获取未读书籍列表
    public List<Book> getUnreadBooks() {
        List<Book> unreadBooks = new ArrayList<>(allBooks);
        unreadBooks.removeAll(readBooks);
        return unreadBooks;
    }

    // 获取已读书籍列表
    public List<Book> getReadBooks() {
        return new ArrayList<>(readBooks);
    }

    // 搜索未读书籍
    public List<Book> searchUnreadBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getUnreadBooks();
        }

        String lowerKeyword = keyword.toLowerCase();
        return getUnreadBooks().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(lowerKeyword) ||
                                book.getAuthor().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    // 搜索已读书籍
    public List<Book> searchReadBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getReadBooks();
        }

        String lowerKeyword = keyword.toLowerCase();
        return getReadBooks().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(lowerKeyword) ||
                                book.getAuthor().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    // 设置指定书籍为当前阅读
    public boolean setCurrentBook(Book book) {
        if (book != null && allBooks.contains(book)) {
            currentBook = book;
            saveCurrentBook();
            return true;
        }
        return false;
    }

    // 重新阅读已读书籍
    public boolean rereadBook(Book book) {
        if (setCurrentBook(book)) {
            // 如果这本书已经在已读列表中，增加阅读次数
            if (readBooks.contains(book)) {
                int index = readBooks.indexOf(book);
                readBooks.get(index).incrementReadCount();
                saveReadBooks();
            }
            return true;
        }
        return false;
    }

    public Book getRandomBook() {
        List<Book> unreadBooks = getUnreadBooks();
        if (unreadBooks.isEmpty()) {
            return null;
        }

        Random random = new Random();
        Book selectedBook = unreadBooks.get(random.nextInt(unreadBooks.size()));

        currentBook = selectedBook;
        saveCurrentBook();

        return selectedBook;
    }

    public Book getCurrentBook() {
        return currentBook;
    }

    public void markAsRead() {
        if (currentBook != null) {
            // 如果书籍已经在已读列表中，增加阅读次数
            if (readBooks.contains(currentBook)) {
                int index = readBooks.indexOf(currentBook);
                readBooks.get(index).incrementReadCount();
            } else {
                // 否则添加到已读列表，并设置阅读次数为1
                currentBook.setReadCount(1);
                readBooks.add(currentBook);
            }
            saveReadBooks();
            currentBook = null;
            saveCurrentBook();
        }
    }

    private void saveCurrentBook() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(READING_FILE), currentBook);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveReadBooks() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(READ_FILE), readBooks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAllBooks() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(MENU_FILE), allBooks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTotalBooksCount() {
        return allBooks.size();
    }

    public int getReadBooksCount() {
        return readBooks.size();
    }

    public int getRemainingBooksCount() {
        return allBooks.size() - readBooks.size();
    }
}