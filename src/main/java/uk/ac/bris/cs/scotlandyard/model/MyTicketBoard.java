package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.Board.*;

import javax.annotation.Nonnull;

/**
 * 实现了一种将Player.ticket()转换为ScotlandYard TicketBoard的类
 */
@Nonnull
public final class MyTicketBoard implements TicketBoard {
    final ImmutableMap<ScotlandYard.Ticket, Integer> ticketMap;

    /**
     * 在初始化时保存玩家的储存票类型和对应票数的ImmutableMap
     */
    public MyTicketBoard(ImmutableMap<ScotlandYard.Ticket, Integer> ticketMap) {
        this.ticketMap = ticketMap;
    }

    /**
     * 在调用时在ImmutableMap中查找输入的票类型，返回对应的票数
     */
    @Override
    public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
        return ticketMap.get(ticket);
    }
}