import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.io.IOException;

public class InputHandler implements Runnable {
    private final RequestTable inputRequests;

    public InputHandler() {
        inputRequests = new RequestTable();
    }

    public RequestTable getInputRequests() {
        return inputRequests;
    }

    public void run() {
        try {
            ElevatorInput input = new ElevatorInput(System.in);
            while (true) {
                Request request = input.nextRequest();
                if (request == null) {
                    inputRequests.setEnd();
                    break;
                } else {
                    if (request instanceof PersonRequest) {
                        PersonRequest personRequest = (PersonRequest) request;
                        Person person = new Person(
                            personRequest.getFromFloor(),
                            personRequest.getToFloor(),
                            personRequest.getPersonId(),
                            personRequest.getPriority(),
                            personRequest.getElevatorId());
                        inputRequests.AddRequest(person);
                    }
                }
            }
            input.close();
//            try{
//                throw new RuntimeException("Input is finished");
//            } catch (Exception e){
//                e.printStackTrace();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
