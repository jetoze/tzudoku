package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public abstract class PencilMarks {

    private static final PencilMarks EMPTY = new NoPencilMarks();
    
    public static PencilMarks forGivenCell() {
        return EMPTY;
    }
    
    public static PencilMarks forUnknownCell() {
        return new EditablePencilMarks();
    }
    
    
    public abstract boolean isEmpty();
    
    public abstract PencilMarks setValues(Set<Value> values);
    
    public abstract ImmutableSet<Value> getValues();

    public abstract PencilMarks toggle(Value value);
    
    public abstract void remove(Value value);
    
    public abstract boolean contains(Value value);

    public abstract void clear();
    
    @Override
    public int hashCode() {
        return getValues().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PencilMarks) {
            return this.getValues().equals(((PencilMarks) obj).getValues());
        }
        return false;
    }


    private static class NoPencilMarks extends PencilMarks {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public PencilMarks setValues(Set<Value> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableSet<Value> getValues() {
            return ImmutableSet.of();
        }

        @Override
        public PencilMarks toggle(Value value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Value value) {
            return false;
        }

        @Override
        public void remove(Value value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
    

    private static class EditablePencilMarks extends PencilMarks {

        private final EnumSet<Value> values = EnumSet.noneOf(Value.class);

        @Override
        public boolean isEmpty() {
            return values.isEmpty();
        }

        @Override
        public PencilMarks setValues(Set<Value> values) {
            requireNonNull(values);
            checkArgument(values.stream().allMatch(Objects::nonNull));
            this.values.clear();
            this.values.addAll(values);
            return null;
        }

        @Override
        public ImmutableSet<Value> getValues() {
            return ImmutableSet.copyOf(values);
        }

        @Override
        public PencilMarks toggle(Value value) {
            requireNonNull(value);
            if (values.contains(value)) {
                values.remove(value);
            } else {
                values.add(value);
            }
            return this;
        }

        @Override
        public boolean contains(Value value) {
            requireNonNull(value);
            return values.contains(value);
        }

        @Override
        public void remove(Value value) {
            requireNonNull(value);
            values.remove(value);
        }

        @Override
        public void clear() {
            values.clear();
        }
    }
    
}
