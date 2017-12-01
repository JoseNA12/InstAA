package cr.ac.tec.aa.proyecto_1_aa;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.ByteArrayOutputStream;


/**
 * Created by jose_ on 2/9/2017.
 */


public class Convolution {


    /*public static double[][] GaussMatrix = { { 0.0113437, 0.083819, 0.011343 },
                                       { 0.0838195, 0.619347, 0.083819},
                                       { 0.0113437, 0.083819, 0.011343 } };*/

    /*public static double[][] GaussMatrix = {
            {0.0625,0.125,0.0625},
            {0.125,0.25,0.125},
            {0.0625,0.125,0.0625},
    };*/

    /*public static double[][] GaussMatrix =  { { 0.002969f, 0.01330f, 0.021938f, 0.01330f, 0.00296f },
            { 0.013306f, 0.05963f, 0.098320f, 0.05963f, 0.01330f },
            { 0.021938f, 0.09832f, 0.163102f, 0.09832f, 0.02193f },
            { 0.013306f, 0.05963f, 0.098320f, 0.05963f, 0.01330f },
            { 0.002969f, 0.01330f, 0.021938f, 0.01330f, 0.00296f } };*/


    public static double[][] GaussMatrix = { { 0.00134196f, 0.004075f, 0.007939f, 0.009915f, 0.007939f, 0.0040765f, 0.001341f },
            { 0.00407653f, 0.012383f, 0.024119f, 0.030121f, 0.024119f, 0.0123834f, 0.004076f },
            { 0.00793999f, 0.024119f, 0.046978f, 0.058669f, 0.046978f, 0.0241195f, 0.007939f },
            { 0.00991585f, 0.030121f, 0.058669f, 0.073268f, 0.058669f, 0.0301217f, 0.009915f },
            { 0.00793999f, 0.024119f, 0.046978f, 0.058669f, 0.046978f, 0.0241195f, 0.007939f },
            { 0.00407653f, 0.012383f, 0.024119f, 0.030121f, 0.024119f, 0.0123834f, 0.004076f },
            { 0.00134196f, 0.004076f, 0.007939f, 0.009915f, 0.007939f, 0.0040765f, 0.001341f } };

    public static double[][] MercaPropiaMatrix = { { -1, 0, 1 }, { -2, 0, 2}, { -1, 0, 1 } };

