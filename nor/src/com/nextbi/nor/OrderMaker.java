package com.nextbi.nor;

import java.util.*;

public class OrderMaker
{
    public static List<Usage> makeOrder(Collection<Usage> terms )
    {
        TreeMap< Float, Usage> order = new TreeMap<>();
        int max = 0;

        // Получить максимыльную длину строки в позициях
        for( Usage usage : terms )
            max = getMaxLength( max, usage.getPositions() );

        //  считаем цену позиции
        for( Usage usage : terms )
        {
            List<Float> pns = new ArrayList<>();

            for( Position p : usage.getPositions() )
                pns.add( (( float ) p.getPos() ) * p.getTotal() / max );
            float avg = avg( pns );

            order.put( avg, usage );
        }

        List<Usage> result = new ArrayList<>();
        result.addAll( order.values() );
        return result;
    }

    private static float avg(List<Float> pns){
        int amount = pns.size();
        float summ = 0;

        for( Float pos : pns )
            summ += pos;

        return summ / amount;

    }

    private static int getMaxLength( int max, List<Position> pos )
    {

        for( Position p : pos )
        {
            if( p.getTotal() > max )
                max = p.getTotal();
        }
        return max;
    }
}