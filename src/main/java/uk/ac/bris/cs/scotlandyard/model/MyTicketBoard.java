package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.Board.TicketBoard;

import javax.annotation.Nonnull;

/**
 * the class achieved to turn Player.ticket() into ScotlandYard TicketBoard
 */
@Nonnull
public final class MyTicketBoard implements TicketBoard {
    final ImmutableMap<ScotlandYard.Ticket, Integer> ticketMap;

    /**
     * store the ImmutableMap including tickets type players have and corresponding number of tickets when initialising
     */
    public MyTicketBoard(ImmutableMap<ScotlandYard.Ticket, Integer> ticketMap) {
        this.ticketMap = ticketMap;
    }

    /**
     * look up the ticket type entered at ImmutableMap when the function is called, return the number of that type of ticket
     */
    @Override
    public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
        return ticketMap.get(ticket);
    }
}