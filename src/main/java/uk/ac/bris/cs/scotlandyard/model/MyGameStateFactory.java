package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.Move.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move.SingleMove;

import javax.annotation.Nonnull;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
    @Override
    @Nonnull
    public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
        return new MyGameState(setup, ImmutableSet.of(mrX), ImmutableList.of(), mrX, detectives);
    }

    @Nonnull
    private static final class MyGameState implements GameState {
        final private GameSetup setup;
        private final ImmutableSet<Player> remaining;
        private final ImmutableList<LogEntry> log;
        private final Player mrX;
        private final ImmutableList<Player> detectives;
        private final ImmutableSet<Piece> winner;
        private final ImmutableSet<Move> moves;

        private MyGameState(
                final GameSetup setup,
                final ImmutableSet<Player> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final ImmutableList<Player> detectives) {
            if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
            if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is invalid"); //check if the game is using correct graph
            this.setup = setup;
            this.remaining = remaining;
            this.log = log;
            if (!mrX.isMrX()) throw new IllegalArgumentException("No mrX find in the game!"); //check if the first player in game is mrX
            this.mrX = mrX;
            testDetectivesValid(detectives);
            this.detectives = detectives;
            this.moves = makeMove(setup, detectives, this.remaining, log.size());
            this.winner = findWinner();
        }

        /**
         * check if there is error for detectives, throw error if there is one
         * @param detectives the list of all detectives
         */
        private void testDetectivesValid(final List<Player> detectives) {
            List<Piece> detectiveList = new ArrayList<>();
            List<Integer> detectiveLocation = new ArrayList<>();
            if (detectives.isEmpty())
                throw new IllegalArgumentException("No detective find in the game!"); //check if there's at least one detective
            for (Player eachDetectives : detectives) {
                if (eachDetectives.isMrX())
                    throw new IllegalArgumentException("Mrx in the detective list!"); //check if mrX is in the detective list
                if (eachDetectives.has(DOUBLE) || eachDetectives.has(SECRET))
                    throw new IllegalArgumentException("Detective has invalid ticket!"); //check if detective has invalid ticket
                if (detectiveList.contains(eachDetectives.piece()))
                    throw new IllegalArgumentException("There are duplicate detectives in the game!"); //check if there is repeated detectives by pieces
                if (detectiveLocation.contains(eachDetectives.location()))
                    throw new IllegalArgumentException("Some detective are in the same location!"); //check if there is detectives at same location
                detectiveList.add(eachDetectives.piece());
                detectiveLocation.add(eachDetectives.location()); //store detectives locations and pieces when everything all right
            }
        }

        /**
         * check if the location is occupied by any detective
         * @param location the location want to test if occupied
         * @param detectives the list of all detectives
         *
         * @return the boolean value if the location is occupied
         */
        private boolean isLocationOccupied(int location, final List<Player> detectives) {
            for (Player eachDetectives : detectives) {
                if (eachDetectives.location() == location) //check if the detective is at this location
                    return true;
            }
            return false;
        }

        private Set<Piece> playersToPieces(final List<Player> players) {
            Set<Piece> playerPieces = new HashSet<>();
            for (Player eachDetectives : players) {
                playerPieces.add(eachDetectives.piece());
            }
            return playerPieces;//return piece list according players
        }

        /**
         * check if there is a winner
         * @return the set of the winners if there is one
         */
        private ImmutableSet<Piece> findWinner() {
            // if there is a detective catch mrX then detectives win
            if (isLocationOccupied(mrX.location(), detectives)) {
                return ImmutableSet.copyOf(playersToPieces(detectives));
            }
            // if mrX finished travelling log then mrX wins
            if (log.size() == setup.moves.size()) {
                return ImmutableSet.of(mrX.piece());
            }
            if (moves.isEmpty()) {
                // if mrX can not make move next round then detectives win
                if (remaining.iterator().next().piece().equals(mrX.piece())) {
                    return ImmutableSet.copyOf(playersToPieces(detectives));
                }
            }
            // if all detectives can not make move next round then MrX wins
            if (makeMove(setup,detectives,ImmutableSet.copyOf(detectives),log.size()).isEmpty()){
                return ImmutableSet.of(mrX.piece());
            }
            return ImmutableSet.of();
        }

        /**
         * find a player by piece, return Optional.empty() when player is not found
         * @param playerToFind the piece of the player trying to find
         *
         * @return the player that matches or empty otherwise
         */
        private Optional<Player> findPlayer(Piece playerToFind) {
            if (this.mrX.piece().equals(playerToFind)) {
                return Optional.of(mrX); //compare if the piece is MrX
            }
            for (Player oneOfDetective : detectives) {
                if (oneOfDetective.piece().equals(playerToFind)) { //compare every piece in detectives list with the piece trying to find
                    return Optional.of(oneOfDetective);
                }
            }
            return Optional.empty();
        }

        /**
         * return all possible moves according to current game state
         * @param detectives the list of all detectives
         * @param remaining the remaining players that haven't moved this round
         * @param lengthOfMrXLog number of rounds MrX has moved
         *
         * @return An immutable set of available move of remaining player
         */
        private ImmutableSet<Move> makeMove(final GameSetup setup, final List<Player> detectives, final ImmutableSet<Player> remaining, int lengthOfMrXLog) {
            // call makeDoubleMoves and makeSingleMoves when there is Double ticket otherwise just call makeSingleMoves
            Set<Move> availableMove = new HashSet<>(Set.of());
            for (Player eachPlayer : remaining) {
                Set<SingleMove> availableSingleMove = makeSingleMoves(setup, detectives, eachPlayer, eachPlayer.location());
                availableMove.addAll(availableSingleMove);
                if (eachPlayer.has(DOUBLE) && ((setup.moves.size() - lengthOfMrXLog) > 1)) { // check if the player has double ticket, and whether this is the last round
                    Set<DoubleMove> availableDoubleMove = makeDoubleMove(setup, detectives, eachPlayer, eachPlayer.location());
                    availableMove.addAll(availableDoubleMove);
                }
            }
            return ImmutableSet.copyOf(availableMove);
        }

        /**
         * return all possible double moves for certain player
         * @param detectives the list of all detectives
         * @param player the remaining players that haven't moved this round
         * @param source the position that player at before move
         *
         * @return A set of double move available move of remaining player
         */
        private Set<DoubleMove> makeDoubleMove(GameSetup setup, List<Player> detectives, Player player, int source) {
            // return all possible double moves by calling makeSingleMoves twice
            Set<DoubleMove> availableDoubleMove = new HashSet<>();
            Set<SingleMove> availableSingleMove = makeSingleMoves(setup, detectives, player, source);
            for (SingleMove eachSingleMove : availableSingleMove) {
                Set<SingleMove> availableSecondSingleMove = makeSingleMoves(setup, detectives, player, eachSingleMove.destination);
                for (SingleMove eachSecondSingleMove : availableSecondSingleMove) {
                    if (!eachSingleMove.ticket.equals(eachSecondSingleMove.ticket)
                            || player.hasAtLeast(eachSingleMove.ticket, 2)) { // 当两步使用同一张车票时，检测是否有两张足够的车票
                        availableDoubleMove.add(new DoubleMove(
                                player.piece(),
                                source,
                                eachSingleMove.ticket,
                                eachSingleMove.destination,
                                eachSecondSingleMove.ticket,
                                eachSecondSingleMove.destination));
                    }
                }
            }
            return availableDoubleMove;
        }

        /**
         * return all possible single moves for certain player
         * @param detectives the list of all detectives
         * @param player the remaining players that haven't moved this round
         * @param source the position that player at before move
         *
         * @return A set of single move available move of remaining player
         */
        private Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

            Set<SingleMove> availableSingleMove = new HashSet<>();

            for (int destination : setup.graph.adjacentNodes(source)) {
                // find out if destination is occupied by a detective
                //  if the location is occupied, don't add to the collection of moves to return
                if (!isLocationOccupied(destination, detectives)) {
                    for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
                        // find out if the player has the required tickets
                        //  if it does, construct a SingleMove and add it the collection of moves to return
                        if (player.has(t.requiredTicket()))
                            availableSingleMove.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
                        if (player.has(SECRET))
                            availableSingleMove.add(new SingleMove(player.piece(), source, SECRET, destination));
                    }
                }
            }

            return availableSingleMove;
        }

        @Override
        @Nonnull
        public GameSetup getSetup() {
            return setup;
        }

        @Override
        @Nonnull
        public ImmutableSet<Piece> getPlayers() {
            Set<Piece> AllPlayers = new HashSet<>();
            AllPlayers.add(mrX.piece());
            for (Player eachDetective : detectives) {
                AllPlayers.add(eachDetective.piece());
            }
            return ImmutableSet.copyOf(AllPlayers);
        }

        @Override
        @Nonnull
        public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
            Optional<Player> thatDetective = findPlayer(detective);
            return thatDetective.map(Player::location); // Optional.map在值存在时会返回Player.location()，否则返回Optional.empty()
        }

        @Override
        @Nonnull
        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
            Optional<Player> thatPlayer = findPlayer(piece);
            return thatPlayer.map(player -> new MyTicketBoard(player.tickets()));
        }

        @Override
        @Nonnull
        public ImmutableList<LogEntry> getMrXTravelLog() {
            return log;
        }

        @Override
        @Nonnull
        public ImmutableSet<Piece> getWinner() {
            return winner;
        }

        @Override
        @Nonnull
        public ImmutableSet<Move> getAvailableMoves() {
            if (winner.isEmpty()) { // determine if the game is over
                return moves;
            }
            return ImmutableSet.of(); // return empty set when the game is over
        }


        /**
         * TODO 在这里补上这个方法详细的说明！
         */
        private GameState applyMrXMove(Move move) {
            MyMoveDestinationVisitor visitor = new MyMoveDestinationVisitor(); // create a new visitor to return all 创建一个新的访客，用于返回move移动的目标位置集合
            ImmutableList<Integer> destinations = move.accept(visitor); // 无论该移动时单走还是双走，访客都会返回一个整数集合，代表移动的步数以及每一步的目的地
            Iterator<Ticket> usedTickets = move.tickets().iterator(); // 用move.tickets()创建一个iterator，用于获取消耗的票
            Ticket ticketUsed;
            Player newMrX = mrX;
            List<LogEntry> newLog = new ArrayList<>(log); // copy log to a mutable list
            for (Integer eachDestinations : destinations) {
                ticketUsed = usedTickets.next(); // 获取下一张用的票（双走时获取两次）
                newMrX = newMrX.use(ticketUsed).at(eachDestinations); // mrX使用这张票，然后移动到目标位置
                if (setup.moves.get(newLog.size())) { // check if this round need to make
                    newLog.add(LogEntry.reveal(ticketUsed, eachDestinations)); // 记录一个reveal log，包含位置和用的票
                } else {
                    newLog.add(LogEntry.hidden(ticketUsed)); // 不公开位置则记录一个hidden log，只包含用的票
                }
            }
            if (destinations.size() == 2) newMrX = newMrX.use(DOUBLE); // use a Double ticket if mrX moved twice
            return new MyGameState(setup, ImmutableSet.copyOf(detectives), ImmutableList.copyOf(newLog), newMrX, detectives);
        }

        /**
         * TODO 在这里补上这个方法详细的说明！
         */
        private GameState applyDetectiveMove(Move move) {
            MyMoveDestinationVisitor visitor = new MyMoveDestinationVisitor(); // 创建一个新的访客，用于返回move移动的目标位置
            Integer destination = move.accept(visitor).get(0); // 获取这次行动的目标位置
            Ticket ticketUsed = move.tickets().iterator().next(); // 获取这次行动消耗的票
            Player newMrX = mrX;
            List<Player> detectivesAfterMove = new ArrayList<>(); // 创建一个可变列表存放移动后的侦探们的状态
            Set<Player> remainingAfterMove = new HashSet<>(remaining); // 复制remaining为一个可变集合，便于之后修改
            for (Player oneDetective : detectives) {
                if (move.commencedBy().equals(oneDetective.piece())) { // 如果是这个侦探移动
                    remainingAfterMove.remove(oneDetective); // 将其从remaining中删除，以防在一回合中再次移动
                    oneDetective = oneDetective.use(ticketUsed).at(destination); // 拿走这名侦探用的票，并把他放到他的目的地
                    newMrX = newMrX.give(ticketUsed); // 把侦探用的票交给mrX
                }
                detectivesAfterMove.add(oneDetective); // 无论侦探是否移动都将其保存到新的侦探列表
            }
            if (remainingAfterMove.isEmpty() || makeMove(setup, detectivesAfterMove, ImmutableSet.copyOf(remainingAfterMove), log.size()).isEmpty()) {
                // 当所有侦探已经移动或之后的侦探都无法移动时，直接切换到MrX的回合
                return new MyGameState(setup, ImmutableSet.of(newMrX), log, newMrX, ImmutableList.copyOf(detectivesAfterMove));
            }
            return new MyGameState(setup, ImmutableSet.copyOf(remainingAfterMove), log, newMrX, ImmutableList.copyOf(detectivesAfterMove));
        }

        @Override
        @Nonnull
        public GameState advance(Move move) {
            if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
            if (move.commencedBy().isMrX()) return applyMrXMove(move);
            return applyDetectiveMove(move);
        }
    }
}
