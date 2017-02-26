import javax.xml.stream.XMLStreamException;
import java.util.List;

import static java.lang.Double.NaN;

public interface Calculator {
    default double sum(double a, double b) {
        return a + b;
    }

    default double division(double a, double b) {
        return b == 0 ? NaN : a / b;
    }

    default double sub(double a, double b) {
        return a - b;
    }

    default double mul(double a, double b) {
        return a * b;
    }

    List<Double> solve();
    boolean isValid(String path);
    void writeToXML(List<Double> results) throws XMLStreamException;
    void writeToXML(List<Double> results, String path) throws XMLStreamException;
}
