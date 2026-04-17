package defs;

import java.util.List;

public sealed interface Formula permits Formula.Forall, Formula.Exists, Formula.And, Formula.Or, Formula.Implies, Formula.Not, Formula.Bfunc {
    record Forall(String var, String inSet, Formula body) implements Formula {
        @Override
        public String toString() {
            return "forall " + var + " in " + inSet + ", " + body;
        }
    }

    record Exists(String var, String inSet, Formula body) implements Formula {
        @Override
        public String toString() {
            return "exists " + var + " in " + inSet + ", " + body;
        }
    }

    record And(Formula lhs, Formula rhs) implements Formula {
        @Override
        public String toString() {
            return "(" + lhs + ") and (" + rhs + ")";
        }
    }

    record Or(Formula lhs, Formula rhs) implements Formula {
        @Override
        public String toString() {
            return "(" + lhs + ") or (" + rhs + ")";
        }
    }

    record Implies(Formula lhs, Formula rhs) implements Formula {
        @Override
        public String toString() {
            return "(" + lhs + ") implies (" + rhs + ")";
        }
    }

    record Not(Formula operand) implements Formula {
        @Override
        public String toString() {
            return "not (" + operand + ")";
        }
    }

    record Bfunc(String name, List<String> args) implements Formula {
        @Override
        public String toString() {
            return name + "(" + String.join(", ", args) + ")";
        }
    }
}
