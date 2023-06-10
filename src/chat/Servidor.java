package chat;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Servidor {
    ArrayList<Conexion> conexiones;
    ServerSocket   ss;
    
    String [][] usuarios = {{"alex",  "123"},
                            {"kevin",  "345"},
                            {"alan",  "890"},
                            {"luis","678"}};
    
    public static void main(String args[]) {
       
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new Servidor()).start();
            }
        });

    }

    private void start() {
        this.conexiones = new ArrayList<>();
        Socket socket;
        Conexion cnx;
        
        try {
            ss = new ServerSocket(3000); // 1001 - 65535
            System.out.println("Servidor iniciado, en espera de conexiones");
            
            while (true){             
                socket = ss.accept();
                cnx = new Conexion(this, socket, conexiones.size());
                conexiones.add(cnx);
                cnx.start();                
            }            
                       
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }            
    }
    
    // Broadcasting
    private void difundir(String id, String mensaje) {
        Conexion hilo;
        for (int i = 0; i < this.conexiones.size(); i++){
            hilo = this.conexiones.get(i);
            if (hilo.cnx.isConnected()){
                if (!id.equals(hilo.id)){
                    hilo.enviar(mensaje);
                }
            }
        }//To change body of generated methods, choose Tools | Templates.
    }
    
    class Conexion extends Thread {
        BufferedReader in;
        PrintWriter    out;        
        Socket cnx;
        Servidor padre;
        int numCnx = -1;
        String id = "";
        
        public final int SIN_USER   = 0;
        public final int USER_IDENT = 1;
        public final int PASS_PDTE  = 2;
        public final int PASS_OK    = 3;
        public final int CHAT       = 4;
                
        public Conexion(Servidor padre, Socket socket, int num){
            this.cnx = socket;
            this.padre = padre;
            this.numCnx = num;
            this.id = socket.getInetAddress().getHostAddress()+num;
        }
        
        @Override
        public void run() {
            String linea="", user="", pass="", mensaje="";
            int estado = SIN_USER;
            int usr = -1;
            
            try {
                in = new BufferedReader(new InputStreamReader(cnx.getInputStream()));
                out = new PrintWriter(cnx.getOutputStream(),true);
                
                System.out.printf("Aceptando conexion desde %s\n",
                        cnx.getInetAddress().getHostAddress());
                                
                while(!mensaje.toLowerCase().equals("salir")){                    
                    switch (estado){
                        case SIN_USER:
                            out.println("Bienvenido, proporcione su usuario");

                            estado = USER_IDENT;
                            break;
                        case USER_IDENT:
                            user = in.readLine();
                            boolean found = false;
                            for (int i=0; i < usuarios.length; i++){
                                if (user.equals(usuarios[i][0])){
                                    found = true;
                                    usr = i;
                                }
                            }                            
                            
                            if (!found){
                                estado = SIN_USER;
                            } else {
                                estado = PASS_PDTE;
                            }                                                                                                             
                            break;
                        case PASS_PDTE:
                            out.println("Escriba el password");
                            pass = in.readLine();                            
                            if (pass.equals(usuarios[usr][1])){
                                estado = PASS_OK;
                            }
                            break;
                        case PASS_OK:
                            out.println("Autenticado!");
                            this.padre.difundir(this.id, "conectado:"+user);
                            estado = CHAT;
                            break;
                        case CHAT:
                            mensaje = in.readLine();
                            System.out.printf("%s - %s\n",
                                        cnx.getInetAddress().getHostAddress(),
                                        user + ":" +mensaje);
                            
                            this.padre.difundir(this.id, user+" : "+mensaje);
                            break;
                    }                        
                }                
                this.cnx.close();
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }

        private void enviar(String mensaje) {
            this.out.println(mensaje); //To change body of generated methods, choose Tools | Templates.
        } 
    }   
}


