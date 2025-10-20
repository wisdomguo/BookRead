import javax.swing.table.AbstractTableModel;
import java.util.List;

public class BookTableModel extends AbstractTableModel {
    private List<Book> books;
    private final String[] columnNames;
    private final boolean showReadInfo;

    public BookTableModel(List<Book> books, boolean showReadInfo) {
        this.books = books;
        this.showReadInfo = showReadInfo;

        if (showReadInfo) {
            columnNames = new String[]{"书名", "作者", "国籍", "年代", "已读次数", "已读完次数", "摘抄数量"};
        } else {
            columnNames = new String[]{"书名", "作者", "国籍", "年代"};
        }
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return books.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Book book = books.get(rowIndex);

        switch (columnIndex) {
            case 0: return book.getTitle();
            case 1: return book.getAuthor();
            case 2: return book.getNation();
            case 3: return book.getEra();
            case 4: return showReadInfo ? book.getReadCount() : null;
            case 5: return showReadInfo ? book.getFinishedCount() : null;
            case 6: return showReadInfo ? book.getMaximCount() : null;
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= 4 && showReadInfo) {
            return Integer.class;
        }
        return String.class;
    }

    public Book getBookAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < books.size()) {
            return books.get(rowIndex);
        }
        return null;
    }
}