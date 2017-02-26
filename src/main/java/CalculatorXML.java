import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.stream.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Double.NaN;


/**
 *  Simple Calculator which works with XML. Validation schema for Input XML file is Calculator.xsd.
 *  Output result file must be validate by the schema too.
 */
@SuppressWarnings("WeakerAccess")
public class CalculatorXML implements Calculator {
    private static final Logger log = Logger.getLogger(CalculatorXML.class);
    private String inputXMLPath;

    /**
     * @param inputXMLPath path to the input file with expressions
     */
    public CalculatorXML(String inputXMLPath) {
        this.inputXMLPath = inputXMLPath;
    }

    /**
     * Create new instance of the factory and then
     * create a new XMLStreamReader from a java.io.InputStream.
     * Call the process method with parameter XMLStreamReader.
     * @return List<Double> which contain results of all expressions
     */
    public List<Double> solve() {
        List<Double> results = new LinkedList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (InputStream stream = new FileInputStream(inputXMLPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(stream);
            results = process(reader);
            reader.close();
        } catch (IOException | XMLStreamException e) {
            log.error(e);
        }
        return results;
    }

    /**
     * Parse input file and calculate results of expressions.
     * Used StaX xml parser.
     *
     * @param reader XMLStreamReader
     * @return List<Double> which contains results of all expressions
     * @throws XMLStreamException - processing error
     */
    private List<Double> process(XMLStreamReader reader) throws XMLStreamException {
        List<Double> results = new LinkedList<>();
        Deque<String> operations = new LinkedList<>();
        Deque<Double> values = new LinkedList<>();
        String elementName = "";
        while (reader.hasNext()) {
            int type = reader.next();

            switch (type) {
                case XMLStreamConstants.START_ELEMENT:
                    elementName = reader.getLocalName();
                    if (elementName.equals("operation"))
                        operations.add(reader.getAttributeValue(0));
                    break;

                case XMLStreamConstants.CHARACTERS:
                    String text = reader.getText().trim();
                    if (!text.isEmpty() && elementName.equals("arg"))
                        values.add(Double.parseDouble(text));
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    elementName = reader.getLocalName();

                    switch (elementName) {
                        case "expression":
                            results.add(values.pollLast());
                            break;
                        case "operation":
                            Double secondValue = values.pollLast();
                            values.add(calculate(values.pollLast(), secondValue, operations.pollLast()));
                            break;
                    }
                    break;
            }
        }
        return results;
    }


    /**
     * Compares string and equal math operation
     * @param operation mathematics operation
     * @return calculation result or NaN if operation is invalid
     *
     */
    private double calculate(double a, double b, String operation) {
        switch (operation) {
            case "SUM":
                return sum(a, b);
            case "SUB":
                return sub(a, b);
            case "DIV":
                return division(a, b);
            case "MUL":
                return mul(a, b);
        }
        return NaN;
    }


    /**
     * Create result xml file
     * @param results - the calculation results
     * @param path - result xml file
     * @throws XMLStreamException - processing error
     */
    public void writeToXML(List<Double> results, String path) throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try (FileWriter out = new FileWriter(path)) {
            XMLStreamWriter writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));
            writer.writeStartDocument();
            writer.writeStartElement("simpleCalculator");
            writer.writeStartElement("expressionResults");
            for (Double element : results) {
                writer.writeStartElement("expressionResult");
                writer.writeStartElement("result");
                writer.writeCharacters(element.toString());
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(e);
        }

    }


    /**
     * Create result file in the same directory
     * with standard name (inputFileName + "Result.xml").
     * @throws XMLStreamException - processing error
     */
    @Override
    public void writeToXML(List<Double> results) throws XMLStreamException {
        String path = inputXMLPath.substring(0, inputXMLPath.lastIndexOf('.')) + "Result.xml";
        writeToXML(results, path);
    }

    /**
     * Validate XML file
     * @param schemaPath - path to XML schema
     * @return true if input XML file is valid to input schema
     */
    public boolean isValid(String schemaPath){
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        try {
            Schema schema = factory.newSchema(new File(schemaPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(inputXMLPath));
        } catch (SAXException | IOException e) {
            log.error("XML is not valid", e);
            return false;
        }
        log.info("XML is valid");
        return true;
    }
}
