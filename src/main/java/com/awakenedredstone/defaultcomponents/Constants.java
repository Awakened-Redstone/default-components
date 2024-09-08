package com.awakenedredstone.defaultcomponents;

import blue.endless.jankson.JsonGrammar;

public class Constants {
    public static final JsonGrammar GRAMMAR = JsonGrammar.builder()
      .withComments(true)
      .printTrailingCommas(false)
      .printWhitespace(true)
      .printUnquotedKeys(false)
      .bareSpecialNumerics(true)
      .build();
}
