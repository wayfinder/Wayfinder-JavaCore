/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
/*
  This class must always be compiled even if we don't use the
  vectormaps because we reference it from wmmg.data.Language.
*/

package com.wayfinder.core.shared.internal.settings.language;

/**
   Languages in the MapLib tilemap maps are represented by numbers.

   see mc2:Shared/include/LangTypes.h
   and mc2:Shared/src/LangTypes.cpp

   We don't use LangTypes::languageAsString from
   mc2:Shared/src/LangTypes.cpp
   since that table
   contains errors with respect to the iso-639 standards and there is
   not really a need to obscure the int values from the caller. We
   can't change them anyway (used in the protocol) and we don't want
   extensive string processing in the app.

   ISO-639-1: 2002 Codes for the representation of names of languages
   -- Part 1: Alpha-2 code

   ISO 639-2: 1998 Codes for the representation of names of languages
   -- Part 2: Alpha-3 code List of ISO 639-2 codes

   ISO 639-3: 2007 Codes for the representation of names of languages
   -- Part 3: Alpha-3 code for comprehensive coverage of languages.

   ISO 639-3 is an extension of ISO 639-2. It is a superset of the
   individual languages in ISO 639-2 but ISO 639-2 also contains
   language collections.
*/
public final class LangTypes {
    
    private LangTypes() {}
    
    //Languages
    //Maintain the order.
    /**
       English (UK), ISO-639-1: en ISO-639-3: eng
     */
    public static final int ENGLISH = 0;

    /**
       Swedish, ISO-639-1: sv ISO-639-3: swe
     */
    public static final int SWEDISH = 1;

    /**
       German, ISO-639-1: de ISO-639-3: deu
     */
    public static final int GERMAN = 2;

    /**
       Danish, ISO-639-1: da ISO-639-3: dan
     */
    public static final int DANISH = 3;

    /**
       Italian, ISO-639-1: it ISO-639-3: ita
     */
    public static final int ITALIAN = 4;

    /**
       Dutch, ISO-639-1: nl ISO-639-3: nld
     */
    public static final int DUTCH = 5;

    /**
       Spanish, ISO-639-1: es ISO-639-3: spa
     */
    public static final int SPANISH = 6;

    /**
       French, ISO-639-1: fr ISO-639-3: fra
     */
    public static final int FRENCH = 7;

    /**
       Welch ("Cymraeg"), ISO-639-1: cy ISO-639-2: wel/cym ISO-639-3: cym 
     */
    public static final int WELSH = 8;

    /**
       Finnish, ISO-639-1: fi ISO-639-3: fin
     */
    public static final int FINNISH = 9;

    /**
       Norwegian (we don't distinguish between Bokmaal (nob) and
       Nynorsk (nno)), ISO-639-1: no ISO-639-3: nor (macrolanguage)
     */
    public static final int NORWEGIAN = 10;

    /**
       Portuguese, ISO-639-1: pt ISO-639-3: por
     */
    public static final int PORTUGUESE = 11;

    /**
       US English, There is no ISO code for English. "ame" is Yanesha
       spoken by a small (10000 people) minority in Peru. "usa" is
       Usarufa spoken by a minority in New Guinea.

       Using IANA subtags: en-US.
     */
    public static final int US_ENGLISH = 12;

    /**
       Czech, ISO-639-1: cs ISO-639-3: ces
     */
    public static final int CZECH = 13;

    /**
       Albanian, ISO-639-1: sq ISO-639-3: sqi (macrolanguage,
       individual languages: aae, aat, aln, als)
     */
    public static final int ALBANIAN = 14;

    /**
       Basque, ISO-639-1: eu ISO-639-3: eus 
     */
    public static final int BASQUE = 15;

    /**
       Catalan, ISO-639-1: ca ISO-639-3: cat 
     */
    public static final int CATALAN = 16;

    /**
       (Western) Frisian, ISO-639-1: fy ISO-639-3: fry
     */
    public static final int FRISIAN = 17;

    /**
       Irish, ISO-639-1: ga ISO-639-3: gle
     */
    public static final int IRISH = 18;

    /**
       Galician, ISO-639-1: gl ISO-639-3: glg
     */
    public static final int GALICIAN = 19;

    /**
       Luxembourgish ("Letzebuergesch"), ISO-639-1: lb ISO-639-3: ltz
     */
    public static final int LUXEMBOURGISH = 20;

    /**
       Raeto-Romance ("rumantsch grischun"), ISO-639-1: rm ISO-639-3: roh
     */
    public static final int RAETO_ROMANCE = 21;

