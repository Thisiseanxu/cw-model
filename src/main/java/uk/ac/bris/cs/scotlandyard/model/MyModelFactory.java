package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class MyModelFactory implements Factory<Model> {

    @Nonnull
    @Override
    public Model build(GameSetup setup,
                       Player mrX,
                       ImmutableList<Player> detectives) {
        return new MyModel(setup, mrX, detectives);
    }

    private final static class MyModel implements Model {
        private final Set<Observer> observers = new HashSet<>(); // A hashset of observers that registered this model
        private GameState currentGame; // stores the current running game of this model

        private MyModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
            this.currentGame = new MyGameStateFactory().build(setup, mrX, detectives); // build a new game from setup
        }

        @Nonnull
        @Override
        public Board getCurrentBoard() {
            return currentGame;
        }

        @Override
        public void registerObserver(@Nonnull Observer observer) {
            Objects.requireNonNull(observer, "Observer should not be null!"); // if the observer is null then throw error
            if (observers.contains(observer)) // test observers set has the observer to register or not
                throw new IllegalArgumentException("Same observer can not register again!");
            else observers.add(observer); // register this observer
        }

        /**
         * @param observer the observer that need to be unregistered
         */
        @Override
        public void unregisterObserver(@Nonnull Observer observer) {
            Objects.requireNonNull(observer, "Observer should not be null!"); // if the observer is null then throw error
            if (observers.contains(observer))
                observers.remove(observer);// if the observer exists remove it form the set
            else throw new IllegalArgumentException("Observer " + observer + " doesn't exist!");
        }

        @Nonnull
        @Override
        public ImmutableSet<Observer> getObservers() {
            return ImmutableSet.copyOf(observers); // copy the observers set to an immutable set
        }

        @Override
        public void chooseMove(@Nonnull Move move) {
            this.currentGame = currentGame.advance(move); // apply the chosen move to the current game
            if (currentGame.getWinner().isEmpty()) { // test the game is end or not
                for (Observer eachObserver : observers) {
                    // if the game did not end, notify all the observer registered that a move have been made
                    eachObserver.onModelChanged(currentGame, Event.MOVE_MADE);
                }
            } else {
                for (Observer eachObserver : observers) {
                    // if the game ended, notify all the observer registered that game is over
                    eachObserver.onModelChanged(currentGame, Event.GAME_OVER);
                }
            }
        }
    }
}
