package cr.ac.tec.aa.proyecto_1_aa;

/**
 * Created by jose_ on 8/9/2017.
 */

public class Chronometer {

    private java.util.Date t_inicio, t_final;

    public void InitTime() {
        t_inicio = new java.util.Date();
    }

    public long GetTime() {
        t_final = new java.util.Date();
        return (t_final.getTime() - t_inicio.getTime());
    }

    public static String ToString(long time){
        String timeStr = Long.toString(time);
        String newTimeStr = "";

        int l = timeStr.length();

        for(int j = 0; j < timeStr.length(); j++){
            if(l == 3){
                newTimeStr += ".";
            }
            l--;
            newTimeStr += timeStr.charAt(j);
        }

        String unit = "";
        if(newTimeStr.length() >= 5){unit = " segundos.";}
        else{
            if(newTimeStr.length() <= 4){unit = " milisegundos.";}
        }

        return newTimeStr + unit;
    }
}
