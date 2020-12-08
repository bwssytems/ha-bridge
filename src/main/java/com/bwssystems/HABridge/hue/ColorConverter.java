package com.bwssystems.HABridge.hue;

/**
 * Convert between different color spaces supported.
 * RGB -> CMYK -> RGB
 * RGB -> YIQ -> RGB
 * RGB -> YCbCr -> RGB
 * RGB -> YUV -> RGB
 * RGB -> RGChromaticity
 * RGB -> HSV -> RGB
 * RGB -> YCC -> RGB
 * RGB -> YCoCg -> RGB
 * RGB -> XYZ -> RGB
 * RGB -> HunterLAB -> RGB
 * RGB -> HLS -> RGB
 * RGB -> CIE-LAB -> RGB
 * XYZ -> HunterLAB -> XYZ
 * XYZ -> CIE-LAB -> XYZ
 * @author Diego Catalano
 */


public class ColorConverter {

    /**
     * Don't let anyone instantiate this class.
     */
    private ColorConverter() {}
    
    public static enum YCbCrColorSpace {ITU_BT_601,ITU_BT_709_HDTV};
    private final static double EPSILON = 0.00001;
    
    // XYZ (Tristimulus) Reference values of a perfect reflecting diffuser
    
    //2o Observer (CIE 1931)
    // X2, Y2, Z2
    public static float[] CIE2_A = {109.850f, 100f, 35.585f}; //Incandescent
    public static float[] CIE2_C = {98.074f, 100f, 118.232f};
    public static float[] CIE2_D50 = {96.422f, 100f, 82.521f};
    public static float[] CIE2_D55 = {95.682f, 100f, 92.149f};
    public static float[] CIE2_D65 = {95.047f, 100f, 108.883f}; //Daylight
    public static float[] CIE2_D75 = {94.972f, 100f, 122.638f};
    public static float[] CIE2_F2 = {99.187f, 100f, 67.395f}; //Fluorescent
    public static float[] CIE2_F7 = {95.044f, 100f, 108.755f};
    public static float[] CIE2_F11 = {100.966f, 100f, 64.370f};
    
    //10o Observer (CIE 1964)
    // X2, Y2, Z2
    public static float[] CIE10_A = {111.144f, 100f, 35.200f}; //Incandescent
    public static float[] CIE10_C = {97.285f, 100f, 116.145f};
    public static float[] CIE10_D50 = {96.720f, 100f, 81.427f};
    public static float[] CIE10_D55 = {95.799f, 100f, 90.926f};
    public static float[] CIE10_D65 = {94.811f, 100f, 107.304f}; //Daylight
    public static float[] CIE10_D75 = {94.416f, 100f, 120.641f};
    public static float[] CIE10_F2 = {103.280f, 100f, 69.026f}; //Fluorescent
    public static float[] CIE10_F7 = {95.792f, 100f, 107.687f};
    public static float[] CIE10_F11 = {103.866f, 100f, 65.627f};
    
    /**
     * RFB -> CMYK
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return CMYK color space. Normalized.
     */
    public static float[] RGBtoCMYK(int red, int green, int blue){
        float[] cmyk = new float[4];
        
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;
        
        float k = 1.0f - Math.max(r, Math.max(g, b));
        float c = (1f-r-k) / (1f-k);
        float m = (1f-g-k) / (1f-k);
        float y = (1f-b-k) / (1f-k);
        
        cmyk[0] = c;
        cmyk[1] = m;
        cmyk[2] = y;
        cmyk[3] = k;
        
        return cmyk;
    }
    
    /**
     * CMYK -> RGB
     * @param c Cyan.
     * @param m Magenta.
     * @param y Yellow.
     * @param k Black.
     * @return RGB color space.
     */
    public static int[] CMYKtoRGB(float c, float m, float y, float k){
        int[] rgb = new int[3];
        
        rgb[0] = (int)(255 * (1-c) * (1-k));
        rgb[1] = (int)(255 * (1-m) * (1-k));
        rgb[2] = (int)(255 * (1-y) * (1-k));
        
        return rgb;
    }
    
