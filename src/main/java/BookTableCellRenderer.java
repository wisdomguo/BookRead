import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BookTableCellRenderer extends DefaultTableCellRenderer {
    private final boolean showReadInfo;

    public BookTableCellRenderer(boolean showReadInfo) {
        this.showReadInfo = showReadInfo;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // 设置字体
        c.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 设置内边距
        if (c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        // 修复：使用模型索引而不是视图索引来判断奇偶行
        int modelRow = table.convertRowIndexToModel(row);

        // 设置交替行颜色
        if (!isSelected) {
            if (modelRow % 2 == 0) {
                c.setBackground(Color.WHITE); // 偶数行白色
            } else {
                c.setBackground(new Color(240, 240, 240)); // 奇数行浅灰色背景
            }
        } else {
            // 选中状态使用表格的选中背景色
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        }

        // 数值列右对齐
        if (value instanceof Integer || value instanceof Double) {
            setHorizontalAlignment(JLabel.RIGHT);
        } else {
            setHorizontalAlignment(JLabel.LEFT);
        }

        // 移除表头设置代码（应该在表格初始化时设置）
        // if (table.getTableHeader() != null) { ... }

        return c;
    }
}