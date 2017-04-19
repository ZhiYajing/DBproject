import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class BookStoreFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel searchPanel;
	private JPanel makePanel;
	private Connection conn;//hold the connection

	/*
	 * get the student's orders
	 * if all given then get all of the system's orders
	 */
	private Object[][] getOrderData(String studentnumber) {
		try {
			PreparedStatement pst;
			if (studentnumber.equals("all")) {
				pst = conn.prepareStatement("select * from orders");
			} else {
				pst = conn.prepareStatement("select * from orders where studentnumber = ?");
				pst.setString(1, studentnumber);
			}
			ResultSet rs = pst.executeQuery();
			List<Object[]> list = new ArrayList<Object[]>();
			//construct the data array
			while(rs.next()){
				Object a[] = new Object[6];
				for(int i=1;i<=6;i++){
					a[i-1] = rs.getObject(i);
				}
				list.add(a);
			}
			Object data[][] = new Object[list.size()][6];
			for(int i=0;i<list.size();i++){
				data[i] = list.get(i);
			}
			rs.close();
			pst.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * get all the books by orderid
	 */
	private Object[][] getBookData(String orderId) {
		try {
			PreparedStatement pst;
			pst = conn.prepareStatement("select * from orderbook where orderid = ?");
			pst.setString(1, orderId);
			ResultSet rs = pst.executeQuery();
			List<Object[]> list = new ArrayList<Object[]>();
			//iterate the results and put data to data array
			while(rs.next()){
				Object a[] = new Object[5];
				for(int i=2;i<=5;i++){
					a[i-2] = rs.getObject(i);
					if(i==5){
						BigDecimal flag = (BigDecimal)a[i-2];
						//change integer to text
						if(flag.intValue() == 1){
							a[i-2] = "delivered";
						}else{
							a[i-2] = "not delivered";
						}
					}
				}
				list.add(a);
			}
			Object data[][] = new Object[list.size()][5];
			for(int i=0;i<list.size();i++){
				data[i] = list.get(i);
			}
			rs.close();
			pst.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * init the view
	 */
	public BookStoreFrame(Connection conn) {
		super();
		this.conn = conn;
		this.setBounds(200, 100, 1000, 500);
		this.setTitle("BookStore");
		//menu bar
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("Select Action");
		bar.add(menu);
		JMenuItem searchMenu = new JMenuItem("Order Search");
		menu.add(searchMenu);
		JMenuItem makeMenu = new JMenuItem("Order Make");
		menu.add(makeMenu);
		//search panel
		searchPanel = new JPanel();
		//search panel uses border layout
		searchPanel.setLayout(new BorderLayout());
		JPanel upPanel = new JPanel();
		JLabel inputSearchLabel = new JLabel("Input student number:");
		upPanel.add(inputSearchLabel);
		JTextField searchTxt = new JTextField(20);
		upPanel.add(searchTxt);
		JButton searchBt = new JButton("Search");
		upPanel.add(searchBt);
		searchPanel.add(upPanel, BorderLayout.NORTH);
		JPanel middlePanel = new JPanel();
		//table header
		final String[] orderColumnNames = { "OrderId", "Student Number", "Order Date", "Total Price", "Payment Method",
				"Card No" };
		//table datas
		Object orderData[][] = getOrderData("all");
		DefaultTableModel model2 = new DefaultTableModel(orderData, orderColumnNames);
		//JTable orderTable = new JTable(model2);
		JTable orderTable = new JTable(model2)
		{
            public boolean isCellEditable(int row, int column)
                 {
                            return false;}//±í¸ñ²»ÔÊÐí±»±à¼­
                 };
	
		//only select one row
		orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		final String[] bookColumnNames = { "OrderId", "Book Number", "Delivery Date", "Delivered" };
		DefaultTableModel model = new DefaultTableModel(null, bookColumnNames);
		JTable orderbookTable = new JTable(model)
		{
            public boolean isCellEditable(int row, int column)
                 {
                            return false;}//±í¸ñ²»ÔÊÐí±»±à¼­
                 };
		JScrollPane scrollPanelForOrderTable = new JScrollPane(orderTable);
		scrollPanelForOrderTable.setPreferredSize(new Dimension(400, 300));
		JScrollPane scrollPanelForOrderBookTable = new JScrollPane(orderbookTable);
		scrollPanelForOrderBookTable.setPreferredSize(new Dimension(400, 300));
		middlePanel.add(scrollPanelForOrderTable);
		middlePanel.add(scrollPanelForOrderBookTable);
		searchPanel.add(middlePanel, BorderLayout.CENTER);
		JPanel downPanel = new JPanel();
		searchPanel.add(downPanel, BorderLayout.SOUTH);
		JButton cancelBut = new JButton("Cancel Order");
		downPanel.add(cancelBut);
		JButton deliverBut = new JButton("Deliver Book");
		downPanel.add(deliverBut);
		
		//cancel an order
		cancelBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int row = orderTable.getSelectedRow();
				if(row < 0 ){
					JOptionPane.showMessageDialog(null, "You must select an order to cancel!");
				}else{
					try{
						//get selected order number
						String orderNum = (String) orderTable.getValueAt(row, 0);
						
						PreparedStatement pst = conn.prepareStatement("select *  from orderbook where orderid=? and delivered=1");
						pst.setString(1, orderNum);
						ResultSet rs = pst.executeQuery();
						//check if any book has been delivered
						if(rs.next()){
							rs.close();
							pst.close();
							JOptionPane.showMessageDialog(null, "Some of the books in the order has been delivered!");
							return;
						}else{
							rs.close();
							pst.close();
						}
						//check if the order was made in 7 days
						pst = conn.prepareStatement("select *  from orders where orderid=? and orderdate <= sysdate-7");
						pst.setString(1, orderNum);
						rs = pst.executeQuery();
						if(rs.next()){
							rs.close();
							pst.close();
							JOptionPane.showMessageDialog(null, "The order was not made within 7 days!");
							return;
						}else{
							rs.close();
							pst.close();
						}
						//delete the order
						pst = conn.prepareStatement("delete from orders where orderid=?");
						pst.setString(1, orderNum);
						pst.execute();
						pst.close();
						//delete relevant orderbooks
						pst = conn.prepareStatement("delete from orderbook where orderid=?");
						pst.setString(1, orderNum);
						pst.execute();
						pst.close();
						JOptionPane.showMessageDialog(null, "delete successfully!");
						searchMenu.doClick();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		//deliver a book
		deliverBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = orderbookTable.getSelectedRow();
				if(row < 0 ){
					JOptionPane.showMessageDialog(null, "You must select a book to deliver!");
				}else{
					try{
						String orderNum = (String) orderbookTable.getValueAt(row, 0);
						String bookNum = (String) orderbookTable.getValueAt(row, 1);
						PreparedStatement pst = conn.prepareStatement("update orderbook set deliverydate=sysdate,delivered=1 where orderid=? and booknumber=?");
						pst.setString(1, orderNum);
						pst.setString(2, bookNum);
						pst.execute();
						pst.close();
						JOptionPane.showMessageDialog(null, "delivered successfully!");
						searchMenu.doClick();
					}catch(Exception ee){
						ee.printStackTrace();
					}
				}
			}
		});
		/*
		 * when clicked,get all the books in the order
		 */
		orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                  int row = orderTable.getSelectedRow();
                  String orderId = (String)orderTable.getValueAt(row, 0);
                  Object data[][] = getBookData(orderId);
                  DefaultTableModel model = (DefaultTableModel)orderbookTable.getModel();
                  model.setRowCount(0);
                  for(Object d[] : data){
                	  model.addRow(d);
                  }
            }
        });
		/*
		 * when search button is clicked, list the orders of the student
		 */
		searchBt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel m = (DefaultTableModel)orderTable.getModel();
				m.setRowCount(0);
				Object datas[][]  = getOrderData(searchTxt.getText());
				for(Object ds[] : datas){
					m.addRow(ds);
				}
				DefaultTableModel m2 = (DefaultTableModel)orderbookTable.getModel();
				m2.setRowCount(0);
			}
		});
		
		//make order panel
		makePanel = new JPanel();
		makePanel.setLayout(null);
		JLabel snumLabel = new JLabel("Student Number:");
		JTextField snumField = new JTextField();
		snumLabel.setBounds(10, 10, 100, 30);
		snumField.setBounds(160, 10, 100, 30);
		makePanel.add(snumLabel);
		makePanel.add(snumField);
		
		JLabel payLabel = new JLabel("Payment Method:");
		JLabel payDetial = new JLabel("(choose credit card or Alipay)");
		JTextField payField = new JTextField();
		payLabel.setBounds(10, 50, 100, 30);
		payDetial.setBounds(260,50,200,30);
		payField.setBounds(160, 50, 100, 30);
		makePanel.add(payLabel);
		makePanel.add(payField);
		makePanel.add(payDetial);
		
		JLabel numberLabel = new JLabel("Card No:");
		JTextField numberField = new JTextField();
		numberLabel.setBounds(10, 90, 100, 30);
		numberField.setBounds(160, 90, 100, 30);
		makePanel.add(numberLabel);
		makePanel.add(numberField);
		
		
		DefaultTableModel model1 = new DefaultTableModel(null, new String[]{"Book Number","Title","Author","Price","Amount"});
		JTable booksTable = new JTable(model1);
		JScrollPane booksPane = new JScrollPane(booksTable);
		booksPane.setBounds(10, 130, 500, 160);
		makePanel.add(booksPane);
		
		JButton makeBt = new JButton("Make Order");
		makeBt.setBounds(10, 300, 160, 30);
		makePanel.add(makeBt);
		
		//when make button is clicked
		makeBt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					//use UUID as orderid
					String orderid = UUID.randomUUID().toString();
					String orderbookid = UUID.randomUUID().toString();
					BigDecimal totalPrice = new BigDecimal(0);
					for( int i:booksTable.getSelectedRows()){
						BigDecimal amount = (BigDecimal)booksTable.getValueAt(i, 4);
						if(amount.compareTo(new BigDecimal(0))<=0){
							JOptionPane.showMessageDialog(null, "Some book in the order is out of stock!","information", JOptionPane.INFORMATION_MESSAGE); 
							return;
						}
					}
					String payFieldString = payField.getText();
					String numberFieldString = numberField.getText();
					if(payFieldString.toLowerCase().contains("card") && numberFieldString.equals(""))
					{
						JOptionPane.showMessageDialog(null,"you need to input card number","information",JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					String stunum = snumField.getText();
					//checkout if the student has outstanding orders
					PreparedStatement pst = conn.prepareStatement("select * from orderbook a,orders b where a.orderid=b.orderid and a.delivered=0 and b.studentnumber = ?");
					pst.setString(1, stunum);
					ResultSet rs = pst.executeQuery();
					if(rs.next()){
						rs.close();
						pst.close();
						JOptionPane.showMessageDialog(null, "The student has outstanding orders!","information", JOptionPane.INFORMATION_MESSAGE); 
						return;
					}else{
						rs.close();
						pst.close();
					}
					
					//insert to orderbook
					for( int i:booksTable.getSelectedRows()){
						BigDecimal price = (BigDecimal)booksTable.getValueAt(i, 3);
						String booknum = (String)booksTable.getValueAt(i, 0);
						PreparedStatement pst1 = conn.prepareStatement("insert into orderbook (orderbookid,orderid,booknumber,delivered) VALUES (?,?,?,0)");
						pst1.setString(1, orderbookid);
						pst1.setString(2, orderid);
						pst1.setString(3, booknum);
						pst1.execute();
						pst1.close();
						totalPrice = totalPrice.add(price);
					}
					
					
					
					String paymethod = payField.getText();
					String cardno = numberField.getText();
					//insert to orders
					try{
						String sql = "insert into orders VALUES (?,?,sysdate,?,?,?)";
						pst = conn.prepareStatement(sql);
						pst.setString(1, orderid);
						pst.setString(2, stunum);
						pst.setDouble(3, totalPrice.doubleValue());
						pst.setString(4, paymethod);
						pst.setString(5, cardno);
						pst.execute();
						pst.close();
						JOptionPane.showMessageDialog(null, "Make Order Successfully!","information", JOptionPane.INFORMATION_MESSAGE); 
						searchMenu.doClick();
					}catch(SQLException ee){
						//the tr5 tigger raise an application error -20001
						String msg  = ee.getMessage();
						int i = msg.indexOf("ORA-20001:");
						int j = msg.indexOf('\n');
						
						JOptionPane.showMessageDialog(null, msg.substring(i+10, j),"information", JOptionPane.INFORMATION_MESSAGE); 
						
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		//menu is clicked
		searchMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BookStoreFrame.this.remove(makePanel);
				BookStoreFrame.this.add(searchPanel);
				BookStoreFrame.this.setBounds(200, 100, 1000, 500);
				DefaultTableModel m = (DefaultTableModel)orderTable.getModel();
				if(!searchTxt.getText().equals("")){
					m.setRowCount(0);
					Object datas[][]  = getOrderData(searchTxt.getText());
					for(Object ds[] : datas){
						m.addRow(ds);
					}
					DefaultTableModel m2 = (DefaultTableModel)orderbookTable.getModel();
					m2.setRowCount(0);
				}else{
					m.setRowCount(0);
					Object datas[][]  = getOrderData("all");
					for(Object ds[] : datas){
						m.addRow(ds);
					}
					DefaultTableModel m2 = (DefaultTableModel)orderbookTable.getModel();
					m2.setRowCount(0);
				}
				BookStoreFrame.this.validate();
				
			}
		});
		//menu is clicked
		makeMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BookStoreFrame.this.remove(searchPanel);
				BookStoreFrame.this.add(makePanel);
				DefaultTableModel tableModel = (DefaultTableModel) booksTable.getModel();
				tableModel.setRowCount(0);
				snumField.setText("");
				payField.setText("");
				numberField.setText("");
				try {
					//get all the books
					PreparedStatement pst;
					pst = conn.prepareStatement("select * from BOOKS");
					ResultSet rs = pst.executeQuery();
					while(rs.next()){
						Object a[] = new Object[5];
						for(int i=1;i<=5;i++){
							a[i-1] = rs.getObject(i);
						}
						tableModel.addRow(a);
					}
					rs.close();
					pst.close();
				} catch (Exception ee) {
					ee.printStackTrace();
				}
				BookStoreFrame.this.setBounds(200, 100, 600, 450);
				BookStoreFrame.this.validate();
			}
		});
		searchMenu.doClick();
		this.setJMenuBar(bar);
	}
}
