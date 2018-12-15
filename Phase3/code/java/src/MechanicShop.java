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
import java.util.Date;
import java.text.DateFormat; 
import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Objects;

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
	
	final static String DATE_FORMAT = "dd-MM-yyyy";

	public static boolean checkDate(String date) 
	{
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
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

		System.out.print("Please enter your first name: ");
		try
		{
			firstName = in.readLine();
		} catch(Exception e) {
			System.err.println (e.getMessage ());
		}
		if(firstName.length() > 32) {
			do {
				System.out.print("Please enter your first name: ");
				try
				{
					firstName = in.readLine();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			} while(valid);
			valid = true;
		}
		System.out.print("Please enter your last name: ");
		try
				{
					lastName = in.readLine();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
		if(lastName.length() > 32) {
			do {
				System.out.print("Please enter your last name: ");
				try
				{
					lastName = in.readLine();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			} while(valid);
			valid = true;
		}
		System.out.print("Please enter your phone number: ");
		try
			{
				phone = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		if(phone.length() > 13) {
			do {
				System.out.print("Please enter your phone number: ");
				try
				{
					phone = in.readLine();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			} while(valid);
			valid = true; 
		}
		System.out.print("Please enter your address: ");
		try
			{
				address = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		if(address.length() > 256)
		{
			do {
				System.out.print("Please enter your address: ");
				try
				{
					address = in.readLine();
				} catch(Exception e) {
					System.err.println (e.getMessage ());
				}
				valid = false; 
			}while(valid);
		}
		String SQL = "INSERT INTO Customer(fname, lname, phone, address) Values(\'" + firstName + "\', \'" + lastName + "\', \'" + phone + "\', \'" + address + "\')";
		try {
		esql.executeUpdate(SQL);
		}catch(Exception e) {
				System.err.println (e.getMessage ());
		}
	}
	public static void AddMechanic(MechanicShop esql){//2
		String firstName = "";
		String lastName = "";
		int yearExp = 0;
		do
		{
			System.out.print("Please enter your first name: ");
			try
			{
				firstName = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		} while(firstName.length() > 32);
	
		do
		{
			System.out.print("Please enter your last name: ");
			try
			{
				lastName = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		} while(lastName.length() > 32);
		
		
		do
		{
			System.out.print("Please enter your year of experience: ");
			try
			{
				 yearExp = Integer.parseInt(in.readLine());
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		} while((yearExp < 0 || yearExp > 100));
		
		String yExp = Integer.toString(yearExp); 
		
		String SQL = "INSERT INTO Mechanic(fname, lname, experience) Values('" + firstName + "\', \'" + lastName + "\', \'" + yExp + "\')";
		try {
		esql.executeUpdate(SQL);
		}catch(Exception e){
				System.err.println (e.getMessage ());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3

		String VIN = "";
		String make = "";
		int year = 0;
		String model = "";
		
		do
		{
			System.out.print("Please enter the VIN: ");
			try
			{
				VIN = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		}while(VIN.length() > 16);
		
		do
		{
			System.out.print("Please enter the make: ");
			try
			{
				make = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		}while(make.length() > 32);
		
		do
		{
			System.out.print("Please enter the model: ");
			try
			{
				model = in.readLine();
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			}
		
		} while(model.length() > 32);
		
		do
		{
			System.out.print("Please enter the year: ");
			try
			{
				year = Integer.parseInt(in.readLine());
			} catch(Exception e) {
				System.err.println (e.getMessage ());
			} 
		} while (year < 1970);
		
		String yr = Integer.toString(year); 
		String SQL = "INSERT INTO Car(make,model,year) Values('" + make + "\', \'" + model + "\', \'" + yr + "')";
		try {
			esql.executeUpdate(SQL);
		}catch(Exception e) {
				System.err.println (e.getMessage ());
		}
	}

	public static void InsertServiceRequest(MechanicShop esql){//4
		String lname,input,fname,vin,cid;
		try{

			System.out.print("\tEnter last name: ");
         	lname = in.readLine();

		String query = "SELECT C.fname FROM Customer C WHERE C.lname = '" + lname + "'";         	
		List<List<String>> q2 = esql.executeQueryAndReturnResult(query);

		for(int i = 0; i<q2.size(); i++)
       		{
          		System.out.println((i+1)+ ". " + q2.get(i).get(0));
       		}
		
		if (q2.isEmpty()) {
			System.out.print("No customer with the last name " + lname + ".  Add as new customer? (Y/N):");
			input = in.readLine();
			
			if (input.contains("Y") || input.contains("y")) {
				AddCustomer(esql);
			query = "SELECT C.id, C.fname, C.lname FROM Customer C WHERE C.id = (SELECT MAX(C2.id) FROM Customer C2)";
                        q2 = esql.executeQueryAndReturnResult(query);
                        cid = q2.get(q2.size()-1).get(0);
                        fname = q2.get(q2.size()-1).get(1);
                        lname = q2.get(q2.size()-1).get(2);
			}
			else {
				return;
			}
		}
		else {
		System.out.print("Type 1 to choose an existing customer or 2 to add a new customer (1/2): ");
		input = in.readLine();
		
		if (input.contains("1")) {
			System.out.print("\tChoose the number corresponding to your first name: ");
   			fname = in.readLine();
			int num = Integer.parseInt(fname);
                	fname = q2.get(num-1).get(0);

			query = "SELECT C.id FROM Customer C WHERE C.fname = '" + fname + "' AND C.lname = '" + lname + "'";
			q2 = esql.executeQueryAndReturnResult(query);
			cid = q2.get(q2.size()-1).get(0);
		}
		else if (input.contains("2")) {
			AddCustomer(esql);
			query = "SELECT C.id, C.fname, C.lname FROM Customer C WHERE C.id = (SELECT MAX(C2.id) FROM Customer C2)";
			q2 = esql.executeQueryAndReturnResult(query);
			cid = q2.get(q2.size()-1).get(0);
			fname = q2.get(q2.size()-1).get(1);
			lname = q2.get(q2.size()-1).get(2);
		}
		else {
			System.out.println("Error: Invalid input.");
			return;
		}
		}
		System.out.println("Your name is " + fname + " " + lname + ".");
		System.out.println("Customer id is " + cid + ".");
		System.out.println("List of related cars: ");

		query = "SELECT c.vin, c.make, c.model, c.year FROM Customer cust, Car c, Owns o WHERE o.customer_id = cust.id AND o.car_vin = c.vin AND cust.fname = '" + fname + "' AND cust.lname = '" + lname + "'";
		q2 = esql.executeQueryAndReturnResult(query);

		for(int i = 0; i<q2.size(); i++)
       {
           System.out.println((i+1)+ ". " + q2.get(i).get(0)+ " " + q2.get(i).get(1) + ", " + q2.get(i).get(2) + ", " + q2.get(i).get(3));
       }

		System.out.print("Type 1 to choose an existing car or 2 to add a new car (1/2): ");
		input = in.readLine();
		if (input.contains("1")) {
			System.out.print("\tChoose the number corresponding to the car of your choice: ");
                	input = in.readLine();
                	int num = Integer.parseInt(input);
        	        vin = q2.get(num-1).get(0);
	
		}
		else if (input.contains("2")) {
			AddCar(esql);
			System.out.print("\tRe-enter your car's VIN: ");
			vin = in.readLine();
		}
		else {
			System.out.println("\tError: Invalid input.");
			return;
		}

			
		query = "SELECT MAX(rid) AS maxID FROM Service_Request";
                List<List<String>> maxIDStr = esql.executeQueryAndReturnResult(query);
                int rid = Integer.parseInt(maxIDStr.get(0).get(0)) + 1;
		

		System.out.print("\tEnter today's date in the format YYYY-MM-DD: ");
		String date = in.readLine();


		while (!checkDate(date)) {
                        System.out.print("\t Error: invalid. Enter today's date in the format YYYY-MM-DD: ");
                        date = in.readLine();
                }	

		System.out.print("\tEnter the number of miles on your odometer: ");
		String odometer = in.readLine();

		while (!isNumeric(odometer)) {
			System.out.print("\t Error: invalid. Enter the number of miles on your odometer: ");
                	odometer = in.readLine();
		}

		System.out.print("\tReason for service: ");
		String complain = in.readLine();

		query = "INSERT INTO Service_Request VALUES (" + rid + "," + cid + ",'" + vin + "','" + date + "'," + odometer + ",'" + complain + "')";
                esql.executeUpdate(query);

     }catch(Exception e){
         System.err.println (e.getMessage());
      }
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		String srNum, empid, choice;
		try{
		System.out.print("Enter Service Request Number: ");
         srNum = in.readLine();
         while(isNumeric(srNum) == false)
         {
           System.out.print("\tError: Not a number. Please Enter Service Request Number: ");
           srNum = in.readLine();
         }
         String query = "SELECT * FROM Service_Request WHERE rid =  '" + srNum + "'";
         List<List<String>> s1 = esql.executeQueryAndReturnResult(query);
         while(s1.isEmpty()){
           System.out.print("Service Request Number: " + srNum + " DNE.              \nRe-Enter the Service Request Number? (Y/N): ");
           choice = in.readLine();
           if(choice.contains("Y") || choice.contains("y")){
             System.out.print("Enter Service Request Number: ");
             srNum = in.readLine();
             query = "SELECT * FROM Service_Request WHERE rid =  '" + srNum + "'";
             s1 = esql.executeQueryAndReturnResult(query);
             }
           else if(choice.contains("N") || choice.contains ("n")){
             return;
           }
           else{
             System.out.print("Invalid input");
             return;
           }
         }
         
         String d = s1.get(0).get(3);
         int year = Integer.parseInt(d.substring(0,4));
         int month = Integer.parseInt(d.substring(5,7));
         int day = Integer.parseInt(d.substring(8,10));

	System.out.print("Enter Employee ID: ");
         empid = in.readLine();
         while(isNumeric(srNum) == false)
         {
           System.out.print("\tError: Not a number.Enter Service Request Number: ");
           srNum = in.readLine();
         }
         query = "SELECT * FROM Mechanic WHERE id =  '" + empid + "'";
         List<List<String>> m1 = esql.executeQueryAndReturnResult(query);
         
         while(m1.isEmpty()){
           System.out.print("Employee ID: " + empid + " does not exist.                         \nRe-Enter ID? (Y/N): ");
           choice = in.readLine();
           if(choice.contains("Y") || choice.contains("y")){
             System.out.print("Enter Employee ID: ");
             empid = in.readLine();
             query = "SELECT * FROM Mechanic WHERE rid =  '" + empid + "'";
             m1 = esql.executeQueryAndReturnResult(query);
             }
           else if(choice.contains("N") || choice.contains ("n")){
             return;
           }
           else{
             System.out.print("Invalid input");
             return;
           }
         }
         query = "SELECT MAX(wid) AS maxID FROM Closed_Request";
         List<List<String>> maxIDStr = esql.executeQueryAndReturnResult(query);
         int wid = Integer.parseInt(maxIDStr.get(0).get(0)) + 1;
                
         System.out.print("This Service Request was made on " +d+ ".\nEnter Today's date in the format YYYY-MM-DD: ");
         String date = in.readLine();
         int year2 = Integer.parseInt(date.substring(0,4));
         int month2 = Integer.parseInt(date.substring(5,7));
         int day2 = Integer.parseInt(date.substring(8,10));
         while(year2<year || (year2==year && month2<month) || (year2==year && month2==month && day2<day)){
         System.out.print("Inputed Date is after Service date. Please enter a valid date: ");
         date = in.readLine();
         year2 = Integer.parseInt(date.substring(0,4));
         month2 = Integer.parseInt(date.substring(5,7));
         day2 = Integer.parseInt(date.substring(8,10));
        }
         System.out.print("Enter comments:");
         String comments = in.readLine();
         System.out.print("Enter Bill: $");
         String bill = in.readLine();
         query = "INSERT INTO Closed_Request VALUES (" + wid + "," + srNum + "," + empid + ",'" + date + "','" + comments + "'," + bill +")";
         esql.executeUpdate(query);


      }catch(Exception e){
         System.err.println (e.getMessage());
      }
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		String SQL = "SELECT C.date,C.comment,C.bill FROM Closed_Request C WHERE bill < 100";
		
		try
		{
	         int rowCount = esql.executeQueryAndPrintResult(SQL);
	         System.out.println ("Customers with bills less than 100: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
		
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
	
		String SQL = "SELECT C.fname, C.lname FROM Customer C,( SELECT customer_id,COUNT(customer_id) as car_num FROM Owns GROUP BY customer_id HAVING COUNT(customer_id) > 20 ) AS O WHERE O.customer_id = id";
		
		try
		{
	         int rowCount = esql.executeQueryAndPrintResult(SQL);
	         System.out.println ("Customers with more than 20 cars: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
		String SQL = "SELECT DISTINCT C.make,C.model, year FROM Car AS C, Service_Request AS S WHERE year < 1995 and S.car_vin = C.vin and S.odometer < 50000";
		
		try
		{
	         int rowCount = esql.executeQueryAndPrintResult(SQL);
	         System.out.println ("Cars before 1995 with 50,000 miles: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//
		System.out.println("Enter an integer for k: "); 
		try{
			String input = in.readLine();
			String SQL = "SELECT C.make, C.model, R.creq FROM Car AS C, ( SELECT car_vin, COUNT(rid) AS creq FROM Service_Request GROUP BY car_vin ) AS R WHERE R.car_vin = C.vin ORDER BY R.creq DESC LIMIT " + input;
	    	int rowCount = esql.executeQueryAndPrintResult(SQL);
	        System.out.println ("Cars with most services: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		String SQL = "SELECT C.fname , C.lname, Total FROM Customer AS C, (SELECT sr.customer_id, SUM(CR.bill) AS Total FROM Closed_Request AS CR, Service_Request AS SR WHERE CR.rid = SR.rid GROUP BY SR.customer_id) AS A WHERE C.id=A.customer_id ORDER BY A.Total DESC";
		
		try
		{
	         int rowCount = esql.executeQueryAndPrintResult(SQL);
	         System.out.println ("Descending order of customers total bill: " + rowCount);
    	}
    	catch(Exception e)
    	{
        	 System.err.println (e.getMessage());
    	}
	}
	
}