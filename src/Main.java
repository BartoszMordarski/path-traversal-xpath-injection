public class Main {

    public static void main(String[] args) {
        String safeDir = System.getenv("SAFE_DIR");
        if (safeDir == null) {
            safeDir = "/app/data/public";
        }

        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("     PATH TRAVERSAL - Vulnerable vs Fixed");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("Safe directory: " + safeDir);
        System.out.println();

        VulnerabilityLogic pathVulnerable = new Vulnerable_Path_Traversal_Bartosz_Mordarski();
        VulnerabilityLogic pathFixed = new Fixed_Path_Traversal_Bartosz_Mordarski();
        EnvironmentContext pathContext = new EnvironmentContext(safeDir, "testUser");

        String[]pathTestCases = {
                "public_file.txt",
                "../secret/secret_file.txt",
                "./../secret/secret_file.txt",
                "../../secret/secret_file.txt",
                "..\\..\\secret\\secret_file.txt",
                "public_file.txt/../../secret/secret_file.txt",
                "....//....//secret//secret_file.txt",
                "file:///../secret/secret_file.txt",
        };

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TEST #1: PATH TRAVERSAL - VULNERABLE                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        for (int i = 0; i < 3; i++) {
            runTest(i + 1, pathTestCases[i], pathVulnerable, pathContext);
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TEST #2: PATH TRAVERSAL - FIXED                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        for (int i = 0; i < pathTestCases.length; i++) {
            runTest(i + 1, pathTestCases[i], pathFixed, pathContext);
        }

        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("     XPATH INJECTION - Vulnerable vs Fixed");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("XML file: /app/data/users.xml");
        System.out.println();

        VulnerabilityLogic xpathVulnerable = new Vulnerable_XPath_Injection_Bartosz_Mordarski();
        VulnerabilityLogic xpathFixed = new Fixed_XPath_Injection_Bartosz_Mordarski();

        EnvironmentContext xpathContext = new EnvironmentContext("/app/data", "testUser");
        xpathContext.setString("xmlFile", "/app/data/users.xml");

        String[] xpathTestCases = {
                "admin",
                "john_doe",
                "guest",
                "admin' or '1'='1",
                "' or 1=1 or ''='",
                "admin'--",
                "admin' and substring(password,1,1)='s",
        };

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TEST #3: XPATH INJECTION - VULNERABLE                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        for (int i = 0; i < xpathTestCases.length; i++) {
            runTest(i + 1, xpathTestCases[i], xpathVulnerable, xpathContext);
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TEST #4: XPATH INJECTION - FIXED                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        for (int i = 0; i < xpathTestCases.length; i++) {
            runTest(i + 1, xpathTestCases[i], xpathFixed, xpathContext);
        }


        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("     TESTS FINISHED");
        System.out.println("═══════════════════════════════════════════════════════════\n");
    }

    private static void runTest(int testNum, String input, VulnerabilityLogic logic, EnvironmentContext context) {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("Test #" + testNum + ": \"" + input + "\"");
        System.out.println("─────────────────────────────────────────────────────────");

        try {
            String result = logic.process(input, context);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println();
    }
}