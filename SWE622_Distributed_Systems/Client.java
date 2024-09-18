import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;

public class Client {

    private RMIInterface rmiInterface;
    private String server = "";
    private boolean resume = false;

    public Client(String server) {
        this.server = server;
    }
    private void establishConnection() {
        String url = "rmi://"+ server+"/Server";
        try {
            rmiInterface = (RMIInterface) Naming.lookup(url);
        }catch (Exception e){
            System.out.println("Exception occurred during connection establishment: "+e.getMessage());
        }
    }

    private void executeCommand(String[] args) throws IOException {

        String action = args[0].toLowerCase();
        switch (action){
            case "mkdir":
                if(args.length != 2){
                    System.out.println("Insufficient parameters passed for creating directory");
                    return;
                }
                rmiInterface.mkdir(args[1]);
                break;
            case "dir":
                if(args.length != 2){
                    System.out.println("Insufficient parameters passed for listing directory");
                    return;
                }
                lsDir(args[1]);
                break;
            case "rmdir":
                if(args.length != 2){
                    System.out.println("Insufficient parameters passed for removing directory");
                    return;
                }
                rmiInterface.rmdir(args[1]);
                break;
            case "upload":
                if(args.length != 3){
                    System.out.println("Insufficient parameters passed for uploading file to directory");
                    return;
                }
                upload(args[1], args[2]);
                break;
            case "download":
                if(args.length != 3){
                    System.out.println("Insufficient parameters passed for downloading file from directory");
                    return;
                }
                download(args[1], args[2]);
                break;
            case "rm":
                if(args.length != 2){
                    System.out.println("Insufficient parameters passed for removing file");
                    return;
                }
                rmiInterface.rm(args[1]);
                break;
            case "shutdown":
                shutdown();
                break;
            default:
                System.out.println("Invalid entry. Accepted options: mkdir, dir, rmdir, upload, download, rm, shutdown");
        }
        try {
            System.out.println(rmiInterface.getExitStatus());
        }catch (UnmarshalException e){
            System.out.println("Sever shutdown");
        }
    }

    private void shutdown() {
        try {
            boolean status = rmiInterface.shutdown();
        }catch (Exception e){
            System.out.println("Exception occurred dring shutdown: "+e.getMessage());
        }
    }

    private void download(String serverPath, String clientPath) throws IOException {

        FileOutputStream fileOutputStream;

        int size = rmiInterface.fileSize(serverPath);

        if(size == -1){
            System.out.println("file does not exist");
            System.exit(0);
        }
        if(size == -2){
            System.out.println("Given Path is a directory");
            System.exit(0);
        }

        byte[] buff = new byte[10*1024];
        File file = new File(clientPath);

        if(!file.exists()){
            File dir = new File(file.getParent());
            if(!dir.exists()) dir.mkdir();
        }

        int bytesUpload = 0;

        if(file.exists()) bytesUpload = (int)file.length();

        if(bytesUpload == size){
            System.out.println("file already downloaded");
            System.exit(0);
        }

        if(bytesUpload < size && file.length() != 0){
            fileOutputStream = new FileOutputStream(file, true);
            resume = true;
        }else{
            fileOutputStream = new FileOutputStream(file);
        }

        try {
            int readBuff = 0;
            int pending = size - bytesUpload;

            while (pending > 0) {
                buff = rmiInterface.download(serverPath, bytesUpload);
                if(buff == null){
                    System.exit(0);
                }
                readBuff = rmiInterface.getRead();

                bytesUpload = bytesUpload + readBuff;
                pending = pending - readBuff;
                System.out.print("\r download in progress: " + ((double) bytesUpload / size) * 100);
                fileOutputStream.write(buff, 0, readBuff);
            }
        }catch (Exception e){
            System.out.println("Exception in download function: "+e.getMessage());
        }finally {
            fileOutputStream.flush();
            fileOutputStream.close();
        }

    }

    private void upload(String clientPath, String serverPath) throws IOException {
        int bytesUpload = 0;
        int readBuff = 0;
        byte buff[] = new byte[10*1024];

        File file = new File(clientPath);

        if(!file.exists()){
            System.out.println("File does not exist");
            System.exit(0);
        }

        if(file.isDirectory()){
            System.out.println("Given path is a directory");
            System.exit(0);
        }

        FileInputStream fileInputStream = new FileInputStream(clientPath);

        int size = (int)file.length();
        bytesUpload = rmiInterface.currentStatus(serverPath, size);

        int pending = size-bytesUpload;
        fileInputStream.skip(bytesUpload);

        try {
            while ((readBuff = fileInputStream.read(buff, 0, Math.min(buff.length, pending))) > 0) {
                double compeletion = rmiInterface.upload(serverPath, buff, readBuff, size, bytesUpload);
                bytesUpload = bytesUpload + readBuff;
                pending = pending - readBuff;
                System.out.print("\r upload in progress: " + compeletion + "%");
            }
        }catch (Exception e){
            System.out.println("Exception ocuured while uploading: "+e.getMessage());
        }
        System.out.println(" File uploaded");
        fileInputStream.close();
    }

    private void lsDir(String path) throws RemoteException {
        String[] list = rmiInterface.dir(path);
        for(String s: list){
            System.out.println(s);
        }
    }

    public static void main(String[] args) throws IOException {
        String server = System.getenv("PA1_SERVER");
        Client client = null;
        if(server == null || server.equalsIgnoreCase("")){
            System.out.println("please set the value for \"PA1_SERVER\" example: export PA1_SERVER=localhost:9090 in linux!");
        }else{
            client = new Client(server);
        }
        if(client != null){
            client.establishConnection();
            client.executeCommand(args);
        }
        else {
            System.out.println("unable to create connection");
            System.exit(-1);
        }
    }
}
