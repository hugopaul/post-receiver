package com.post.receiver.domain;

public enum SourceSite {
    ACORDA_DF("acordadf", "Acorda DF"),
    DF_MOBILIDADE("dfmobilidade", "DF Mobilidade");

    private final String code;
    private final String displayName;

    SourceSite(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }
}
