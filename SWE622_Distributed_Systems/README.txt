Name - Janasri Rega
Email - jrega2@gmu.edu
Student id - G01351956
-------------------------------------------------------------


Instructions for running and testing Client and Server Setup :
--------------------------------------------------------------

-> SERVER : java -cp project.jar Server start <portnumber>

-> CLIENT : export PA1_SERVER=localhost:<portnumber>

-> port_number can be any number.

-> Command for uploading the file :  java -cp project.jar Client upload <Client_path> <Server_path>

-> Command for downloding the file : java -cp project.jar Client download <server_path> <Client_path>

-> Below are the additional supported Commands :

java -cp project.jar Client dir <server_path>
java -cp project.jar Client mkdir <server_path>
java -cp project.jar Client rmdir <server_path>
java -cp project.jar Client rm <server_path>
java -cp project.jar Client shutdown

---------------------------------------------------------------

Open two windows, one for the server and one for the client.
Run the Server application first.
Then, in a different terminal, launch the Client application.
Now run the required command as mentioned above.