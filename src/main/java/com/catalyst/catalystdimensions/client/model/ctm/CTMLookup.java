package com.catalyst.catalystdimensions.client.model.ctm;

public final class CTMLookup {
    private static final int[] BASE16 = {
            /*0000*/0, /*0001 L*/4, /*0010 D*/3, /*0011 DL*/12,
            /*0100 R*/2, /*0101 RL*/10, /*0110 RD*/11, /*0111 RDL*/15,
            /*1000 U*/1, /*1001 UL*/13, /*1010 UD*/9,  /*1011 UDL*/14,
            /*1100 UR*/8, /*1101 URL*/7, /*1110 URD*/6, /*1111 URDL*/5
    };

    private static final int OPEN_UR = 20;
    private static final int OPEN_RD = 21;
    private static final int OPEN_DL = 22;
    private static final int OPEN_LU = 23;

    public static int pick(boolean u, boolean r, boolean d, boolean l,
                           boolean ur, boolean rd, boolean dl, boolean lu) {
        int mask = (u?8:0)|(r?4:0)|(d?2:0)|(l?1:0);
        int idx = BASE16[mask];
        if (u && r && !ur) return OPEN_UR;
        if (r && d && !rd) return OPEN_RD;
        if (d && l && !dl) return OPEN_DL;
        if (l && u && !lu) return OPEN_LU;
        return idx;
    }

    public static int simple16(boolean u, boolean r, boolean d, boolean l) {
        int mask = (u ? 8 : 0) | (r ? 4 : 0) | (d ? 2 : 0) | (l ? 1 : 0);
        return BASE16[mask];
    }
}
