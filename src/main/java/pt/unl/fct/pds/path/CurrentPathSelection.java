package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;

public class CurrentPathSelection implements PathSelection {

    private Circuit circuit;

    public CurrentPathSelection(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public Path selectPath() {
        // TODO("Implement CurrentPathSelection.selectPath");
        return null;
    }

    @Override
    public Node selectExit() {
        // TODO("Implement CurrentPathSelection.selectExit");
        return null;
    }

    @Override
    public Node selectGuard() {
        // TODO("Implement CurrentPathSelection.selectGuard");
        return null;
    }

    @Override
    public Node selectMiddle(Node guard, Node exit) {
        // TODO("Implement CurrentPathSelection.selectMiddle");
        return null;
    }
}
