public class ModelTest {

    String item;
    boolean completed;

    public ModelTest() {}

    public ModelTest(String item, boolean completed) {
        this.item = item;
        this.completed = completed;
    }

    public ModelTest(String item) {
        this.item = item;
        this.completed = false;
    }

}
