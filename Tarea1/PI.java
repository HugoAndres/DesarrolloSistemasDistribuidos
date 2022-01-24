import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class PI {
    static Object lock = new Object();
    static double pi = 0;

    static class Worker extends Thread{
        Socket conexion;
        Worker(Socket conexion)
        {
            this.conexion = conexion;
        }

        public void run(){
            try {
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                double x = entrada.readDouble();
                synchronized (lock){
                    pi += x;
                }
                entrada.close();
                salida.close();
            } catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
    }

    public static  void main(String[] args) throws Exception{
        if (args.length != 1){
            System.err.println("Uso");
            System.err.println("Java PI <nodo>");
            System.exit(0);
        }
        int nodo = Integer.valueOf(args[0]);
        if (nodo == 0){
            ServerSocket servidor = new ServerSocket(50000);
            Vector<Worker> vectorWorker = new Vector<>();
            for (int i = 0; i < 4; i++){
                vectorWorker.add(null);
            }
            for (int i = 0; i < 4; i++){
                Socket conexion = servidor.accept();
                Worker w = new Worker(conexion);
                vectorWorker.set(i,w);
                vectorWorker.get(i).start();
            }
            for (int j = 0; j < 4; j++) {
                vectorWorker.get(j).join();
            }
            System.out.println(pi);
        }
        else {
            Socket conexion = null;
            for (;;) {
                try {
                    conexion = new Socket("localhost", 50000);
                    break;
                } catch (Exception e) {
                    Thread.sleep(100);
                }
            }
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                double suma = 0;
                for (int i = 0; i <= 10000000; i++){
                    suma = 4.0/(8*i+2*(nodo-2)+3) + suma;
                }
                suma = nodo%2 == 0 ? -suma: suma;
                salida.writeDouble(suma);

                salida.close();
                entrada.close();
                conexion.close();
        }
    }
}
