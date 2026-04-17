package check;

import collection.LinksModel.Assignment;
import collection.LinksModel.AssignmentLinkedListNode;
import collection.LinksModel.Link;
import collection.LinksModel.Links;
import defs.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Cct {
    public CctNode root;
    public final SyntaxTree syntaxTree;

    public Cct(SyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public List<String> link() {
        List<String> out = new ArrayList<>();
        if (root == null || root.links.links.isEmpty()) {
            return out;
        }
        for (Link link : root.links.links) {
            StringJoiner joiner = new StringJoiner(",");
            for (Assignment assignment : link.bindings) {
                joiner.add("(" + syntaxTree.dfn2VarName.get(assignment.varId) + ", " + assignment.ctx.id + ")");
            }
            out.add(syntaxTree.name + "(" + link.linkType + ",{" + joiner + "})");
        }
        return out;
    }

    public abstract static class CctNode {
        public boolean truthValue = false;
        public Links links = new Links(new ArrayList<>());
        public final SyntaxTree.SyntaxTreeNode syntaxTreeNode;
        public final AssignmentLinkedListNode assignments;

        protected CctNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            this.syntaxTreeNode = syntaxTreeNode;
            this.assignments = assignments;
        }
    }

    public abstract static class QuantifierNode extends CctNode {
        public final Map<Integer, CctNode> ctx2child = new HashMap<>();

        protected QuantifierNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public abstract static class NonleafNode extends CctNode {
        public final List<CctNode> children = new ArrayList<>();

        protected NonleafNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class ForallNode extends QuantifierNode {
        public ForallNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class ExistsNode extends QuantifierNode {
        public ExistsNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class AndNode extends NonleafNode {
        public AndNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class OrNode extends NonleafNode {
        public OrNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class ImpliesNode extends NonleafNode {
        public ImpliesNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class NotNode extends NonleafNode {
        public NotNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static final class BfuncNode extends CctNode {
        public BfuncNode(SyntaxTree.SyntaxTreeNode syntaxTreeNode, AssignmentLinkedListNode assignments) {
            super(syntaxTreeNode, assignments);
        }
    }

    public static class Builder {
        private final Ecc.EccListener listener = new Ecc.EccListener();
        private final CctChecker checker;

        public Builder(CctChecker checker) {
            this.checker = checker;
        }

        public CctNode build(SyntaxTree.SyntaxTreeNode refNode) {
            return buildHelper(refNode, null, null);
        }

        public CctNode build(SyntaxTree.SyntaxTreeNode refNode, CctNode parent, Context ctx) {
            return buildHelper(refNode, parent, new Assignment(parent.syntaxTreeNode.dfn, ctx));
        }

        private AssignmentLinkedListNode makeAssignment(AssignmentLinkedListNode existing, Assignment newAssignment) {
            if (newAssignment == null) {
                return existing;
            }
            return new AssignmentLinkedListNode(existing, newAssignment);
        }

        private CctNode buildHelper(SyntaxTree.SyntaxTreeNode refNode, CctNode parent, Assignment assignment) {
            AssignmentLinkedListNode assignments = makeAssignment(parent == null ? null : parent.assignments, assignment);
            return switch (refNode.nodeType) {
                case FORALL, EXISTS -> {
                    Cct.QuantifierNode node = quantifierHelper((SyntaxTree.QuantifierSyntaxTreeNode) refNode, assignments);
                    if (node instanceof Cct.ForallNode f) {
                        listener.exitForall(f);
                    } else {
                        listener.exitExists((Cct.ExistsNode) node);
                    }
                    yield node;
                }
                case AND, OR, IMPLIES, NOT -> {
                    Cct.NonleafNode node = nonleafHelper(refNode, assignments);
                    if (node instanceof Cct.AndNode n) {
                        listener.exitAnd(n);
                    } else if (node instanceof Cct.OrNode n) {
                        listener.exitOr(n);
                    } else if (node instanceof Cct.ImpliesNode n) {
                        listener.exitImplies(n);
                    } else {
                        listener.exitNot((Cct.NotNode) node);
                    }
                    yield node;
                }
                case BFUNC -> {
                    Cct.BfuncNode node = new Cct.BfuncNode(refNode, assignments);
                    listener.exitBfunc(node);
                    yield node;
                }
            };
        }

        private Cct.QuantifierNode quantifierHelper(SyntaxTree.QuantifierSyntaxTreeNode refNode, AssignmentLinkedListNode assignments) {
            Cct.QuantifierNode cctNode = switch (refNode.nodeType) {
                case FORALL -> new Cct.ForallNode(refNode, assignments);
                case EXISTS -> new Cct.ExistsNode(refNode, assignments);
                default -> throw new IllegalStateException("Unexpected quantifier type: " + refNode.nodeType);
            };
            int patternNo = refNode.patternNo;
            if (patternNo != -1) {
                var contextSet = checker.contextSets.get(patternNo).contextSet;
                for (Map.Entry<Integer, ContextSet.ContextCounter> e : contextSet.entrySet()) {
                    CctNode child = buildHelper(refNode.children.get(0), cctNode, new Assignment(refNode.dfn, e.getValue().context));
                    cctNode.ctx2child.put(e.getKey(), child);
                }
            }
            return cctNode;
        }

        private Cct.NonleafNode nonleafHelper(SyntaxTree.SyntaxTreeNode refNode, AssignmentLinkedListNode assignments) {
            Cct.NonleafNode cctNode = switch (refNode.nodeType) {
                case AND -> new Cct.AndNode(refNode, assignments);
                case OR -> new Cct.OrNode(refNode, assignments);
                case IMPLIES -> new Cct.ImpliesNode(refNode, assignments);
                case NOT -> new Cct.NotNode(refNode, assignments);
                default -> throw new IllegalStateException("Unexpected nonleaf type: " + refNode.nodeType);
            };
            for (SyntaxTree.SyntaxTreeNode child : refNode.children) {
                cctNode.children.add(buildHelper(child, cctNode, null));
            }
            return cctNode;
        }
    }
}
