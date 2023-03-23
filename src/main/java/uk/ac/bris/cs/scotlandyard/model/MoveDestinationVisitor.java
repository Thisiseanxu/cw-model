package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableSet;


/**
 * A destination visitor for use with the {@link Move#accept(Move.Visitor)} method.
 */
public class MoveDestinationVisitor implements Move.Visitor<ImmutableSet<Integer>> {
    /**
     * @param move the single move
     * @return A set of one Integer shows the destination of that single move
     */
    @Override
    public ImmutableSet<Integer> visit(Move.SingleMove move) {
        return ImmutableSet.of(1);
    }

    /**
     * @param move the double move
     * @return A set of two Integer shows the first and second destination of that double move
     */
    @Override
    public ImmutableSet<Integer> visit(Move.DoubleMove move) {
        return ImmutableSet.of(1, 2);
    }
}
