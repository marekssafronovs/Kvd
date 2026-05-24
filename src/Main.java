import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Main {

   
    static class Prece {
        int id;
        String nosaukums, foto, apraksts;
        double cena;

        Prece(int id, String nosaukums, String foto, String apraksts, double cena) {
            this.id = id;
            this.nosaukums = nosaukums;
            this.foto = foto;
            this.apraksts = apraksts;
            this.cena = cena;
        }
    }

    
    static final Color BG        = new Color(244, 242, 238);
    static final Color WHITE     = Color.WHITE;
    static final Color RED       = new Color(192, 57, 43);
    static final Color GREEN     = new Color(39, 174, 96);
    static final Color BORDER    = new Color(220, 220, 220);
    static final Color TEXT_DARK = new Color(30, 30, 30);
    static final Color TEXT_GREY = new Color(120, 120, 120);
    static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    static final Font  FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font  FONT_PRICE = new Font("Segoe UI", Font.BOLD, 15);

    
    static List<Prece> visiProdukti = new ArrayList<>();
    static List<Prece> redzamie    = new ArrayList<>();
    static JPanel listPanel;
    static JFrame frame;
    static JLabel statusLabel;

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            buildUI();
            loadData();
        });
    }

    
    static void loadData() {
        statusLabel.setText("Ielādē datus...");
        new SwingWorker<List<Prece>, Void>() {
            protected List<Prece> doInBackground() throws Exception {
                List<Prece> list = new ArrayList<>();
                Database db = new Database();
                String sql = "SELECT id, nosaukums, foto, apraksts, cena FROM preces ORDER BY id";
                ResultSet rs = db.getConn().createStatement().executeQuery(sql);
                while (rs.next()) {
                    list.add(new Prece(
                        rs.getInt("id"),
                        rs.getString("nosaukums"),
                        rs.getString("foto"),
                        rs.getString("apraksts"),
                        rs.getDouble("cena")
                    ));
                }
                return list;
            }
            protected void done() {
                try {
                    visiProdukti = get();
                    redzamie.clear();
                    redzamie.addAll(visiProdukti);
                    renderList();
                    statusLabel.setText("Atrasti " + visiProdukti.size() + " sludinājumi");
                } catch (Exception e) {
                    statusLabel.setText("Kļūda: " + e.getCause().getMessage());
                    JOptionPane.showMessageDialog(frame,
                        "Nevar ielādēt datus:\n" + e.getCause().getMessage(),
                        "Kļūda", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

  
    static void buildUI() {
        frame = new JFrame("Sludinājumi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLocationRelativeTo(null);
        frame.setBackground(BG);

        
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(WHITE);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel logo = new JLabel("Sludinājumi");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(RED);

        JTextField searchField = new JTextField();
        searchField.setFont(FONT_BODY);
        searchField.setPreferredSize(new Dimension(300, 32));
        searchField.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Meklēt...");

        JButton searchBtn = redButton("Meklēt");
        searchBtn.addActionListener(e -> filter(searchField.getText()));
        searchField.addActionListener(e -> filter(searchField.getText()));

        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setBackground(WHITE);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        header.add(logo, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);

        
        statusLabel = new JLabel("Ielādē...");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_GREY);
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        statusBar.setBackground(BG);
        statusBar.add(statusLabel);

        
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG);
        listPanel.setBorder(new EmptyBorder(8, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.add(statusBar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        frame.setLayout(new BorderLayout());
        frame.add(header, BorderLayout.NORTH);
        frame.add(center, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    
    static void filter(String q) {
        redzamie.clear();
        String ql = q.toLowerCase().trim();
        for (Prece p : visiProdukti) {
            if (ql.isEmpty()
                || p.nosaukums.toLowerCase().contains(ql)
                || (p.apraksts != null && p.apraksts.toLowerCase().contains(ql))) {
                redzamie.add(p);
            }
        }
        statusLabel.setText("Atrasti " + redzamie.size() + " sludinājumi");
        renderList();
    }

   
    static void renderList() {
        listPanel.removeAll();

        if (redzamie.isEmpty()) {
            JLabel empty = new JLabel("Nav atrasts neviens sludinājums");
            empty.setFont(FONT_BODY);
            empty.setForeground(TEXT_GREY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(40, 0, 0, 0));
            listPanel.add(empty);
        } else {
            for (Prece p : redzamie) {
                listPanel.add(buildCard(p));
                listPanel.add(Box.createVerticalStrut(4));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    
    static JPanel buildCard(Prece p) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(90, 60));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(new Color(238, 238, 238));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadThumb(p.foto, imgLabel, 90, 60);

       
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(WHITE);

        JLabel title = new JLabel(p.nosaukums);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_DARK);

        String shortDesc = p.apraksts != null && p.apraksts.length() > 80
            ? p.apraksts.substring(0, 80) + "..."
            : (p.apraksts != null ? p.apraksts : "");
        JLabel desc = new JLabel(shortDesc);
        desc.setFont(FONT_SMALL);
        desc.setForeground(TEXT_GREY);

        info.add(title);
        info.add(Box.createVerticalStrut(4));
        info.add(desc);

 
        JLabel priceLabel = new JLabel(String.format("%.2f €", p.cena));
        priceLabel.setFont(FONT_PRICE);
        priceLabel.setForeground(GREEN);
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        priceLabel.setPreferredSize(new Dimension(90, 40));

        card.add(imgLabel, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(priceLabel, BorderLayout.EAST);

        
        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(249, 249, 249));
                info.setBackground(new Color(249, 249, 249));
                card.setBorder(new CompoundBorder(
                    new LineBorder(new Color(180, 180, 180), 1, true),
                    new EmptyBorder(10, 12, 10, 14)
                ));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(WHITE);
                info.setBackground(WHITE);
                card.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(10, 12, 10, 14)
                ));
            }
            public void mouseClicked(MouseEvent e) { openDetail(p); }
        };

        card.addMouseListener(hover);
        imgLabel.addMouseListener(hover);
        info.addMouseListener(hover);
        priceLabel.addMouseListener(hover);

        return card;
    }

    
    static void openDetail(Prece p) {
        JDialog dialog = new JDialog(frame, p.nosaukums, true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(frame);
        dialog.setBackground(WHITE);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(WHITE);

     
        JLabel bigImg = new JLabel("Ielādē attēlu...", SwingConstants.CENTER);
        bigImg.setPreferredSize(new Dimension(620, 260));
        bigImg.setOpaque(true);
        bigImg.setBackground(new Color(230, 230, 230));
        bigImg.setFont(FONT_SMALL);
        bigImg.setForeground(TEXT_GREY);
        loadThumb(p.foto, bigImg, 620, 260);

        
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(WHITE);
        body.setBorder(new EmptyBorder(16, 20, 20, 20));

        
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(WHITE);

        JLabel nameLabel = new JLabel("<html><b>" + escHtml(p.nosaukums) + "</b></html>");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_DARK);

        JLabel cenaLabel = new JLabel(String.format("%.2f €", p.cena));
        cenaLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        cenaLabel.setForeground(GREEN);

        topRow.add(nameLabel, BorderLayout.CENTER);
        topRow.add(cenaLabel, BorderLayout.EAST);

    
        JTextArea descArea = new JTextArea(p.apraksts != null ? p.apraksts : "Nav apraksta.");
        descArea.setFont(FONT_BODY);
        descArea.setForeground(new Color(60, 60, 60));
        descArea.setBackground(WHITE);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setBorder(new EmptyBorder(10, 0, 0, 0));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        descScroll.setBackground(WHITE);
        descScroll.getViewport().setBackground(WHITE);

        
        JButton closeBtn = redButton("Aizvērt");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(WHITE);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        btnRow.add(closeBtn);

        body.add(topRow);
        body.add(descScroll);
        body.add(btnRow);

        root.add(bigImg, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    
    static void loadThumb(String path, JLabel label, int w, int h) {
        if (path == null || path.isBlank()) return;
        new SwingWorker<ImageIcon, Void>() {
            protected ImageIcon doInBackground() throws Exception {
                BufferedImage img = null;
                java.io.File localFile = new java.io.File(path);
                if (localFile.exists()) {
                    img = ImageIO.read(localFile);
                } else {
                    try {
                        java.net.URLConnection conn = new URL(path).openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        img = ImageIO.read(conn.getInputStream());
                    } catch (Exception ignored) {}
                }
                if (img == null) throw new Exception("Nav bildes");
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
            protected void done() {
                try {
                    label.setIcon(get());
                    label.setText("");
                } catch (Exception ignored) {
                    label.setText("Nav bildes");
                }
            }
        }.execute();
    }

    
    static JButton redButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(RED);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 18, 7, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(169, 50, 38)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(RED); }
        });
        return btn;
    }

    static String escHtml(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}