import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class InputHandler implements Runnable{
    public final RequestTable inputRequests;

    public InputHandler() {
        inputRequests = new RequestTable();
    }

    public void run() {
        try {
            ElevatorInput input = new ElevatorInput(System.in);
            while (true) {
                Request request = input.nextRequest();
                if (request == null) {
                    inputRequests.setEnd();
                    break;
                }
                else {
                    if(request instanceof PersonRequest) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