    /**
     * RGB -> YUV.
     * Y in the range [0..1].
     * U in the range [-0.5..0.5].
     * V in the range [-0.5..0.5].
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return YUV color space.
     */
    public static float[] RGBtoYUV(int red, int green, int blue){
        
        float r = (float)red / 255;
        float g = (float)green / 255;
        float b = (float)blue / 255;
        
        float[] yuv = new float[3];
        float y,u,v;
        
        y = (float)(0.299 * r + 0.587 * g + 0.114 * b);
        u = (float)(-0.14713 * r - 0.28886 * g + 0.436 * b);
        v = (float)(0.615 * r - 0.51499 * g - 0.10001 * b);
        
        yuv[0] = y;
        yuv[1] = u;
        yuv[2] = v;
        
        return yuv;
    }
    
    /**
     * YUV -> RGB.
     * @param y Luma. In the range [0..1].
     * @param u Chrominance. In the range [-0.5..0.5].
     * @param v Chrominance. In the range [-0.5..0.5].
     * @return RGB color space.
     */
    public static int[] YUVtoRGB(float y, float u, float v){
        int[] rgb = new int[3];
        float r,g,b;
        
        r = (float)((y + 0.000 * u + 1.140 * v) * 255);
        g = (float)((y - 0.396 * u - 0.581 * v) * 255);
        b = (float)((y + 2.029 * u + 0.000 * v) * 255);
        
        rgb[0] = (int)r;
        rgb[1] = (int)g;
        rgb[2] = (int)b;
        
        return rgb;
    }
    
    /**
     * RGB -> YIQ.
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return YIQ color space.
     */
    public static float[] RGBtoYIQ(int red, int green, int blue){
        float[] yiq = new float[3];
        float y,i,q;
        
        float r = (float)red / 255;
        float g = (float)green / 255;
        float b = (float)blue / 255;
        
        y = (float)(0.299 * r + 0.587 * g + 0.114 * b);
        i = (float)(0.596 * r - 0.275 * g - 0.322 * b);
        q = (float)(0.212 * r - 0.523 * g + 0.311 * b);
        
        yiq[0] = y;
        yiq[1] = i;
        yiq[2] = q;
        
        return yiq;
    }
    
    /**
     * YIQ -> RGB.
     * @param y Luma. Values in the range [0..1].
     * @param i In-phase. Values in the range [-0.5..0.5].
     * @param q Quadrature. Values in the range [-0.5..0.5].
     * @return RGB color space.
     */
    public static int[] YIQtoRGB(double y, double i, double q){
        int[] rgb = new int[3];
        int r,g,b;
        
        r = (int)((y + 0.956 * i + 0.621 * q) * 255);
        g = (int)((y - 0.272 * i - 0.647 * q) * 255);
        b = (int)((y - 1.105 * i + 1.702 * q) * 255);
        
        r = Math.max(0,Math.min(255,r));
        g = Math.max(0,Math.min(255,g));
        b = Math.max(0,Math.min(255,b));
        
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
        
        return rgb;
    }
    
    public static float[] RGBtoYCbCr(int red, int green, int blue, YCbCrColorSpace colorSpace){
        
        float r = (float)red / 255;
        float g = (float)green / 255;
        float b = (float)blue / 255;
        
        float[] YCbCr = new float[3];
        float y,cb,cr;
        
        if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
            y = (float)(0.299 * r + 0.587 * g + 0.114 * b);
            cb = (float)(-0.169 * r - 0.331 * g + 0.500 * b);
            cr = (float)(0.500 * r - 0.419 * g - 0.081 * b);
        }
        else{
            y = (float)(0.2215 * r + 0.7154 * g + 0.0721 * b);
            cb = (float)(-0.1145 * r - 0.3855 * g + 0.5000 * b);
            cr = (float)(0.5016 * r - 0.4556 * g - 0.0459 * b);
        }
        
        YCbCr[0] = (float)y;
        YCbCr[1] = (float)cb;
        YCbCr[2] = (float)cr;
        
