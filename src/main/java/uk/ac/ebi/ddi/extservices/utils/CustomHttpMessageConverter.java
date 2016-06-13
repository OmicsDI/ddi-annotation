package uk.ac.ebi.ddi.extservices.utils;


import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/05/2015
 */

public class CustomHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public CustomHttpMessageConverter() {
        List<MediaType> types = Arrays.asList(
                new MediaType("text", "html", DEFAULT_CHARSET),
                new MediaType("text", "plain", DEFAULT_CHARSET),
                new MediaType("application", "json", DEFAULT_CHARSET),
                new MediaType("application", "*+json", DEFAULT_CHARSET),
                new MediaType("application", "octet-stream", DEFAULT_CHARSET),
                new MediaType("text", "html", Charset.forName("ISO-8859-1")),
                new MediaType("text", "javascript", Charset.forName("ISO-8859-1"))
        );
        super.setSupportedMediaTypes(types);
    }
}
