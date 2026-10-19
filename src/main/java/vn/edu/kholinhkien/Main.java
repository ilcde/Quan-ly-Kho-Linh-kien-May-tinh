package vn.edu.kholinhkien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public final class Main extends JFrame {
    private final JPanel content=new JPanel(new BorderLayout(12,12));
    private final JLabel status=new JLabel("Sẵn sàng • Java Swing / JDBC / SQL Server");
    private String page="Tồn kho";
    private JTable table;
    private JTextField search;
    private JButton reload;
    private int generation=0;
    interface Job<T> { T run() throws Exception; }
    interface Done<T> { void accept(T value); }
    record Choice(String id,String label) { public String toString(){return id+" — "+label;} }
    record Entity(String table,String[] columns,String[] labels,int[] limits) {}
    private static final Map<String,Entity> ENTITIES=Map.of(
        "Loại linh kiện",new Entity("LoaiLinhKien",new String[]{"MaLoai","TenLoai"},new String[]{"Mã loại","Tên loại"},new int[]{20,100}),
        "Nhà cung cấp",new Entity("NhaCungCap",new String[]{"MaNCC","TenNCC","DienThoai","DiaChi"},new String[]{"Mã NCC","Tên nhà cung cấp","Điện thoại","Địa chỉ"},new int[]{20,150,20,250}),
        "Linh kiện",new Entity("LinhKien",new String[]{"MaLK","TenLK","MaLoai","Hang","DonVi","GiaBan","TonToiThieu"},new String[]{"Mã linh kiện","Tên linh kiện","Loại linh kiện","Hãng","Đơn vị","Giá bán (VND)","Tồn tối thiểu"},new int[]{20,150,20,80,20,18,10})
    );
    public Main() {
        super("Quản lý Kho Linh kiện Máy tính");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(1200,760); setMinimumSize(new Dimension(950,620)); setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout()); setContentPane(root);
        JPanel sidebar=new JPanel(); sidebar.setLayout(new BoxLayout(sidebar,BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(21,38,61)); sidebar.setBorder(BorderFactory.createEmptyBorder(24,16,24,16));
        JLabel brand=new JLabel("KHO LINH KIỆN"); brand.setForeground(Color.WHITE); brand.setFont(new Font("Segoe UI",Font.BOLD,20)); sidebar.add(brand); sidebar.add(Box.createVerticalStrut(30));
        for(String name:List.of("Tồn kho","Linh kiện","Loại linh kiện","Nhà cung cấp","Phiếu nhập","Phiếu xuất")) {
            JButton b=new JButton(name); b.setPreferredSize(new Dimension(200,44)); b.setMaximumSize(new Dimension(200,44)); b.setAlignmentX(LEFT_ALIGNMENT);
            b.addActionListener(e->showPage(name)); sidebar.add(b); sidebar.add(Box.createVerticalStrut(10));
        }
        sidebar.add(Box.createVerticalGlue());
        root.add(sidebar,BorderLayout.WEST); content.setBorder(BorderFactory.createEmptyBorder(24,24,16,24)); root.add(content);
        status.setBorder(BorderFactory.createEmptyBorder(8,24,8,24)); root.add(status,BorderLayout.SOUTH);
        showPage(page);
    }
    private static DefaultTableModel model(String[] cols) { return new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}}; }
    private static JTable grid(DefaultTableModel m) {
        JTable t=new JTable(m); t.setRowHeight(32); t.setAutoCreateRowSorter(true); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); t.getTableHeader().setReorderingAllowed(false); return t;
    }
    private <T> void work(Component owner,JButton button,Job<T> job,Done<T> done) {
        if(button!=null) button.setEnabled(false); status.setText("Đang xử lý…");
        new SwingWorker<T,Void>() {
            protected T doInBackground() throws Exception {return job.run();}
            protected void done() {try {done.accept(get()); status.setText("Hoàn tất");} catch(Exception e){error(owner,e);status.setText("Không hoàn tất thao tác");} finally {if(button!=null)button.setEnabled(true);}}
        }.execute();
    }
    private static void error(Component owner,Throwable e) {
        while(e.getCause()!=null)e=e.getCause();
        String msg=e.getMessage();
        if(e instanceof java.sql.SQLException sql) {
            if(sql.getErrorCode()==547) msg="Dữ liệu đang được sử dụng hoặc mã liên kết không tồn tại. Không thể thực hiện thao tác.";
            else if(sql.getErrorCode()==2627 || sql.getErrorCode()==2601) msg="Mã hoặc tên đã tồn tại. Hãy dùng giá trị khác.";
        }
        JOptionPane.showMessageDialog(owner,msg==null?e.toString():msg,"Không thể thực hiện",JOptionPane.ERROR_MESSAGE);
    }
    private void showPage(String name) {
        page=name; generation++; content.removeAll(); JPanel top=new JPanel(new BorderLayout(8,14));
        JLabel title=new JLabel(name); title.setFont(new Font("Segoe UI",Font.BOLD,28)); top.add(title,BorderLayout.NORTH);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); search=new JTextField(22); search.setToolTipText("Tìm theo mã, tên hoặc đối tác");
        actions.add(new JLabel("Tìm kiếm")); actions.add(search); reload=new JButton("Tìm / Tải lại"); actions.add(reload); reload.addActionListener(e->load()); search.addActionListener(e->load());
        Entity entity=ENTITIES.get(page);
        if(entity!=null) {
            JButton add=new JButton("Thêm"),edit=new JButton("Sửa"),delete=new JButton("Xóa"); actions.add(add);actions.add(edit);actions.add(delete);
            add.addActionListener(e->edit(entity,null)); edit.addActionListener(e->{Object[] row=selected();if(row!=null)edit(entity,row);});
            delete.addActionListener(e->{Object[] row=selected();if(row!=null && JOptionPane.showConfirmDialog(this,"Xóa bản ghi "+row[0]+"?","Xác nhận xóa",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
                work(this,delete,()->{Db.execute("DELETE FROM "+entity.table()+" WHERE "+entity.columns()[0]+"=?",row[0]);return null;},v->load());});
        } else if(page.startsWith("Phiếu")) {
            boolean receipt=page.equals("Phiếu nhập"); JButton add=new JButton("Lập "+page.toLowerCase());actions.add(add);
            add.addActionListener(e->prepareVoucher(receipt,add)); JButton detail=new JButton("Xem chi tiết");actions.add(detail);detail.addActionListener(e->detail(receipt,detail));
        }
        JButton csv=new JButton("Xuất CSV");actions.add(csv);csv.addActionListener(e->exportCsv());
        top.add(actions,BorderLayout.CENTER);content.add(top,BorderLayout.NORTH);
        String[] cols=entity!=null?entity.labels():page.equals("Tồn kho")?new String[]{"Mã","Tên linh kiện","Loại","Hãng","Đơn vị","Giá bán","Ngưỡng","Tồn kho","Trạng thái"}:new String[]{"Mã phiếu","Ngày tạo","Đối tác","Người lập","Ghi chú","Tổng tiền"};
        table=grid(model(cols)); table.getColumnModel().getColumn(1).setPreferredWidth(240); content.add(new JScrollPane(table));
        JLabel help=new JLabel(page.equals("Tồn kho")?"Tồn kho = tổng nhập − tổng xuất • Cảnh báo khi tồn kho ≤ ngưỡng tối thiểu":page.startsWith("Phiếu")?"Phiếu đã ghi sổ được giữ nguyên để bảo toàn lịch sử. Kiểm tra kỹ trước khi lưu.":"Chọn một dòng để sửa hoặc xóa. Mã đã tạo không thay đổi.");
        content.add(help,BorderLayout.SOUTH);content.revalidate();content.repaint();load();
    }
    private Object[] selected() {
        int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Hãy chọn một dòng.");return null;}
        Object[] row=new Object[table.getColumnCount()];for(int c=0;c<row.length;c++)row[c]=table.getValueAt(r,c);return row;
    }
    private void load() {
        int token=++generation; JTable target=table; String term="%"+search.getText().trim().replace("[","[[]").replace("%","[%]").replace("_","[_]")+"%";
        Entity en=ENTITIES.get(page);String sql;
        if(en!=null)sql="SELECT "+String.join(",",en.columns())+" FROM "+en.table()+" WHERE "+en.columns()[0]+" LIKE ? OR "+en.columns()[1]+" LIKE ? ORDER BY "+en.columns()[0];
        else if(page.equals("Tồn kho"))sql="SELECT MaLK,TenLK,MaLoai,Hang,DonVi,GiaBan,TonToiThieu,TonKho,CASE WHEN TonKho<=TonToiThieu THEN N'Cần nhập thêm' ELSE N'Đủ hàng' END FROM v_TonKho WHERE MaLK LIKE ? OR TenLK LIKE ? ORDER BY MaLK";
        else {boolean receipt=page.equals("Phiếu nhập");String suffix=receipt?"Nhap":"Xuat",id=receipt?"MaPN":"MaPX",partner=receipt?"n.TenNCC":"p.NguoiNhan";
            sql="SELECT p."+id+",p.NgayTao,"+partner+",p.NguoiLap,p.GhiChu,(SELECT SUM(CAST(d.SoLuong AS decimal(18,0))*d.DonGia) FROM ChiTiet"+suffix+" d WHERE d."+id+"=p."+id+") FROM Phieu"+suffix+" p "+(receipt?"JOIN NhaCungCap n ON n.MaNCC=p.MaNCC ":"")+"WHERE CAST(p."+id+" AS nvarchar(20)) LIKE ? OR "+partner+" LIKE ? ORDER BY p."+id+" DESC";
        }
        work(this,reload,()->Db.query(sql,term,term),rows->{if(token!=generation)return;DefaultTableModel m=(DefaultTableModel)target.getModel();m.setRowCount(0);rows.forEach(m::addRow);});
    }
    private void edit(Entity en,Object[] row) {
        if(en.table().equals("LinhKien"))work(this,null,()->Db.query("SELECT MaLoai,TenLoai FROM LoaiLinhKien ORDER BY MaLoai"),choices->editDialog(en,row,choices));
        else editDialog(en,row,List.of());
    }
    private void editDialog(Entity en,Object[] row,List<Object[]> choices) {
        if(en.table().equals("LinhKien") && choices.isEmpty()){JOptionPane.showMessageDialog(this,"Hãy tạo loại linh kiện trước.");return;}
        JDialog d=new JDialog(this,row==null?"Thêm "+en.labels()[1].toLowerCase():"Sửa "+row[0],true);
        JPanel form=new JPanel(new GridLayout(0,2,12,12));form.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JComponent[] fields=new JComponent[en.columns().length];
        for(int i=0;i<fields.length;i++) {
            form.add(new JLabel(en.labels()[i]));
            if(en.columns()[i].equals("MaLoai") && en.table().equals("LinhKien")) {
                JComboBox<Choice> combo=new JComboBox<>();for(Object[] c:choices)combo.addItem(new Choice(c[0].toString(),c[1].toString()));
                if(row!=null)for(int j=0;j<combo.getItemCount();j++)if(combo.getItemAt(j).id().equals(row[i].toString()))combo.setSelectedIndex(j);fields[i]=combo;
            } else {JTextField f=new JTextField(row==null?"":String.valueOf(row[i]),24);fields[i]=f;if(row!=null&&i==0)f.setEditable(false);}
            form.add(fields[i]);
        }
        JButton save=new JButton("Lưu"),cancel=new JButton("Đóng");form.add(cancel);form.add(save);cancel.addActionListener(e->d.dispose());
        save.addActionListener(e->{try {
            Object[] values=new Object[fields.length];
            for(int i=0;i<values.length;i++) {
                String text=fields[i] instanceof JTextField f?f.getText().trim():((Choice)((JComboBox<?>)fields[i]).getSelectedItem()).id();
                if(text.length()>en.limits()[i])throw new IllegalArgumentException(en.labels()[i]+" tối đa "+en.limits()[i]+" ký tự.");
                if(text.isEmpty() && !Set.of("DienThoai","DiaChi").contains(en.columns()[i]))throw new IllegalArgumentException("Vui lòng nhập "+en.labels()[i]+".");
                values[i]=text;
                if(en.columns()[i].equals("GiaBan"))values[i]=money(text);
                if(en.columns()[i].equals("TonToiThieu")){int v=Integer.parseInt(text);if(v<0)throw new IllegalArgumentException("Ngưỡng tồn không âm.");values[i]=v;}
            }
            String sql;Object[] args;
            if(row==null){sql="INSERT INTO "+en.table()+"("+String.join(",",en.columns())+") VALUES("+String.join(",",Collections.nCopies(values.length,"?"))+")";args=values;}
            else {List<String> sets=new ArrayList<>();args=new Object[values.length];for(int i=1;i<values.length;i++){sets.add(en.columns()[i]+"=?");args[i-1]=values[i];}args[values.length-1]=row[0];sql="UPDATE "+en.table()+" SET "+String.join(",",sets)+" WHERE "+en.columns()[0]+"=?";}
            work(d,save,()->{Db.execute(sql,args);return null;},v->{d.dispose();load();});
        }catch(Exception ex){error(d,ex);}});
        d.add(form);d.pack();d.setLocationRelativeTo(this);d.setVisible(true);
    }
    static BigDecimal money(String s) {
        BigDecimal v=new BigDecimal(s);if(v.signum()<0||v.scale()>2||v.precision()-v.scale()>16)throw new IllegalArgumentException("Số tiền không âm, tối đa 16 chữ số nguyên và 2 chữ số thập phân; không nhập dấu phân cách hàng nghìn.");return v.setScale(2);
    }
    private void prepareVoucher(boolean receipt,JButton b) {
        work(this,b,()->List.of(Db.query("SELECT MaLK,TenLK,GiaBan,TonKho FROM v_TonKho ORDER BY MaLK"),Db.query("SELECT MaNCC,TenNCC FROM NhaCungCap ORDER BY MaNCC")),data->voucher(receipt,data.get(0),data.get(1)));
    }
    private void voucher(boolean receipt,List<Object[]> products,List<Object[]> partners) {
        if(products.isEmpty() || receipt&&partners.isEmpty()){JOptionPane.showMessageDialog(this,"Cần có linh kiện và nhà cung cấp trước khi lập phiếu.");return;}
        JDialog d=new JDialog(this,receipt?"Lập phiếu nhập":"Lập phiếu xuất",true);d.setSize(1050,620);d.setLocationRelativeTo(this);
        JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));d.add(root);
        JPanel header=new JPanel(new GridLayout(0,2,10,10));JTextField operator=new JTextField(),note=new JTextField(),receiver=new JTextField();JComboBox<Choice> suppliers=new JComboBox<>();partners.forEach(p->suppliers.addItem(new Choice(p[0].toString(),p[1].toString())));
        header.add(new JLabel(receipt?"Nhà cung cấp":"Người nhận"));header.add(receipt?suppliers:receiver);header.add(new JLabel("Người lập phiếu"));header.add(operator);header.add(new JLabel("Ghi chú"));header.add(note);root.add(header,BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(8,8));root.add(center);JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));JComboBox<Choice> product=new JComboBox<>();products.forEach(p->product.addItem(new Choice(p[0].toString(),p[1]+" (tồn "+p[3]+")")));
        JSpinner qty=new JSpinner(new SpinnerNumberModel(1,1,1000000,1));JTextField price=new JTextField(10);price.setText(products.get(0)[2].toString());
        product.addActionListener(e->price.setText(products.get(product.getSelectedIndex())[2].toString()));
        JButton add=new JButton("Thêm dòng");line.add(product);line.add(new JLabel("SL"));line.add(qty);line.add(new JLabel("Đơn giá"));line.add(price);line.add(add);center.add(line,BorderLayout.NORTH);
        DefaultTableModel m=model(new String[]{"Mã linh kiện","Tên linh kiện","Số lượng","Đơn giá","Thành tiền"});JTable items=grid(m);center.add(new JScrollPane(items));
        JLabel total=new JLabel("Tổng tiền: 0 VND");Runnable sum=()->{BigDecimal v=BigDecimal.ZERO;for(int i=0;i<m.getRowCount();i++)v=v.add((BigDecimal)m.getValueAt(i,4));total.setText("Tổng tiền: "+v.toPlainString()+" VND");};
        add.addActionListener(e->{try{Choice p=(Choice)product.getSelectedItem();for(int i=0;i<m.getRowCount();i++)if(m.getValueAt(i,0).equals(p.id()))throw new IllegalArgumentException("Linh kiện đã có. Xóa dòng cũ nếu muốn thay đổi.");qty.commitEdit();int count=(Integer)qty.getValue();BigDecimal amount=money(price.getText().trim());m.addRow(new Object[]{p.id(),products.get(product.getSelectedIndex())[1],count,amount,amount.multiply(BigDecimal.valueOf(count))});sum.run();}catch(Exception ex){error(d,ex);}});
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton remove=new JButton("Xóa dòng"),post=new JButton("Ghi sổ phiếu"),close=new JButton("Đóng");bottom.add(total);bottom.add(remove);bottom.add(close);bottom.add(post);root.add(bottom,BorderLayout.SOUTH);
        remove.addActionListener(e->{if(items.getSelectedRow()>=0){m.removeRow(items.convertRowIndexToModel(items.getSelectedRow()));sum.run();}});close.addActionListener(e->d.dispose());
        post.addActionListener(e->{List<InventoryService.Line> lines=new ArrayList<>();for(int i=0;i<m.getRowCount();i++)lines.add(new InventoryService.Line(m.getValueAt(i,0).toString(),(Integer)m.getValueAt(i,2),(BigDecimal)m.getValueAt(i,3)));
            String partner=receipt?((Choice)suppliers.getSelectedItem()).id():receiver.getText().trim(),who=operator.getText().trim(),memo=note.getText().trim();
            if(lines.isEmpty()||partner.isBlank()||who.isBlank()){JOptionPane.showMessageDialog(d,"Nhập đối tác, người lập và ít nhất một dòng linh kiện.");return;}
            if(JOptionPane.showConfirmDialog(d,"Ghi sổ phiếu? Phiếu đã lưu không sửa/xóa trực tiếp.","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            // Freeze all editing until the transaction completes.
            setEnabledTree(root,false);d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            new SwingWorker<Integer,Void>(){protected Integer doInBackground()throws Exception{return InventoryService.post(receipt,partner,who,memo,lines);}protected void done(){try{int id=get();d.dispose();JOptionPane.showMessageDialog(Main.this,"Đã ghi sổ phiếu #"+id);load();}catch(Exception ex){error(d,ex);}finally{setEnabledTree(root,true);d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);}}}.execute();
        });d.setVisible(true);
    }
    private static void setEnabledTree(Component c,boolean value){c.setEnabled(value);if(c instanceof Container ct)for(Component child:ct.getComponents())setEnabledTree(child,value);}
    private void detail(boolean receipt,JButton b) {
        Object[] row=selected();if(row==null)return;
        String sql="SELECT d.MaLK,l.TenLK,d.SoLuong,d.DonGia,CAST(d.SoLuong AS decimal(18,0))*d.DonGia FROM ChiTiet"+(receipt?"Nhap":"Xuat")+" d JOIN LinhKien l ON l.MaLK=d.MaLK WHERE "+(receipt?"MaPN":"MaPX")+"=?";
        work(this,b,()->Db.query(sql,row[0]),rows->{DefaultTableModel m=model(new String[]{"Mã","Tên linh kiện","Số lượng","Đơn giá","Thành tiền"});rows.forEach(m::addRow);JScrollPane sc=new JScrollPane(grid(m));sc.setPreferredSize(new Dimension(780,300));JOptionPane.showMessageDialog(this,sc,"Chi tiết phiếu #"+row[0],JOptionPane.PLAIN_MESSAGE);});
    }
    private void exportCsv() {
        JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("kho-linh-kien.csv"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path path=chooser.getSelectedFile().toPath();if(Files.exists(path)&&JOptionPane.showConfirmDialog(this,"Ghi đè tệp đã tồn tại?","Xuất CSV",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        StringBuilder csv=new StringBuilder("\uFEFF");for(int r=-1;r<table.getRowCount();r++){for(int c=0;c<table.getColumnCount();c++){if(c>0)csv.append(',');String v=String.valueOf(r<0?table.getColumnName(c):table.getValueAt(r,c));if(v.matches("^[=+@\\-].*"))v="'"+v;csv.append('"').append(v.replace("\"","\"\"")).append('"');}csv.append("\r\n");}
        try{Files.writeString(path,csv,StandardCharsets.UTF_8);status.setText("Đã xuất "+path);}catch(Exception e){error(this,e);}
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].equals("--check-db")){System.out.println(Db.query("SELECT DB_NAME()").get(0)[0]);return;}
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Font font=new Font("Segoe UI",Font.PLAIN,14);for(Object key:Collections.list(UIManager.getDefaults().keys()))if(UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)UIManager.put(key,font);
        SwingUtilities.invokeLater(()->new Main().setVisible(true));
    }
}
