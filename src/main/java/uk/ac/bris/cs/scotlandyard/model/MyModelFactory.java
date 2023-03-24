package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event;

import java.util.*;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull
	@Override
	public Model build(GameSetup setup,
					   Player mrX,
					   ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);
	}
	// TODO 写注释！！！我都不知道怎么就过了！
	private static final class MyModel implements Model {
		private final Set<Observer> observers = new HashSet<>();
		private GameState currentGame;

		private MyModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
			this.currentGame = new MyGameStateFactory().build(setup,mrX,detectives);
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return currentGame;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			Objects.requireNonNull(observer,"Observer should not be null!");
			if (observers.contains(observer))
				throw new IllegalArgumentException("Same observer can not register again!");
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			Objects.requireNonNull(observer,"Observer should not be null!");
			if (!observers.contains(observer))
				throw new IllegalArgumentException("Observer "+observer+" doesn't exist!");
			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			this.currentGame = currentGame.advance(move);
			if (currentGame.getWinner().isEmpty()) {
				for (Observer eachObserver : observers) {
					eachObserver.onModelChanged(currentGame, Event.MOVE_MADE);
				}
			} else {
				for (Observer eachObserver : observers) {
					eachObserver.onModelChanged(currentGame, Event.GAME_OVER);
				}
			}
		}
	}
}
