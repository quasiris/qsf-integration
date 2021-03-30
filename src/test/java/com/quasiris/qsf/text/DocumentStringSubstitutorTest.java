package com.quasiris.qsf.text;

import com.quasiris.qsf.response.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentStringSubstitutorTest {

    @Test
    void replaceSimpleValue() {
        Document document = new Document();
        document.setValue("foo", "bar");

        DocumentStringSubstitutor documentStringSubstitutor = new DocumentStringSubstitutor(document);

        String actual = documentStringSubstitutor.replace("This is a test for ${foo}");
        assertEquals("This is a test for bar", actual);
    }
}