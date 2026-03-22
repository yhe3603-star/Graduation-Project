package com.dongmedicine.config.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.dongmedicine.common.util.SensitiveDataUtils;

public class SensitiveDataConverter extends MessageConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null) {
            return null;
        }
        return SensitiveDataUtils.autoMask(message);
    }
}
