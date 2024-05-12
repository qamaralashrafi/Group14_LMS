/*reference :
 * java point website "https://www.javatpoint.com/how-to-create-a-thread-in-java "
 * course Textbook "James F. Kurose, Keith W. Ross, , "Computer Networking: A Top_Down Approach",Pearson Education; 8th edition (2020)"
 */

package group14_lms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class client {
    
    private String username;
    public static boolean isLibrarian;

    public client() {}

    public void start(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        System.out.println("Welcome To The Library Management System!");
        // Authentication
        authenticateUser(UserInput,outToServer,ServerInput);}

    private void authenticateUser(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        boolean authenticated = false;
        while (!authenticated) {
            // User input (username)
            System.out.print("Please Enter Your Username: ");
            username = UserInput.nextLine().trim();
            username = username.trim();
            // User input (password)
            System.out.print("Please Enter Your Password: ");
            String password = UserInput.nextLine().trim();
            password = password.trim();
            outToServer.writeBytes("2\n");
            outToServer.writeBytes(username + '\n');
            outToServer.writeBytes(password + '\n');
            // check if username and password are valid
            authenticated = Boolean.parseBoolean(ServerInput.readLine());
            if (!authenticated) {
                System.out.println("Invalid Username or Password. Please Try Again.");
                authenticateUser(UserInput,outToServer,ServerInput);
            }else System.out.println("Authenticated User.");
            System.out.println("Confirming if Client is Member/Librarian...");
            isLibrarian(UserInput, outToServer, ServerInput);
        }
         
    }
    
    private void isLibrarian(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException{
      
        outToServer.writeBytes("1\n");
        outToServer.writeBytes(username + '\n');
        // Main menu
        isLibrarian =Boolean.parseBoolean(ServerInput.readLine());
        System.out.println("You Are Authenticated as A " + (isLibrarian ? "Librarian" : "Member") + ". Please Press Enter To Display The Menu.");
        // Wait for the user to press enter
        UserInput.nextLine(); 
        if (!isLibrarian) {
             // Check if there are notifications about books
            boolean notify = Boolean.parseBoolean(ServerInput.readLine());
             // Print notifications
            if (notify) {
                System.out.println("\nBooks that should be returned:");
                int size = Integer.parseInt(ServerInput.readLine());
                // Read lines until reaching the end of input
                for (int i = 0; i < size; i++) {
                    System.out.println(ServerInput.readLine());
                }
                if( size == 0)System.out.println("No Borrowed Books! You Can Borrow a Book Now ;)");
                
                System.out.println("\nNotifications:");
                String line;
                while (!(line = ServerInput.readLine()).equalsIgnoreCase("END_OF_NOTIFICATIONS")) {
                    System.out.println("Received notification: " + line);
                }
                System.out.println("All notifications have been received.");
            }
        }

        System.out.println("Please press enter to go to the menu.");
        UserInput.nextLine();  // Wait for the user to press enter

        if(isLibrarian){
            // Print the main menu for librarian
            librarianMenu(UserInput,outToServer,ServerInput);
        }
        else {
            // Print the main menu for Member
            memberMenu(UserInput, outToServer, ServerInput);
        }                
    }
   

    private void librarianMenu(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        while (true) {
            System.out.println("\nLibrarian Menu:");
            System.out.println("1. Add a New book");
            System.out.println("2. Update Book Information");
            System.out.println("3. Delete a Book");
            System.out.println("4. Exit");
            System.out.print("Please Enter Your Choice: ");
            int choice = UserInput.nextInt();
            // Consume newline character
            UserInput.nextLine();
            // Users Choice From The Menu
            switch (choice) {
                case 1:
                    addNewBook(UserInput,outToServer, ServerInput);
                    break;
                case 2:
                    updateBook(UserInput,outToServer, ServerInput);
                    break;
                case 3:
                    deleteBook(UserInput,outToServer, ServerInput);
                    break;
                case 4:
                    Exit(outToServer,ServerInput);
                    return;
                default:
                    System.out.println("Invalid Choice. Please Try Again.");
            }
        }
    }

    private void addNewBook(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        try {
            System.out.print("Please Enter The Book Title: ");
            String title = UserInput.nextLine().trim();
            
            System.out.print("Please Enter The Number of Copies:  ");
            int copies = UserInput.nextInt();
            // Consume newline character
            UserInput.nextLine(); 
            // Send the info to the server
            outToServer.writeBytes("3\n");
            outToServer.writeBytes(title + '\n');
            outToServer.writeBytes(""+copies + '\n');
            // Check if the book is added
            boolean added = Boolean.parseBoolean(ServerInput.readLine());   
            if (added) {
                System.out.println("Book Added Successfully.");    
            } 
            else {        
                System.out.println("Book Could Not be Added.");    
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input. Please Enter a Valid Number of Copies.");   
        }   
    }

    private void updateBook(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        System.out.print("Enter The Title of The Book to Update: ");
        String title = UserInput.nextLine().trim();
        System.out.print("Enter The Number of Copies: ");
        int copies = UserInput.nextInt();
        // Consume newline character
        UserInput.nextLine(); 
        // Send the info to the server
        outToServer.writeBytes("4\n");
        outToServer.writeBytes(title + '\n');
        outToServer.writeBytes(""+copies + '\n');
        // Check if the book is uodated
        boolean updated = Boolean.parseBoolean(ServerInput.readLine());
        if (updated) {
            System.out.println("Book Information Updated Successfully.");
        } 
        else {
            System.out.println("Book Not Found.");
        }
    }
    
    private void deleteBook(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        System.out.print("Enter The Title of The Book to Delete: ");
        String title = UserInput.nextLine().trim();
        // Send the info to the server
        outToServer.writeBytes("5\n");
        outToServer.writeBytes(title + '\n');
        // Check if the book is updated
        boolean deleted = Boolean.parseBoolean(ServerInput.readLine());
        if (deleted) {
            System.out.println("Book Deleted Successfully.");
        }   
        else {
            System.out.println("Could Not Delete The Book.");
        }
    }

    private void memberMenu(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        while (true) {
            System.out.println("\nMember Menu:");
            System.out.println("1. Browse Available Books");
            System.out.println("2. Borrow a Book");
            System.out.println("3. Return a Book");
            System.out.println("4. Exit");
            System.out.print("Please Enter Your Choice: ");
            int choice = UserInput.nextInt();
            // Consume newline character
            UserInput.nextLine(); 
            // Users Choice From The Menu
            switch (choice) {
                case 1:
                    browseBooks(outToServer,ServerInput);
                    break;
                case 2:
                    borrowBook(UserInput,outToServer,ServerInput);
                    break;
                case 3:
                    returnBook(UserInput,outToServer,ServerInput);
                    break;
                case 4:
                    Exit(outToServer,ServerInput);
                    return;
                default:
                    System.out.println("Invalid Choice. Please Try Again.");
            }
        }
    }

    private void browseBooks(DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        // Print all available books
        System.out.println("\nAvailable Books:");        
        outToServer.writeBytes("6\n");
        int size= Integer.parseInt(ServerInput.readLine());    
        // Read lines until reaching the end of input
        for(int i =0;i<size;i++) {
            System.out.println(ServerInput.readLine());
        }
    }
    
    private void borrowBook(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        System.out.println("Enter The Title of The Book You Want to Borrow: ");
        String title = UserInput.nextLine().trim();
        // Send the info to the server
        outToServer.writeBytes("7\n");
        outToServer.writeBytes(title+'\n');
        // Check if the book is borrowd
        boolean borrowed = Boolean.parseBoolean(ServerInput.readLine());
        if (borrowed) {
            System.out.println("Book Borrowed Successfully.");
        } 
        else {
            System.out.println("Book Not Available or Does Not Exist. You Will Be Notified When It Is Available.");
        }
    }

    private void returnBook(Scanner UserInput,DataOutputStream outToServer,BufferedReader ServerInput) throws IOException {
        
        System.out.println("Enter The Title of The Book You Want to Return: ");
        String title = UserInput.nextLine().trim();
        // Send the info to the server
        outToServer.writeBytes("8\n");
        outToServer.writeBytes(title+'\n');
        // Check if the book is returned
        boolean returned = Boolean.parseBoolean(ServerInput.readLine());               
        if (returned) {
            System.out.println("Book Returned Successfully.");
        } 
        else {
            System.out.println("Book Could Not Be Returned");
        }
    }
    
    private void Exit(DataOutputStream outToServer, BufferedReader ServerInput) throws IOException {
        
        outToServer.writeBytes("9\n");
        System.out.println("See You Next Time " + username + "!");
    }

    public static void main(String[] args) throws IOException {
        
        client client = new client();
        //read from user
        Scanner UserInput = new Scanner(System.in);
        //create socket for client and assign port number
        Socket ClientSocket = new Socket("localHost", 8998);        
        //to send out data to server 
        DataOutputStream outToServer = new DataOutputStream(ClientSocket.getOutputStream());
        //to recieve from server
        BufferedReader ServerInput = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
        client.start(UserInput,outToServer,ServerInput);  
        ClientSocket.close();
        ServerInput.close();
        UserInput.close();
        outToServer.close(); 
    }
    
}
        