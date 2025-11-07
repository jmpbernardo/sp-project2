package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;

public interface PathSelection {

    Path selectPath();

    Node selectExit();

    Node selectGuard();

    Node selectMiddle(Node guard, Node exit);
}