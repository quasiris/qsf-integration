package com.quasiris.qsf.text;

import com.quasiris.qsf.response.Document;
import org.apache.commons.text.StringSubstitutor;

/**
 *  Created by tbl on 30.03.21.
 *
 *  Replaces all variables in a string with the values of the document.
 */
public class DocumentStringSubstitutor {

    private Document document;

    public DocumentStringSubstitutor(Document document) {
        this.document = document;
    }


    public String replace(final String source) {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(document.getDocument());
        String replaced  = stringSubstitutor.replace(source);
        return replaced;
    }

}
