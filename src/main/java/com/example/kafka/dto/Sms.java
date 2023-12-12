
package com.example.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class Sms {
    private String sender;
    private String recipient;
    private String message;

    @JsonProperty("sender")
    public String getSender() {
        return sender;
    }

    @JsonProperty("recipient")
    public String getRecipient() {
        return recipient;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    // TODO: could be better written and handle the case when there is no URL
    // TODO: once computed it should be stored in the property to avoid efficiency
    public String getUrl() {
        // TODO: at least to properties, telnet, gohper protocols should be propably avoided
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(message);
        String url = null; // assume there is only one url
        while(urlMatcher.find()) {
            url = message.substring(urlMatcher.start(0), urlMatcher.end(0));
        }
        return url;
    }
}
