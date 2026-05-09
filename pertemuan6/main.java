import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * Main - Form Data Nilai Mahasiswa
 * Pertemuan 6 - Pemrograman 2
 * Penggunaan Database pada Aplikasi
 */
public class Main extends JFrame {

    JTextField nimTF, namaTF, nil1TF, nil2TF, rataTF;
    DefaultTableModel tableModel;
    JTable table;

    public Main() {
        DatabaseHelper.initDB();

        setTitle("Data Nilai Mahasiswa - Pertemuan 6");
        setSize(620, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // ===== Panel Form Input (NORTH) =====
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 5, 10),
            BorderFactory.createTitledBorder("Input Data Nilai")));

        formPanel.add(new JLabel("NIM:"));
        nimTF = new JTextField();
        formPanel.add(nimTF);

        formPanel.add(new JLabel("Nama:"));
        namaTF = new JTextField();
        formPanel.add(namaTF);

        formPanel.add(new JLabel("Nilai 1:"));
        nil1TF = new JTextField();
        formPanel.add(nil1TF);

        formPanel.add(new JLabel("Nilai 2:"));
        nil2TF = new JTextField();
        formPanel.add(nil2TF);

        formPanel.add(new JLabel("Rata-rata:"));
        rataTF = new JTextField();
        rataTF.setEditable(false);  // otomatis dihitung
        rataTF.setBackground(new Color(240, 240, 240));
        formPanel.add(rataTF);

        // ===== Auto hitung rata saat Nilai 1/2 diketik =====
        nil1TF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                hitungRata();
            }
        });
        nil2TF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                hitungRata();
            }
        });

        // ===== Panel Tombol (CENTER) =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        JButton tambahBtn = new JButton("Tambah");
        JButton cariBtn   = new JButton("Cari");
        JButton updateBtn = new JButton("Update");
        JButton hapusBtn  = new JButton("Hapus");
        JButton tampilBtn = new JButton("Tampil Semua");
        JButton clearBtn  = new JButton("Clear");

        // warnain tombol biar lebih jelas
        tambahBtn.setBackground(new Color(70, 130, 180));
        tambahBtn.setForeground(Color.WHITE);
        hapusBtn.setBackground(new Color(205, 92, 92));
        hapusBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(new Color(60, 179, 113));
        updateBtn.setForeground(Color.WHITE);

        btnPanel.add(tambahBtn);
        btnPanel.add(cariBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(hapusBtn);
        btnPanel.add(tampilBtn);
        btnPanel.add(clearBtn);

        // ===== Tabel (SOUTH) =====
        String[] kolom = {"NIM", "Nama", "Nilai 1", "Nilai 2", "Rata-rata"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 10, 10, 10),
            BorderFactory.createTitledBorder("Tabel Data Nilai")));
        scrollPane.setPreferredSize(new Dimension(0, 220));

        add(formPanel, BorderLayout.NORTH);
        add(btnPanel,  BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // ===== Klik baris tabel → isi form otomatis =====
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    nimTF.setText(tableModel.getValueAt(row, 0).toString());
                    namaTF.setText(tableModel.getValueAt(row, 1).toString());
                    nil1TF.setText(tableModel.getValueAt(row, 2).toString());
                    nil2TF.setText(tableModel.getValueAt(row, 3).toString());
                    rataTF.setText(tableModel.getValueAt(row, 4).toString());
                    nimTF.setEditable(false); // NIM primary key, ga boleh diubah
                }
            }
        });

        // ===== Aksi Tambah =====
        tambahBtn.addActionListener(e -> {
            try {
                String nim  = nimTF.getText().trim();
                String nama = namaTF.getText().trim();
                if (nim.isEmpty() || nama.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "NIM dan Nama harus diisi!");
                    return;
                }
                double nil1 = Double.parseDouble(nil1TF.getText().trim());
                double nil2 = Double.parseDouble(nil2TF.getText().trim());
                DatabaseHelper.tambahData(nim, nama, nil1, nil2);
                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
                clearFields();
                loadSemuaData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Nilai 1 & Nilai 2 harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal! NIM mungkin sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ===== Aksi Cari (by nama, LIKE search) =====
        cariBtn.addActionListener(e -> {
            String keyword = namaTF.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Isi kolom Nama untuk mencari!");
                return;
            }
            tableModel.setRowCount(0);
            try (Connection conn = DatabaseHelper.connect();
                 ResultSet rs = DatabaseHelper.cariData(conn, keyword)) {
                boolean ada = false;
                while (rs.next()) {
                    ada = true;
                    tableModel.addRow(new Object[]{
                        rs.getString("nim"),
                        rs.getString("nama"),
                        rs.getString("nil1"),
                        rs.getString("nil2"),
                        rs.getString("rata")
                    });
                }
                if (!ada) {
                    JOptionPane.showMessageDialog(this,
                        "Data tidak ditemukan untuk: \"" + keyword + "\"",
                        "Informasi", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                System.out.println("Cari gagal: " + ex.getMessage());
            }
        });

        // ===== Aksi Update =====
        updateBtn.addActionListener(e -> {
            String nim = nimTF.getText().trim();
            if (nim.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih data dari tabel dulu!");
                return;
            }
            try {
                double nil1 = Double.parseDouble(nil1TF.getText().trim());
                double nil2 = Double.parseDouble(nil2TF.getText().trim());
                DatabaseHelper.updateData(nim, namaTF.getText().trim(), nil1, nil2);
                JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
                clearFields();
                loadSemuaData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Nilai 1 & Nilai 2 harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ===== Aksi Hapus =====
        hapusBtn.addActionListener(e -> {
            String nim = nimTF.getText().trim();
            if (nim.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih data dari tabel dulu!");
                return;
            }
            int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin hapus data NIM: " + nim + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (konfirmasi == JOptionPane.YES_OPTION) {
                DatabaseHelper.hapusData(nim);
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                clearFields();
                loadSemuaData();
            }
        });

        // ===== Aksi Tampil Semua =====
        tampilBtn.addActionListener(e -> loadSemuaData());

        // ===== Aksi Clear =====
        clearBtn.addActionListener(e -> clearFields());

        // Load data saat pertama buka
        loadSemuaData();
        setVisible(true);
    }

    // Hitung rata-rata otomatis saat mengetik
    void hitungRata() {
        try {
            double nil1 = Double.parseDouble(nil1TF.getText().trim());
            double nil2 = Double.parseDouble(nil2TF.getText().trim());
            rataTF.setText(String.format("%.2f", (nil1 + nil2) / 2));
        } catch (NumberFormatException e) {
            rataTF.setText("");
        }
    }

    // Load semua data dari DB ke tabel
    void loadSemuaData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM datanilai ORDER BY nim")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("nim"),
                    rs.getString("nama"),
                    rs.getString("nil1"),
                    rs.getString("nil2"),
                    rs.getString("rata")
                });
            }
        } catch (SQLException e) {
            System.out.println("Load data gagal: " + e.getMessage());
        }
    }

    // Kosongkan semua field
    void clearFields() {
        nimTF.setText(""); namaTF.setText("");
        nil1TF.setText(""); nil2TF.setText(""); rataTF.setText("");
        nimTF.setEditable(true);
        nimTF.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}