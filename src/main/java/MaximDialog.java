import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MaximDialog extends JDialog {
    private JTextArea sentenceArea;
    private JTextArea analysisArea;
    private Book currentBook;
    private BookManager bookManager;
    private JLabel maximCountLabel;

    public MaximDialog(Frame parent, Book book, BookManager bookManager) {
        super(parent, "好词好句摘抄本 - " + book.getTitle(), true);
        this.currentBook = book;
        this.bookManager = bookManager;
        initializeUI();
    }

    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部信息面板
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maximCountLabel = new JLabel("当前已保存 " + currentBook.getMaximCount() + " 条摘抄");
        infoPanel.add(maximCountLabel);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // 中间输入面板
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        // 句子输入区域
        JPanel sentencePanel = new JPanel(new BorderLayout(5, 5));
        sentencePanel.setBorder(BorderFactory.createTitledBorder("摘抄句子"));
        sentenceArea = new JTextArea(5, 20);
        sentenceArea.setLineWrap(true);
        sentenceArea.setWrapStyleWord(true);
        JScrollPane sentenceScroll = new JScrollPane(sentenceArea);
        sentencePanel.add(sentenceScroll, BorderLayout.CENTER);
        inputPanel.add(sentencePanel);

        // 解析输入区域
        JPanel analysisPanel = new JPanel(new BorderLayout(5, 5));
        analysisPanel.setBorder(BorderFactory.createTitledBorder("句子解析"));
        analysisArea = new JTextArea(5, 20);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        JScrollPane analysisScroll = new JScrollPane(analysisArea);
        analysisPanel.add(analysisScroll, BorderLayout.CENTER);
        inputPanel.add(analysisPanel);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMaxim();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void saveMaxim() {
        String sentence = sentenceArea.getText().trim();
        String analysis = analysisArea.getText().trim();

        if (sentence.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入摘抄的句子！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Maxim maxim = new Maxim(sentence, analysis);
        bookManager.saveMaxim(currentBook, maxim);

        // 更新计数显示
        maximCountLabel.setText("当前已保存 " + currentBook.getMaximCount() + " 条摘抄");

        JOptionPane.showMessageDialog(this, "保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);

        // 清空输入框
        sentenceArea.setText("");
        analysisArea.setText("");
    }
}