    public static int[][] MatrizEdgeDetection = {{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};

    public static int[][] MatrizSharpen = {{0, -1, 0}, {-1, 8, -1}, {-1, -1, -1}};


    private static int kernelLength_Gauss = GaussMatrix.length;
    //private static int kernelSize_Gauss = kernelLength_Gauss * kernelLength_Gauss;
    private static int mitad_Gauss = kernelLength_Gauss / 2;

    private static int kernelLength_MercaPropia = MercaPropiaMatrix.length;
    private static int mitad_MercaPropia = kernelLength_MercaPropia / 2;


    public static Bitmap Compute_GrayScale(Bitmap originalImage, MainActivity.imageFilters pImageFilter)
    {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Bitmap modifiedImage = Bitmap.createBitmap(width, height, originalImage.getConfig());

        int R, G, B, grayValue = 0;

        int imageSize = width * height; // Obtener ancho x alto
        int[] arrayPixels = new int [imageSize]; // Crear un array del tamaño de la imagen
        originalImage.getPixels(arrayPixels, 0, width, 0, 0, width, height); // Inicializaro el array con los pixels

        int i = 0; int j = 0; // Indices para trabajar en la nueva imagen

        for(int pIj = 0; pIj < imageSize; pIj++) // Hasta que pIj sea igual al ij (ancho x alto) de la imagen, haga:
        {
            if(i ==  width){ // Bajar al pixel según la altura
                i = 0;  // Si i es igual al ancho, quiere decir que ya se recorrio el ancho segun la altura del indice j
                j++;    // Se reinicia el i para empezar desde el principio en cuanto al ancho en un nuevo valor de j
            }

            int arrayIndex = (j * width) + i; // Obtener la posicion del pixel
            R = (arrayPixels[arrayIndex] >> 16) & 0xff;
            G = (arrayPixels[arrayIndex] >> 8) & 0xff; // Magia
            B = arrayPixels[arrayIndex] & 0xff;

            // Decidir cuál filtro aplicar
            if (pImageFilter.equals(MainActivity.imageFilters.Averaging))
            {
                grayValue = (R + G + B) / 3;
            }
            else
            {
                if (pImageFilter.equals(MainActivity.imageFilters.Desaturation))
                { // Gray = ( Max(Red, Green, Blue) + Min(Red, Green, Blue) ) / 2
                    int valueMax =  GetMaxValue(R, G, B);
                    int valueMin = GetMinValue(R, G, B);

                    grayValue = (valueMax + valueMin) / 2;
                }
                else
                {
                    if (pImageFilter.equals(MainActivity.imageFilters.Decomposition_Max))
                    {
                        grayValue = GetMaxValue(R, G, B);
                    }
                    else
                    {
                        if (pImageFilter.equals(MainActivity.imageFilters.Decomposition_Min))
                        {
                            grayValue = GetMinValue(R, G, B);
                        }
                    }
                }
            }
            i++;
            arrayPixels[arrayIndex] = 0xff000000 | (grayValue << 16) | (grayValue << 8) | (grayValue); // set
        }
        modifiedImage.setPixels(arrayPixels, 0, width, 0, 0, width, height);
        arrayPixels = null;
        // Final image
        return modifiedImage;
    }

    /**
     * Stack Blur v1.0 from
     * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
     * Java Author: Mario Klingemann <mario at quasimondo.com>
     * http://incubator.quasimondo.com
     *
     * created Feburary 29, 2004
     * Android port : Yahel Bouaziz <yahel at kayenko.com>
     * http://www.kayenko.com
     * ported april 5th, 2012
     *
     * This is a compromise between Gaussian Blur and Box blur
     * It creates much better looking blurs than Box Blur, but is
     * 7x faster than my Gaussian Blur implementation.
     *
     * I called it Stack Blur because this describes best how this
     * filter works internally: it creates a kind of moving stack
     * of colors whilst scanning through the image. Thereby it
     * just has to add one new block of color to the right side
     * of the stack and remove the leftmost color. The remaining
     * colors on the topmost layer of the stack are either added on
     * or reduced by one, depending on if they are on the right or
     * on the left side of the stack.
     *
     * If you are using this algorithm in your code please add
     * the following line:
     * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     */
    public static Bitmap Compute_GaussianBlur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap modifiedImage = sentBitmap.copy(sentBitmap.getConfig(), true); // Nueva imagen reducida (copia)

        if (radius < 1) {
            return (null);
        }

        int w = modifiedImage.getWidth();
        int h = modifiedImage.getHeight(); // Valores del nuevo bitmap

        int[] pix = new int[w * h]; // Array del alto por ancho de la imagen trabajar

        modifiedImage.getPixels(pix, 0, w, 0, 0, w, h); // Set de los pixles "en blanco" segun el alto y ancho

        int wm = w - 1; // Padding
        int hm = h - 1;
        int wh = w * h; // Alto por ancho
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) { // Alto de la imagen reducida: n

            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;

            for (i = -radius; i <= radius; i++) { // Radio, intendad del filtro: r
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) { // Ancho de la imagen reducida: m

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) { // Ancho de la imagen reducida: m
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) { // Radio, intendad del filtro: r
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) { // Alto de la imagen reducida: n
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        modifiedImage.setPixels(pix, 0, w, 0, 0, w, h);

        return (modifiedImage);
    }


    public static Bitmap ComputeConvolution_MercaPropia(Bitmap originalImage)
    {
        int width = Math.round(originalImage.getWidth() * 0.5f);
        int height = Math.round(originalImage.getHeight() * 0.5f);
        originalImage = Bitmap.createScaledBitmap(originalImage, width, height, false);

        Bitmap modifiedImage = originalImage.copy(originalImage.getConfig(), true);

        width = modifiedImage.getWidth();
        height = modifiedImage.getHeight();

        /*int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Bitmap modifiedImage = Bitmap.createBitmap(width, height, originalImage.getConfig());*/

        int i = 0, j = 0, R, G, B, m, mm, n, nn, ii, jj, new_pixel = 0;

        int imageSize = width * height; // Obtener ancho x alto
        int[] arrayPixels = new int[imageSize]; // Crear un array del tamaño de la imagen
        modifiedImage.getPixels(arrayPixels, 0, width, 0, 0, width, height); // Inicializar el array con los pixels

        for (int pIj = 0; pIj < imageSize; pIj++) // Imagen
        {
            if (i == width) { // Bajar al pixel según la altura
                i = 0;  // Si i es igual al ancho, quiere decir que ya se recorrio el ancho segun la altura del indice j
                j++;    // Se reinicia el i para empezar desde el principio en cuanto al ancho en un nuevo valor de j
            }

            int arrayIndex = (j * width) + i; // Obtener la posicion del pixel

            R = (66 - (arrayPixels[arrayIndex] >> 16) & 0xff);
            G = (69 - (arrayPixels[arrayIndex] >> 8) & 0xff); // Magia
            B = (99 - arrayPixels[arrayIndex] & 0xff);

            arrayPixels[arrayIndex] = 0xff000000 | (new_pixel<<16) | (new_pixel<<8) | new_pixel;

            for (m = 0; m < kernelLength_MercaPropia; ++m) // Filas del Kernel. 3 -> tamaño del kernel
            {
                mm = kernelLength_MercaPropia - 1 - m; // Indice de la fila del kernel alrevez

                for (n = 0; n < kernelLength_MercaPropia; ++n) // Columnas del kernel
                {
                    nn = kernelLength_MercaPropia - 1 - n; // Indice de la columna del kernel alrevez

                    ii = i + (m - mitad_MercaPropia);
                    jj = j + (n - mitad_MercaPropia);

                    // Validar limites de la imagen (Padding)
                    if (ii >= 0 && ii < width && jj >= 0 && jj < height)
                    {
                        R += (int) (((arrayPixels[arrayIndex] >> 16) & 0xff) * MercaPropiaMatrix[mm][nn]);
                        G += (int) (((arrayPixels[arrayIndex] >> 8) & 0xff) * MercaPropiaMatrix[mm][nn]);
                        B += (int) ((arrayPixels[arrayIndex] & 0xff) * MercaPropiaMatrix[mm][nn]);
                    }
                }
            }
            i++;
            arrayPixels[arrayIndex] = 0xff000000 | (R << 16) | (G << 8) | B;
        }

        modifiedImage.setPixels(arrayPixels, 0, width, 0, 0, width, height);

        return modifiedImage; // Bitmap
    }

    private static int GetMaxValue(int R, int G, int B)
    {
        return Math.max(R, Math.max(G, B));
    }

    private static int GetMinValue(int R, int G, int B)
    {
        return Math.min(R, Math.min(G, B));
    }



}
