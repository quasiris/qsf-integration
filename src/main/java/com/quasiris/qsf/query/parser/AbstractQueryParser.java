package com.quasiris.qsf.query.parser;

import com.quasiris.qsf.query.PosTag;
import com.quasiris.qsf.query.Token;

import java.util.List;

public abstract class AbstractQueryParser {


    private Token current;
    private Token last;
    private Token next;

    private int pos = 0;

    private int tokenCount;

    private List<Token> tokens;

    public AbstractQueryParser() {
    }

    public AbstractQueryParser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokenCount = tokens.size();
    }

    public void parse() {
        if(tokens.isEmpty()) {
            return;
        }

        init();
        processNext();
        while(pos + 1 < tokenCount) {
            next();
            processNext();
        }

        end();

    }

    protected abstract void processNext();


    protected void end() {

    }

    protected void init() {
        current = tokens.get(pos);
        int nextPos = pos + 1;
        if(nextPos < tokenCount) {
            next = tokens.get(nextPos);
        }
    }

    private void next() {
        pos++;
        last = getTokens().get(pos -1);
        current = getTokens().get(pos);

        int nextPos = pos + 1;
        if(nextPos < tokenCount) {
            next = tokens.get(nextPos);
        } else {
            next = null;
        }
    }

    public Token getCurrent() {
        return current;
    }

    public void setCurrent(Token current) {
        this.current = current;
    }

    public Token getLast() {
        return last;
    }

    public void setLast(Token last) {
        this.last = last;
    }

    public Token getNext() {
        return next;
    }

    public void setNext(Token next) {
        this.next = next;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
        this.tokenCount = tokens.size();
    }

    public boolean isPosTag(PosTag posTag) {
        return posTag.isValue(getCurrent().getPosTag());
    }

    public boolean isOneOfPosTag(PosTag... posTags) {
        for (PosTag posTag: posTags) {
            if(posTag.isValue(getCurrent().getPosTag())) {
                return true;
            }
        }
        return false;
    }

    public boolean isNextPosTag(PosTag posTag) {
        if(getNext() == null) {
            return false;
        }
        return posTag.isValue(getNext().getPosTag());
    }

    public void increasePos(int inc) {
        this.pos = pos + inc -1;
        if(this.pos >= getTokenCount() ) {
            this.pos = getTokenCount() - 1;
        }
    }

}
