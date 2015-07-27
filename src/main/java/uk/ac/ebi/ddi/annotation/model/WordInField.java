package uk.ac.ebi.ddi.annotation.model;

/**
 * Created by mingze on 22/07/15.
 * word has been found by recommender in each field.
 */
public class WordInField {

    public WordInField(String text, int from, int to) {
        this.text = text;
        this.from = from;
        this.to = to;
    }

    private String text;
    private int from;
    private int to;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "{" +
                "\"text\" : \"" + text + '\"' +
                ", \"from\" : \"" + from + '\"' +
                ", \"to\" : \"" + to + '\"' +
                '}';
    }
}
