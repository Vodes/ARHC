package pw.vodes.animerename;

import java.io.File;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class FileParseTableModel extends AbstractTableModel {

	private ArrayList<FileParse> parseFiles;
	private String[] columnNames = { "File", "Target" };

	public FileParseTableModel(ArrayList<FileParse> parseFiles) {
		this.parseFiles = parseFiles;
	}

	@Override
	public int getRowCount() {
		if (parseFiles == null) {
			return 0;
		} else {
			return parseFiles.size();
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Class getColumnClass(int col) {
		return String.class;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 1) {
			return true;
		}
		return false;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex == 1) {
			try {
				parseFiles.get(rowIndex).target = new File(parseFiles.get(rowIndex).target.getParentFile(), (String)aValue);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object temp = null;
		if (col == 0) {
			temp = parseFiles.get(row).file.getName();
		} else if (col == 1) {
			temp = parseFiles.get(row).target.getName();
		} else if (col == -1) {
			temp = parseFiles.get(row).file;
		} else if (col == -2) {
			temp = parseFiles.get(row).target;
		}
		return temp;
	}

}
