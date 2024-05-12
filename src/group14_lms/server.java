/*reference :
 * java point website "https://www.javatpoint.com/how-to-create-a-thread-in-java "
 * course Textbook "James F. Kurose, Keith W. Ross, , "Computer Networking: A Top_Down Approach",Pearson Education; 8th edition (2020)"
 */

package group14_lms;

import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class server {
   
    // Map to store username-password pairs
    private static Map<String, String> users; 
    // Map to store book-title and number of copies pair
    private static Map<String, Integer> books; 
    private static Map<String, BorrowLogEntry> borrowLog;
    //to log requests of unavailable books
    private static final Map<String, List<String>> memberRequests = new HashMap<>(); 
    
    public static void main(String[] args) throws IOException{
        
        System.out.println("Welcome To Library Management System (LMS) :)");
        users = new HashMap<>();
        books = new HashMap<>();
        borrowLog = new HashMap<>();
        loadUserData();
        loadBookData();
        loadBorrowLog();
            try {
                ServerSocket serverSocket = new ServerSocket(8998);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New Client Connected: " + clientSocket);
                    // Handle client in a new thread
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }
    
    static class ClientHandler implements Runnable {
        
        // Define a Runnable task to handle each client connection
        private final Socket clientSocket;
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        @Override
        public void run() {
            String username="";
            String password;
            String title;
            int copies;
            BufferedReader inFromClient = null;
            DataOutputStream outToClient = null;
           try {
               // to read input from client
               inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
               // to send message to client
               outToClient = new DataOutputStream(clientSocket.getOutputStream());
               String number= "0";
               
                while(!number.equals("9")){ 
                    
                    number = inFromClient.readLine();
                    
                    switch(number){
                        
                        case "1":
                            // recieve client username
                            username = inFromClient.readLine();
                            System.out.println(username +" Has Logged In The System");
                            // confirm to client if client member/librarian
                            boolean isLib = isLibrarian(username);
                            outToClient.writeBytes(String.valueOf(isLib)+ '\n');
                            if(!isLib){
                                List<String> borrowedBooks=hasBorrowed(username);
                                if(borrowedBooks!= null){
                                    outToClient.writeBytes(String.valueOf(true)+'\n');
                                    NotifyOfBorrowedBooks(username, borrowedBooks, outToClient); 
                                }
                                else outToClient.writeBytes(String.valueOf(false)+'\n'); 
                                checkAndNotifyMembers(outToClient);
                            }
                            break;
    
                        case "2":
                            // recieve client username
                            username = inFromClient.readLine();
                            // recieve client password
                            password = inFromClient.readLine();
                            boolean authenticated = authenticateUser(username, password);
                            outToClient.writeBytes("" + authenticated + '\n');
                   
                            break;
                            
                        case "3" :
                            // Reading the title from the client
                            title = inFromClient.readLine();
                            // recieve copies
                            copies = Integer.parseInt(inFromClient.readLine());
                            outToClient.writeBytes(""+addBook(title, copies)+ '\n');
                            System.out.println(username+" Librarian Added a Book To The library");
                            break;
                            
                        case "4" :
                            // Reading the title from the client
                            title = inFromClient.readLine();
                            // recieve copies
                            copies = Integer.parseInt(inFromClient.readLine());
                            outToClient.writeBytes(""+updateBook(title, copies)+ '\n');
                            System.out.println(username+" Librarian Modified The Number of Copies in The Library");
                            break;
                            
                        case "5":
                            // Reading the title from the client
                            title = inFromClient.readLine();
                            outToClient.writeBytes(""+deleteBook(title)+ '\n');
                            System.out.println(username+" Librarian Removed a Book From The Library");
                            break;
                            
                        case "6" :
                            browseBooks(outToClient);
                            System.out.println(username+" Member Displayed The Books List");
                            break;
                            
                        case "7" :
                            // Reading the title from the client
                            title = inFromClient.readLine();
                            outToClient.writeBytes(""+borrowBook(title,username)+ '\n');
                            System.out.println(username+" Member Borrowed a Book From The Library");
                            break;
                            
                        case "8":
                            // Reading the title from the client
                            title = inFromClient.readLine();  
                            // Calling the updated returnBook method with both title and DataOutputStream
                            outToClient.writeBytes(""+returnBook(title,outToClient)+ '\n');
                            System.out.println(username+" Member Returned Back The Book They Had Borrowed");
                            break;                                            
                    }
                }
            }  
            catch (IOException e) {
               e.printStackTrace();
            } 
            finally { 
               try {  
                  System.out.println(username +" has logged out of the system");
                  inFromClient.close();
                  outToClient.close();
                  clientSocket.close();
                }
               catch (IOException e) {
                   e.printStackTrace();
                }
            }
        }
    }

    private static void loadUserData() throws FileNotFoundException, IOException {
        
        // Load user data from text file
        BufferedReader reader = new BufferedReader(new FileReader("users.txt")); 
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                users.put(parts[0], parts[1]);
            }
    }

    private static void loadBookData() throws FileNotFoundException, IOException {
        
        // Load book data from text file
        BufferedReader reader = new BufferedReader(new FileReader("books.txt")); 
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                books.put(parts[0], Integer.parseInt(parts[1]));
            }     
    }
    
    private static void loadBorrowLog() throws IOException {
        
        // Load borrow log from text file
        File borrowLogFile = new File("borrow_log.txt");
        if (!borrowLogFile.exists()) {
            // Create a new borrow log file if it doesn't exist
            borrowLogFile.createNewFile();
        }
        
        BufferedReader reader = new BufferedReader(new FileReader(borrowLogFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            // Assuming the format is bookTitle,memberName,borrowDate,returnDate
            borrowLog.put(parts[0], new BorrowLogEntry(parts[1], parts[2], parts[3])); 
        }
        reader.close();
    }

    public static boolean authenticateUser(String username, String password) {
        
        // Authenticate user based on username and password
        return users.containsKey(username) && users.get(username).equals(password);
    }
    
    public static boolean isLibrarian(String username) {
        
        // Check if the user is authorized as librarian
        return username.toLowerCase().contains("librarian");
    }
    
    public static synchronized List<String> hasBorrowed(String memberName){
        
        List<String> borrowedBooks = new ArrayList<>();
        for (Map.Entry<String, BorrowLogEntry> entry : borrowLog.entrySet()) {
            if (entry.getValue().getMemberName().equals(memberName)) {
                // Add book title to the list of found books
                borrowedBooks.add(entry.getKey()); 
            }
        }
        return borrowedBooks;
    }
    
    public static synchronized void NotifyOfBorrowedBooks(String memberName,List<String> borrowedBooks,DataOutputStream outToClient) throws IOException {
        
        outToClient.writeBytes(""+borrowedBooks.size()+'\n');
        for (String book : borrowedBooks) {
            BorrowLogEntry entry = borrowLog.get(book);
            // Assuming BorrowLogEntry has a method to get copies
            String bookInfo = book + " Must Be Returned By: " + entry.returnDate+"!"; 
            outToClient.writeBytes(bookInfo + '\n');
        }
    }
    
    public static void browseBooks(DataOutputStream outToClient) throws IOException {
        
        // Return available books information
        outToClient.writeBytes(""+books.size()+'\n');
        for (Map.Entry<String, Integer> entry : books.entrySet()) {
            String bookInfo = entry.getKey() + " - Copies: " + entry.getValue() ;
            outToClient.writeBytes(""+bookInfo+'\n');
        }
    }

    public static synchronized boolean updateBook(String bookTitle, int newCopies) {
        
        // Allow librarian to update book information
        if (books.containsKey(bookTitle)) {
            books.put(bookTitle, newCopies);
            // Save changes to the text file
            saveBookData();
            return true;
        }
        return false;
    }

    public static synchronized boolean addBook(String bookTitle, int numCopies) {
        
        // Allow librarian to add a new book
        if (!books.containsKey(bookTitle)) {
            books.put(bookTitle, numCopies);
            // Save changes to the text file
            saveBookData();
            return true;
        }
        return false;
    }

    public static synchronized boolean deleteBook(String bookTitle) {
        
        // Allow librarian to delete a book
        if (books.containsKey(bookTitle)) {
            books.remove(bookTitle);
            // Save changes to the text file
            saveBookData();
            return true;
        }
        return false;
    }

    public static synchronized boolean borrowBook(String bookTitle,String memberName) {
        // Allow a member to borrow a book
        if (books.containsKey(bookTitle) && books.get(bookTitle) > 0) {
            // Calculate return date (borrow date + 24 hours)
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
            String borrowDate = dateFormat.format(calendar.getTime());
            // Calculate return date (borrow date + 24 hours)
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            String returnDate = dateFormat.format(calendar.getTime());
   
            books.put(bookTitle, books.get(bookTitle) - 1);
            borrowLog.put(bookTitle, new BorrowLogEntry(memberName, borrowDate, returnDate)); 
            // Save changes to the text file
            saveBookData();
            // Save changes to the borrow log file
            saveBorrowLog();
            return true;
        }
        else{
            addBookRequest( bookTitle,  memberName);
            return false;}
    }
    
    public static synchronized boolean returnBook(String bookTitle, DataOutputStream outToClient) throws IOException {
        
        // Allow a member to return a book
        if (borrowLog.containsKey(bookTitle)) {
            books.put(bookTitle, books.get(bookTitle) + 1);
            borrowLog.remove(bookTitle);
            // Save changes to the text file
            saveBookData();
            // Save changes to the borrow log file
            saveBorrowLog();  

        return true;
        }
        else return false;
    }

    public Map<String, Integer> getBooks() {
        
        // Ensure thread-safe access to the books map
        return Collections.unmodifiableMap(books);
    }

    private static void saveBookData() {
        
        // Save book data to the text file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Map.Entry<String, Integer> entry : books.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void saveBorrowLog() {
        
        // Save borrow log data to the text file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("borrow_log.txt"))) {
            for (Map.Entry<String, BorrowLogEntry> entry : borrowLog.entrySet()) {
                String bookTitle = entry.getKey();
                String memberName = entry.getValue().getMemberName();
                String borrowDate = entry.getValue().getBorrowDate();
                String returnDate = entry.getValue().getReturnDate();
                writer.write(bookTitle + "," + memberName + "," + borrowDate + "," + returnDate);
                writer.newLine();
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addBookRequest(String bookTitle, String memberName) {
        
        // Method to add a request from a member for a book
        List<String> membersRequesting = memberRequests.getOrDefault(bookTitle, new ArrayList<>());        
            membersRequesting.add(memberName);
            memberRequests.put(bookTitle, membersRequesting);
         
    }
 
    public static synchronized void removeBookRequest(String bookTitle, String memberName) {
        
        // Method to remove a member's request (called when a member is notified)
        List<String> membersRequesting = memberRequests.get(bookTitle);
            if (membersRequesting != null) { 
                membersRequesting.remove(memberName);
                if (membersRequesting.isEmpty()) {
                    memberRequests.remove(bookTitle);
                }
                else {
                    memberRequests.put(bookTitle, membersRequesting);
                }
            }
    }


    public static synchronized void checkAndNotifyMembers(DataOutputStream outToClient) throws IOException {
        String response = "END_OF_NOTIFICATIONS"; 
        // Check for available books and notify members
        for (Map.Entry<String, Integer> entry : books.entrySet()) {
            if (entry.getValue() > 0 && memberRequests.containsKey(entry.getKey())) {
                List<String> membersToNotify = memberRequests.get(entry.getKey());
                Iterator<String> iterator = membersToNotify.iterator();

                while (iterator.hasNext()) {
                    String member = iterator.next();
                    outToClient.writeBytes("Notification: Based on your request, The book " + entry.getKey() + " is now available.\n");
                    System.out.println("Book available: " + entry.getKey() + ", notifying member: " + member);
                    //iterator.remove();  // Remove the member from the list using iterator's remove method
                }
               // Remove request after notification
                if (membersToNotify.isEmpty()) {  memberRequests.remove(entry.getKey());}
            }
        }
        outToClient.writeBytes("END_OF_NOTIFICATIONS\n");
        outToClient.flush();    
}
    
    private static class BorrowLogEntry {
        
        // Class for borrow log entry
        private String memberName;
        private String borrowDate;
        private String returnDate;

        public BorrowLogEntry(String memberName, String borrowDate, String returnDate) {
            
            this.memberName = memberName;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate;
        }

        public String getMemberName() {
            
            return memberName;
        }

        public String getBorrowDate() {
            
            return borrowDate;
        }

        public String getReturnDate() {
            
            return returnDate;
        }
    }
       
}




