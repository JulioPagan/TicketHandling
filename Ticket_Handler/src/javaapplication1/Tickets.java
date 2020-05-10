package javaapplication1;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class Tickets extends JFrame implements ActionListener {

	// class level member objects
	Dao dao = new Dao(); // for CRUD operations
	Boolean chkIfAdmin = null;

	// Main menu object items
	private JMenu mnuFile = new JMenu("File");
	private JMenu mnuAdmin = new JMenu("Admin");
	private JMenu mnuTickets = new JMenu("Tickets");

	// Sub menu item objects for all Main menu item objects
	JMenuItem mnuItemExit;
	JMenuItem mnuItemUpdate;
	JMenuItem mnuItemDelete;
	JMenuItem mnuItemViewTicketHistory;
	JMenuItem mnuItemOpenTicket;
	JMenuItem mnuItemCloseTicket;
	JMenuItem mnuItemViewTicket;

	public Tickets(Boolean isAdmin) {

		chkIfAdmin = isAdmin;
		Dao.chkIfUserIsAdmin = chkIfAdmin;
		createMenu();
		prepareGUI();
		Dao.readRecords();
	}

	private void createMenu() {

		/* Initialize sub menu items **************************************/

		// initialize sub menu item for File main menu
		mnuItemExit = new JMenuItem("Exit");
		// add to File main menu item
		mnuFile.add(mnuItemExit);

		// initialize first sub menu items for Admin main menu
		mnuItemUpdate = new JMenuItem("Update Ticket");
		// add to Admin main menu item
		mnuAdmin.add(mnuItemUpdate);

		// initialize second sub menu items for Admin main menu
		mnuItemDelete = new JMenuItem("Delete Ticket");
		// add to Admin main menu item
		mnuAdmin.add(mnuItemDelete);

		//initialize third sub menu item for Admin main menu
		mnuItemViewTicketHistory = new JMenuItem("View Ticket History");
		//add to Admin main menu item
		mnuAdmin.add(mnuItemViewTicketHistory);

		// initialize first sub menu item for Tickets main menu
		mnuItemOpenTicket = new JMenuItem("Open Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemOpenTicket);

		//initialize close menu item for Tickets main menu
		mnuItemCloseTicket = new JMenuItem("Close Ticket");
		//add to Ticket Main menu item
		mnuTickets.add(mnuItemCloseTicket);

		// initialize third sub menu item for Tickets main menu
		mnuItemViewTicket = new JMenuItem("View Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemViewTicket);

		// initialize any more desired sub menu items below

		/* Add action listeners for each desired menu item *************/
		mnuItemExit.addActionListener(this);
		mnuItemUpdate.addActionListener(this);
		mnuItemDelete.addActionListener(this);
		mnuItemViewTicketHistory.addActionListener(this);
		mnuItemOpenTicket.addActionListener(this);
		mnuItemCloseTicket.addActionListener(this);
		mnuItemViewTicket.addActionListener(this);

		 /*
		  * continue implementing any other desired sub menu items (like 
		  * for update and delete sub menus for example) with similar 
		  * syntax & logic as shown above*
		 */

		System.out.println("Menu has been created");
	}

	private void prepareGUI() {

		// create JMenu bar
		JMenuBar bar = new JMenuBar();
		bar.add(mnuFile); // add main menu items in order, to JMenuBar
		if(chkIfAdmin){
			bar.add(mnuAdmin);
		}
		bar.add(mnuTickets);
		// add menu bar components to frame
		setJMenuBar(bar);

		addWindowListener(new WindowAdapter() {
			// define a window close operation
			public void windowClosing(WindowEvent wE) {
				System.exit(0);
			}
		});
		// set frame options
		setSize(800, 800);
		getContentPane().setBackground(Color.LIGHT_GRAY);
		setLocationRelativeTo(null);
		setVisible(true);

		System.out.println("GUI being displayed...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// implement actions for sub menu items
		if (e.getSource() == mnuItemExit) {
			System.exit(0);
		} 
		else if (e.getSource() == mnuItemOpenTicket) {

			System.out.println("Opening ticket...");
			// get ticket information
			String ticketName = JOptionPane.showInputDialog(null, "Enter your name");
			String ticketDesc = JOptionPane.showInputDialog(null, "Enter a ticket description");
			String sdate = JOptionPane.showInputDialog(null, "Enter start date: 'yyyy-mm-dd");
			// insert ticket information to database

			int id = dao.insertRecords(ticketName, ticketDesc, sdate);

			// display results if successful or not to console / dialog box
			if (id != 0) {
				System.out.println("Ticket ID : " + id + " created successfully!!!");
				JOptionPane.showMessageDialog(null, "Ticket id: " + id + " created");
			}else{
				System.out.println("Ticket cannot be created!!!");
			}
		}
		else if(e.getSource() == mnuItemCloseTicket){
			System.out.println("Closing ticket...");
			int ticketID = Integer.parseInt(JOptionPane.showInputDialog(null, "Choose ticket to close"));
			String cdate = (JOptionPane.showInputDialog(null, "Enter Closing Date: 'yyyy-mm-dd"));
			dao.closeRecord(ticketID, cdate);
		}
		else if (e.getSource() == mnuItemUpdate){

			System.out.println("Updating ticket description....");
			int ticketID = Integer.parseInt(JOptionPane.showInputDialog(null, "Choose ticket to update"));
			String newDesc = JOptionPane.showInputDialog(null, "Update Description");
			
			dao.updateRecords(ticketID, newDesc);;

		}
		else if (e.getSource() == mnuItemDelete){
			System.out.println("Deleting record...");
			int ticketID = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter ticket ID to delete"));
			dao.deleteRecords(ticketID);
		}

		else if (e.getSource() == mnuItemViewTicket) {

			// retrieve all tickets details for viewing in JTable
			try {
				System.out.println("Displaying records...");
				// Use JTable built in functionality to build a table model and
				// display the table model of your result set!!!
				JTable jt = new JTable(ticketsJTable.buildTableModel(dao.readRecords()));
				jt.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(jt);
				add(sp);
				setVisible(true); // refreshes or repaints frame on screen
				System.out.println("Records displayed successfully!");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == mnuItemViewTicketHistory){
			
			System.out.println("Displaying history records...");
			//retrieve ticket history for viewing in JTable
			try{
				// Use JTable built in functionality to build a table model 
				//and display the table model of your result set
				JTable jt = new JTable(ticketsJTable.buildTableModel(dao.readHistoryRecords()));
				jt.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(jt);
				add(sp);
				setVisible(true);
				System.out.println("History records displayed successfully");
			}catch (SQLException e1){
				e1.printStackTrace();
			}
		}

	}

}
