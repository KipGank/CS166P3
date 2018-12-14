/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1
		boolean valid = true; 
		String fakeid = "0"; 
		String firstName = ""; 
		String lastName = ""; 
		String phone = ""; 
		String address = ""; 
		int id = 0;
		
		system.out.print("Please enter your customer id: ");
		try
		{
			id = in.readline();
		} catch(Exception e) {
			System.err.println (e.getMessage ());
		}
		
		system.out.print("Please enter your first name: ");
		try
		{
			firstName = in.readline();
		} catch(Exception e) {
			System.err.println (e.getMessage ());
		}
		if(firstName.length() > 32) {
			do {
				system.out.print("Please enter your first name: ");
				try
				{
					firstName = in.readline();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			} while(valid);
			valid = true;
		}
		system.out.print("Please enter your last name: ");
		try
				{
					lastName = in.readline();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
		if(lastName.length() > 32) {
			do {
				system.out.print("Please enter your last name: ");
				try
				{
					lastName = in.readline();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			} while(valid);
			valid = true;
		}
		system.out.print("Please enter your phone number: ");
		try
			{
				phone = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		if(phone.length() > 13) {
			do {
				system.out.print("Please enter your phone number: ");
				try
				{
					phone = in.readline();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			} while(valid);
			valid = true; 
		}
		system.out.print("Please enter your address: ");
		try
			{
				address = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		if(address.length() > 256)
		{
			do {
				system.out.print("Please enter your address: ");
				try
				{
					address = in.readline();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			}while(valid);
		}
		String SQL = "INSERT INTO Customer(fname, lname, phone, address) Values('" + firstName + "\', \'" + lastName + "\', \'" + phone + "\', \'" + address + "\')";
	}
	public static void AddMechanic(MechanicShop esql){//2
		Scanner reader = new scanner(system.in);
		int id = 0;
		String firstName = "";
		String lastName = "";
		int yearExp = 0;
		
	
		system.out.print("Please enter your mechanic id: ");
		try
		{
			id = in.readline();
		} catch(Exception e) {
			System.err.println (e.getMessage ());
		}
		
		do
		{
			system.out.print("Please enter your first name: ");
			try
			{
				firstName = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		} while(firstName.length() > 32);
	
		do
		{
			system.out.print("Please enter your last name: ");
			try
			{
				lastName = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		} while(lastName.length() > 32);
		
		
		do
		{
			system.out.print("Please enter your year of experience: ");
			try
			{
				 yearExp = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		} while(yearExp >= 0 && yearExp < 100);
		
		String SQL = "INSERT INTO Mechanic(fname, lname, experience) Values('" + firstName + "\', \'" + lastName + "\', \'" + yearExp + "\')";
	}
	
	
	
	public static void AddCar(MechanicShop esql){//3

		String VIN = "";
		String make = "";
		int year = 0;
		String model = "";
		
		do
		{
			system.out.print("Please enter the VIN: ");
			try
			{
				VIN = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		}while(VIN.length() > 16);
		
		do
		{
			system.out.print("Please enter the make: ");
			try
			{
				make = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		}while(make.length > 32);
		
		do
		{
			system.out.print("Please enter the model: ");
			try
			{
				model = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		
		} while(model.length() > 32);
		
		do
		{
			system.out.print("Please enter the year: ");
			try
			{
				year = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			} 
		} while (year < 1970);
		String SQL = "INSERT INTO Car(make,model,year) Values('" + make + "\', \'" + model + "\', \'" + year + "')";
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		String lastName = "";
		String SQL = ""; 
		String car = "";
		String customerID = ""; 
		boolean valid = false;
		boolean fnameFound = false; 
		system.out.println("Please enter your last name: "); //user enters last name 
		try
		{
			lastName = in.readline();
		} catch(Exception e) {
			System.err.println (e.getMessage ());
		}
		
		SQL = "SELECT C.fname FROM Customer C WHERE C.lname = " + lastName; 
		
		list<list<string>> results = executeQueryAndReturnResult(SQL); //run a query to search for first names with the last name entered from the user
		if(results.size != 0) {
			system.out.println("Select from available customers: ");
			for(int i = 0; i < results.size(); ++i) { //print out all first names associated with the last name entered
				system.out.println(results.get(i).get(0)); 
				system.out.printf("%n");
			}
			string firstName = ""; 
			try{
				firstName = in.readline();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
			for(int i = 0; i < results.size(); ++i) { //check if first name exists
				if(results.get(i).get(0) == firstName) {
					fnameFound = true; 	
					break;
				} 
			}
		}
		if(!fnameFound) //if the user needs to create a service request as a new customer
		{
			system.out.println("Name not found. Please provide information for a new customer: ");
			system.out.printf("%n");
			AddCustomer(esql);
		}
		else { //find all VINs associated with the first and last name provided 
			SQL = "SELECT C.vin, O.customer_id FROM Car C, Owns O, Customer C2 WHERE C.vin = O.car_vin AND C2.id = O.customer_id AND C2.fname = " + firstname + " AND C2.lname = " + lastname; 
			results = executeQueryAndReturnResult(SQL);
			system.out.println("Select from available cars to service: "); 
			system.out.printf("%n");
			for(int i = 0; i < results.size(); ++i) {
				system.println(results.get(i).get(0));
				customerID = results.get(i).get(1); //obtain customerID so we can create the request later on in the function 
				break;
			} 
			try{
				car = in.readline(); //user enters VIN for car to be serviced 
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
			for(int i = 0; i < results.size(); ++i) { //check if VIN entered exists 
				if(car == results.get(i).get(0)) {
					valid = true;
					break;
				}
			}
			if(!valid) { //if the VIN entered is not found, prompt user to enter VIN again 
				system.out.println("VIN doesn't exist in database. Please enter information for the new vehicle: ");
				system.out.prinf("%n");
				AddCar(esql);
			}
			Date serviceDate = new Date(); //create a date for creating a new service request 
			serviceDate = string(serviceDate); //convert date into string
			String complaint = ""; //complaint will be empty
			String odometer = "5000"; //no way to get real odometer reading so we will just use 5000 
			String fakerid = "0"; //this rid will be overwritten by the trigger implemented at the bottom of create.sql 
			SQL = "INSERT INTO Service_Request(customer_id, car_vin, date, odometer, complain) Values('" + customerID + "\', \'" + car + "\', \'" + serviceDate + "\', \'" + odometer + "\', \'" + complaint + "\')"; 
			executeUpdate(SQL); //create new service request 
		}
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
		String SQLRID = "SELECT COUNT(R.rid) FROM Service_Request R WHERE R.rid = ";
		String SQLMID = "SELECT COUNT(M.id) FROM Mechanic M WHERE M.id = ";
		int WID = 0;
		int RID = 0;
		int MID = 0;
		/*
		boolean ridBool = 0;
		boolean midBool = 0;
	
		system.out.print("Please enter the WID: ");
		try
		{
			WID = in.readline();
		} catch(Exception e) {
			System.err.println (e.getMessage ());
		}
		
		do
		{
			system.out.print("Please enter the RID: ");
			try
			{
				RID = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
			String input = RID;
        	SQLRID += input;
        	 int rowCount = esql.executeQuery(SQLRID);
        	 if (rowCount >= 1)
        	 {
        	 	ridBool = 1;
        	 }
			
		} while (!ridBool);
		
		do
		{
			system.out.print("Please enter the MID: ");
			try
			{
				MID = in.readline();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
			String input = MID;
        	SQLMID += input;
        	 int rowCount = esql.executeQuery(SQLMID);
        	 if (rowCount >= 1)
        	 {
        	 	midBool = 1;
        	 }
			
		} while (!midBool);
		
		int bill = 1000;
		Date serviceDate = new Date(); //create a date for creating a new service request 
		serviceDate = string(serviceDate); //convert date into string
		String complaint = ""; //complaint will be empty
		*/
	//	SQL = 'INSERT INTO Closed_Request(' + WID + ', ' + RID + ', ' +  MID + ', ' + serviceDate + ', ' + bill + ', ' + complaint + ')'; 
		SQL = "INSERT INTO Closed_Request(date, comment, bill) Values('" + serviceDate + "\', \'" + complaint + "\', \'" + bill + "')"; 
		executeUpdate(SQL); //create new service request 
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		string SQL = "SELECT C.date,C.comment,C.bill FROM Closed_Request C WHERE bill < 100";
		
		try
		{
	         int rowCount = esql.executeQuery(SQL);
	         System.out.println ("Customers with bills less than 100: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
		
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
	
		string SQL = 'SELECT C.fname, C.lname FROM Customer C,( SELECT customer_id,COUNT(customer_id) as car_num FROM Owns GROUP BY customer_id HAVING COUNT(customer_id) > 20 ) AS O WHERE O.customer_id = id';
		
		try
		{
	         int rowCount = esql.executeQuery(SQL);
	         System.out.println ("Customers with more than 20 cars: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
		string SQL = 'SELECT DISTINCT C.make,C.model, year FROM Car AS C, Service_Request AS S WHERE year < 1995 and S.car_vin = C.vin and S.odometer < 50000';
		
		try
		{
	         int rowCount = esql.executeQuery(SQL);
	         System.out.println ("Cars before 1995 with 50,000 miles: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		string SQL = 'SELECT C.make, C.model, R.creq FROM Car AS C, ( SELECT car_vin, COUNT(rid) AS creq FROM Service_Request GROUP BY car_vin ) AS R WHERE R.car_vin = C.vin ORDER BY R.creq DESC LIMIT 10	';
		
		try
		{
	         int rowCount = esql.executeQuery(SQL);
	         System.out.println ("Cars with most services: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		string SQL = 'SELECT C.fname , C.lname, Total FROM Customer AS C, (SELECT sr.customer_id, SUM(CR.bill) AS Total FROM Closed_Request AS CR, Service_Request AS SR WHERE CR.rid = SR.rid GROUP BY SR.customer_id) AS A WHERE C.id=A.customer_id ORDER BY A.Total DESC';
		
		try
		{
	         int rowCount = esql.executeQuery(SQL);
	         System.out.println ("Descending order of customers total bill: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
	}
	
}