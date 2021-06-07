package com.alrayan.blackboxtester;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HttpTestHeader implements Header {

    private String name;
    private String value;

    @Override
    public HeaderElement[] getElements() throws ParseException {
        return new HeaderElement[0];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }
}
