import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
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
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JButton readButton;
    private JButton finishedButton;
    private JButton nextButton;

    // 目录区域组件
    private JTabbedPane tabbedPane;

    // 表格模型和排序器
    private BookTableModel unreadTableModel;
    private BookTableModel readTableModel;
    private BookTableModel finishedTableModel;
    private TableRowSorter<BookTableModel> unreadSorter;
    private TableRowSorter<BookTableModel> readSorter;
    private TableRowSorter<BookTableModel> finishedSorter;

    // 表格组件
    private JTable unreadTable;
    private JTable readTable;
    private JTable finishedTable;

    // 搜索字段
    private JTextField unreadSearchField;
    private JTextField readSearchField;
    private JTextField finishedSearchField;

    public ReadingApp() {
        bookManager = new BookManager();
        initializeUI();
        loadCurrentBook();
        refreshBookTables();
    }

    private void initializeUI() {
        setTitle("随机书籍阅读器 - 表格版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
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
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        titleLabel = createStyledLabel("", Font.BOLD, 16);
        authorLabel = createStyledLabel("", Font.PLAIN, 14);
        nationLabel = createStyledLabel("", Font.PLAIN, 12);
        eraLabel = createStyledLabel("", Font.PLAIN, 12);
        statusLabel = createStyledLabel("", Font.ITALIC, 12);

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
        finishedButton = new JButton("标记为已读完");
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

        // 书籍表格
        unreadTableModel = new BookTableModel(bookManager.getUnreadBooks(), false);
        unreadTable = new JTable(unreadTableModel);
        unreadSorter = new TableRowSorter<>(unreadTableModel);
        unreadTable.setRowSorter(unreadSorter);

        // 设置列宽
        unreadTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 书名
        unreadTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 作者
        unreadTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 国籍
        unreadTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 年代

        // 双击未读书籍设置为当前阅读
        unreadTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setSelectedUnreadAsCurrent();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(unreadTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton setUnreadAsCurrentButton = new JButton("设为当前阅读");
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

        // 书籍表格
        readTableModel = new BookTableModel(bookManager.getReadBooks(), true);
        readTable = new JTable(readTableModel);
        readSorter = new TableRowSorter<>(readTableModel);
        readTable.setRowSorter(readSorter);

        // 设置列宽
        readTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 书名
        readTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 作者
        readTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 国籍
        readTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 年代
        readTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 已读次数
        readTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 已读完次数
        readTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // 摘抄数量

        // 双击已读书籍重新阅读
        readTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    rereadSelectedBook();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(readTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton rereadButton = new JButton("重新阅读");
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

        // 搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("搜索书名/作者:"), BorderLayout.WEST);
        finishedSearchField = new JTextField();
        finishedSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterFinishedBooks(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterFinishedBooks(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterFinishedBooks(); }
        });
        searchPanel.add(finishedSearchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 书籍表格
        finishedTableModel = new BookTableModel(bookManager.getFinishedBooks(), true);
        finishedTable = new JTable(finishedTableModel);
        finishedSorter = new TableRowSorter<>(finishedTableModel);
        finishedTable.setRowSorter(finishedSorter);

        // 设置列宽
        finishedTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 书名
        finishedTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 作者
        finishedTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 国籍
        finishedTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 年代
        finishedTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 已读次数
        finishedTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 已读完次数
        finishedTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // 摘抄数量

        // 双击已读完书籍重新阅读
        finishedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Book selectedBook = getSelectedFinishedBook();
                    if (selectedBook != null) {
                        if (bookManager.rereadBook(selectedBook)) {
                            displayBook(selectedBook);
                            refreshBookTables();
                            JOptionPane.showMessageDialog(ReadingApp.this,
                                    "已重新开始阅读《" + selectedBook.getTitle() + "》",
                                    "重新阅读",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(finishedTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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
            refreshBookTables();
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
        statusLabel.setText("状态: " + book.getStatusText());
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
                refreshBookTables();
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
                refreshBookTables();
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

        // 更新标签页标题
        if (tabbedPane != null && tabbedPane.getTabCount() >= 3) {
            tabbedPane.setTitleAt(0, "未读目录 (" + remaining + ")");
            tabbedPane.setTitleAt(1, "已读目录 (" + read + ")");
            tabbedPane.setTitleAt(2, "已读完目录 (" + finished + ")");
        }
    }

    private void refreshBookTables() {
        // 刷新未读表格
        if (unreadTableModel != null) {
            unreadTableModel.setBooks(bookManager.searchUnreadBooks(
                    unreadSearchField != null ? unreadSearchField.getText() : ""));
        }

        // 刷新已读表格
        if (readTableModel != null) {
            readTableModel.setBooks(bookManager.searchReadBooks(
                    readSearchField != null ? readSearchField.getText() : ""));
        }

        // 刷新已读完表格
        if (finishedTableModel != null) {
            finishedTableModel.setBooks(bookManager.searchFinishedBooks(
                    finishedSearchField != null ? finishedSearchField.getText() : ""));
        }

        updateStats();
    }

    private void filterUnreadBooks() {
        if (unreadTableModel != null) {
            unreadTableModel.setBooks(bookManager.searchUnreadBooks(
                    unreadSearchField != null ? unreadSearchField.getText() : ""));
        }
    }

    private void filterReadBooks() {
        if (readTableModel != null) {
            readTableModel.setBooks(bookManager.searchReadBooks(
                    readSearchField != null ? readSearchField.getText() : ""));
        }
    }

    private void filterFinishedBooks() {
        if (finishedTableModel != null) {
            finishedTableModel.setBooks(bookManager.searchFinishedBooks(
                    finishedSearchField != null ? finishedSearchField.getText() : ""));
        }
    }

    private void setSelectedUnreadAsCurrent() {
        if (unreadTable != null) {
            int selectedRow = unreadTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = unreadTable.convertRowIndexToModel(selectedRow);
                Book selectedBook = unreadTableModel.getBookAt(modelRow);
                if (selectedBook != null) {
                    if (bookManager.setCurrentBook(selectedBook)) {
                        displayBook(selectedBook);
                        JOptionPane.showMessageDialog(this,
                                "已设置《" + selectedBook.getTitle() + "》为当前阅读书籍",
                                "设置成功",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
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
        if (readTable != null) {
            int selectedRow = readTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = readTable.convertRowIndexToModel(selectedRow);
                Book selectedBook = readTableModel.getBookAt(modelRow);
                if (selectedBook != null) {
                    if (bookManager.rereadBook(selectedBook)) {
                        displayBook(selectedBook);
                        refreshBookTables();
                        JOptionPane.showMessageDialog(this,
                                "已重新开始阅读《" + selectedBook.getTitle() + "》",
                                "重新阅读",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "请先选择一本书籍！",
                        "提示",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private Book getSelectedFinishedBook() {
        if (finishedTable != null) {
            int selectedRow = finishedTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = finishedTable.convertRowIndexToModel(selectedRow);
                return finishedTableModel.getBookAt(modelRow);
            }
        }
        return null;
    }

    // 好词好句相关方法
    private void addMaxim() {
        Book currentBook = bookManager.getCurrentBook();
        if (currentBook != null) {
            new MaximDialog(this, currentBook, bookManager).setVisible(true);
            refreshBookTables();
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
        // 抑制 libpng 警告
        System.setProperty("sun.awt.silent", "true");

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