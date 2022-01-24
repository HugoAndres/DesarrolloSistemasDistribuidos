import java.rmi.Naming;

public class ClienteRMI {

    private static final int N = 8;
    //     private static final int N = 1000;

    static void imprime_matriz(float matriz[][], int renglon, int columna) {
        for (int i = 0; i < renglon; i++) {
            for (int j = 0; j < columna; j++) {
                System.out.printf("%8.1f", matriz[i][j]);
            }
            System.out.println("");
        }
    }

    static void transpone_matriz(float matriz[][]) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
                float t = matriz[i][j];
                matriz[i][j] = matriz[j][i];
                matriz[j][i] = t;
            }
        }
    }

    static float[][] separa_matriz(float[][] A, int inicio) {
        float[][] M = new float[N / 2][N];
        for (int i = 0; i < N / 2; i++)
            for (int j = 0; j < N; j++)
                M[i][j] = A[i + inicio][j];
        return M;
    }

    static void acomoda_matriz(float[][] C, float[][] A, int renglon, int columna) {
        for (int i = 0; i < N / 2; i++)
            for (int j = 0; j < N / 2; j++)
                C[i + renglon][j + columna] = A[i][j];
    }

    public static void main(String args[]) throws Exception {

        float[][] A = new float[N][N];
        float[][] B = new float[N][N];
        float[][] C = new float[N][N];
        double checksum = 0;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = 2 * i - j;
                B[i][j] = 2 * i + j;
                C[i][j] = 0;
            }
        }

        System.out.println("Matriz A:");
        imprime_matriz(A, N, N);
        System.out.println("Matriz B:");
        imprime_matriz(B, N, N);
        transpone_matriz(B);
        System.out.println("Matriz B [Transpuesta]:");
        imprime_matriz(B, N, N);

        float[][] A1 = separa_matriz(A, 0);
        float[][] A2 = separa_matriz(A, N / 2);
        float[][] B1 = separa_matriz(B, 0);
        float[][] B2 = separa_matriz(B, N / 2);

        String url = "rmi://localhost/prueba";
        InterfaceRMI nodo1 = (InterfaceRMI) Naming.lookup(url);
        InterfaceRMI nodo2 = (InterfaceRMI) Naming.lookup(url);

        float[][] C1 = nodo1.multiplica_matrices(A1, B1);
        float[][] C2 = nodo1.multiplica_matrices(A1, B2);
        float[][] C3 = nodo2.multiplica_matrices(A2, B1);
        float[][] C4 = nodo2.multiplica_matrices(A2, B2);

        acomoda_matriz(C, C1, 0, 0);
        acomoda_matriz(C, C2, 0, N / 2);
        acomoda_matriz(C, C3, N / 2, 0);
        acomoda_matriz(C, C4, N / 2, N / 2);

        System.out.println("Matriz C:");
        imprime_matriz(C, N, N);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                checksum += C[i][j];
            }
        }
        System.out.println("Checksum de C: " + checksum);
    }
}
