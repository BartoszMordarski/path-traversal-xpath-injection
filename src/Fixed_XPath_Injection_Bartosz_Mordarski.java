import javax.xml.xpath.*;
import javax.xml.namespace.QName;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.XMLConstants;
import java.io.*;

public class Fixed_XPath_Injection_Bartosz_Mordarski implements VulnerabilityLogic {

    private static final int MAX_INPUT_LENGTH = 50;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{1,50}$";

    @Override
    public String process(String userInput, EnvironmentContext context) throws Exception {
        try {
            // 1. Walidacja wejścia
            if (!isInputValid(userInput)) {
                logAttempt(userInput, context.getUserId(), "Nieprawidłowy format username");
                return "[BLOCKED] Nieprawidłowy format username";
            }

            // 2. Pobranie ścieżki do XML
            String xmlPath = context.getString("xmlFile");

            // 3. Bezpieczne parsowanie XML
            Document doc = parseXmlSecurely(xmlPath);

            // 4. Bezpieczne zapytanie XPath
            String result = executeSecureXPath(doc, userInput);

            return result;

        } catch (IllegalArgumentException e) {
            return "[BLOCKED] " + e.getMessage();
        } catch (Exception e) {
            logError(e);
            return "[ERROR] Wystąpił błąd podczas przetwarzania";
        }
    }

    // Walidacja wejścia - blokuje potencjalne payloady XPath Injection
    private boolean isInputValid(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        if (input.length() > MAX_INPUT_LENGTH) {
            return false;
        }

        if (!input.matches(USERNAME_PATTERN)) {
            return false;
        }

        String[] dangerousChars = {"'", "\"", "/", "\\", "[", "]", "(", ")", "|", "&", "="};
        for (String dangerous : dangerousChars) {
            if (input.contains(dangerous)) {
                return false;
            }
        }

        return true;
    }

    private Document parseXmlSecurely(String xmlPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Zabezpieczenia przed XXE i innymi atakami XML
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(xmlPath));
    }

    private String executeSecureXPath(Document doc, String username) throws Exception {
        XPathFactory xPathfactory = XPathFactory.newInstance();


        xPathfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        XPath xpath = xPathfactory.newXPath();

        // NAJWAŻNIEJSZE: Parametryzowane zapytanie XPath
        // Użycie zmiennych eliminuje możliwość injection
        xpath.setXPathVariableResolver(new XPathVariableResolver() {
            @Override
            public Object resolveVariable(QName variableName) {
                if ("username".equals(variableName.getLocalPart())) {
                    return username;
                }
                return null;
            }
        });

        // Bezpieczne zapytanie ze zmienną $username
        String xpathQuery = "//user[username=$username]";

        System.out.println("[DEBUG] Bezpieczne zapytanie XPath: " + xpathQuery);
        System.out.println("[DEBUG] Z parametrem username = '" + username + "'");

        XPathExpression expr = xpath.compile(xpathQuery);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        if (nodes.getLength() > 0) {
            Element userElement = (Element) nodes.item(0);
            String foundUsername = userElement.getElementsByTagName("username").item(0).getTextContent();
            String role = userElement.getElementsByTagName("role").item(0).getTextContent();

            return String.format("[SUCCESS] Znaleziono użytkownika: %s | Rola: %s",
                    foundUsername, role);
        } else {
            return "[ERROR] Nie znaleziono użytkownika";
        }
    }

    // Logowanie prób ataku
    private void logAttempt(String input, String userId, String reason) {
        System.err.println(String.format(
                "[SECURITY WARNING] User: %s | Input: %s | Reason: %s",
                userId, input, reason
        ));
    }

    private void logError(Exception e) {
        System.err.println("[ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
}