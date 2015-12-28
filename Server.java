/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author taylo
 */
public class Server {


    public void main(String[] args) {
        
        Stack clientStack = new Stack();
        Client client;
        Connection con;
        
        ExecutorService socketExecutor = Executors.newCachedThreadPool();
        ExecutorService convoExecutor = Executors.newCachedThreadPool();
        while(true){        
            socketExecutor.execute(con = new Connection());
            client = con.getClient();
            clientStack.add(client);            
            //conversation start when stack has at least 2 clients
                if(clientStack.size()>= 2){
                    convoExecutor.execute(new Conversation((Client)clientStack.pop(), (Client)clientStack.pop()));
                }           
        }   
        //socketExecutor.shutdown();              
    }
    
    private class Connection implements Runnable{
    
        private Lock lock = new ReentrantLock();
        Socket socket;
        InetAddress address;
        Client client;
        
        @Override
        public void run(){
            lock.lock();
            try{                
                socket = getSocket();                
                client = new Client(socketNumber, clientCount, socket);
                //Thread.sleep(5);
            }
            //catch(InterruptedException e){} 
            catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                lock.unlock();
            }
        }
        
        public Client getClient(){
            return client;
        }
        private Socket getSocket() throws IOException{
        ServerSocket serverSocket = new ServerSocket((socketNumber));
        Socket tempSocket;
        
        tempSocket = serverSocket.accept();       
        
        return tempSocket;
        }
    }
    
    private static Integer socketNumber = 8001;
    private static Integer clientCount = 0;
    
    public static int getSocketNumber(){
        return socketNumber;            
    }
    
    private class Client{
        int sockNum;
        int id;
        Socket socket;
        
        Client(int sockNum, int id, Socket socket){
            this.sockNum = sockNum;
            this.id = id;
            this.socket = socket;
            clientCount++;
            socketNumber++;
        }
        
        public int getSockNum(){
            return sockNum;
        }
        public int getID(){
            return id;
        }
        public Socket getSocket(){
            return socket;
        }
        public void setSocket(Socket socket){
            this.socket = socket;
        }
        public void setSockNum(int sockNum){
            this.sockNum = sockNum;
        }
        public void setID(int id){
            this.id = id;
        }
    }
    
    private class Conversation implements Runnable{
        private Client client1, client2;
        String message1, message2;
        DataInputStream inputFC1, inputFC2;
        DataOutputStream outputFC1, outputFC2;
        
        public Conversation(Client client1, Client client2){
            this.client1 = client1;
            this.client2 = client2;
        }
        
        @Override
        public void run(){
            
            try{
                inputFC1 = new DataInputStream(client1.socket.getInputStream());
                inputFC2 = new DataInputStream(client2.socket.getInputStream());
                outputFC1 = new DataOutputStream(client1.socket.getOutputStream());
                outputFC2 = new DataOutputStream(client2.socket.getOutputStream());
                
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            //continously serve the clients
            //conversation between two clients
            while(true){
                try{
                message1 = inputFC1.readUTF();
                message2 = inputFC2.readUTF();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                try{
                if (!message1.equals(""))                                     
                    outputFC2.writeUTF(message1);
                
                if(!message2.equals(""))
                    outputFC1.writeUTF(message2);
                
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                message1 = "";
                message2 = "";                
            }
        }       
    }
    


    
}
