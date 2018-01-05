package de.quasiris.qsf.text;

/**
 * Created by mki on 17.12.17.
 */
public class TextUtils {


    public static String replaceGermanUmlaut(String input) {

        //replace all lower Umlauts
        String output =
                input
                        .replaceAll("ü", "ue")
                        .replaceAll("ö", "oe")
                        .replaceAll("ä", "ae")
                        .replaceAll("ß", "ss");

        //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        output =
                output
                        .replaceAll("Ü(?=[a-zäöüß ])", "Ue")
                        .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                        .replaceAll("Ä(?=[a-zäöüß ])", "Ae");

        //now replace all the other capital umlaute
        output =
                output
                        .replaceAll("Ü", "UE")
                        .replaceAll("Ö", "OE")
                        .replaceAll("Ä", "AE");

        return output;
    }

    public static boolean isGermanPostalCode(String token) {
        return token.matches("\\d{5}");
    }
}