    /**
       Serbo-Croatian, ISO-639-1: sh ISO-639-3: hbs (macrolanguage -
       bos=Bosnian, hrv=Croatian, srp=Serbian)
     */
    public static final int SERBO_CROATIAN = 22;

    /**
       Slovenian, ISO-639-1: sl ISO-639-3: slv 
     */
    public static final int SLOVENIAN = 23;

    /**
       Valencian (variant of Catalan), ISO-639-1: ? ISO-639-3: ?
       ("val" is Vehes, spoken in the Morobe Province in Papua New
       Guinea)
     */
    public static final int VALENCIAN = 24;

    /**
       Hungarian ("Magyar"), ISO-639-1: hu ISO-639-3: hun 
     */
    public static final int HUNGARIAN = 25;

    /**
       Greek (we mean modern Greek with current Greek letters, not
       transcribed to latin alphabet) , ISO-639-1: el ISO-639-3: ell
     */
    public static final int GREEK = 26;

    /**
       Polish, ISO-639-1: pl ISO-639-3: pol
     */
    public static final int POLISH = 27;

    /**
       Slovak, ISO-639-1: sk ISO-639-3: slk
     */
    public static final int SLOVAK = 28;

    /**
       Russian, ISO-639-1: ru ISO-639-3: rus
     */
    public static final int RUSSIAN = 29;

    /**
       Greek with latin syntax (script?). There are no iso codes for
       this.  Using IANA subtags: el-Latn
     */
    public static final int GREEK_LATIN_STX = 30;

    /**
     * Reserved, don't use. It is NOT equal to the total number of
     * languages.
     */
    public static final int INVALID_LANGUAGE = 31;

    /**
       Russian with latin syntax (script?), There are no iso codes for
       this.  Using IANA subtags: ru-Latn
     */
    public static final int RUSSIAN_LATIN_STX = 32;

    /**
       Turkish, ISO-639-1: tr ISO-639-3: tur
     */
    public static final int TURKISH = 33;

    /**
     * Arabic ISO-639-1: ar ISO-639-3: ara (macrolanguage, 30
     * individual codes)
     */
    public static final int ARABIC = 34;

    /**
     * Chinese Simplified script (zh-Hans). Falls back to Traditional
     * Chinese if Simplified is not available.
     *
     * Previously (before w822) Chinese without preference.
     *
     * TeleAtlas: CHI TA still uses this as Chinese without script
     * info but TA only provides one type of Chinese in each map.
     */
    public static final int ZH_HANS_WITH_HANT_FALLBACK = 35;

    /**
     * Chinese transcribed into English. This is likely not pin yin
     * but more of word to word translations, "West Yellow Road"
     *
     * TeleAtlas: CHL
     */
    public static final int CHINESE_LATIN_STX = 36;

    /**
     * Estonian, ISO-639-1: et ISO-639-3: est
     * TeleAtlas: EST
     */
    public static final int ESTONIAN = 37;

    /**
     * Latvian, ISO-639-1: lv ISO-639-3: lav
     * TeleAtlas: LAV 
     */
    public static final int LATVIAN = 38;

    /**
     * Lithuanian, ISO-639-1: lt ISO-639-3: lit
     * TeleAtlas: 
     */
    public static final int LITHUANIAN = 39;

    /**
     * Thai, ISO-639-1: th ISO-639-3: tha
     * TeleAtlas: THA
     */
    public static final int THAI = 40;

    /**
     * Bulgarian, ISO-639-1: bg ISO-639-3: bul
     * TeleAtlas: BUL
     */
    public static final int BULGARIAN = 41;

    /**
     * Cyrillic transcript. Used by AND when printing cyrillic names
     * with latin characters.
     *
     * No iso code
     */
    public static final int CYRILLIC_TRANSCRIPT = 42;

    /**
     * Indonesian, ISO-639-1: id ISO-639-3: ind
     * TeleAtlas: IND
     */
    public static final int INDONESIAN = 43;

    /**
     * Malay, ISO-639-1: ms ISO-639-3: several, generic code is msa
     * TeleAtlas: MAY
     *
     * may is iso-639-2 type B for Malay
     */
    public static final int MALAY = 44;

    /**
     *
     */
    public static final int ICELANDIC = 45;

    /**
     *
     */
    public static final int JAPANESE = 46;

