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
    private static final String MAXIM_DIR = "src/main/resources/maxim";

    private List<Book> allBooks;
    private List<Book> readBooks; // 现在包含所有已读过的书籍（包括已读完）
    private Book currentBook;
    private final ObjectMapper objectMapper;

    public BookManager() {
        this.objectMapper = new ObjectMapper();
        // 创建好词好句目录
        new File(MAXIM_DIR).mkdirs();
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

                // 为每本书加载好词好句数量
                for (Book book : allBooks) {
                    book.setMaximCount(getMaximCount(book));
                }
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

                    // 为已读书籍加载好词好句数量
                    for (Book book : readBooks) {
                        book.setMaximCount(getMaximCount(book));
                    }
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
                    // 加载当前书籍的好词好句数量
                    currentBook.setMaximCount(getMaximCount(currentBook));
                    System.out.println("当前阅读书籍: " + currentBook.getTitle());
                } catch (Exception e1) {
                    try {
                        List<Book> readingList = objectMapper.readValue(readingFile, new TypeReference<List<Book>>() {});
                        if (!readingList.isEmpty()) {
                            currentBook = readingList.get(0);
                            currentBook.setMaximCount(getMaximCount(currentBook));
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

    // 获取书籍的好词好句文件路径
    private String getMaximFilePath(Book book) {
        String fileName = book.getTitle() + "_" + book.getAuthor() + ".json";
        // 替换文件名中的非法字符
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return MAXIM_DIR + File.separator + fileName;
    }

    // 保存好词好句 - 修复摘抄逻辑
    public void saveMaxim(Book book, Maxim maxim) {
        try {
            String filePath = getMaximFilePath(book);
            File maximFile = new File(filePath);

            List<Maxim> maxims;
            if (maximFile.exists()) {
                maxims = objectMapper.readValue(maximFile, new TypeReference<List<Maxim>>() {});
            } else {
                maxims = new ArrayList<>();
            }

            maxims.add(maxim);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(maximFile, maxims);

            // 更新书籍的好词好句数量
            book.setMaximCount(maxims.size());

            // 修复：标记为已读但不增加阅读次数
            if (!book.isRead()) {
                book.setRead(true);
                // 如果这本书不在已读列表中，添加到已读列表
                if (!readBooks.contains(book)) {
                    readBooks.add(book);
                    saveReadBooks();
                }
            }

            // 如果这本书在已读列表中，更新已读列表中的对应书籍
            if (readBooks.contains(book)) {
                int index = readBooks.indexOf(book);
                Book existingBook = readBooks.get(index);
                existingBook.setMaximCount(book.getMaximCount());
                existingBook.setRead(true); // 确保标记为已读
                // 保持原有的阅读次数和已读完次数
                existingBook.setReadCount(book.getReadCount());
                existingBook.setFinishedCount(book.getFinishedCount());
                saveReadBooks();
            }

            System.out.println("成功保存好词好句，当前数量: " + book.getMaximCount());

        } catch (IOException e) {
            System.err.println("保存好词好句时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 获取书籍的好词好句列表
    public List<Maxim> getMaxims(Book book) {
        try {
            String filePath = getMaximFilePath(book);
            File maximFile = new File(filePath);

            if (maximFile.exists()) {
                return objectMapper.readValue(maximFile, new TypeReference<List<Maxim>>() {});
            }
        } catch (IOException e) {
            System.err.println("读取好词好句时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // 获取书籍的好词好句数量
    public int getMaximCount(Book book) {
        return getMaxims(book).size();
    }

    // 标记为已读（不增加已读完次数，不清空当前书籍）
    public void markAsRead() {
        if (currentBook != null) {
            // 增加阅读次数 - 确保只增加一次
            int oldReadCount = currentBook.getReadCount();
            currentBook.setReadCount(oldReadCount + 1);
            currentBook.setRead(true);

            // 如果书籍不在已读列表中，添加到已读列表
            if (!readBooks.contains(currentBook)) {
                readBooks.add(currentBook);
            } else {
                // 如果已经在已读列表中，更新阅读次数
                int index = readBooks.indexOf(currentBook);
                readBooks.get(index).setReadCount(currentBook.getReadCount());
                readBooks.get(index).setRead(true);
            }

            saveReadBooks();
            // 注意：不保存当前书籍，不清空当前阅读状态
            System.out.println("标记为已读，阅读次数: " + currentBook.getReadCount());
        }
    }

    // 标记为已读完（增加已读完次数，清空当前书籍）
    public void markAsFinished() {
        if (currentBook != null) {
            // 增加已读完次数 - 确保只增加一次
            int oldFinishedCount = currentBook.getFinishedCount();
            currentBook.setFinishedCount(oldFinishedCount + 1);
            currentBook.setRead(true);

            // 如果书籍不在已读列表中，添加到已读列表
            if (!readBooks.contains(currentBook)) {
                readBooks.add(currentBook);
            } else {
                // 如果已经在已读列表中，更新已读完次数
                int index = readBooks.indexOf(currentBook);
                readBooks.get(index).setFinishedCount(currentBook.getFinishedCount());
                readBooks.get(index).setRead(true);
            }

            saveReadBooks();

            // 清空当前阅读书籍
            Book finishedBook = currentBook;
            currentBook = null;
            saveCurrentBook();

            System.out.println("标记为已读完，已读完次数: " + finishedBook.getFinishedCount());
        }
    }

    // 其余方法保持不变...
    private void createSampleData() {
        System.out.println("创建示例数据...");
        allBooks.add(new Book("示例书籍", "示例作者", "中国", "现代"));
        saveAllBooks();
    }

    public List<Book> getUnreadBooks() {
        List<Book> unreadBooks = new ArrayList<>(allBooks);
        unreadBooks.removeAll(readBooks);
        return unreadBooks;
    }

    // 获取已读书籍列表（包括已读完）
    public List<Book> getReadBooks() {
        return new ArrayList<>(readBooks);
    }

    // 获取已读完书籍列表（finishedCount > 0）
    public List<Book> getFinishedBooks() {
        return readBooks.stream()
                .filter(book -> book.getFinishedCount() > 0)
                .collect(Collectors.toList());
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

    // 搜索已读完书籍
    public List<Book> searchFinishedBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getFinishedBooks();
        }

        String lowerKeyword = keyword.toLowerCase();
        return getFinishedBooks().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(lowerKeyword) ||
                                book.getAuthor().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public boolean setCurrentBook(Book book) {
        if (book != null && allBooks.contains(book)) {
            currentBook = book;
            saveCurrentBook();
            return true;
        }
        return false;
    }

    public boolean rereadBook(Book book) {
        return setCurrentBook(book);
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

    public int getFinishedBooksCount() {
        return getFinishedBooks().size();
    }

    public int getRemainingBooksCount() {
        return allBooks.size() - readBooks.size();
    }
}