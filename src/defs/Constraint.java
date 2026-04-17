package defs;

public record Constraint(String name, Formula formula) {
    @Override
    public String toString() {
        return "Constraint " + name + ": " + formula;
    }
}
