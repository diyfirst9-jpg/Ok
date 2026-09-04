package com.winlator.cmod.runtime.display.renderer.effects;

public abstract class Effect {
    public static final int TYPE_CRT      = 0;
    public static final int TYPE_VIVID    = 1;
    public static final int TYPE_HDR      = 2;
    public static final int TYPE_NATURAL  = 3;
    public static final int TYPE_TOON      = 4;
    public static final int TYPE_NTSC      = 5;
    public static final int TYPE_COLORADJ  = 6;
    public static final int TYPE_COLORGRADE = 7;
    public static final int TYPE_SHARPEN   = 8;
    public static final int TYPE_SCANLINES = 9;
    public static final int TYPE_NTSC2     = 10;
    public static final int TYPE_COLORBLIND = 11;
    public static final int TYPE_PIXELATE  = 12;

    public abstract int getNativeType();

    /**
     * Up to four floats forwarded to the native effect path:
     * [0] mode/reserved,
     * [1] param0 (e.g. saturation, strength),
     * [2] param1 (e.g. contrast),
     * [3] param2 (e.g. sharpness).
     */
    public float[] getParams() {
        return new float[]{0f, 0f, 0f, 0f};
    }

    public void writeParams(float[] out, int offset) {
        out[offset] = 0f;
        out[offset + 1] = 0f;
        out[offset + 2] = 0f;
        out[offset + 3] = 0f;
    }
}