    /**
     * Amharic, ISO-639-1: am ISO-639-3: amh
     *
     * Amharic is a Semitic language spoken in North Central Ethiopia
     * by the Amhara. It is the second most spoken Semitic language in
     * the world, after Arabic, and the "official working" language of
     * the Federal Democratic Republic of Ethiopia. (en.wikipedia.org)
     */
    public static final int AMHARIC = 47;

    /**
     *
     */
    public static final int ARMENIAN = 48;

    /**
     *
     */
    public static final int TAGALOG = 49;

    /**
     * Belarusian (cyrillic)
     */
    public static final int BELARUSIAN = 50;

    /**
     *
     */
    public static final int BENGALI = 51;

    /**
     *
     */
    public static final int BURMESE = 52;

    /**
     *
     */
    public static final int CROATIAN = 53;

    /**
     * Persian/Farsi ISO-639-1: fa ISO-639-3: fas etc.
     *
     * Iran, Afghanistan, Tajikistan.
     */
    public static final int FARSI = 54;

    /**
     *
     */
    public static final int GAELIC = 55;

    /**
     *
     */
    public static final int GEORGIAN = 56;

    /**
     * Gujarati ISO-639-1: gu ISO-639-3: guj
     *
     * s an Indo-Aryan language, and part of the greater Indo-European
     * language family. It is native to the Indian state of Gujarat,
     * and is its chief language, as well as of the adjacent union
     * territories of Daman and Diu and Dadra and Nagar Haveli. There
     * are about 46 million speakers of Gujarati worldwide, making it
     * the 26th most spoken native language in the world.
     * (http://en.wikipedia.org/wiki/Gujarati_language)
     */
    public static final int GUJARATI = 57;

    /**
     *
     */
    public static final int HEBREW = 58;

    /**
     *
     */
    public static final int HINDI = 59;

    /**
     * Kannada ISO-639-1: kn ISO-639-3: kan
     *
     * Kannada is one of the major Dravidian languages of India,
     * spoken predominantly in the southern state of Karnataka. It is
     * the 27th most spoken language in the world.
     * (http://en.wikipedia.org/wiki/Kannada)
     */
    public static final int KANNADA = 60;

    /**
     *
     */
    public static final int KAZAKH = 61;

    /**
     *
     */
    public static final int KHMER = 62;

    /**
     *
     */
    public static final int KOREAN = 63;

    /**
     * Lao. Official language of Laos.
     */
    public static final int LAO = 64;

    /**
     * Macedonian (cyrillic)
     */
    public static final int MACEDONIAN = 65;

    /**
     * Malayalam ISO-639-1: ml ISO-639-3: mal
     *
     * Malayalam is the language spoken predominantly in the state of
     * Kerala, in southern India. It is one of the 22 official
     * languages of India, spoken by around 37 million people.
     * (http://en.wikipedia.org/wiki/Malayalam)
     */
    public static final int MALAYALAM = 66;

    /**
     * Marathi ISO-639-1: mr ISO-639-3: mar
     *
     * Marathi is an Indo-Aryan language spoken by the Marathi people
     * of what is considered western India. It is the official
     * language of the state of Maharashtra. There are 90 million
     * fluent speakers worldwide. Marathi is the 4th most spoken
     * language in India[8] and the 15th most spoken language in
     * world.
     * (http://en.wikipedia.org/wiki/Marathi)
     */
    public static final int MARATHI = 67;

    /**
     *
     */
    public static final int MOLDAVIAN = 68;

    /**
     *
     */
    public static final int MONGOLIAN = 69;

    /**
     *
     */
    public static final int PUNJABI = 70;

    /**
     * Romanian ISO-639-1: ro ISO-639-3: ron
     */
    public static final int ROMANIAN = 71;

    /**
     *
     */
    public static final int SERBIAN = 72;

    /**
     *
     */
    public static final int SINHALESE = 73;

    /**
     *
     */
    public static final int SOMALI = 74;

    /**
     *
     */
    public static final int SWAHILI = 75;

    /**
     *
     */
    public static final int TAMIL = 76;

    /**
     * Telugu ISO-639-1: te ISO-639-3: tel
     *
     * Telugu is a Dravidian language (South-Central Dravidian
     * languages) mostly spoken in the Indian state of Andhra Pradesh,
     * where it is the official language. Sanskrit has a huge
     * influence on it. ...  Telugu is one of the top fifteen most
     * widely spoken languages in the world as well as the most spoken
     * language within the Dravidian family. It is widely spoken
     * outside of Andhra Pradesh in cities of neighboring states such
     * as Bangalore and Chennai.
     * (http://en.wikipedia.org/wiki/Telugu)
     */
    public static final int TELUGU = 77;

