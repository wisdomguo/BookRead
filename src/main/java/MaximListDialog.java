import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MaximListDialog extends JDialog {
    private Book currentBook;
    private BookManager bookManager;
    private JList<Maxim> maximList;
    private DefaultListModel<Maxim> listModel;
    private JTextArea detailArea;

    public MaximListDialog(Frame parent, Book book, BookManager bookManager) {
        super(parent, "好词好句列表 - " + book.getTitle(), true);
        this.currentBook = book;
        this.bookManager = bookManager;
        initializeUI();
        loadMaxims();
    }

    private void initializeUI() {
        setSize(600, 500);
        setLocationRelativeTo(getOwner());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("《" + currentBook.getTitle() + "》共有 " + currentBook.getMaximCount() + " 条摘抄"));
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // 中间内容面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);

        // 左侧列表
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("摘抄列表"));
        listModel = new DefaultListModel<>();
        maximList = new JList<>(listModel);
        maximList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        maximList.setCellRenderer(new MaximListCellRenderer());

        maximList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showMaximDetail(maximList.getSelectedValue());
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(maximList);
        listPanel.add(listScroll, BorderLayout.CENTER);
        splitPane.setLeftComponent(listPanel);

        // 右侧详情
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("摘抄详情"));
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailPanel.add(detailScroll, BorderLayout.CENTER);
        splitPane.setRightComponent(detailPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadMaxims() {
        List<Maxim> maxims = bookManager.getMaxims(currentBook);
        listModel.clear();
        for (Maxim maxim : maxims) {
            listModel.addElement(maxim);
        }
    }

    private void showMaximDetail(Maxim maxim) {
        if (maxim != null) {
            detailArea.setText(String.format("句子:\n%s\n\n解析:\n%s\n\n保存时间:\n%s",
                    maxim.getSentence(), maxim.getAnalysis(), maxim.getSaveTime()));
        } else {
            detailArea.setText("");
        }
    }

    // 自定义列表单元格渲染器
    private class MaximListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Maxim) {
                Maxim maxim = (Maxim) value;
                // 显示句子的前50个字符
                String displayText = maxim.getSentence();
                if (displayText.length() > 50) {
                    displayText = displayText.substring(0, 50) + "...";
                }
                label.setText((index + 1) + ". " + displayText);
            }
            return label;
        }
    }
}