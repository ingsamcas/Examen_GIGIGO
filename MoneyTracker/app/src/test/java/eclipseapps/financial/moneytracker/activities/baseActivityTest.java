package eclipseapps.financial.moneytracker.activities;

import org.junit.Assert;
import org.junit.Test;

public class baseActivityTest {

    @Test
    public void isPremium() {
        baseActivity base=new baseActivity() {};
        base.onCreate(null);
        boolean in=false;
        boolean expected=in;
        boolean out=base.isPremium(in);
        Assert.assertArrayEquals(new boolean[]{expected},new boolean[]{out});
    }
}