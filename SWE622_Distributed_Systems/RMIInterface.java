import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {

    public void mkdir(String path) throws RemoteException;
    public String[] dir(String path) throws RemoteException;
    public void rmdir(String path) throws RemoteException;
    public double upload(String path,byte[] buff, int readBuff,int inputBuff,int bytesUpload) throws IOException;
    public byte[] download(String path,int pos) throws IOException;
    public void rm(String path) throws RemoteException;
    public boolean shutdown() throws RemoteException;
    public int getRead() throws RemoteException;
    public int fileSize(String path) throws RemoteException;
    public int currentStatus(String path, int Fsize) throws RemoteException;
    public String  getExitStatus() throws RemoteException;


}
