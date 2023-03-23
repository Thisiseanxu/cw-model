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
        private final List<Player> detectives;
        private final ImmutableSet<Piece> winner; // 创建一个空的不可变集
        private final ImmutableSet<Move> moves;

        private MyGameState(
                final GameSetup setup,
                final ImmutableSet<Player> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final List<Player> detectives) {
            if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
            if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is invalid"); //检查游戏使用的图是否正确
            this.setup = setup;
            this.remaining = remaining;
            this.log = log;
            if (!mrX.isMrX()) throw new IllegalArgumentException("No mrX find in the game!"); //检查游戏里第一位玩家是不是MrX
            this.mrX = mrX;
            testDetectivesValid(detectives);
            this.detectives = detectives;
            this.winner = ImmutableSet.of(); // 后面需要判断赢家，先在这里生成一个空列表应付一下
            this.moves = makeMove(setup,detectives,this.remaining,log.size());
        }

        /**
         * 检测侦探的类型、车票、位置是否正确。如果不正确则抛出错误。
         */
        private static void testDetectivesValid(final List<Player> detectives) {
            List<Piece> detectiveList = new ArrayList<>();
            List<Integer> detectiveLocation = new ArrayList<>();
            if (detectives.isEmpty())
                throw new IllegalArgumentException("No detective find in the game!"); //检查游戏里是不是至少有一个侦探
            for (Player eachDetectives : detectives) {
                if (eachDetectives.isMrX())
                    throw new IllegalArgumentException("Mrx in the detective list!"); //检查侦探里是否混入了MrX
                if (eachDetectives.has(DOUBLE) || eachDetectives.has(SECRET))
                    throw new IllegalArgumentException("Detective has invalid ticket!"); //检查侦探是否持有他们不该有的票
                if (detectiveList.contains(eachDetectives.piece()))
                    throw new IllegalArgumentException("There are duplicate detectives in the game!"); //通过piece检查是否有重复的侦探
                if (detectiveLocation.contains(eachDetectives.location()))
                    throw new IllegalArgumentException("Some detective are in the same location!"); //检查是否有侦探在同一个位置
                detectiveList.add(eachDetectives.piece());
                detectiveLocation.add(eachDetectives.location()); //当侦探各项信息都正确时，保存侦探的piece和location
            }
        }

        /**
         * 通过Piece查找当前GameState的一个玩家，找不到时返回Optional.empty()。
         */
        private Optional<Player> findPlayer(Piece playerToFind) {
            if (this.mrX.piece().equals(playerToFind)) {
                return Optional.of(mrX); //对比要查找的piece是否属于MrX
            }
            for (Player oneOfDetective : detectives) {
                if (oneOfDetective.piece().equals(playerToFind)) { //读取侦探玩家列表，将每个侦探玩家的piece和查找的piece对比
                    return Optional.of(oneOfDetective);
                }
            }
            return Optional.empty();
        }

        /**
         * 检测输入的位置是否被任何侦探占用了
         */
        private boolean isLocationOccupied(int location, final List<Player> detectives) {
            for (Player eachDetectives : detectives) {
                if (eachDetectives.location() == location)
                    return false;
            }
            return true;
        }

        /**
         * 根据游戏状态返回现在所有可能的移动
         */

        private ImmutableSet<Move> makeMove(final GameSetup setup, final List<Player> detectives, final ImmutableSet<Player> remaining, int lengthOfMrXLog) {
            // 判断玩家是否有双走票，以及当前是否是最后一回合
            // 能双走则调用makeDoubleMove和makeSingleMove，否则只调用makeSingleMove
            // TODO 判断游戏是否结束，如果已经有赢家则立即停止游戏
            Set<Move> availableMove = new HashSet<>(Set.of());
            for (Player eachPlayer:remaining){
                Set<SingleMove> availableSingleMove = makeSingleMoves(setup, detectives, eachPlayer, eachPlayer.location());
                availableMove.addAll(availableSingleMove);
                if (eachPlayer.has(DOUBLE) && ((setup.moves.size() - lengthOfMrXLog) > 1)) {
                    Set<DoubleMove> availableDoubleMove = makeDoubleMove(setup, detectives, eachPlayer, eachPlayer.location());
                    availableMove.addAll(availableDoubleMove);
                }
            }
            return ImmutableSet.copyOf(availableMove);
//            return new HashSet<>(Set.of());
        }

        /**
         * 返回指定玩家现在所有可能的双走移动
         */
        private Set<DoubleMove> makeDoubleMove(GameSetup setup, List<Player> detectives, Player player, int source) {
            // 通过调用两次SingleMove再合成的方式获取所有可用的DoubleMove
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
         * 返回指定玩家现在所有可能的单步移动
         */
        private Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

            Set<SingleMove> availableSingleMove = new HashSet<>();

            for (int destination : setup.graph.adjacentNodes(source)) {
                // find out if destination is occupied by a detective
                //  if the location is occupied, don't add to the collection of moves to return
                if (isLocationOccupied(destination, detectives)) {
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
            return moves;
        }

        private GameState applyMove(Move move){
            MoveDestinationVisitor visitor = new MoveDestinationVisitor();
            ImmutableSet<Integer> destinations = move.accept(visitor);
            Iterator<Ticket> usedTickets = move.tickets().iterator();
            if (move.commencedBy().equals(mrX.piece())){
                return new MyGameState(setup, ImmutableSet.copyOf(detectives), ImmutableList.of(), mrX, detectives);
            }
            Set<Player> movedDetectives = new HashSet<>();
            Set<Player> newRemaining = remaining;
            for (Player eachDetectives:detectives){
                if (move.commencedBy().equals(eachDetectives.piece())){
                    movedDetectives.add(eachDetectives);
                }
                movedDetectives.add(eachDetectives);
            }
            return new MyGameState(setup, ImmutableSet.copyOf(newRemaining), ImmutableList.of(), mrX, detectives);
        }

        @Override
        @Nonnull
        public GameState advance(Move move) {
            if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
            return applyMove(move);
        }
    }
}
