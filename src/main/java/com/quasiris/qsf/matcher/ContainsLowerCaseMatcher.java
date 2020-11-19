package com.quasiris.qsf.matcher;

public class ContainsLowerCaseMatcher implements Matcher {


    @Override
    public boolean matches(String left, String right) {
        if(left == null || right == null) {
            return false;
        }
        return left.toLowerCase().contains(right.toLowerCase());
    }
}
