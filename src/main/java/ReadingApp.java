import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ReadingApp extends JFrame {
    private BookManager bookManager;

    // 当前阅读区域组件
    private JLabel titleLabel;
    private JLabel authorLabel;
    private JLabel nationLabel;
    private JLabel eraLabel;
    private JLabel statusLabel; // 新增：书籍状态标签
    private JLabel statsLabel;
    private JButton readButton;
    private JButton finishedButton; // 新增：已读完按钮
    private JButton nextButton;

    // 目录区域组件
    private JTabbedPane tabbedPane;

    // 未读目录组件
    private JList<Book> unreadList;
    private DefaultListModel<Book> unreadListModel;
    private JTextField unreadSearchField;
    private JButton setUnreadAsCurrentButton;

    // 已读目录组件
    private JList<Book> readList;
    private DefaultListModel<Book> readListModel;
    private JTextField readSearchField;
    private JButton rereadButton;

    public ReadingApp() {
        bookManager = new BookManager();
        initializeUI();
        loadCurrentBook();
        refreshBookLists();
    }

    private void initializeUI() {
        setTitle("随机书籍阅读器 - 增强版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // 创建主面板 - 使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部：当前阅读和统计信息
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 中部：目录标签页
        tabbedPane = createDirectoryTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 当前阅读书籍面板
        JPanel bookPanel = createBookPanel();
        panel.add(bookPanel, BorderLayout.CENTER);

        // 统计信息面板
        JPanel statsPanel = createStatsPanel();
        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("当前阅读书籍"));

        // 顶部按钮面板
        JPanel topButtonPanel = new JPanel(new BorderLayout());

        // 左上角：查看好词好句按钮
        JButton viewMaximButton = new JButton("查看摘抄");
        viewMaximButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewMaxims();
            }
        });
        topButtonPanel.add(viewMaximButton, BorderLayout.WEST);

        // 右上角：添加好词好句按钮
        JButton addMaximButton = new JButton("添加摘抄");
        addMaximButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMaxim();
            }
        });
        topButtonPanel.add(addMaximButton, BorderLayout.EAST);

        panel.add(topButtonPanel, BorderLayout.NORTH);

        // 书籍信息面板
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5)); // 改为5行，添加状态行
        titleLabel = createStyledLabel("", Font.BOLD, 16);
        authorLabel = createStyledLabel("", Font.PLAIN, 14);
        nationLabel = createStyledLabel("", Font.PLAIN, 12);
        eraLabel = createStyledLabel("", Font.PLAIN, 12);
        statusLabel = createStyledLabel("", Font.ITALIC, 12); // 状态标签

        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(nationLabel);
        infoPanel.add(eraLabel);
        infoPanel.add(statusLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        statsLabel = new JLabel();
        updateStats();
        panel.add(statsLabel);

        // 添加按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        readButton = new JButton("标记为已读");
        finishedButton = new JButton("标记为已读完"); // 新增按钮
        nextButton = new JButton("随机抽取下一本");

        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAsRead();
            }
        });

        finishedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAsFinished();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNextBook();
            }
        });

        buttonPanel.add(readButton);
        buttonPanel.add(finishedButton);
        buttonPanel.add(nextButton);

        panel.add(buttonPanel);

        return panel;
    }

    private JTabbedPane createDirectoryTabbedPane() {
        JTabbedPane pane = new JTabbedPane();

        // 未读目录标签页
        JPanel unreadPanel = createUnreadDirectoryPanel();
        pane.addTab("未读目录 (" + bookManager.getUnreadBooks().size() + ")", unreadPanel);

        // 已读目录标签页
        JPanel readPanel = createReadDirectoryPanel();
        pane.addTab("已读目录 (" + bookManager.getReadBooks().size() + ")", readPanel);

        // 已读完目录标签页
        JPanel finishedPanel = createFinishedDirectoryPanel();
        pane.addTab("已读完目录 (" + bookManager.getFinishedBooksCount() + ")", finishedPanel);

        return pane;
    }

    private JPanel createUnreadDirectoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("搜索书名/作者:"), BorderLayout.WEST);
        unreadSearchField = new JTextField();
        unreadSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterUnreadBooks(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterUnreadBooks(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterUnreadBooks(); }
        });
        searchPanel.add(unreadSearchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 书籍列表
        unreadListModel = new DefaultListModel<>();
        unreadList = new JList<>(unreadListModel);
        unreadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        unreadList.setCellRenderer(new BookListCellRenderer());

        // 双击未读书籍设置为当前阅读
        unreadList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setSelectedUnreadAsCurrent();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(unreadList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        setUnreadAsCurrentButton = new JButton("设为当前阅读");
        setUnreadAsCurrentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSelectedUnreadAsCurrent();
            }
        });
        buttonPanel.add(setUnreadAsCurrentButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReadDirectoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("搜索书名/作者:"), BorderLayout.WEST);
        readSearchField = new JTextField();
        readSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterReadBooks(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterReadBooks(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterReadBooks(); }
        });
        searchPanel.add(readSearchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 书籍列表
        readListModel = new DefaultListModel<>();
        readList = new JList<>(readListModel);
        readList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        readList.setCellRenderer(new BookListCellRenderer());

        // 双击已读书籍重新阅读
        readList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    rereadSelectedBook();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(readList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        rereadButton = new JButton("重新阅读");
        rereadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rereadSelectedBook();
            }
        });
        buttonPanel.add(rereadButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFinishedDirectoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 说明标签
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("已读完的书籍列表（已读完次数 > 0）"));
        panel.add(infoPanel, BorderLayout.NORTH);

        // 书籍列表
        DefaultListModel<Book> finishedListModel = new DefaultListModel<>();
        JList<Book> finishedList = new JList<>(finishedListModel);
        finishedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        finishedList.setCellRenderer(new BookListCellRenderer());

        // 加载已读完书籍
        List<Book> finishedBooks = bookManager.getFinishedBooks();
        for (Book book : finishedBooks) {
            finishedListModel.addElement(book);
        }

        // 双击已读完书籍重新阅读
        finishedList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Book selectedBook = finishedList.getSelectedValue();
                    if (selectedBook != null) {
                        if (bookManager.rereadBook(selectedBook)) {
                            displayBook(selectedBook);
                            refreshBookLists();
                            JOptionPane.showMessageDialog(ReadingApp.this,
                                    "已重新开始阅读《" + selectedBook.getTitle() + "》",
                                    "重新阅读",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(finishedList);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 自定义列表单元格渲染器，用于显示书籍信息
    private class BookListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Book) {
                Book book = (Book) value;
                label.setText(book.getDisplayText());

                // 根据书籍状态设置不同的颜色
                if (book.getFinishedCount() > 0) {
                    // 已读完的书籍用深绿色显示
                    label.setForeground(new Color(0, 100, 0));
                } else if (book.isRead()) {
                    // 已读但未读完的书籍用蓝色显示
                    label.setForeground(new Color(0, 0, 150));
                } else {
                    // 未读的书籍用默认颜色
                    label.setForeground(Color.BLACK);
                }
            }
            return label;
        }
    }

    private JLabel createStyledLabel(String text, int style, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", style, size));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private void loadCurrentBook() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            displayBook(currentBook);
        } else {
            clearBookDisplay();
        }
    }

    private void getNextBook() {
        Book nextBook = bookManager.getRandomBook();
        if (nextBook != null) {
            displayBook(nextBook);
            updateStats();
            refreshBookLists();
        } else {
            JOptionPane.showMessageDialog(this,
                    "恭喜！您已经阅读完所有书籍！",
                    "阅读完成",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void displayBook(Book book) {
        titleLabel.setText("《" + book.getTitle() + "》");
        authorLabel.setText("作者: " + book.getAuthor());
        nationLabel.setText("国籍: " + book.getNation());
        eraLabel.setText("年代: " + book.getEra());
        statusLabel.setText("状态: " + book.getStatusText()); // 显示书籍状态
    }

    private void clearBookDisplay() {
        titleLabel.setText("");
        authorLabel.setText("");
        nationLabel.setText("");
        eraLabel.setText("");
        statusLabel.setText("");
    }

    // 标记为已读（不增加已读完次数，不清空当前书籍）
    private void markAsRead() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要将《" + currentBook.getTitle() + "》标记为已读吗？\n" +
                            "（这将增加阅读次数，但不会清空当前阅读状态）",
                    "确认标记为已读",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                bookManager.markAsRead();
                updateStats();
                refreshBookLists();
                // 更新当前书籍状态显示
                displayBook(currentBook);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "当前没有正在阅读的书籍！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // 标记为已读完（增加已读完次数，清空当前书籍）
    private void markAsFinished() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要将《" + currentBook.getTitle() + "》标记为已读完吗？\n" +
                            "（这将增加已读完次数，并清空当前阅读状态）",
                    "确认标记为已读完",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                bookManager.markAsFinished();
                updateStats();
                refreshBookLists();
                clearBookDisplay();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "当前没有正在阅读的书籍！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateStats() {
        int total = bookManager.getTotalBooksCount();
        int read = bookManager.getReadBooksCount();
        int finished = bookManager.getFinishedBooksCount();
        int remaining = bookManager.getRemainingBooksCount();

        statsLabel.setText(String.format(
                "总计: %d本 | 已读: %d本 | 已读完: %d本 | 剩余: %d本 | 进度: %.1f%%",
                total, read, finished, remaining, ((read + finished) * 100.0 / total)
        ));

        // 只有在tabbedPane已经初始化的情况下才更新标签页标题
        if (tabbedPane != null && tabbedPane.getTabCount() >= 3) {
            tabbedPane.setTitleAt(0, "未读目录 (" + remaining + ")");
            tabbedPane.setTitleAt(1, "已读目录 (" + read + ")");
            tabbedPane.setTitleAt(2, "已读完目录 (" + finished + ")");
        }
    }

    private void refreshBookLists() {
        // 刷新未读列表
        if (unreadListModel != null) {
            unreadListModel.clear();
            List<Book> unreadBooks = bookManager.searchUnreadBooks(
                    unreadSearchField != null ? unreadSearchField.getText() : "");
            for (Book book : unreadBooks) {
                unreadListModel.addElement(book);
            }
        }

        // 刷新已读列表
        if (readListModel != null) {
            readListModel.clear();
            List<Book> readBooks = bookManager.searchReadBooks(
                    readSearchField != null ? readSearchField.getText() : "");
            for (Book book : readBooks) {
                readListModel.addElement(book);
            }
        }

        updateStats();
    }

    private void filterUnreadBooks() {
        if (unreadListModel != null) {
            unreadListModel.clear();
            List<Book> filteredBooks = bookManager.searchUnreadBooks(
                    unreadSearchField != null ? unreadSearchField.getText() : "");
            for (Book book : filteredBooks) {
                unreadListModel.addElement(book);
            }
        }
    }

    private void filterReadBooks() {
        if (readListModel != null) {
            readListModel.clear();
            List<Book> filteredBooks = bookManager.searchReadBooks(
                    readSearchField != null ? readSearchField.getText() : "");
            for (Book book : filteredBooks) {
                readListModel.addElement(book);
            }
        }
    }

    private void setSelectedUnreadAsCurrent() {
        if (unreadList != null) {
            Book selectedBook = unreadList.getSelectedValue();
            if (selectedBook != null) {
                if (bookManager.setCurrentBook(selectedBook)) {
                    displayBook(selectedBook);
                    JOptionPane.showMessageDialog(this,
                            "已设置《" + selectedBook.getTitle() + "》为当前阅读书籍",
                            "设置成功",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "请先选择一本书籍！",
                        "提示",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void rereadSelectedBook() {
        if (readList != null) {
            Book selectedBook = readList.getSelectedValue();
            if (selectedBook != null) {
                if (bookManager.rereadBook(selectedBook)) {
                    displayBook(selectedBook);
                    refreshBookLists(); // 刷新列表以更新阅读次数
                    JOptionPane.showMessageDialog(this,
                            "已重新开始阅读《" + selectedBook.getTitle() + "》",
                            "重新阅读",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "请先选择一本书籍！",
                        "提示",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // 好词好句相关方法保持不变
    private void addMaxim() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            new MaximDialog(this, currentBook, bookManager).setVisible(true);
            refreshBookLists(); // 刷新列表以更新摘抄数量显示
            // 更新当前书籍状态显示（因为摘抄会自动标记为已读）
            displayBook(currentBook);
        } else {
            JOptionPane.showMessageDialog(this,
                    "当前没有正在阅读的书籍！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewMaxims() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            if (currentBook.getMaximCount() > 0) {
                new MaximListDialog(this, currentBook, bookManager).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "这本书还没有保存任何好词好句摘抄！",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "当前没有正在阅读的书籍！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                new ReadingApp().setVisible(true);
            }
        });
    }
}