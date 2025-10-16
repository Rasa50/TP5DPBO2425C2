import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.*;

public class ProductMenu {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField hargaField;
    private JComboBox<String> kategoriComboBox;
    private JComboBox<String> StatusBarang;
    private JTable productTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JLabel idLabel;
    private JLabel namaLabel;
    private JLabel hargaLabel;
    private JLabel kategoriLabel;
    private JLabel Status;

    private Database database;
    private DefaultTableModel tableModel;
    private boolean isUpdate = false; // untuk mendeteksi mode add/update

    public ProductMenu() {
        database = new Database();

        // isi combo box
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                "Elektronik", "Makanan", "Minuman", "Alat Tulis", "Pakaian", "Aksesoris"
        }));
        StatusBarang.setModel(new DefaultComboBoxModel<>(new String[]{
                "Tersedia", "Tidak Tersedia"
        }));

        // Load data awal
        loadTableData();

        // Event tombol
        addUpdateButton.addActionListener(e -> handleAddOrUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        cancelButton.addActionListener(e -> clearForm());

        // Klik tabel → isi field
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = productTable.getSelectedRow();
                if (row >= 0) {
                    idField.setText(productTable.getValueAt(row, 0).toString());
                    namaField.setText(productTable.getValueAt(row, 1).toString());
                    hargaField.setText(productTable.getValueAt(row, 2).toString());
                    kategoriComboBox.setSelectedItem(productTable.getValueAt(row, 3).toString());
                    StatusBarang.setSelectedItem(productTable.getValueAt(row, 4).toString());

                    idField.setEnabled(false);
                    isUpdate = true;
                    addUpdateButton.setText("Update");
                }
            }
        });
    }

    private void loadTableData() {
        try (ResultSet rs = database.executeQuery("SELECT * FROM product")) {
            String[] columnNames = {"ID", "Nama", "Harga", "Kategori", "Status"};
            tableModel = new DefaultTableModel(columnNames, 0);

            while (rs.next()) {
                Object[] row = {
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getDouble("harga"),
                        rs.getString("kategori"),
                        rs.getString("status")
                };
                tableModel.addRow(row);
            }

            productTable.setModel(tableModel);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal load data: " + e.getMessage());
        }
    }

    private void handleAddOrUpdate() {
        String id = idField.getText().trim();
        String nama = namaField.getText().trim();
        String hargaText = hargaField.getText().trim();
        String kategori = kategoriComboBox.getSelectedItem().toString();
        String status = StatusBarang.getSelectedItem().toString();

        if (id.isEmpty() || nama.isEmpty() || hargaText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!");
            return;
        }

        double harga;
        try {
            harga = Double.parseDouble(hargaText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga harus berupa angka!");
            return;
        }

        try {
            if (isUpdate) {
                database.executeUpdate(
                        "UPDATE product SET nama=?, harga=?, kategori=?, status=? WHERE id=?",
                        nama, harga, kategori, status, id
                );
                JOptionPane.showMessageDialog(null, "Data berhasil diperbarui!");
            } else {
                database.executeUpdate(
                        "INSERT INTO product (id, nama, harga, kategori, status) VALUES (?, ?, ?, ?, ?)",
                        id, nama, harga, kategori, status
                );
                JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan!");
            }

            clearForm();
            loadTableData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data: " + e.getMessage());
        }
    }

    private void handleDelete() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Pilih data terlebih dahulu!");
            return;
        }

        String id = productTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(null,
                "Yakin ingin menghapus ID: " + id + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            database.executeUpdate("DELETE FROM product WHERE id = ?", id);
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
            loadTableData();
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal menghapus data: " + e.getMessage());
        }
    }

    private void clearForm() {
        idField.setText("");
        namaField.setText("");
        hargaField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        StatusBarang.setSelectedIndex(0);

        idField.setEnabled(true);
        isUpdate = false;
        addUpdateButton.setText("Add");
        productTable.clearSelection();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Data Produk");
        frame.setContentPane(new ProductMenu().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
