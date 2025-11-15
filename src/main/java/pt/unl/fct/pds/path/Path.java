package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;

public class Path {

    private Node guard;
    private Node middle;
    private Node exit;

    public Path() {}

    public Path(Node guard, Node middle, Node exit) {
        this.guard = guard;
        this.middle = middle;
        this.exit = exit;
    }

    public Node getGuard() {return guard;}
    public Node getMiddle() {return middle;}
    public Node getExit() {return exit;}

    public void setGuard(Node guard) {this.guard = guard;}
    public void setMiddle(Node middle) {this.middle = middle;}
    public void setExit(Node exit) {this.exit = exit;}
}
