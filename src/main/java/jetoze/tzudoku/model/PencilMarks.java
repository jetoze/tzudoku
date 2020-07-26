package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PencilMarks {

    private static final PencilMarks EMPTY = new NoPencilMarks();
    
    public static PencilMarks forGivenCell() {
        return EMPTY;
    }
    
    public static PencilMarks forUnknownCell() {
        return new EditablePencilMarks();
    }
    
    
    public final boolean isEmpty() {
        return !hasCornerMarks() && !hasCenterMarks();
    }
    
    // TODO: This API is becoming very wide and shallow. Revisit it.

    public abstract boolean hasCornerMarks();

    public abstract boolean hasCenterMarks();

    public abstract PencilMarks toggleCorner(Value value);

    public abstract PencilMarks toggleCenter(Value value);
    
    public abstract void remove(Value value);
    
    public abstract PencilMarks setCenterMarks(Set<Value> values);
    
    public abstract boolean containsCenterMark(Value value);

    public abstract void clear();

    public abstract Iterable<Value> iterateOverCornerMarks();

    public abstract Iterable<Value> iterateOverCenterMarks();

    public abstract String cornerAsString();

    public abstract String centerAsString();
    
    @Override
    public int hashCode() {
        return Objects.hash(cornerAsString(), centerAsString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PencilMarks) {
            PencilMarks that = (PencilMarks) obj;
            return this.cornerAsString().equals(that.cornerAsString()) &&
                    this.centerAsString().equals(that.centerAsString());
        }
        return false;
    }


    private static class NoPencilMarks extends PencilMarks {

        @Override
        public boolean hasCornerMarks() {
            return false;
        }

        @Override
        public boolean hasCenterMarks() {
            return false;
        }

        @Override
        public void remove(Value value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PencilMarks toggleCorner(Value value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PencilMarks toggleCenter(Value value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PencilMarks setCenterMarks(Set<Value> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsCenterMark(Value value) {
            requireNonNull(value);
            return false;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<Value> iterateOverCornerMarks() {
            return Collections.emptySet();
        }

        @Override
        public Iterable<Value> iterateOverCenterMarks() {
            return Collections.emptySet();
        }

        @Override
        public String cornerAsString() {
            return "";
        }

        @Override
        public String centerAsString() {
            return "";
        }
    }
    

    private static class EditablePencilMarks extends PencilMarks {

        private final EnumSet<Value> corner = EnumSet.noneOf(Value.class);
        private final EnumSet<Value> center = EnumSet.noneOf(Value.class);

        @Override
        public boolean hasCornerMarks() {
            return !corner.isEmpty();
        }

        @Override
        public boolean hasCenterMarks() {
            return !center.isEmpty();
        }

        @Override
        public PencilMarks toggleCorner(Value value) {
            toggle(value, corner);
            return this;
        }

        @Override
        public PencilMarks toggleCenter(Value value) {
            toggle(value, center);
            return this;
        }

        @Override
        public PencilMarks setCenterMarks(Set<Value> values) {
            center.clear();
            center.addAll(values);
            return this;
        }

        @Override
        public boolean containsCenterMark(Value value) {
            requireNonNull(value);
            return center.contains(value);
        }

        @Override
        public void remove(Value value) {
            requireNonNull(value);
            corner.remove(value);
            center.remove(value);
        }

        @Override
        public void clear() {
            corner.clear();
            center.clear();
        }

        @Override
        public String cornerAsString() {
            return asString(corner);
        }

        @Override
        public String centerAsString() {
            return asString(center);
        }

        @Override
        public Iterable<Value> iterateOverCornerMarks() {
            return corner;
        }

        @Override
        public Iterable<Value> iterateOverCenterMarks() {
            return center;
        }

        private void toggle(Value value, EnumSet<Value> set) {
            requireNonNull(value);
            if (set.contains(value)) {
                set.remove(value);
            } else {
                set.add(value);
            }
        }

        private String asString(EnumSet<Value> set) {
            return set.stream().map(Object::toString).collect(Collectors.joining());
        }

    }
}
