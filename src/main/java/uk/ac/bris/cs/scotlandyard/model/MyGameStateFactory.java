package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.List;
import java.util.Optional;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	@Override @Nonnull public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(mrX.piece()), ImmutableList.of(), mrX, detectives);
	}

	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, Player first, Player... rest) {
		return Factory.super.build(setup, mrX, first, rest); // mrX有问题
	}

	@Nonnull private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = setup;
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}
		@Override @Nonnull public GameSetup getSetup() {  return this.setup; }
		@Override @Nonnull public ImmutableSet<Piece> getPlayers() { return null; }
		@Override @Nonnull public Optional<Integer> getDetectiveLocation(Piece.Detective detective) { return null;}
		@Override @Nonnull public Optional<TicketBoard> getPlayerTickets(Piece piece) { return null; }
		@Override @Nonnull public ImmutableList<LogEntry> getMrXTravelLog() { return null; }
		@Override @Nonnull public ImmutableSet<Piece> getWinner() { return null; }
		@Override @Nonnull public ImmutableSet<Move> getAvailableMoves()  { return null; }
		@Override public GameState advance(Move move) {  return null;  }
	}
}
