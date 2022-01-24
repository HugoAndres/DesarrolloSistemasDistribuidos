import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.Thread;

class MultiplicationDistributedMatrix {

    static Object lock = new Object();
    static int N = 1000;

    static double[][] A = new double[N][N];
    static double[][] B = new double[N][N];
    static double[][] C = new double[N][N];

    static double[][] A1 = new double[N/2][N];
    static double[][] A2 = new double[N/2][N];
    static double[][] B1 = new double[N/2][N];
    static double[][] B2 = new double[N/2][N];

    static class Worker extends Thread{
        Socket conexion;
        Worker(Socket conexion){
            this.conexion = conexion;
        }

        public void run(){
            try {

                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

                int x = entrada.readInt();

                if (x == 1){
                    for(int i = 0; i < (N/2); i++)
                        for(int j = 0; j < N; j++){
                            A1 [i][j] = A [i][j];
                            B1 [i][j] = B [i][j];
                            salida.writeDouble(A1[i][j]);
                            salida.writeDouble(B1[i][j]);
                        }
                }
                else if (x == 2){
                    for(int i = 0; i < (N/2); i++)
                        for(int j = 0; j < N; j++){
                            A1 [i][j] = A [i][j];
                            salida.writeDouble(A1[i][j]);
                        }
                    for(int i = (N/2); i < N; i++)
                        for(int j = 0; j < N; j++){
                            B2 [i - (N/2)][j] = B [i][j];
                            salida.writeDouble(B2[i - (N/2)][j]);
                        }
                }
                else if (x == 3){
                    for(int i = (N/2); i < N; i++)
                        for(int j = 0; j < N;j++){
                            A2 [i - (N/2)][j] = A [i][j];
                            salida.writeDouble(A2[i - (N/2)][j]);
                        }
                    for(int i = 0; i < (N/2); i++)
                        for(int j = 0; j < N; j++){
                            B1 [i][j] = B [i][j];
                            salida.writeDouble(B1[i][j]);
                        }
                }
                else if (x == 4){
                    for(int i = (N/2); i < N; i++)
                        for(int j = 0; j < N;j++){
                            A2 [i - (N/2)][j] = A [i][j];
                            B2 [i - (N/2)][j] = B [i][j];
                            salida.writeDouble(A2[i - (N/2)][j]);
                            salida.writeDouble(B2[i - (N/2)][j]);
                        }
                }

                synchronized(lock){
                    if(x == 1){
                        double[][] C1 = new double[N/2][N/2];
                        for(int i = 0; i < (N/2); i++)
                            for(int j = 0; j < (N/2); j++){
                                C1[i][j] = entrada.readDouble();
                                C[i][j] = C1[i][j];
                            }
                    }
                    else if (x == 2){
                        double[][] C2 = new double[N/2][N];
                        for(int i = 0; i < (N/2); i++)
                            for(int j = (N/2); j < N; j++){
                                C2[i][j] = entrada.readDouble();
                                C[i][j] = C2[i][j];
                            }
                    }
                    else if (x == 3){
                        double[][] C3 = new double[N][N/2];
                        for(int i = (N/2); i < N; i++)
                            for(int j = 0; j < (N/2); j++){
                                C3[i - (N/2)][j] = entrada.readDouble();
                                C[i][j] = C3[i - (N/2)][j];
                            }
                    }
                    else if (x == 4){
                        double[][] C4 = new double[N][N];
                        for(int i = (N/2); i < N; i++)
                            for(int j = (N/2); j < N; j++){
                                C4[i - (N/2)][j - (N/2)] = entrada.readDouble();
                                C[i][j] = C4[i - (N/2)][j - (N/2)];
                            }
                    }
                }

                entrada.close();
                salida.close();
                conexion.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1){
            System.err.println("Uso");
            System.err.println("java MultiplicationDistributedMatrix <nodo>");
            System.exit(0);
        }

        int nodo = Integer.valueOf(args[0]);
        long checksum = 0;
        System.out.println("N = "+N);
        if (nodo == 0){
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    A[i][j] = 2 * i + j;
                    B[i][j] = 2 * i - j;
                    C[i][j] = 0;
                }

            if (N == 4){
                System.out.println("\n La matriz A es: ");
                for (int i = 0; i < N; i++){
                    for (int j = 0; j < N; j++) {
                        System.out.print((int) A[i][j] + " ");
                    }
                    System.out.println("");
                }
                System.out.println("\n La matriz B es: ");
                for (int i = 0; i < N; i++){
                    for (int j = 0; j < N; j++) {
                        System.out.print((int) B[i][j] + " ");
                    }
                    System.out.println("");
                }
            }

            for (int i = 0; i < N; i++)
                for (int j = 0; j < i; j++){
                    double t = B[i][j];
                    B[i][j] = B[j][i];
                    B[j][i] = t;
                }

            ServerSocket servidor = new ServerSocket(50000);
                Worker[] w = new Worker[4];

            for (int i = 0; i < 4; i++){
                Socket conexion = servidor.accept();
                w[i] = new Worker(conexion);
                w[i].start();
            }

            for (int j = 0;j < 4; j++){
                w[j].join();
            }

            servidor.close();

            for(int i = 0; i < N; i++){
                for(int j = 0; j < N; j++){
                    checksum += C[i][j];
                }
            }

            System.out.println("El checksum de matriz C es: " + checksum);

            if (N == 4){
                System.out.println("La matriz C = A x B es:");
                for(int i = 0; i < N; i++) {
                    for(int j = 0; j < N; j++) {
                        System.out.print((int) C[i][j] + " ");
                    }
                    System.out.println("");
                }
            }
        }
        else{
            double[][] recibeA = new double[N/2][N];
            double[][] recibeB = new double[N/2][N];
            double[][] productoC = new double[N/2][N/2];

            Socket conexion = new Socket("localhost", 50000);

            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

            salida.writeInt(nodo);
            if(nodo == 1){
                for (int i = 0; i < (N/2); i++)
                    for (int j = 0; j < N; j++){
                        recibeA[i][j] = entrada.readDouble();
                        recibeB[i][j] = entrada.readDouble();
                    }

                for (int i = 0; i < (N/2); i++)
                    for (int j = 0; j < (N/2); j++)
                        for (int k = 0; k < N; k++)
                            productoC[i][j] += recibeA[i][k] * recibeB[j][k];

                for(int i = 0; i < (N / 2); i++)
                    for(int j = 0; j < (N / 2); j++){
                        salida.writeDouble(productoC[i][j]);
                    }

            }
            else if (nodo == 2){
                for(int i = 0; i < (N/2); i++)
                    for(int j = 0; j < N; j++){
                        recibeA[i][j] = entrada.readDouble();
                    }
                for(int i = (N/2); i < N; i++)
                    for(int j = 0; j < N; j++){
                        recibeB[i-(N/2)][j] = entrada.readDouble();
                    }
                for (int i = 0; i < (N/2); i++)
                    for (int j = 0; j < (N/2); j++)
                        for (int k = 0; k < N; k++)
                            productoC[i][j] += recibeA[i][k] * recibeB[j][k];
                for(int i = 0; i < (N / 2); i++)
                    for(int j = 0; j < (N / 2); j++){
                        salida.writeDouble(productoC[i][j]);
                    }
            }
            else if (nodo == 3){
                for(int i = (N/2); i < N; i++)
                    for(int j = 0; j < N; j++){
                        recibeA[i-(N/2)][j] = entrada.readDouble();
                    }
                for(int i = 0; i < (N/2); i++)
                    for(int j = 0; j < N; j++){
                        recibeB[i][j] = entrada.readDouble();
                    }
                for(int i = 0; i < (N/2); i++)
                    for(int j = 0; j < (N/2); j++)
                        for(int k = 0; k < N; k++)
                            productoC[i][j] += recibeA[i][k] * recibeB[j][k];
                for(int i = 0; i < (N/2); i++)
                    for(int j = 0; j < (N/2); j++){
                        salida.writeDouble(productoC[i][j]);
                    }
            }
            else if (nodo == 4){
                for(int i = (N/2); i < N; i++)
                    for(int j = 0; j < N; j++){
                        recibeA[i-(N/2)][j] = entrada.readDouble();
                        recibeB[i-(N/2)][j] = entrada.readDouble();
                    }
                for(int i = 0; i < (N/2); i++)
                    for(int j = 0; j < (N/2); j++)
                        for (int k = 0; k < N; k++)
                            productoC[i][j] += recibeA[i][k] * recibeB[j][k];
                for(int i = 0; i < (N/2); i++)
                    for(int j = 0; j < (N/2); j++){
                        salida.writeDouble(productoC[i][j]);
                    }
            }

            entrada.close();
            salida.close();
            conexion.close();
        }
    }
}