import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
                // 创建示例数据
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

            // 加载当前阅读书籍 - 修复这里！
            File readingFile = new File(READING_FILE);
            if (readingFile.exists() && readingFile.length() > 0) {
                try {
                    // 首先尝试作为单个Book对象读取
                    currentBook = objectMapper.readValue(readingFile, Book.class);
                    System.out.println("当前阅读书籍: " + currentBook.getTitle());
                } catch (Exception e1) {
                    try {
                        // 如果失败，尝试作为Book列表读取，取第一个
                        List<Book> readingList = objectMapper.readValue(readingFile, new TypeReference<List<Book>>() {});
                        if (!readingList.isEmpty()) {
                            currentBook = readingList.get(0);
                            System.out.println("当前阅读书籍(从列表中): " + currentBook.getTitle());
                            // 重新保存为单个对象格式
                            saveCurrentBook();
                        } else {
                            currentBook = null;
                            System.out.println("阅读列表为空");
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

            // 创建空列表作为后备
            allBooks = new ArrayList<>();
            readBooks = new ArrayList<>();
            currentBook = null;

            // 如果文件损坏，创建示例数据
            createSampleData();
        }
    }

    private void createSampleData() {
        System.out.println("创建示例数据...");
        allBooks.add(new Book("示例书籍", "示例作者", "中国", "现代"));
        saveAllBooks();
    }

    private void saveAllBooks() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(MENU_FILE), allBooks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Book getRandomBook() {
        if (allBooks.isEmpty()) {
            return null;
        }

        // 获取未阅读的书籍
        List<Book> unreadBooks = new ArrayList<>(allBooks);
        unreadBooks.removeAll(readBooks);

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
        if (currentBook != null && !readBooks.contains(currentBook)) {
            readBooks.add(currentBook);
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