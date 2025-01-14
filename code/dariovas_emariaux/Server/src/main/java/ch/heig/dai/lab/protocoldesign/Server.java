package ch.heig.dai.lab.protocoldesign;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;

public class Server {
    final int SERVER_PORT = 42069;

    /**
     * Stores all possible operations.
     */
    private enum Operation  {
        ADD("+"),
        SUB("-"),
        DIV("/"),
        MULT("*"),
        POW("^");

        public final String label;
        Operation(String label) {
            this.label = label;
        }
    }

    public static void main(String[] args) {
        // Create a new server and run it
        Server server = new Server();
        server.run();
    }

    /**
     * Runs the Server implementation.
     */
    private void run() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server up and running on port " + SERVER_PORT);
            try (Socket socket = serverSocket.accept();
                     var in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), UTF_8));
                     var out = new BufferedWriter(new OutputStreamWriter(
                             socket.getOutputStream(), UTF_8))) {

                // Send WELCOME message (with the possible operators) to the client on new connection
                out.write("WELCOME 12+12| " + getOperations() + "\n");
                out.flush();

                while (true) {
                    // Reads the input from the client
                    String msg = in.readLine();
                    String[] msgParts = msg.split("\\|");

                    if (msgParts[0].equals("CALCULATION")) {
                        try {
                            out.write("RESULT|" + calculate(msgParts[1]) + "\n");
                        } catch (RuntimeException e) {
                            out.write("ERROR|" + e.getMessage() + "\n");
                        }
                    } else if (msgParts[0].equals("END")) {
                        out.flush();
                        socket.close();
                        break;
                    } else {
                        out.write("Please enter an instruction.\n");
                    }
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("Server: socket ex. : " + e);
            }
        } catch (IOException e) {
            System.out.println("Server: socket ex. : " + e);
        }
    }

    /**
     * Gets the operators
     * @return operators
     */
    private String getOperations(){
        StringBuilder operation = new StringBuilder();

        for (Operation op : Operation.values()){
            operation.append(op.label);
        }

        return operation.toString();
    }

    /**
     * Converts all numbers entered.
     * @param tab number in the string format.
     * @return number in Integer.
     * @throws RuntimeException if the number can not be converted.
     */
    private Integer[] getNumbersFromString(String[] tab) throws RuntimeException{
        Integer[] numbers = new Integer[tab.length];
        for(int i = 0;i < tab.length;i++){
            try {
                numbers[i] = Integer.parseInt(tab[i]);
            }
            catch (RuntimeException e) {
                throw new RuntimeException("NUMBER_NOT_VALID");
            }
        }
        return numbers;
    }

    /**
     * Calculates the operation sent by the client.
     * @param str calculation sent.
     * @return result.
     */
    private int calculate(String str){
        // Gets numbers from the string by removing everything except the numbers.
        Integer[] numbers = getNumbersFromString(str.split("[^0-9\\\\.]"));
        // Gets operators from the string by removing all numbers
        String[] operators = str.split("[0-9]");
        // Removes the empty values
        operators = Arrays.stream(operators).filter(o -> !o.isEmpty()).toArray(String[]::new);

        int n1 = numbers[0];
        int n2 = numbers[1];
        String op = operators[0];

        if(op.equals(Operation.ADD.label)){
            return n1 + n2;
        } else if (op.equals(Operation.SUB.label)) {
            return n1 - n2;
        }
        else if (op.equals(Operation.DIV.label)){
            if(n2 != 0)
                return n1 / n2;
            throw new RuntimeException("CANNOT_DIVIDE_BY_0");
        }
        else if (op.equals(Operation.MULT.label)){
            return n1 * n2;
        } else if (op.equals(Operation.POW.label)) {
            return (int) Math.pow(n1, n2);
        }else {
            throw new RuntimeException("OPERATION_NOT_VALID");
        }
    }
}