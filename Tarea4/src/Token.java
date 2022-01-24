import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Token {
    static DataInputStream entrada;
    static DataOutputStream salida;
    static boolean primera_vez = true;
    static String ip;
    static int nodo;
    static int token;
    static int contador = 0;

    static class Worker extends Thread{
        public void run (){
            try {
                ServerSocket servidor;
                servidor = new ServerSocket(50000);
                Socket conexion;
                conexion = servidor.accept();
                entrada = new DataInputStream(conexion.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2){
            System.err.println("Se debe pasar como parametro el numero del nodo y la ip del siguiente nodo");
            System.exit(1);
        }

        nodo = Integer.valueOf(args[0]);
        ip = args[1];
        Worker w;
        w = new Worker();
        w.start();
        Socket conexion = null;

        for (;;){
            try {
                conexion = new Socket(ip,50000);
                break;
            } catch (Exception e){
                Thread.sleep(500);
            }
        }

        salida = new DataOutputStream(conexion.getOutputStream());
        w.join();

        for (;;){
            if (nodo == 0){
                if ( primera_vez){
                    primera_vez = false;
                    token = 1;
                } else {
                    token = entrada.readInt();
                    contador++;
                    System.out.println("Nodo = "+nodo+", Contador = "+contador+", Token ="+token);
                }
            } if (nodo != 0){
                token = entrada.readInt();
                contador++;
                System.out.println("Nodo = "+nodo+", Contador = "+contador+", Token ="+token);
            } if (nodo == 0 && contador == 1000){
                break;
            }
            salida.writeInt(token);
        }
    }
}
