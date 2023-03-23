package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;


/**
 * A destination visitor for use with the {@link Move#accept(Move.Visitor)} method.
 */
public class MoveDestinationVisitor implements Move.Visitor<ImmutableList<Integer>> {
    /**
     * @param move the single move
     * @return A set of one Integer shows the destination of that single move
     */
    @Override
    public ImmutableList<Integer> visit(Move.SingleMove move) {
        return ImmutableList.of(move.destination);
    }

    /**
     * @param move the double move
     * @return A set of two Integer shows the first and second destination of that double move
     */
    @Override
    public ImmutableList<Integer> visit(Move.DoubleMove move) {
        return ImmutableList.of(move.destination1, move.destination2);
    }
}
