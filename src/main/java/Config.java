import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class Config {

    // THESE ARE DEFAULTS TO BE OVERRIDEN BY SETUP FUNCTION
    public static String HOST = "localhost";
    public static int PORT = 6379;
    public static Charset CHARSET = StandardCharsets.UTF_8;
    public static int MAX_BULK_STRING_BYTES = 512 * 1000;
    public static String DIR = "/etc/redis";

    public static String DB_FILE_NAME = "writeahead.rdb";


    private Config() {
    }

    public static void setUp(String[] args) {
        if (args.length < 1) {
            return;
        }
        System.out.println("ARGS:");
        for (int i = 0; i < args.length; i++) {
            String v = args[i].toLowerCase();
            switch (v) {
                case "--dir":
                    DIR = args[i + 1];
                    break;
                case "--dbfilename":
                    DB_FILE_NAME = args[i + 1];
                    break;
                case "--port":
                    PORT = Integer.parseInt(args[i + 1]);
                    break;
                case "--host":
                    HOST = args[i + 1];
                    break;
            }
        }

    }

    public static ArrayList<String> get(String key) {
        switch (key) {
            case "dir":
                ArrayList<String> l = new ArrayList<>();
                l.add("dir");
                l.add(DIR);
                return l;
            break;
            case "dbfilename":
                ArrayList<String> l = new ArrayList<>();
                l.add("dbfilename");
                l.add(DB_FILE_NAME);
                return l;
            break;
            default:
                return null;
        }
    }

}
