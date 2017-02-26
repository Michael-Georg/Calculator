import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CalculatorXMLTest {

    private static final String xsdSchema = "src\\main\\resources\\Calculator.xsd";

    @Test
    public void isNotValidXML() throws Exception {
        String inputXML = "src\\main\\resources\\schemaIsNotValid.xml";
        Calculator calc = new CalculatorXML(inputXML);
        assertFalse(calc.isValid(xsdSchema));
    }

    @Test
    public void ValidXML() throws Exception {
        String inputXML = "src\\main\\resources\\sampleTest.xml";
        Calculator calc = new CalculatorXML(inputXML);
        assertTrue(calc.isValid(xsdSchema));
    }

    @Test
    public void solveSimpleExpressionXML() throws Exception {
        String inputXml = "src\\main\\resources\\calculatorTestOne.xml";
        Calculator calc = new CalculatorXML(inputXml);
        assertThat(calc.solve(), hasItems(Double.NaN));
    }

    @Test
    public void solveSomeExpressionsXML() throws Exception {
        String inputXML = "src\\main\\resources\\sampleTest.xml";
        Calculator calc = new CalculatorXML(inputXML);
        List<Double> results = calc.solve();
        assertThat(results, hasItems(59747.58686350021, -2443.75));
        assertThat(results.size(), is(2));
    }

    @Test
    public void resultXmlIsValid() throws Exception {
        String inputXML = "src\\main\\resources\\sampleTest.xml";
        String outputXML = "src\\test\\resources\\sampleTestResult.xml";
        Calculator calc = new CalculatorXML(inputXML);
        calc.writeToXML(calc.solve(), outputXML);
        Calculator result = new CalculatorXML(outputXML);
        assertTrue(result.isValid(xsdSchema));
    }
}