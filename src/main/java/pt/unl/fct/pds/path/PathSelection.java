package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;

public interface PathSelection {

    Node[] selectPath();

    Node selectExit();

    Node selectGuard(Node exit);

    Node selectMiddle(Node guard, Node exit);
}