package check;

import defs.Common;
import defs.Context;
import defs.ContextChangeType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import logger.GlobalLogger;

public final class Pcc {
    public interface PccEvaluatorListener extends Listener.Evaluator {
        void exitForall(Cct.ForallNode node, Cct.CctNode newNode);
        void exitExists(Cct.ExistsNode node, Cct.CctNode newNode);
    }

    public interface PccGeneratorListener extends Listener.Generator {
        void exitForall(Cct.ForallNode node, Cct.CctNode newNode);
        void exitExists(Cct.ExistsNode node, Cct.CctNode newNode);
    }

    public static final class PccListener extends Listener.CheckListener<PccEvaluatorListener, PccGeneratorListener> {
        public PccListener() {
            super(new PccEvaluator(), new PccGenerator());
        }

        public void exitForall(Cct.ForallNode node, Cct.CctNode newNode) {
            evaluator.exitForall(node, newNode);
            generator.exitForall(node, newNode);
        }

        public void exitExists(Cct.ExistsNode node, Cct.CctNode newNode) {
            evaluator.exitExists(node, newNode);
            generator.exitExists(node, newNode);
        }
    }

    public static final class PccMethod implements CheckMethod {
        private final PccAdjuster adjuster;

        public PccMethod(CctChecker checker) {
            this.adjuster = new PccAdjuster(new Cct.Builder(checker));
        }

        @Override
        public void handleAdd(Cct cct, List<Integer> nodeList, int contextId, Context context) {
            Instant start = Instant.now();
            List<Integer> sorted = new ArrayList<>(nodeList);
            sorted.sort((a, b) -> Integer.compare(cct.syntaxTree.nodes.get(b).depth, cct.syntaxTree.nodes.get(a).depth));
            for (int nodeId : sorted) {
                adjuster.handleAdd(cct, nodeId, contextId, context);
            }

            Duration elapsed = Duration.between(start, Instant.now());
            for (String link : cct.link()) {
                GlobalLogger.get().log(link);
            }
            GlobalLogger.get().addDuration(elapsed);
            long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Common.maxUsedHeapSize = Math.max(Common.maxUsedHeapSize, used);
            Common.maxAllocatedHeapSize = Math.max(Common.maxAllocatedHeapSize, Runtime.getRuntime().totalMemory());
        }

        @Override
        public void handleDelete(Cct cct, List<Integer> nodeList, int contextId) {
            Instant start = Instant.now();
            List<Integer> sorted = new ArrayList<>(nodeList);
            sorted.sort(Comparator.comparingInt(a -> cct.syntaxTree.nodes.get(a).depth));
            for (int nodeId : sorted) {
                adjuster.handleDelete(cct, nodeId, contextId);
            }

            Duration elapsed = Duration.between(start, Instant.now());
            for (String link : cct.link()) {
                GlobalLogger.get().log(link);
            }
            GlobalLogger.get().addDuration(elapsed);
            long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Common.maxUsedHeapSize = Math.max(Common.maxUsedHeapSize, used);
            Common.maxAllocatedHeapSize = Math.max(Common.maxAllocatedHeapSize, Runtime.getRuntime().totalMemory());
        }
    }

    public static final class PccAdjuster {
        private final PccListener listener = new PccListener();
        private final Cct.Builder builder;
        private boolean[] affected = new boolean[0];
        private int syntaxTreeNodeId;
        private int contextId;
        private Context context;
        private ContextChangeType changeType;

        public PccAdjuster(Cct.Builder builder) {
            this.builder = builder;
        }

        public void handleAdd(Cct cct, int syntaxTreeNodeId, int contextId, Context context) {
            if (cct.root == null) {
                cct.root = builder.build(cct.syntaxTree.root);
                return;
            }
            initAdjust(cct.syntaxTree, syntaxTreeNodeId, ContextChangeType.ADD, contextId, context);
            adjustHelper(cct.root);
        }

        public void handleDelete(Cct cct, int syntaxTreeNodeId, int contextId) {
            initAdjust(cct.syntaxTree, syntaxTreeNodeId, ContextChangeType.DELETE, contextId, null);
            adjustHelper(cct.root);
        }

