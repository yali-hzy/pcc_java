package check;

import defs.Formula;

public final class Convert {
    private Convert() {
    }

    public static Formula kernelConvert(Formula formula) {
        return switch (formula) {
            case Formula.Forall f -> new Formula.Forall(f.var(), f.inSet(), kernelConvert(f.body()));
            case Formula.Exists f -> new Formula.Not(new Formula.Forall(f.var(), f.inSet(), new Formula.Not(kernelConvert(f.body()))));
            case Formula.And f -> new Formula.And(kernelConvert(f.lhs()), kernelConvert(f.rhs()));
            case Formula.Or f -> new Formula.Not(new Formula.And(new Formula.Not(kernelConvert(f.lhs())), new Formula.Not(kernelConvert(f.rhs()))));
            case Formula.Implies f -> new Formula.Not(new Formula.And(kernelConvert(f.lhs()), new Formula.Not(kernelConvert(f.rhs()))));
            case Formula.Not f -> new Formula.Not(kernelConvert(f.operand()));
            case Formula.Bfunc f -> f;
        };
    }

    public static Formula notLeafConvert(Formula formula) {
        return switch (formula) {
            case Formula.Forall f -> new Formula.Forall(f.var(), f.inSet(), notLeafConvert(f.body()));
            case Formula.Exists f -> new Formula.Exists(f.var(), f.inSet(), notLeafConvert(f.body()));
            case Formula.And f -> new Formula.And(notLeafConvert(f.lhs()), notLeafConvert(f.rhs()));
            case Formula.Or f -> new Formula.Or(notLeafConvert(f.lhs()), notLeafConvert(f.rhs()));
            case Formula.Implies f -> new Formula.Implies(notLeafConvert(f.lhs()), notLeafConvert(f.rhs()));
            case Formula.Bfunc f -> f;
            case Formula.Not n -> switch (n.operand()) {
                case Formula.Forall f -> new Formula.Exists(f.var(), f.inSet(), notLeafConvert(new Formula.Not(f.body())));
                case Formula.Exists f -> new Formula.Forall(f.var(), f.inSet(), notLeafConvert(new Formula.Not(f.body())));
                case Formula.And f -> new Formula.Or(notLeafConvert(new Formula.Not(f.lhs())), notLeafConvert(new Formula.Not(f.rhs())));
                case Formula.Or f -> new Formula.And(notLeafConvert(new Formula.Not(f.lhs())), notLeafConvert(new Formula.Not(f.rhs())));
                case Formula.Implies f -> new Formula.And(notLeafConvert(f.lhs()), notLeafConvert(new Formula.Not(f.rhs())));
                case Formula.Not f -> notLeafConvert(f.operand());
                case Formula.Bfunc f -> new Formula.Not(f);
            };
        };
    }
}
