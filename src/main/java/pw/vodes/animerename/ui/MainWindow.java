package pw.vodes.animerename.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;

import pw.vodes.animerename.App;
import pw.vodes.animerename.FileParse;
import pw.vodes.animerename.TagUtil;
import pw.vodes.animerename.cli.CommandLineUtil;

public class MainWindow {

	public JFrame frame;
	private JTable table;
	private JTextField txtanimetitleseasonepisode;
	private JTextField textField;
	private File previousParent;
	private boolean clicked = false;
	
	public MainWindow() {
		changeTheme();
		initialize();
	}
	
	public void changeTheme() {
		UIManager.put("ScrollBar.width", 12);
		UIManager.put("ScrollBar.trackArc", 999);
		UIManager.put("ScrollBar.thumbArc", 999);
		UIManager.put("ScrollBar.trackInsets", new Insets(2, 4, 2, 4));
		UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
		UIManager.put("ScrollBar.track", new Color(0xe0e0e0));
		UIManager.put("TextComponent.arc", 5);
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 680, 575);
		frame.setMinimumSize(new Dimension(600, 465));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Anime Renamer / Hardlink Creator");
		
		final JCheckBox chckbxNewCheckBox_2 = new JCheckBox("Allow other media files");
		
		JButton btnNewButton = new JButton("Load Folder");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser f = new JFileChooser();
				if(previousParent != null && previousParent.exists() && previousParent.isDirectory()) {
					f = new JFileChooser(previousParent);
				}
				f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(f.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					previousParent = f.getSelectedFile().getParentFile();
					App.currentFiles.clear();
					for(File file : f.getSelectedFile().listFiles()) {
						if(chckbxNewCheckBox_2.isSelected()) {
							if(FilenameUtils.isExtension(file.getName().toLowerCase(), new String[] {"mkv", "mp4", "mp3", "ac3", "eac3", "aac", "ogg", "avi", "opus", "flac", "ape", "mka", "mks", "ass", "xml", "cbz", "cbr", "cbt"})) {
								App.currentFiles.add(file);
							}
						} else {
							if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("mkv")) {
								App.currentFiles.add(file);
							}
						}

					}
					updateTable();
				}
			}
		});
		btnNewButton.setFocusPainted(false);
		
		final JCheckBox chckbxNewCheckBox = new JCheckBox("Change mkv Title");
		chckbxNewCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(chckbxNewCheckBox.isSelected()) {
					try {
						Runtime.getRuntime().exec("mkvpropedit");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, "You do not have mkvpropedit in your PATH!", "Error", 0);
						chckbxNewCheckBox.setSelected(false);
					}
				}		
			}
		});
		chckbxNewCheckBox.setFocusPainted(false);
		
		final JCheckBox chckbxNewCheckBox_1 = new JCheckBox("use Filename Template");
		chckbxNewCheckBox_1.setFocusPainted(false);
		chckbxNewCheckBox_1.setSelected(true);
		chckbxNewCheckBox_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {			
				textField.setEnabled(!chckbxNewCheckBox_1.isSelected());
			}
		});
		
		JButton btnNewButton_1 = new JButton("Rename Files");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ((File)table.getValueAt(count, -1)) - gets file for input
				// ((File)table.getValueAt(count, -2)) - gets file for output
				for (int count = 0; count < table.getRowCount(); count++) {
					File in = ((File)table.getValueAt(count, -1));
					File out = ((File)table.getValueAt(count, -2));
					in.renameTo(out);
				}
				
				JOptionPane.showMessageDialog(frame, "Done renaming Files.", "Done", 1);
				if(chckbxNewCheckBox.isSelected()) {
					for (int count = 0; count < table.getRowCount(); count++) {
						String template = txtanimetitleseasonepisode.getText();
						if(!chckbxNewCheckBox_1.isSelected()) {
							template = textField.getText();
						}
						String title = App.doTokenReplace(((File)table.getValueAt(count, -1)).getName(), template, (File)table.getValueAt(count, -1));
						String command = String.format("mkvpropedit \"%s\" --edit info --set title=\"%s\"", ((File)table.getValueAt(count, -2)).getAbsolutePath(), title);
						ArrayList<String> commands = new ArrayList<>();
						commands.add(command);
						try {
							CommandLineUtil.runCommand(commands, true).waitFor();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					JOptionPane.showMessageDialog(frame, "Done setting the mkv titles.", "Done", 1);
				}
				
				App.currentFiles.clear();
				updateTable();
			}
		});
		btnNewButton_1.setFocusPainted(false);
		
		JLabel lblNewLabel = new JLabel("Preview & Editing");
		
		JScrollPane scrollPane = new JScrollPane();
		
		JLabel lblNewLabel_1 = new JLabel("Filename Template");
		
		txtanimetitleseasonepisode = new JTextField();
		txtanimetitleseasonepisode.setText("%release_group_b% %anime_title% - %season_number_s%%episode_number_e%");
		txtanimetitleseasonepisode.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				  updateTable();
			  }
			  public void removeUpdate(DocumentEvent e) {
				  updateTable();
			  }
			  public void insertUpdate(DocumentEvent e) {
				  updateTable();
			  }
			});
		txtanimetitleseasonepisode.setColumns(10);
		
		JButton btnNewButton_2 = new JButton("?");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, App.getTokenString(), "Available Tokens", 1);
			}
		});
		btnNewButton_2.setFocusPainted(false);
		
		JLabel lblNewLabel_2 = new JLabel("Mkv Title Template");
		
		textField = new JTextField();
		textField.setEnabled(false);
		textField.setText("%anime_title% - %season_number_s%%episode_number_e%");
		textField.setColumns(10);
		
		JButton btnNewButton_3 = new JButton("Create Hardlinks");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File outDir = null;
				for (int count = 0; count < table.getRowCount(); count++) {
					File in = ((File)table.getValueAt(count, -1));
					outDir = new File(in.getParentFile(), "links");
					outDir.mkdir();
					File out = new File(outDir, ((File)table.getValueAt(count, -2)).getName());
					try {
						Files.createLink(out.toPath(), in.toPath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				JOptionPane.showMessageDialog(frame, "Done creating hardlinks.", "Done", 1);
				try {
					Desktop.getDesktop().open(outDir);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				App.currentFiles.clear();
				updateTable();
			}
		});
		btnNewButton_3.setFocusPainted(false);
		
		JButton btnNewButton_4 = new JButton("Manage Overrides");
		btnNewButton_4.setFocusPainted(false);
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OverridesWindow dialog = new OverridesWindow();
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
				dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor((Component) e.getSource()));
			}
		});
		
		JButton btnNewButton_4_1 = new JButton("Fix Tagging");
		btnNewButton_4_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxNewCheckBox_2.isSelected()) {
					JOptionPane.showMessageDialog(frame, "You can only fix the tags for mkv files!", "Error", 0);
					return;
				}
				
				for (int count = 0; count < table.getRowCount(); count++) {
					File in = ((File)table.getValueAt(count, -1));
					TagUtil.fixTagging(in, null);
				}
				JOptionPane.showMessageDialog(frame, "Done fixing tags.", "Done", 1);
			}
		});
		btnNewButton_4_1.setFocusPainted(false);
				
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(chckbxNewCheckBox, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxNewCheckBox_2)
							.addGap(418))
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
									.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
											.addComponent(lblNewLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(btnNewButton_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
											.addComponent(btnNewButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
											.addComponent(btnNewButton_4, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
											.addGroup(groupLayout.createSequentialGroup()
												.addComponent(btnNewButton_3, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(btnNewButton_4_1, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)))))
								.addContainerGap())
							.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblNewLabel_1, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
										.addGap(297))
									.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblNewLabel_2, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chckbxNewCheckBox_1, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE))
									.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
											.addComponent(textField, Alignment.LEADING)
											.addComponent(txtanimetitleseasonepisode, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 404, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnNewButton_2, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)))
								.addGap(192)))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton)
						.addComponent(btnNewButton_4))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton_1)
						.addComponent(btnNewButton_3)
						.addComponent(btnNewButton_4_1))
					.addGap(5)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxNewCheckBox)
						.addComponent(chckbxNewCheckBox_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtanimetitleseasonepisode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNewButton_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_2)
						.addComponent(chckbxNewCheckBox_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel)
					.addGap(6)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
					.addGap(4))
		);
		DefaultTableModel model = new DefaultTableModel() {

			@Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex != 0;
            }
        };
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(true);
		scrollPane.setViewportView(table);
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setAutoCreateGaps(true);
		frame.getContentPane().setLayout(groupLayout);
	}
	
	public void updateTable() {
		if(App.currentFiles == null) {
			return;
		}
		ArrayList<FileParse> parsed = new ArrayList<FileParse>();
		for(File file : App.currentFiles) {
			parsed.add(new FileParse(file, new File(file.getParentFile(), App.doTokenReplace(file.getName(), txtanimetitleseasonepisode.getText(), file).trim() + "." + FilenameUtils.getExtension(file.getName()))));
		}
		table.setModel(new FileParseTableModel(parsed));
		table.repaint();
	}
}
