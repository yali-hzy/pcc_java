package collection;

import defs.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LinksModel {
    private LinksModel() {
    }

    public static final class Assignment {
        public final int varId;
        public final Context ctx;

        public Assignment(int varId, Context ctx) {
            this.varId = varId;
            this.ctx = ctx;
        }
    }

    public static final class AssignmentLinkedListNode {
        public final AssignmentLinkedListNode prev;
        public final Assignment assignment;

        public AssignmentLinkedListNode(AssignmentLinkedListNode prev, Assignment assignment) {
            this.prev = prev;
            this.assignment = assignment;
        }

        public Map<Integer, Context> collectMap(int capacityHint) {
            Map<Integer, Context> out = new HashMap<>(Math.max(16, capacityHint));
            for (AssignmentLinkedListNode cur = this; cur != null; cur = cur.prev) {
                out.put(cur.assignment.varId, cur.assignment.ctx);
            }
            return out;
        }
    }

    public enum LinkType {
        SATISFIED,
        VIOLATED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static final class Link {
        public final LinkType linkType;
        public final List<Assignment> bindings;

        public Link(LinkType linkType, List<Assignment> bindings) {
            this.linkType = linkType;
            this.bindings = bindings;
        }

        public Link flip() {
            return new Link(linkType == LinkType.SATISFIED ? LinkType.VIOLATED : LinkType.SATISFIED, bindings);
        }

        public Link cartesian(Link other) {
            List<Assignment> combined = new ArrayList<>(bindings);
            combined.addAll(other.bindings);
            return new Link(linkType, combined);
        }
    }

    public static final class Links {
        public final List<Link> links;

        public Links(List<Link> links) {
            this.links = links;
        }

        public Links flipset() {
            List<Link> flipped = new ArrayList<>(links.size());
            for (Link link : links) {
                flipped.add(link.flip());
            }
            return new Links(flipped);
        }

        public Links product(Links other) {
            if (links.isEmpty()) {
                return other;
            }
            if (other.links.isEmpty()) {
                return this;
            }
            List<Link> out = new ArrayList<>();
            for (Link l1 : links) {
                for (Link l2 : other.links) {
                    out.add(l1.cartesian(l2));
                }
            }
            return new Links(out);
        }

        public Links union(Links other) {
            List<Link> out = new ArrayList<>(links);
            out.addAll(other.links);
            return new Links(out);
        }
    }
}
