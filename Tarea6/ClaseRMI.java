import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class ClaseRMI extends UnicastRemoteObject implements InterfaceRMI {


  int N = 8;
  // int N = 1000;

  public ClaseRMI() throws RemoteException {
    super();
  }

  public float[][] multiplica_matrices(float[][] A, float[][] B) throws RemoteException {
    float[][] C = new float[N / 2][N / 2];
    for (int i = 0; i < N / 2; i++)
        for (int j = 0; j < N / 2; j++)
            for (int k = 0; k < N; k++)
                C[i][j] += A[i][k] * B[j][k];
    return C;
  }
}
