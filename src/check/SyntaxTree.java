package check;

import defs.Constraint;
import defs.Formula;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SyntaxTree {
    public final String name;
    public final SyntaxTreeNode root;
    public final List<SyntaxTreeNode> nodes;
    public final Map<Integer, String> dfn2VarName;

    public SyntaxTree(String name, SyntaxTreeNode root, List<SyntaxTreeNode> nodes, Map<Integer, String> dfn2VarName) {
        this.name = name;
        this.root = root;
        this.nodes = nodes;
        this.dfn2VarName = dfn2VarName;
    }

    public abstract static class SyntaxTreeNode {
        public final int dfn;
        public final int depth;
        public final NodeType nodeType;
        public final SyntaxTreeNode parent;
        public final List<SyntaxTreeNode> children = new ArrayList<>();

        protected SyntaxTreeNode(int dfn, int depth, NodeType nodeType, SyntaxTreeNode parent) {
            this.dfn = dfn;
            this.depth = depth;
            this.nodeType = nodeType;
            this.parent = parent;
        }
    }

    public static final class QuantifierSyntaxTreeNode extends SyntaxTreeNode {
        public final int patternNo;

        public QuantifierSyntaxTreeNode(int dfn, int depth, NodeType nodeType, SyntaxTreeNode parent, int patternNo) {
            super(dfn, depth, nodeType, parent);
            this.patternNo = patternNo;
        }
    }

    public static final class NormalSyntaxTreeNode extends SyntaxTreeNode {
        public NormalSyntaxTreeNode(int dfn, int depth, NodeType nodeType, SyntaxTreeNode parent) {
            super(dfn, depth, nodeType, parent);
        }
    }

    public static final class BfuncInfo {
        public final String funcName;
        public final List<Integer> argsId;

        public BfuncInfo(String funcName, List<Integer> argsId) {
            this.funcName = funcName;
            this.argsId = argsId;
        }
    }

    public static final class BfuncSyntaxTreeNode extends SyntaxTreeNode {
        public final BfuncInfo bfunc;

        public BfuncSyntaxTreeNode(int dfn, int depth, NodeType nodeType, SyntaxTreeNode parent, BfuncInfo bfunc) {
            super(dfn, depth, nodeType, parent);
            this.bfunc = bfunc;
        }
    }

    public static final class Builder {
        private int id = -1;
        private int dfnCounter = -1;
        private final Map<String, Integer> namedVar = new HashMap<>();
        private final Map<Integer, String> dfn2VarName = new HashMap<>();
        private final Map<Integer, SyntaxTreeNode> dfn2SyntaxNode = new HashMap<>();

        private final Map<String, Integer> patternNames2Id;
        private final Map<Integer, Map<Integer, List<Integer>>> patternMap;

        public Builder(Map<String, Integer> patternNames2Id, Map<Integer, Map<Integer, List<Integer>>> patternMap) {
            this.patternNames2Id = patternNames2Id;
            this.patternMap = patternMap;
        }

        public SyntaxTree build(int id, Constraint constraint) {
            initialize(id);
            SyntaxTreeNode root = syntaxBuild(constraint.formula(), null);
            List<SyntaxTreeNode> nodes = new ArrayList<>(dfnCounter + 1);
            for (int i = 0; i <= dfnCounter; i++) {
                nodes.add(dfn2SyntaxNode.get(i));
            }
            return new SyntaxTree(constraint.name(), root, nodes, new HashMap<>(dfn2VarName));
        }

        private void initialize(int id) {
            this.id = id;
            dfnCounter = -1;
            dfn2SyntaxNode.clear();
            namedVar.clear();
            dfn2VarName.clear();
        }

        private void insertPatternMap(int patternId, int dfn) {
            patternMap.computeIfAbsent(patternId, k -> new HashMap<>())
                    .computeIfAbsent(id, k -> new ArrayList<>())
                    .add(dfn);
        }

        private SyntaxTreeNode syntaxBuild(Formula formula, SyntaxTreeNode parent) {
            dfnCounter++;
            int currentDfn = dfnCounter;
            int depth = parent == null ? 0 : parent.depth + 1;

            SyntaxTreeNode node = switch (formula) {
                case Formula.Forall f ->
                    quantifierHelper(NodeType.FORALL, currentDfn, depth, parent, f.body(), f.var(), f.inSet());
                case Formula.Exists f ->
                    quantifierHelper(NodeType.EXISTS, currentDfn, depth, parent, f.body(), f.var(), f.inSet());
                case Formula.And f -> binaryHelper(NodeType.AND, currentDfn, depth, parent, f.lhs(), f.rhs());
                case Formula.Or f -> binaryHelper(NodeType.OR, currentDfn, depth, parent, f.lhs(), f.rhs());
                case Formula.Implies f -> binaryHelper(NodeType.IMPLIES, currentDfn, depth, parent, f.lhs(), f.rhs());
                case Formula.Not f -> unaryHelper(NodeType.NOT, currentDfn, depth, parent, f.operand());
                case Formula.Bfunc f -> {
                    List<Integer> args = new ArrayList<>();
                    for (String v : f.args()) {
                        args.add(namedVar.get(v));
                    }
                    yield new BfuncSyntaxTreeNode(currentDfn, depth, NodeType.BFUNC, parent,
                            new BfuncInfo(f.name(), args));
                }
            };
            dfn2SyntaxNode.put(currentDfn, node);
            return node;
        }

        private SyntaxTreeNode quantifierHelper(NodeType nodeType, int dfn, int depth, SyntaxTreeNode parent,
                Formula body, String varName, String setName) {
            int patternNo = patternNames2Id.getOrDefault(setName, -1);
            insertPatternMap(patternNo, dfn);
            dfn2VarName.put(dfn, varName);
            Integer old = namedVar.put(varName, dfn);
            QuantifierSyntaxTreeNode node = new QuantifierSyntaxTreeNode(dfn, depth, nodeType, parent, patternNo);
            node.children.add(syntaxBuild(body, node));
            if (old == null) {
                namedVar.remove(varName);
            } else {
                namedVar.put(varName, old);
            }
            return node;
        }

        private SyntaxTreeNode binaryHelper(NodeType nodeType, int dfn, int depth, SyntaxTreeNode parent, Formula lhs,
                Formula rhs) {
            NormalSyntaxTreeNode node = new NormalSyntaxTreeNode(dfn, depth, nodeType, parent);
            node.children.add(syntaxBuild(lhs, node));
            node.children.add(syntaxBuild(rhs, node));
            return node;
        }

        private SyntaxTreeNode unaryHelper(NodeType nodeType, int dfn, int depth, SyntaxTreeNode parent,
                Formula operand) {
            NormalSyntaxTreeNode node = new NormalSyntaxTreeNode(dfn, depth, nodeType, parent);
            node.children.add(syntaxBuild(operand, node));
            return node;
        }
    }
}
