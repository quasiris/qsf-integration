package com.quasiris.qsf.pipeline.filter.elastic.spellcheck;

import com.quasiris.qsf.pipeline.filter.elastic.Score;
import com.quasiris.qsf.pipeline.filter.elastic.SpellCheckToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpellcheckVariants {

    public List<Score> computeVariants(List<SpellCheckToken> spellCheckTokens ) {

        List<Score> correctedQueryVariants = new ArrayList<>();
        correctedQueryVariants.add(new Score("", 0.0));
        for (SpellCheckToken spellCheckToken : spellCheckTokens) {
            correctedQueryVariants = computeVariants(spellCheckToken, correctedQueryVariants);
        }
        correctedQueryVariants.sort(Comparator.comparing(Score::getScore).reversed());

        return correctedQueryVariants;
    }

    List<Score> computeVariants(SpellCheckToken spellCheckToken, List<Score> spellcheckVariants) {
        List<Score> extendedCorrectedQueryVariants = new ArrayList<>();
        List<Score> correctedVariants = spellCheckToken.getCorrectedVariants();

        if(correctedVariants == null) {
            correctedVariants = new ArrayList<>();
            correctedVariants.add(new Score(spellCheckToken.getToken().getValue(), 0.0));

        }
        for(Score score : correctedVariants) {
            for(Score spellcheckVariant : spellcheckVariants) {
                extendedCorrectedQueryVariants.add(
                        new Score(
                                spellcheckVariant.getText() + " " + score.getText(),
                                spellcheckVariant.getScore() + score.getScore()));
            }
        }

        return extendedCorrectedQueryVariants;
    }
}
