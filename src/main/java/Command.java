import java.util.ArrayList;

public class Command {
    String name;
    ArrayList<Object> args;

    public Command(String name, ArrayList<Object> args) {
        // TODO -> check if command is valid by name
        this.name = name;

        // TODO -> validate that args are good?
        this.args = args;
    }

}
