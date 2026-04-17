package check;

public final class Listener {
    public interface ExitListener {
        void exitForall(Cct.ForallNode node);
        void exitExists(Cct.ExistsNode node);
        void exitAnd(Cct.AndNode node);
        void exitOr(Cct.OrNode node);
        void exitImplies(Cct.ImpliesNode node);
        void exitNot(Cct.NotNode node);
        void exitBfunc(Cct.BfuncNode node);
    }

    public interface Evaluator extends ExitListener {
    }

    public interface Generator extends ExitListener {
    }

    public abstract static class CheckListener<E extends Evaluator, G extends Generator> implements ExitListener {
        protected final E evaluator;
        protected final G generator;

        protected CheckListener(E evaluator, G generator) {
            this.evaluator = evaluator;
            this.generator = generator;
        }

        @Override
        public void exitForall(Cct.ForallNode node) {
            evaluator.exitForall(node);
            generator.exitForall(node);
        }

        @Override
        public void exitExists(Cct.ExistsNode node) {
            evaluator.exitExists(node);
            generator.exitExists(node);
        }

        @Override
        public void exitAnd(Cct.AndNode node) {
            evaluator.exitAnd(node);
            generator.exitAnd(node);
        }

        @Override
        public void exitOr(Cct.OrNode node) {
            evaluator.exitOr(node);
            generator.exitOr(node);
        }

        @Override
        public void exitImplies(Cct.ImpliesNode node) {
            evaluator.exitImplies(node);
            generator.exitImplies(node);
        }

        @Override
        public void exitNot(Cct.NotNode node) {
            evaluator.exitNot(node);
            generator.exitNot(node);
        }

        @Override
        public void exitBfunc(Cct.BfuncNode node) {
            evaluator.exitBfunc(node);
            generator.exitBfunc(node);
        }
    }

    private Listener() {
    }
}
