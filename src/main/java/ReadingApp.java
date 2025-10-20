import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ReadingApp extends JFrame {
    private BookManager bookManager;
    private JLabel titleLabel;
    private JLabel authorLabel;
    private JLabel nationLabel;
    private JLabel eraLabel;
    private JLabel statsLabel;
    private JButton readButton;
    private JButton nextButton;
    
    public ReadingApp() {
        bookManager = new BookManager();
        initializeUI();
        loadCurrentBook();
    }
    
    private void initializeUI() {
        setTitle("随机书籍阅读器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 书籍信息面板
        JPanel bookPanel = createBookPanel();
        mainPanel.add(bookPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 统计信息面板
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        
        add(mainPanel);
    }
    
    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("当前阅读书籍"));
        
        titleLabel = createStyledLabel("", Font.BOLD, 16);
        authorLabel = createStyledLabel("", Font.PLAIN, 14);
        nationLabel = createStyledLabel("", Font.PLAIN, 12);
        eraLabel = createStyledLabel("", Font.PLAIN, 12);
        
        panel.add(titleLabel);
        panel.add(authorLabel);
        panel.add(nationLabel);
        panel.add(eraLabel);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
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
        
        panel.add(readButton);
        panel.add(nextButton);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        statsLabel = new JLabel();
        updateStats();
        panel.add(statsLabel);
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
            getNextBook();
        }
    }
    
    private void getNextBook() {
        Book nextBook = bookManager.getRandomBook();
        if (nextBook != null) {
            displayBook(nextBook);
            updateStats();
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
                getNextBook();
            }
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 修正：使用正确的方法名
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                    // 如果设置系统外观失败，使用默认外观
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