        return YCbCr;
    }
    
    public static int[] YCbCrtoRGB(float y, float cb, float cr, YCbCrColorSpace colorSpace){
        int[] rgb = new int[3];
        float r,g,b;
        
        if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
            r = (float)(y + 0.000 * cb + 1.403 * cr) * 255;
            g = (float)(y - 0.344 * cb - 0.714 * cr) * 255;
            b = (float)(y + 1.773 * cb + 0.000 * cr) * 255;
        }
        else{
            r = (float)(y + 0.000 * cb + 1.5701 * cr) * 255;
            g = (float)(y - 0.1870 * cb - 0.4664 * cr) * 255;
            b = (float)(y + 1.8556 * cb + 0.000 * cr) * 255;
        }
        
        rgb[0] = (int)r;
        rgb[1] = (int)g;
        rgb[2] = (int)b;
        
        return rgb;
    }
    
    /**
     * Rg-Chromaticity space is already known to remove ambiguities due to illumination or surface pose.
     * @see Neural Information Processing - Chi Sing Leung. p. 668
     * @param red Red coefficient.
     * @param green Green coefficient.
     * @param blue Blue coefficient.
     * @return Normalized RGChromaticity. Range[0..1].
     */
    public static float[] RGChromaticity(int red, int green, int blue){
        double[] color = new double[5];
        
        double sum = red + green + blue;
        
        //red
        color[0] = red / sum;
        
        //green
        color[1] = green / sum;
        
        //blue
        color[2] = 1 - color[0] - color[1];
        
        double rS = color[0] - 0.333;
        double gS = color[1] - 0.333;
        
        //saturationBRGBtoHSV(int red, int green, int blue){
        float[] hsv = new float[3];
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;
        
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        
        // Hue
        if (max == min){
            hsv[0] = 0;
        }
        else if (max == r){
            hsv[0] = ((g - b) / delta) * 60f;
        }
        else if (max == g){
            hsv[0] = ((b - r) / delta + 2f) * 60f;
        }
        else if (max == b){
            hsv[0] = ((r - g) / delta + 4f) * 60f;
        }
        
        // Saturation
        if (delta == 0)
            hsv[1] = 0;
        else
            hsv[1] = delta / max;
        
        //Value
        hsv[2] = max;
        
        return hsv;
    }
    
    /**
     * HSV -> RGB.
     * @param hue Hue.
     * @param saturation Saturation. In the range[0..1].
     * @param value Value. In the range[0..1].
     * @return RGB color space. In the range[0..255].
     */
    public static int[] HSVtoRGB(float hue, float saturation, float value){
        int[] rgb = new int[3];
        
        float hi = (float)Math.floor(hue / 60.0) % 6;
        float f =  (float)((hue / 60.0) - Math.floor(hue / 60.0));
        float p = (float)(value * (1.0 - saturation));
        float q = (float)(value * (1.0 - (f * saturation)));
        float t = (float)(value * (1.0 - ((1.0 - f) * saturation)));
        
        if (hi == 0){
            rgb[0] = (int)(value * 255);
            rgb[1] = (int)(t * 255);
            rgb[2] = (int)(p * 255);
        }
        else if (hi == 1){
            rgb[0] = (int)(q * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(p * 255);
        }
        else if (hi == 2){
            rgb[0] = (int)(p * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(t * 255);
        }
        else if (hi == 3){
            rgb[0] = (int)(p * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(q * 255);
        }
        else if (hi == 4){
            rgb[0] = (int)(t * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(p * 255);
        }
        else if (hi == 5){
            rgb[0] = (int)(value * 255);
            rgb[1] = (int)(p * 255);
            rgb[2] = (int)(q * 255);
        }
        
        return rgb;
    }
    
    /**
     * RGB -> YCC.
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @return YCC color space. In the range [0..1].
     */
    public static float[] RGBtoYCC(int red, int green, int blue){
        float[] ycc = new float[3];
        
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;
        
        float y = 0.213f * r + 0.419f * g + 0.081f * b;
        float c1 = -0.131f * r - 0.256f * g + 0.387f * b + 0.612f;
        float c2 = 0.373f * r - 0.312f * r - 0.061f * b + 0.537f;
        
        ycc[0] = y;
        ycc[1] = c1;
        ycc[2] = c2;
        
        return ycc;
    }
    
    /**
     * YCC -> RGB.
     * @param y Y coefficient.
     * @param c1 C coefficient.
     * @param c2 C coefficient.
     * @return RGB color space.
     */
    public static int[] YCCtoRGB(float y, float c1, float c2){
        int[] rgb = new int[3];
        
        float r = 0.981f * y + 1.315f * (c2 - 0.537f);
        float g = 0.981f * y - 0.311f * (c1 - 0.612f)- 0.669f * (c2 - 0.537f);
        float b = 0.981f * y + 1.601f * (c1 - 0.612f);
        
        rgb[0] = (int)(r * 255f);
        rgb[1] = (int)(g * 255f);
        rgb[2] = (int)(b * 255f);
        
        return rgb;
    }
    
    /**
     * RGB -> YCoCg.
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @return YCoCg color space.
     */
    public static float[] RGBtoYCoCg(int red, int green, int blue){
        float[] yCoCg = new float[3];
        
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;
        
        float y = r / 4f + g / 2f + b / 4f;
        float co = r / 2f - b / 2f;
        float cg = -r / 4f + g / 2f - b / 4f;
        
        yCoCg[0] = y;
        yCoCg[1] = co;
        yCoCg[2] = cg;
        
        return yCoCg;
    }
    
    /**
     * YCoCg -> RGB.
     * @param y Pseudo luminance, or intensity.
     * @param co Orange chrominance.
     * @param cg Green chrominance.
     * @return RGB color space.
     */
    public static int[] YCoCgtoRGB(float y, float co, float cg){
        int[] rgb = new int[3];
        
        float r = y + co - cg;
        float g = y + cg;
        float b = y - co - cg;
        
        rgb[0] = (int)(r * 255f);
        rgb[1] = (int)(g * 255f);
        rgb[2] = (int)(b * 255f);
        
        return rgb;
    }
    
    /**
     * RGB -> XYZ
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @return XYZ color space.
     */
    public static float[] RGBtoXYZ(int red, int green, int blue){
        float[] xyz = new float[3];
        
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;
        
        //R
        if ( r > 0.04045)
            r = (float)Math.pow(( ( r + 0.055f ) / 1.055f ), 2.4f);
        else
            r /= 12.92f;
        
        //G
        if ( g > 0.04045)
            g = (float)Math.pow(( ( g + 0.055f ) / 1.055f ), 2.4f);
        else
            g /= 12.92f;
        
        //B
        if ( b > 0.04045)
            b = (float)Math.pow(( ( b + 0.055f ) / 1.055f ), 2.4f);
        else
            b /= 12.92f;
        
        r *= 100;
        g *= 100;
        b *= 100;
        
        float x = 0.412453f * r + 0.35758f * g + 0.180423f * b;
        float y = 0.212671f * r + 0.71516f * g + 0.072169f * b;
        float z = 0.019334f * r + 0.119193f * g + 0.950227f * b;
        
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
        
        return xyz;
    }
    
    /**
     * XYZ -> RGB
     * @param x X coefficient.
     * @param y Y coefficient.
     * @param z Z coefficient.
     * @return RGB color space.
     */
    public static int[] XYZtoRGB(float x, float y, float z){
        int[] rgb = new int[3];
        
        x /= 100;
        y /= 100;
        z /= 100;
        
        float r = 3.240479f * x - 1.53715f * y - 0.498535f * z;
        float g = -0.969256f * x + 1.875991f * y + 0.041556f * z;
        float b = 0.055648f * x - 0.204043f * y + 1.057311f * z;
        
        if ( r > 0.0031308 )
            r = 1.055f * ( (float)Math.pow(r, 0.4166f) ) - 0.055f;
        else
            r = 12.92f * r;
        
        if ( g > 0.0031308 )
            g = 1.055f * ( (float)Math.pow(g, 0.4166f) ) - 0.055f;
        else
            g = 12.92f * g;
        
        if ( b > 0.0031308 )
            b = 1.055f * ( (float)Math.pow(b, 0.4166f) ) - 0.055f;
        else
            b = 12.92f * b;
        
        rgb[0] = (int)(r * 255);
        rgb[1] = (int)(g * 255);
        rgb[2] = (int)(b * 255);
        
        return rgb;
    }
    
    /**
     * XYZ -> HunterLAB
     * @param x X coefficient.
     * @param y Y coefficient.
     * @param z Z coefficient.
     * @return HunterLab coefficient.
     */
    public static float[] XYZtoHunterLAB(float x, float y, float z){
        float[] hunter = new float[3];
        
        
        float sqrt = (float)Math.sqrt(y);
        
        float l = 10 * sqrt;
        float a = 17.5f * (((1.02f * x) - y) / sqrt);
        float b = 7f * ((y - (0.847f * z)) / sqrt);
        
        hunter[0] = l;
        hunter[1] = a;
        hunter[2] = b;
        
        return hunter;
    }
    
    /**
     * HunterLAB -> XYZ
     * @param l L coefficient.
     * @param a A coefficient.
     * @param b B coefficient.
     * @return XYZ color space.
     */
    public static float[] HunterLABtoXYZ(float l, float a, float b){
        float[] xyz = new float[3];
        
        
        float tempY = l / 10f;
        float tempX = a / 17.5f * l / 10f;
        float tempZ = b / 7f * l / 10f;
        
        float y = tempY * tempY;
        float x = (tempX + y) / 1.02f;
        float z = -(tempZ - y) / 0.847f;
        
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
        
        return xyz;
    }
    
    /**
     * RGB -> HunterLAB.
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @return HunterLAB color space.
     */
    public static float[] RGBtoHunterLAB(int red, int green, int blue){
        float[] xyz = RGBtoXYZ(red, green, blue);
        return XYZtoHunterLAB(xyz[0], xyz[1], xyz[2]);
    }
    
    /**
     * HunterLAB -> RGB.
     * @param l L coefficient.
     * @param a A coefficient.
     * @param b B coefficient.
     * @return RGB color space.
     */
    public static int[] HunterLABtoRGB(float l, float a, float b){
        float[] xyz = HunterLABtoXYZ(l, a, b);
        return XYZtoRGB(xyz[0], xyz[1], xyz[2]);
    }    
    
    /**
     * RGB -> HSL.
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @return HSL color space.
     */
    public static float[] RGBtoHSL(int red, int green, int blue){
        float[] hsl = new float[3];
        
        double r = red;
        double g = green;
        double b = blue;
        
        double max = Math.max(r,Math.max(g,b));
        double min = Math.min(r,Math.min(g,b));
//        double delta = max - min;
        
        //HSK
        Double h = 0d;
        Double s = 0d;
        Double l = 0d;
        
            //saturation
            double cnt = (max + min) / 2d;
            if (cnt <= 127d) {
                s = ((max - min) / (max + min));
            }
            else {
                s = ((max - min) / (510d - max - min));
            }

            //lightness
            l = ((max + min) / 2d) / 255d;

            //hue
            if (Math.abs(max - min) <= EPSILON) {
                h = 0d;
                s = 0d;
            }
            else {
                double diff = max - min;

                if (Math.abs(max - r) <= EPSILON) {
                    h = 60d * (g - b) / diff;
                }
                else if (Math.abs(max - g) <= EPSILON) {
                    h = 60d * (b - r) / diff + 120d;
                }
                else {
                    h = 60d * (r - g) / diff + 240d;
                }

                if (h < 0d) {
                    h += 360d;
                }
            }
        
        hsl[0] = h.floatValue();
        hsl[1] = s.floatValue();
        hsl[2] = l.floatValue();
        
        return hsl;
    }
    
    /**
     * HLS -> RGB.
     * @param hue Hue.
     * @param saturation Saturation.
     * @param luminance Luminance.
     * @return RGB color space.
     */
    public static int[] HSLtoRGB(float hue, float saturation, float luminance){
        int[] rgb = new int[3];
        float r = 0, g = 0, b = 0;
        
        if ( saturation == 0 )
        {
            // gray values
            r = g = b = (int) ( luminance * 255 );
        }
        else
        {
            float v1, v2;
            float h = (float) hue / 360;

            v2 = ( luminance < 0.5 ) ?
                ( luminance * ( 1 + saturation ) ) :
                ( ( luminance + saturation ) - ( luminance * saturation ) );
            v1 = 2 * luminance - v2;

            r = (int) ( 255 * Hue_2_RGB( v1, v2, h + ( 1.0f / 3 ) ) );
            g = (int) ( 255 * Hue_2_RGB( v1, v2, h ) );
            b = (int) ( 255 * Hue_2_RGB( v1, v2, h - ( 1.0f / 3 ) ) );
        }
        
        rgb[0] = (int)r;
        rgb[1] = (int)g;
        rgb[2] = (int)b;
        
        return rgb;
    }
    
    private static float Hue_2_RGB( float v1, float v2, float vH ){
        if ( vH < 0 )
            vH += 1;
        if ( vH > 1 )
            vH -= 1;
        if ( ( 6 * vH ) < 1 )
            return ( v1 + ( v2 - v1 ) * 6 * vH );
        if ( ( 2 * vH ) < 1 )
            return v2;
        if ( ( 3 * vH ) < 2 )
            return ( v1 + ( v2 - v1 ) * ( ( 2.0f / 3 ) - vH ) * 6 );
        return v1;
    }
    
    /**
     * RGB -> CIE-LAB.
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @param tristimulus XYZ Tristimulus.
     * @return CIE-LAB color space.
     */
    public static float[] RGBtoLAB(int red, int green, int blue, float[] tristimulus){
        float[] xyz = RGBtoXYZ(red, green, blue);
        float[] lab = XYZtoLAB(xyz[0], xyz[1], xyz[2], tristimulus);
        
        return lab;
    }
    
    /**
     * CIE-LAB -> RGB.
     * @param l L coefficient.
     * @param a A coefficient.
     * @param b B coefficient.
     * @param tristimulus XYZ Tristimulus.
     * @return RGB color space.
     */
    public static int[] LABtoRGB(float l, float a, float b, float[] tristimulus){
        float[] xyz = LABtoXYZ(l, a, b, tristimulus);
        return XYZtoRGB(xyz[0], xyz[1], xyz[2]);
    }
    
    /**
     * XYZ -> CIE-LAB.
     * @param x X coefficient.
     * @param y Y coefficient.
     * @param z Z coefficient.
     * @param tristimulus XYZ Tristimulus.
     * @return CIE-LAB color space.
     */
    public static float[] XYZtoLAB(float x, float y, float z, float[] tristimulus){
        float[] lab = new float[3];
        
        x /= tristimulus[0];
        y /= tristimulus[1];
        z /= tristimulus[2];
        
        if (x > 0.008856)
            x = (float)Math.pow(x,0.33f);
        else
            x = (7.787f * x) + ( 0.1379310344827586f );
        
        if (y > 0.008856)
            y = (float)Math.pow(y,0.33f);
        else
            y = (7.787f * y) + ( 0.1379310344827586f );
        
        if (z > 0.008856)
            z = (float)Math.pow(z,0.33f);
        else
            z = (7.787f * z) + ( 0.1379310344827586f );
        
        lab[0] = ( 116 * y ) - 16;
        lab[1] = 500 * ( x - y );
        lab[2] = 200 * ( y - z );
        
        return lab;
    }
    
    /**
     * CIE-LAB -> XYZ.
     * @param l L coefficient.
     * @param a A coefficient.
     * @param b B coefficient.
     * @param tristimulus XYZ Tristimulus.
     * @return XYZ color space.
     */
    public static float[] LABtoXYZ(float l, float a, float b, float[] tristimulus){
        float[] xyz = new float[3];
        
        float y = ( l + 16f ) / 116f;
        float x = a / 500f + y;
        float z = y - b / 200f;
        
        //Y
        if ( Math.pow(y,3) > 0.008856 )
            y = (float)Math.pow(y,3);
        else
            y = (float)(( y - 16 / 116 ) / 7.787);
        
        //X
        if ( Math.pow(x,3) > 0.008856 )
            x = (float)Math.pow(x,3);
        else
            x = (float)(( x - 16 / 116 ) / 7.787);
        
        // Z
        if ( Math.pow(z,3) > 0.008856 )
            z = (float)Math.pow(z,3);
        else
            z = (float)(( z - 16 / 116 ) / 7.787);
        
        xyz[0] = x * tristimulus[0];
        xyz[1] = y * tristimulus[1];
        xyz[2] = z * tristimulus[2];
        
        return xyz;
    }
    
    /**
     * RGB -> C1C2C3.
     * @param r Red coefficient. Values in the range [0..255].
     * @param g Green coefficient. Values in the range [0..255].
     * @param b Blue coefficient. Values in the range [0..255].
     * @return C1C2C3 color space.
     */
    public static float[] RGBtoC1C2C3(int r, int g, int b){
        
        float[] c = new float[3];
        
        c[0] = (float)Math.atan(r / Math.max(g, b));
        c[1] = (float)Math.atan(g / Math.max(r, b));
        c[2] = (float)Math.atan(b / Math.max(r, g));
        
        return c;
        
    }
    
    /**
     * RGB -> O1O2.
     * @param r Red coefficient. Values in the range [0..255].
     * @param g Green coefficient. Values in the range [0..255].
     * @param b Blue coefficient. Values in the range [0..255].
     * @return O1O2 color space.
     */
    public static float[] RGBtoO1O2(int r, int g, int b){
        
        float[] o = new float[2];
        
        o[0] = (r - g) / 2f;
        o[1] = (r + g) / 4f - (b / 2f);
        
        return o;
        
    }
    
    /**
     * RGB -> Grayscale.
     * @param r Red coefficient. Values in the range [0..255].
     * @param g Green coefficient. Values in the range [0..255].
     * @param b Blue coefficient. Values in the range [0..255].
     * @return Grayscale color space.
     */
    public static float RGBtoGrayscale(int r, int g, int b){
        
        return r*0.2125f + g*0.7154f + b*0.0721f;
        
    }

    /**
     * XYZ -> Philips Hue XY
     * @param x X coefficient.
     * @param y Y coefficient.
     * @param z Z coefficient.
     * @return Hue xy array
     */
    public static XYColorSpace XYZtoXY(float x, float y, float z){
        float[] xy = new float[2];
        
        xy[0] = x / (x + y + z);
        xy[1] = y / (x + y + z);
        
        XYColorSpace xyColor = new XYColorSpace();
        xyColor.setBrightness((int)Math.round(y * 254.0f));
        xyColor.setXy(xy);
        return xyColor;
    }
    
    /**
     * Philips Hue XY -> XYZ
     * @param x X coefficient.
     * @param y Y coefficient.
     * @return XYZ array
     */
    public static float[] XYtoXYZ(XYColorSpace xy){
        float[] xyz = new float[3];
        /* Old Way
        xyz[0] = (xy.getBrightnessAdjusted() / xy.getXy()[1]) * xy.getXy()[0];
        xyz[1] = xy.getBrightnessAdjusted();
        xyz[2] = (xy.getBrightnessAdjusted() / xy.getXy()[1]) * (1.0f - xy.getXy()[0] - xy.getXy()[1]);
        */
        // New Way
        xyz[0] = xy.getXy()[0] * (xy.getBrightnessAdjusted() / xy.getXy()[1]) ;
        xyz[1] = xy.getBrightnessAdjusted();
        xyz[2] = (float) ((1.0 - xy.getXy()[0] - xy.getXy()[1]) * (xy.getBrightnessAdjusted() / xy.getXy()[1]));

        return xyz;
    }

    public static int[] normalizeRGB(int[] rgb) {
        int[] newRGB = new int[3];

        newRGB[0] = assureBounds(rgb[0]);
        newRGB[1] = assureBounds(rgb[1]);
        newRGB[2] = assureBounds(rgb[2]);


        return newRGB;
    }

	private static int assureBounds(int value) {
		if (value < 0.0) {
			value = 0;
		}
		if (value > 255.0) {
			value = 255;
		}
		return value;
	}

}