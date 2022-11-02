package me.shortint.cwlang.parser;

public class OpPrec {
    int prec;
    OpAssoc assoc;

    public OpPrec(int prec, OpAssoc assoc) {
        this.prec = prec;
        this.assoc = assoc;
    }
}
