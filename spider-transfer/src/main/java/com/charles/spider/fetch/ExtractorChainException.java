package com.charles.spider.fetch;

public class ExtractorChainException extends Exception {

    private Behaviour behaviour;

    public ExtractorChainException(Behaviour behaviour) {
        this.behaviour = behaviour;
    }


    public Behaviour result() {
        return behaviour;
    }


}
