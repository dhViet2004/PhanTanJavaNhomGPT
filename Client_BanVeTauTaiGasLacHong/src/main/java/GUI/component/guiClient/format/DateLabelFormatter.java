package GUI.component.guiClient.format;

import javax.swing.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * @Dự án: PhanTanJavaNhomGPT
 * @Class: DateLabelFormatter
 * @Tạo vào ngày: 19/04/2025
 * @Tác giả: Nguyen Huu Sang
 */
public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Object stringToValue(String text) throws ParseException {
        return LocalDate.parse(text, dateFormatter);
    }

    @Override
    public String valueToString(Object value) {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(LocalDate.of(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)));
        }
        return "";
    }
}