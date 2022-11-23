import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

public class AS400TableExporter {

	/**
	 * without primary keys and indexes
	 * @param args
	 */
	public static void main(String[] args) {
		
		// AS400 Library to export
		String exportLib = "";
		// first AS400 table to export. Tables are exported in alphabetic order (a-z)
		// if null start with first table
		String startTable = null;

		
		// AS400 connection parameter
		String as400User = "";
		String as400Pass = "";
		String as400Host = "";
		
		// MySQL connection parameter
		String dbUser = "";
		String dbPass = "";
		String dbHost = "";
		String dbSchema = "";
		
		try {
			// connect to AS400
			AS400Connection as400Con = new AS400Connection (as400User, as400Pass, as400Host);
			// connect to MySQL
			Connection mySqlCon = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/" + dbSchema, dbUser, dbPass);
			
			
			
			/* S T A R T   E X P O R T
			 * -------------------------
			 * First query all data tables/files from AS400. Then loop threw tables. 
			 * First create the table in MySQL DB, then copy all data to the new table.
			 */
			Vector<String> allTableNames = as400Con.getAllTableNames(exportLib, startTable);
			
			
			for (String tablename : allTableNames) {
				
				System.out.println("");
				System.out.println("Beginn export of " + tablename);
				System.out.println("---------------------------------------");
				
				// Load metadata of AS400 table
				ExportTable table = as400Con.loadTableHeader(tablename, exportLib);
				AbstractTableWriter writer = new MySqlTableWriter(table, 100, mySqlCon, dbSchema);
				System.out.println("Create Table");
				writer.prepareTarget();
				System.out.println("Begin data export");
				as400Con.exportTable(table, writer);
				System.out.println("Finnish data export");
				writer.closeTarget();
				System.out.println("---------------------------------------");
				System.out.println("");
				System.out.println("");
			}
			
			// close all connections
			as400Con.close();
			mySqlCon.close();
		
		} catch (Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}
	}
}
