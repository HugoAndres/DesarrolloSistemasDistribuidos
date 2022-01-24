import java.io.*;
import java.net.*;
import java.lang.*;

public class Chat { 

    static class Worker extends Thread {
    
        public void run() {
            /* En un ciclo infinito se recibirán los mensajes enviados al grupo 
             230.0.0.0 a través del puerto 50000 y se desplegarán en la pantalla.*/

            for (;;) {
                try {
                    InetAddress grupo = InetAddress.getByName("230.0.0.0");
                    MulticastSocket socket = new MulticastSocket(50000);
                    socket.joinGroup(grupo);
                    byte[] salida = recibe_mensaje_multicast(socket, 100);
                    System.out.println(new String(salida,"ISO-8859-1"));
                    socket.leaveGroup(grupo);
                    socket.close();                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) throws Exception{
        Worker w = new Worker();
        w.start();
        String nombre = args[0];
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));        
        /* En un ciclo infinito se leerá los mensajes del teclado y se enviarán
        al grupo 230.0.0.0 a través del puerto 50000.*/
        for (;;) {
            System.out.print("Ingrese el mensaje a enviar: ");
            String mensaje = entrada.readLine();
            String salida = "\r" + nombre + ": " + mensaje;
            envia_mensaje_multicast(salida.getBytes(),"230.0.0.0",50000); 
        }
    }

    static void envia_mensaje_multicast(byte[] buffer, String ip, int puerto) throws IOException{
        DatagramSocket socket = new DatagramSocket();
        socket.send(new DatagramPacket(buffer,buffer.length,InetAddress.getByName(ip),puerto));
        socket.close();
    }

    static byte[] recibe_mensaje_multicast(MulticastSocket socket,int longitud_mensaje) throws IOException {
        byte[] buffer = new byte[longitud_mensaje];
        DatagramPacket paquete = new DatagramPacket(buffer,buffer.length);
        socket.receive(paquete);
        return paquete.getData();
    }    
}