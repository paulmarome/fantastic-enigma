package com.classes;

public final class Language
{
    private final String file;
    private final String language;

    public Language(String file, String language) {
        this.file = file;
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
    
    public String getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "Language{" + "file=" + file + ", language=" + language + '}';
    }
}