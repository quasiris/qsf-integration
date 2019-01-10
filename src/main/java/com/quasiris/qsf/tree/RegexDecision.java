package com.quasiris.qsf.tree;

import java.util.regex.Pattern;

public class RegexDecision {

    private Pattern regex;

    private String name;
    private String description;

    private String profile;

    public RegexDecision() {
    }

    public RegexDecision(String name, String regex) {
        this.name = name;
        this.setRegex(regex);
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = Pattern.compile(regex);
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return "RegexDecision{" +
                "regex=" + regex +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
