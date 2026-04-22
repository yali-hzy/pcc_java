package check;

import collection.LinksModel.Assignment;
import collection.LinksModel.Link;
import collection.LinksModel.LinkType;
import collection.LinksModel.Links;
import defs.Context;
import defs.Common;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logger.GlobalLogger;

public final class Ecc {
    public static final class EccListener extends Listener.CheckListener<Listener.Evaluator, Listener.Generator> {
        public EccListener() {
            super(new EccEvaluator(), new EccGenerator());
        }
    }

    public static class EccMethod implements CheckMethod {
        private final Cct.Builder cctBuilder;

        public EccMethod(CctChecker checker) {
            this.cctBuilder = new Cct.Builder(checker);
        }

        private Cct.CctNode handleChange(SyntaxTree.SyntaxTreeNode refRoot) {
            return cctBuilder.build(refRoot);
        }

        @Override
        public void handleAdd(Cct cct, List<Integer> nodeList, int contextId, Context context) {
            Instant start = Instant.now();
            cct.root = handleChange(cct.syntaxTree.root);
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
            handleAdd(cct, nodeList, contextId, null);
        }
    }

    public static class EccEvaluator implements Listener.Evaluator {
        @Override
        public void exitForall(Cct.ForallNode node) {
            node.truthValue = node.ctx2child.values().stream().allMatch(n -> n.truthValue);
        }

        @Override
        public void exitExists(Cct.ExistsNode node) {
            node.truthValue = node.ctx2child.values().stream().anyMatch(n -> n.truthValue);
        }

        @Override
        public void exitAnd(Cct.AndNode node) {
            node.truthValue = node.children.get(0).truthValue && node.children.get(1).truthValue;
        }

        @Override
        public void exitOr(Cct.OrNode node) {
            node.truthValue = node.children.get(0).truthValue || node.children.get(1).truthValue;
        }

        @Override
        public void exitImplies(Cct.ImpliesNode node) {
            node.truthValue = !node.children.get(0).truthValue || node.children.get(1).truthValue;
        }

        @Override
        public void exitNot(Cct.NotNode node) {
            node.truthValue = !node.children.get(0).truthValue;
        }

        @Override
        public void exitBfunc(Cct.BfuncNode node) {
            Map<Integer, Context> map = node.assignments == null ? new HashMap<>()
                    : node.assignments.collectMap(node.syntaxTreeNode.depth);
            SyntaxTree.BfuncSyntaxTreeNode ref = (SyntaxTree.BfuncSyntaxTreeNode) node.syntaxTreeNode;
            Context[] args = new Context[ref.bfunc.argsId.size()];
            for (int i = 0; i < ref.bfunc.argsId.size(); i++) {
                args[i] = map.get(ref.bfunc.argsId.get(i));
            }
            node.truthValue = BfuncRuntime.apply(ref.bfunc.funcName, args);
        }
    }

    public static class EccGenerator implements Listener.Generator {
        @Override
        public void exitForall(Cct.ForallNode node) {
            List<Link> result = new ArrayList<>();
            for (Cct.CctNode child : node.ctx2child.values()) {
                if (!child.truthValue && child.assignments != null) {
                    List<Assignment> one = new ArrayList<>();
                    one.add(child.assignments.assignment);
                    result.addAll(new Links(List.of(new Link(LinkType.VIOLATED, one))).product(child.links).links);
                }
            }
            node.links = new Links(result);
        }

        @Override
        public void exitExists(Cct.ExistsNode node) {
            List<Link> result = new ArrayList<>();
            for (Cct.CctNode child : node.ctx2child.values()) {
                if (child.truthValue && child.assignments != null) {
                    List<Assignment> one = new ArrayList<>();
                    one.add(child.assignments.assignment);
                    result.addAll(new Links(List.of(new Link(LinkType.SATISFIED, one))).product(child.links).links);
                }
            }
            node.links = new Links(result);
        }

        @Override
        public void exitAnd(Cct.AndNode node) {
            boolean l = node.children.get(0).truthValue;
            boolean r = node.children.get(1).truthValue;
            if (l && r) {
                node.links = node.children.get(0).links.product(node.children.get(1).links);
            } else if (l) {
                node.links = node.children.get(1).links;
            } else if (r) {
                node.links = node.children.get(0).links;
            } else {
                node.links = node.children.get(0).links.union(node.children.get(1).links);
            }
        }

        @Override
        public void exitOr(Cct.OrNode node) {
            boolean l = node.children.get(0).truthValue;
            boolean r = node.children.get(1).truthValue;
            if (l && r) {
                node.links = node.children.get(0).links.union(node.children.get(1).links);
            } else if (l) {
                node.links = node.children.get(0).links;
            } else if (r) {
                node.links = node.children.get(1).links;
            } else {
                node.links = node.children.get(0).links.product(node.children.get(1).links);
            }
        }

        @Override
        public void exitImplies(Cct.ImpliesNode node) {
            boolean l = node.children.get(0).truthValue;
            boolean r = node.children.get(1).truthValue;
            if (l && r) {
                node.links = node.children.get(1).links;
            } else if (l) {
                node.links = node.children.get(0).links.flipset().product(node.children.get(1).links);
            } else if (r) {
                node.links = node.children.get(0).links.flipset().union(node.children.get(1).links);
            } else {
                node.links = node.children.get(0).links.flipset();
            }
        }

        @Override
        public void exitNot(Cct.NotNode node) {
            node.links = node.children.get(0).links.flipset();
        }

        @Override
        public void exitBfunc(Cct.BfuncNode node) {
            node.links = new Links(new ArrayList<>());
        }
    }

    private Ecc() {
    }
}
