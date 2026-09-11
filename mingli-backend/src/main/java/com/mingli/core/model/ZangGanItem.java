package com.mingli.core.model;

import com.mingli.core.ShiShen;
import com.mingli.core.TianGan;

/** 地支藏干条目 */
public class ZangGanItem {

    /** 藏干 */
    public TianGan gan;
    /** 本气 / 中气 / 余气 */
    public String level;
    /** 该藏干相对日主的十神 */
    public ShiShen shiShen;

    public ZangGanItem() {}

    public ZangGanItem(TianGan gan, String level, ShiShen shiShen) {
        this.gan = gan;
        this.level = level;
        this.shiShen = shiShen;
    }

    public String getGan() { return gan == null ? null : gan.getCn(); }
    public String getShiShen() { return shiShen == null ? null : shiShen.getCn(); }
    public String getGanWuXing() { return gan == null ? null : gan.getWuXing().getCn(); }
}
