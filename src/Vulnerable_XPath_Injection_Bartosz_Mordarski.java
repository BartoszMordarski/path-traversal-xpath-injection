import javax.xml.xpath.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

public class Vulnerable_XPath_Injection_Bartosz_Mordarski implements VulnerabilityLogic {

    @Override
    public String process(String userInput, EnvironmentContext context) throws Exception {
        String xmlPath = context.getString("xmlFile");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlPath));

        String xpathQuery = "//user[username='" + userInput + "']";

        System.out.println("[DEBUG] Wykonywane zapytanie XPath: " + xpathQuery);

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(xpathQuery);

        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        if (nodes.getLength() > 0) {
            StringBuilder result = new StringBuilder("[SUCCESS] Znaleziono użytkowników:\n");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element userElement = (Element) nodes.item(i);
                String username = userElement.getElementsByTagName("username").item(0).getTextContent();
                String password = userElement.getElementsByTagName("password").item(0).getTextContent();
                String role = userElement.getElementsByTagName("role").item(0).getTextContent();

                result.append(String.format("  User #%d: %s | Password: %s | Role: %s\n",
                        i + 1, username, password, role));
            }

            return result.toString();
        } else {
            return "[ERROR] Nie znaleziono użytkownika";
        }
    }
}