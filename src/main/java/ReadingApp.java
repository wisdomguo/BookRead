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
    private JLabel statsLabel;
    private JButton readButton;
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

    // 在 initializeUI() 方法中修改 createTopPanel() 方法：

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
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        titleLabel = createStyledLabel("", Font.BOLD, 16);
        authorLabel = createStyledLabel("", Font.PLAIN, 14);
        nationLabel = createStyledLabel("", Font.PLAIN, 12);
        eraLabel = createStyledLabel("", Font.PLAIN, 12);

        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(nationLabel);
        infoPanel.add(eraLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addMaxim() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            new MaximDialog(this, currentBook, bookManager).setVisible(true);
            refreshBookLists(); // 刷新列表以更新摘抄数量显示
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

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        statsLabel = new JLabel();
        // 不在这里调用updateStats()，等所有组件初始化完成后再调用
        panel.add(statsLabel);

        // 添加按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        readButton = new JButton("标记为已读");
        nextButton = new JButton("随机抽取下一本");

        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAsRead();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNextBook();
            }
        });

        buttonPanel.add(readButton);
        buttonPanel.add(nextButton);

        panel.add(buttonPanel);

        return panel;
    }

    private JTabbedPane createDirectoryTabbedPane() {
        JTabbedPane pane = new JTabbedPane();

        // 未读目录标签页
        JPanel unreadPanel = createUnreadDirectoryPanel();
        pane.addTab("未读目录", unreadPanel);

        // 已读目录标签页
        JPanel readPanel = createReadDirectoryPanel();
        pane.addTab("已读目录", readPanel);

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

    // 自定义列表单元格渲染器，用于显示书籍信息
    private class BookListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Book) {
                Book book = (Book) value;
                label.setText(book.getDisplayText());

                // 根据阅读次数设置不同的颜色
                if (book.getReadCount() > 0) {
                    if (book.getReadCount() == 1) {
                        label.setForeground(new Color(0, 100, 0)); // 深绿色
                    } else if (book.getReadCount() == 2) {
                        label.setForeground(new Color(0, 150, 0)); // 绿色
                    } else {
                        label.setForeground(new Color(0, 200, 0)); // 亮绿色
                    }
                }

                // 如果有摘抄，添加特殊标记
                if (book.getMaximCount() > 0) {
                    label.setIcon(new ImageIcon("src/main/resources/icons/note.png")); // 可选：添加图标
                    label.setToolTipText("有 " + book.getMaximCount() + " 条摘抄");
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
    }

    private void clearBookDisplay() {
        titleLabel.setText("");
        authorLabel.setText("");
        nationLabel.setText("");
        eraLabel.setText("");
    }

    private void markAsRead() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要将《" + currentBook.getTitle() + "》标记为已读吗？",
                    "确认标记",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                bookManager.markAsRead();
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
        int remaining = bookManager.getRemainingBooksCount();

        statsLabel.setText(String.format(
                "总计: %d本 | 已读: %d本 | 剩余: %d本 | 进度: %.1f%%",
                total, read, remaining, (read * 100.0 / total)
        ));

        // 只有在tabbedPane已经初始化的情况下才更新标签页标题
        if (tabbedPane != null && tabbedPane.getTabCount() >= 2) {
            tabbedPane.setTitleAt(0, "未读目录 (" + remaining + ")");
            tabbedPane.setTitleAt(1, "已读目录 (" + read + ")");
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
                            "已重新开始阅读《" + selectedBook.getTitle() + "》，阅读次数: " + selectedBook.getReadCount(),
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