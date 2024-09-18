import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements RMIInterface {

    private static String name = "";
    private int returnCode = 0;
    private String returnMessage = "";
    private int read=0;


    public Server() throws RemoteException {
        super();
    }


    public static void main(String[] args) throws RemoteException {

//        int port = 8080;
        int port = 0;

        if(args.length != 2){
            System.out.println("2 arguments required to start the server: \"start\" and port-number(ex: 8090)");
        }

        else if(args[0].equalsIgnoreCase("start")){
            port = Integer.parseInt(args[1]);
        }
        name = "rmi://localhost:"+port+"/Server";
        try {
            LocateRegistry.createRegistry(port);
//            Naming.rebind("rmi://localhost:8080/Server", new Server());
            Naming.rebind(name, new Server());
            System.out.println("Server is up.......");
        } catch (MalformedURLException e) {
            System.out.println("Exception occurred: "+e.toString());
            e.printStackTrace();
        }

    }

    @Override
    public void mkdir(String path) throws RemoteException {

        File file = new File(path);
        if(file.exists()){
            System.out.println("directory already exists");
            returnCode = -1;
            returnMessage = "Directory already exists";
        }else if (!file.mkdir()){
            returnCode = -1;
            returnMessage = "Directory not created";
        }else{
            returnCode = 0;
            returnMessage = "Directory created";
        }

    }

    @Override
    public String[] dir(String path) throws RemoteException {
        File file = new File(path);
        String[] list = null;
        if(file.exists()){
            if(file.isDirectory()){
                list = file.list();
            }else {
                returnCode = -1;
                returnMessage = "Given path is not a directory";
            }
        }else{
            returnCode = -1;
            returnMessage = "Directory doesn't exist";
        }
        returnCode = 0;
        returnMessage = "listed directory";
        return list;
    }

    @Override
    public void rmdir(String path) throws RemoteException {
        File file = new File(path);
        if(file.exists()){
            if(file.isDirectory()){
                String[] files = file.list();
                if(files.length == 0){
                    if(file.delete()){
                        returnCode = 0;
                        returnMessage = "directory deleted";
                        return;
                    }else{
                        returnCode = -1;
                        returnMessage = "directory not deleted";
                        return;
                    }
                }else{
                    returnCode = 1;
                    returnMessage = "Directory not empty";
                    return;
                }
            }else {
                returnCode = -1;
                returnMessage = "Given Path is not a directory";
                return;
            }
        }
        returnCode = 0;
        returnMessage = "directory deleted";
    }

    @Override
    public double upload(String path, byte[] buff, int readBuff, int inputBuff, int bytesUpload) throws IOException {
        File file = new File(path);
        long bytes = new File(path).length();

        FileOutputStream fileOutputStream = bytes == 0 ? new FileOutputStream(path, false) : new FileOutputStream(path, true) ;

        double completion = 0.0;

        int pending = inputBuff - bytesUpload;

        try{
            if(pending > 0){
                pending = pending - readBuff;
                bytesUpload = bytesUpload + readBuff;
                completion = (double)(bytesUpload)/inputBuff * 100;
                fileOutputStream.write(buff, 0, readBuff);
            }
        }catch (IOException e){
            returnCode = -1;
            returnMessage = "Upload failed, please retry";
        }finally {
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        returnCode = 0;
        returnMessage = "File uploaded";
        return completion;
    }

    @Override
    public byte[] download(String path, int pos) throws IOException {

        File file = new File(path);

        int size=(int) file.length(), position = pos;
        int remaining = size-position;

        if(file.exists()){
            if(file.isDirectory()){
                returnCode = -1;
                returnMessage = "Given path is a directory";
            }else{
                FileInputStream fileInputStream = new FileInputStream(path);
                fileInputStream.skip(pos);
                byte[] buff = new byte[10*1024];
                if((read = fileInputStream.read(buff, 0, Math.min(buff.length, remaining))) > 0){
                    fileInputStream.close();
                    returnCode = 0;
                    returnMessage = "File Downloaded";
                    return buff;
                }else if((read = fileInputStream.read(buff, 0, Math.min(buff.length, remaining))) ==0){
                    returnCode = 0;
                    returnMessage = "file already downloaded";
                    return null;
                }
                fileInputStream.close();
            }
        }else {
            returnCode = -1;
            returnMessage = "File doesn't exits";
            return null;
        }
        return null;
    }

    @Override
    public void rm(String path) throws RemoteException {
        File file = new File(path);
        if(file.exists()){
            if(file.isDirectory()){
                returnCode = -1;
                returnMessage = "Given path is a directory";
            }else{
                System.out.println("Deleting the file");
                returnCode = 0;
                returnMessage = "file deleted";
                file.delete();
            }
        }else{
            returnCode = -1;
            returnMessage = "File doesn't exist";
        }
    }

    @Override
    public boolean shutdown() throws RemoteException {

        try{
            Naming.unbind(Server.name);
        }catch (Exception e){
            System.out.println("Exception occurred during shutdown: "+e.getMessage());
            return false;
        }
        UnicastRemoteObject.unexportObject(this, true);
        System.out.println("Server shut down....");
        return true;
    }

    @Override
    public int getRead() throws RemoteException {
        return read;
    }

    @Override
    public int fileSize(String path) throws RemoteException {
        File file = new File(path);

        if(!file.exists()){
            return -1;
        }else if(file.isDirectory()){
            return -2;
        }
        return (int) file.length();
    }

    @Override
    public int currentStatus(String path, int fileSize) throws RemoteException {
        File file = new File(path);
        int filePos=(int)file.length();
        return filePos;
    }

    @Override
    public String getExitStatus() throws RemoteException {
        return returnMessage+". returned with status code "+returnCode;
    }
}
