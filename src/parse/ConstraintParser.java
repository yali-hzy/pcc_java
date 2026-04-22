package parse;

import defs.Constraint;
import defs.Formula;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class ConstraintParser {
    private ConstraintParser() {
    }

    public static List<Constraint> parseConstraintsFromXml(Document xml) {
        List<Constraint> constraints = new ArrayList<>();
        var nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node constraintNode = nodes.item(i);
            if (constraintNode.getNodeType() != Node.ELEMENT_NODE
                    || !"constraint".equals(constraintNode.getNodeName())) {
                continue;
            }
            String name = constraintNode.getAttributes().getNamedItem("name").getNodeValue();
            Node formulaNode = firstElementChild(constraintNode);
            constraints.add(new Constraint(name, parseFormulaFromXml(formulaNode)));
        }
        return constraints;
    }

    public static List<Constraint> parseTaxiConstraintsFromXml(Document xml) {
        List<Constraint> constraints = new ArrayList<>();
        var nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node ruleNode = nodes.item(i);
            if (ruleNode.getNodeType() != Node.ELEMENT_NODE || !"rule".equals(ruleNode.getNodeName())) {
                continue;
            }
            String name = null;
            Formula formula = null;
            var children = ruleNode.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                if ("id".equals(child.getNodeName())) {
                    name = child.getTextContent().trim();
                } else if ("formula".equals(child.getNodeName())) {
                    formula = parseFormulaFromXml(firstElementChild(child));
                } else {
                    throw new IllegalArgumentException("Unknown constraint field: " + child.getNodeName());
                }
            }
            constraints.add(new Constraint(name, formula));
        }
        return constraints;
    }

    public static Formula parseFormulaFromXml(Node node) {
        String name = node.getNodeName();
        return switch (name) {
            case "forall" ->
                new Formula.Forall(attr(node, "var"), attr(node, "in"), parseFormulaFromXml(firstElementChild(node)));
            case "exists" ->
                new Formula.Exists(attr(node, "var"), attr(node, "in"), parseFormulaFromXml(firstElementChild(node)));
            case "and" -> {
                List<Node> children = elementChildren(node);
                yield new Formula.And(parseFormulaFromXml(children.get(0)), parseFormulaFromXml(children.get(1)));
            }
            case "or" -> {
                List<Node> children = elementChildren(node);
                yield new Formula.Or(parseFormulaFromXml(children.get(0)), parseFormulaFromXml(children.get(1)));
            }
            case "implies" -> {
                List<Node> children = elementChildren(node);
                yield new Formula.Implies(parseFormulaFromXml(children.get(0)), parseFormulaFromXml(children.get(1)));
            }
            case "not" -> new Formula.Not(parseFormulaFromXml(firstElementChild(node)));
            case "bfunc" -> {
                String bfuncName = attr(node, "name");
                List<String> args = new ArrayList<>();
                for (Node child : elementChildren(node)) {
                    args.add(attr(child, "var"));
                }
                yield new Formula.Bfunc(bfuncName, args);
            }
            default -> throw new IllegalArgumentException("Unknown formula type: " + name);
        };
    }

    private static String attr(Node node, String attr) {
        return node.getAttributes().getNamedItem(attr).getNodeValue();
    }

    private static Node firstElementChild(Node node) {
        for (Node child : elementChildren(node)) {
            return child;
        }
        throw new IllegalArgumentException("No child element for node: " + node.getNodeName());
    }

    private static List<Node> elementChildren(Node node) {
        List<Node> out = new ArrayList<>();
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                out.add(child);
            }
        }
        return out;
    }
}