        private void initAdjust(SyntaxTree syntaxTree, int nodeId, ContextChangeType changeType, int contextId, Context context) {
            this.syntaxTreeNodeId = nodeId;
            this.contextId = contextId;
            this.context = context;
            this.changeType = changeType;
            affected = new boolean[syntaxTree.nodes.size()];
            for (SyntaxTree.SyntaxTreeNode cur = syntaxTree.nodes.get(nodeId); cur != null; cur = cur.parent) {
                affected[cur.dfn] = true;
            }
        }

        private void adjustHelper(Cct.CctNode node) {
            if (!affected[node.syntaxTreeNode.dfn]) {
                return;
            }
            if (syntaxTreeNodeId == node.syntaxTreeNode.dfn) {
                Cct.QuantifierNode q = (Cct.QuantifierNode) node;
                if (changeType == ContextChangeType.ADD) {
                    Cct.CctNode newNode = builder.build(node.syntaxTreeNode.children.get(0), node, context);
                    if (q instanceof Cct.ForallNode f) {
                        listener.exitForall(f, newNode);
                    } else {
                        listener.exitExists((Cct.ExistsNode) q, newNode);
                    }
                    q.ctx2child.put(contextId, newNode);
                } else {
                    q.ctx2child.remove(contextId);
                    if (q instanceof Cct.ForallNode f) {
                        listener.exitForall(f);
                    } else {
                        listener.exitExists((Cct.ExistsNode) q);
                    }
                }
                return;
            }

            if (node instanceof Cct.QuantifierNode q) {
                for (Cct.CctNode child : q.ctx2child.values()) {
                    adjustHelper(child);
                }
                if (q instanceof Cct.ForallNode f) {
                    listener.exitForall(f);
                } else {
                    listener.exitExists((Cct.ExistsNode) q);
                }
            } else if (node instanceof Cct.NonleafNode n) {
                for (Cct.CctNode child : n.children) {
                    adjustHelper(child);
                }
                if (n instanceof Cct.AndNode x) {
                    listener.exitAnd(x);
                } else if (n instanceof Cct.OrNode x) {
                    listener.exitOr(x);
                } else if (n instanceof Cct.ImpliesNode x) {
                    listener.exitImplies(x);
                } else {
                    listener.exitNot((Cct.NotNode) n);
                }
            }
        }
    }

    public static class PccEvaluator extends Ecc.EccEvaluator implements PccEvaluatorListener {
        @Override
        public void exitForall(Cct.ForallNode node, Cct.CctNode newNode) {
            node.truthValue = node.truthValue && newNode.truthValue;
        }

        @Override
        public void exitExists(Cct.ExistsNode node, Cct.CctNode newNode) {
            node.truthValue = node.truthValue || newNode.truthValue;
        }
    }

    public static class PccGenerator extends Ecc.EccGenerator implements PccGeneratorListener {
        @Override
        public void exitForall(Cct.ForallNode node, Cct.CctNode newNode) {
            if (!newNode.truthValue && newNode.assignments != null) {
                var one = java.util.List.of(new collection.LinksModel.Assignment(newNode.assignments.assignment.varId, newNode.assignments.assignment.ctx));
                node.links = node.links.union(new collection.LinksModel.Links(java.util.List.of(new collection.LinksModel.Link(collection.LinksModel.LinkType.VIOLATED, one))).product(newNode.links));
            }
        }

        @Override
        public void exitExists(Cct.ExistsNode node, Cct.CctNode newNode) {
            if (newNode.truthValue && newNode.assignments != null) {
                var one = java.util.List.of(new collection.LinksModel.Assignment(newNode.assignments.assignment.varId, newNode.assignments.assignment.ctx));
                node.links = node.links.union(new collection.LinksModel.Links(java.util.List.of(new collection.LinksModel.Link(collection.LinksModel.LinkType.SATISFIED, one))).product(newNode.links));
            }
        }
    }

    private Pcc() {
    }
}
