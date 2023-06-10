package chat;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente {

    BufferedReader entrada;
    PrintWriter salida;
    Socket conexion;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Cliente().start();
            }
        });
    }

    void start() {
        JFrame frame = new JFrame("Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 410);

         JPanel panel = new JPanel();
        frame.add(panel);

        JLabel label = new JLabel("Datos:");
        panel.add(label);

        JTextField textField = new JTextField(20);
        panel.add(textField);

        JButton button = new JButton("Enviar");
        panel.add(button);

        JTextArea textArea = new JTextArea(20, 30);
        panel.add(new JScrollPane(textArea));

        

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String mensaje = textField.getText();
                
                salida.println(mensaje);
            }
        });

        frame.setVisible(true);

        Conexion hilo;
        try {
            this.conexion = new Socket("127.0.0.1", 3000);

            entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            salida = new PrintWriter(conexion.getOutputStream(), true);

            hilo = new Conexion(entrada, textArea);
            hilo.start(); // Hilo encargado de lecturas del servidor

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class Conexion extends Thread {

        public boolean ejecutar = true;
        BufferedReader entrada;
        JTextArea textArea;

        public Conexion(BufferedReader entrada, JTextArea textArea) {
            this.entrada = entrada;
            this.textArea = textArea;
        }

        @Override
        public void run() {
            String respuesta;
            while (ejecutar) {
                try {
                    respuesta = entrada.readLine();
                    if (respuesta != null) {
                        textArea.append(respuesta + "\n");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
