package com.quasiris.qsf.text;

import java.util.regex.Pattern;

/**
 * Created by mki on 17.12.17.
 */
@Deprecated // move to qsf-commons
public class TextUtils {


    public static Pattern NUMBER_PATTERN = Pattern.compile( "[0-9]" );

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

    public static boolean containsNumber( String value ) {
        return NUMBER_PATTERN.matcher(value).find();
    }
}