    /**
     *
     */
    public static final int TIBETAN = 78;

    /**
     * Tigrinya ISO-639-1: ti ISO-639-3: tir
     *
     * Tigrinya, also spelled Tigrigna, Tigrina, less commonly
     * Tigrinian, Tigrinyan, is a Semitic language spoken by the
     * Tigray-Tigrinya people in central Eritrea
     */
    public static final int TIGRINYA = 79;

    /**
     *
     */
    public static final int TURKMEN = 80;

    /**
     *
     */
    public static final int UKRAINIAN = 81;

    /**
     *
     */
    public static final int URDU = 82;

    /**
     *
     */
    public static final int VIETNAMESE = 83;

    /**
     *
     */
    public static final int ZULU = 84;

    /**
     *
     */
    public static final int SESOTHO = 85;

    /**
     *
     */
    public static final int BULGARIAN_LATIN_STX = 86;

    /**
     *
     */
    public static final int BOSNIAN = 87;

    /**
     *
     */
    public static final int SLAVIC = 88;

    /**
     *
     */
    public static final int BELARUSIAN_LATIN_STX = 89;

    /**
     *
     */
    public static final int MACEDONIAN_LATIN_STX = 90;

    /**
     *
     */
    public static final int SERBIAN_LATIN_STX = 91;

    /**
     *
     */
    public static final int UKRAINIAN_LATIN_STX = 92;

    /**
     *
     */
    public static final int MALTESE = 93;

    /**
     * Chinese Traditional script (zh-Hant). Falls back to Simplified
     * Chinese if Traditional is not available.
     *
     * No Tele Atlas code.
     */
    public static final int ZH_HANT_WITH_HANS_FALLBACK = 94;

    /**
     * Chinese Traditional script Hong Kong variant (zh-Hant-HK). Falls
     * back to Simplified Chinese if Traditional is not available.
     *
     * No Tele Atlas code.
     *
     * From LangTypes.h r 1.35
     */
    public static final int ZH_HANT_HK_WITH_HANS_FALLBACK = 95;


    /**
     * mapping between the language number and strings.
     *
     * Currently, the only string recorded, is the string used as
     * language code for TileMapFormatDesc names. This string is an
     * internal Wayfinder convention where most of the codes are taken
     * from ISO-639-2.
     *
     * see LangTypes::languageAsString[][0] in
     * mc2:Shared/src/LangTypes.cpp
     */
    static final String[] LANGUAGE_STRINGS = {
        "eng",
        "swe",
        "ger",
        "dan",
        "ita",
        "dut",
        "spa",
        "fre",
        "wel",
        "fin",
        "nor",
        "por",
        "ame",
        "cze",
        "alb",
        "baq",
        "cat",
        "fry",
        "gle",
        "glg",
        "ltz",
        "roh",
        "scr",
        "slv",
        "val",
        "hun",
        "gre",
        "pol",
        "slo",
        "rus",
        "grl",
        "invalidLanguage",
        "rul",
        "tur",
        "ara",
        "chi",
        "chl",
        "est",
        "lav",
        "lit",
        "tha",
        "bul",
        "cyt",
        "ind",
        "may",
        "isl",
        "jpn",
        "amh",
        "hye",
        "tgl",
        "bel",
        "ben",
        "mya",
        "hrv",
        "fas",
        "gla",
        "kat",
        "guj",
        "heb",
        "hin",
        "kan",
        "kaz",
        "khm",
        "kor",
        "lao",
        "mkd",
        "mal",
        "mar",
        "mol",
        "mon",
        "pan",
        "ron",
        "srp",
        "sin",
        "som",
        "swa",
        "tam",
        "tel",
        "bod",
        "tir",
        "tuk",
        "ukr",
        "urd",
        "vie",
        "zul",
        "sot",
        "bun",
        "bos",
        "sla",
        "bet",
        "mat",
        "scc",
        "ukl",
        "mlt",
        "zht",
        "zhh"
    };
    

    /**
     * returns the TileMapFormatDesc language string corresponding to
     * langtype. There are no checks for invalid language.
     *
     * @param aLangType - language number as defined in this class.
     */
    public static String getTMFDLanguageStr(int aLangtype) {
        return LANGUAGE_STRINGS[aLangtype];
    }
    
    
    public static int getLangType(String lang) {
        int langType = 0;
        for(int i=0; i<LANGUAGE_STRINGS.length; i++) {
            if(lang.equals(LANGUAGE_STRINGS[i])) {
                langType = i;
                break;
            }
        }       
        return langType;
    }        
}
