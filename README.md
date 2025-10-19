# Security Vulnerabilities Demo - Path Traversal & XPath Injection

Projekt demonstracyjny pokazujący podatności **Path Traversal** oraz **XPath Injection** wraz z ich bezpiecznymi implementacjami.

**Autor:** Bartosz Mordarski

## Opis projektu

Aplikacja zawiera implementacje dwóch popularnych podatności bezpieczeństwa wraz z ich bezpiecznymi wersjami.

### 1. Path Traversal (Directory Traversal)

Path Traversal to podatność umożliwiająca atakującemu dostęp do plików i katalogów znajdujących się poza dozwolonym obszarem aplikacji. Atak polega na manipulacji ścieżkami plików poprzez użycie sekwencji takich jak `../` (wyjście o katalog wyżej), co pozwala na:
- Odczyt wrażliwych plików systemowych (np. `/etc/passwd`)
- Dostęp do plików konfiguracyjnych zawierających hasła
- Kradzież kodu źródłowego aplikacji
- Ujawnienie danych innych użytkowników

**Implementacje w projekcie:**
- **Vulnerable version** - brak walidacji ścieżki, możliwość odczytu plików spoza dozwolonego katalogu
- **Fixed version** - pełna walidacja wejścia, normalizacja ścieżek, weryfikacja granic katalogu

### 2. XPath Injection

XPath Injection to atak analogiczny do SQL Injection, ale wymierzony w zapytania XPath używane do przeszukiwania dokumentów XML. Podatność powstaje gdy dane użytkownika są bezpośrednio wstawiane do zapytań XPath bez walidacji. Atakujący może:
- Ominąć mechanizmy uwierzytelniania
- Uzyskać dostęp do całej bazy danych XML
- Wydobyć informacje o strukturze dokumentu
- Odczytać dane wszystkich użytkowników

**Implementacje w projekcie:**
- **Vulnerable version** - konkatenacja stringów w zapytaniach XPath
- **Fixed version** - parametryzowane zapytania z użyciem `XPathVariableResolver`

## Izolacja środowiska

**Projekt wykorzystuje kontener Docker**, aby zapewnić bezpieczne środowisko testowe. Wszystkie potencjalnie niebezpieczne operacje (odczyt plików poza katalogiem, injection attacks) są wykonywane wewnątrz izolowanego kontenera, co zabezpiecza system hosta przed przypadkowym uszkodzeniem lub nieautoryzowanym dostępem.

**Zalety konteneryzacji**
-  Pełna izolacja od systemu hosta
-  Kontrolowane środowisko testowe
-  Brak ryzyka dla danych produkcyjnych
-  Łatwe resetowanie środowiska
-  Identyczne warunki dla wszystkich użytkowników

## Struktura projektu

```
.
├── Dockerfile
├── docker-compose.yml
├── README.md
├── .gitignore
├── src/
│   ├── Main.java
│   ├── VulnerabilityLogic.java
│   ├── EnvironmentContext.java
│   ├── Vulnerable_Path_Traversal_Bartosz_Mordarski.java
│   ├── Fixed_Path_Traversal_Bartosz_Mordarski.java
│   ├── Vulnerable_XPath_Injection_Bartosz_Mordarski.java
│   └── Fixed_XPath_Injection_Bartosz_Mordarski.java
└── data/
    ├── users.xml
    ├── public/
    │   └── public_file.txt
    └── secret/
        └── secret_file.txt
```

## Wymagania

- Docker
- Docker Compose

## Instalacja i uruchomienie

### Krok 1: Klonowanie repozytorium
```bash
git clone https://github.com/BartoszMordarski/path-traversal-xpath-injection.git
cd path-traversal-xpath-injection
```

### Krok 2: Uruchomienie aplikacji
```bash
docker-compose up --build
```

Aplikacja automatycznie:
1. Skompiluje pliki Java
2. Uruchomi testy dla wszystkich podatności
3. Wyświetli wyniki w konsoli

## Testy wykonywane przez aplikację

### Test #1: Path Traversal - VULNERABLE
Demonstracja ataków:
- Odczyt pliku publicznego
- Path traversal z `../`️
- Path traversal z `./../`

### Test #2: Path Traversal - FIXED
Wszystkie ataki są blokowane przez mechanizmy bezpieczeństwa 

### Test #3: XPath Injection - VULNERABLE
Ataki SQL Injection-style na XPath:
- Normalne zapytanie
- `admin' or '1'='1`️
- `' or 1=1 or ''='`

### Test #4: XPath Injection - FIXED
Wszystkie ataki są blokowane przez parametryzowane zapytania

## Implementowane zabezpieczenia

### Path Traversal - Fixed version
- Walidacja wejścia (blacklist niebezpiecznych sekwencji)
- Normalizacja ścieżek
- Weryfikacja granic katalogu (startsWith)
- Obsługa dowiązań symbolicznych
- Limit rozmiaru pliku
- Detekcja plików binarnych
- Logowanie podejrzanych działań

### XPath Injection - Fixed version
- Parametryzowane zapytania XPath
- Walidacja formatu wejścia (regex)
- Blacklist niebezpiecznych znaków
- Limit długości wejścia
- Secure XML parsing (zabezpieczenie przed XXE)
- Logowanie prób ataków

## Przykłady wyjść z konsoli - wyniki eksperymentów

### PATH TRAVERSAL - Wersja podatna

```
╔══════════════════════════════════════════════════════════╗
║  TEST #1: PATH TRAVERSAL - VULNERABLE                    ║
╚══════════════════════════════════════════════════════════╝

─────────────────────────────────────────────────────────
Test #1: "public_file.txt"
─────────────────────────────────────────────────────────
[SUCCESS] Zawartość pliku:
This is public information that everyone can access.
You can read this file without any special permissions.

─────────────────────────────────────────────────────────
Test #2: "../secret/secret_file.txt"
─────────────────────────────────────────────────────────
[SUCCESS] Zawartość pliku:
This is secret information that should not be accessed.

─────────────────────────────────────────────────────────
Test #3: "./../secret/secret_file.txt"
─────────────────────────────────────────────────────────
[SUCCESS] Zawartość pliku:
This is secret information that should not be accessed.
```

### PATH TRAVERSAL - Wersja zabezpieczona

```
╔══════════════════════════════════════════════════════════╗
║  TEST #2: PATH TRAVERSAL - FIXED                         ║
╚══════════════════════════════════════════════════════════╝

─────────────────────────────────────────────────────────
Test #1: "public_file.txt"
─────────────────────────────────────────────────────────
[SUCCESS] Zawartość pliku:
This is public information that everyone can access.
You can read this file without any special permissions.

─────────────────────────────────────────────────────────
Test #2: "../secret/secret_file.txt"
─────────────────────────────────────────────────────────
[SECURITY WARNING] User: testUser | Input: ../secret/secret_file.txt | Reason: Nieprawidłowe znaki w ścieżce
[BLOCKED] Nieprawidłowa ścieżka

─────────────────────────────────────────────────────────
Test #3: "./../secret/secret_file.txt"
─────────────────────────────────────────────────────────
[SECURITY WARNING] User: testUser | Input: ./../secret/secret_file.txt | Reason: Nieprawidłowe znaki w ścieżce
[BLOCKED] Nieprawidłowa ścieżka

─────────────────────────────────────────────────────────
Test #4: "../../secret/secret_file.txt"
─────────────────────────────────────────────────────────
[SECURITY WARNING] User: testUser | Input: ../../secret/secret_file.txt | Reason: Nieprawidłowe znaki w ścieżce
[BLOCKED] Nieprawidłowa ścieżka
```

### XPATH INJECTION - Wersja podatna

```
╔══════════════════════════════════════════════════════════╗
║  TEST #3: XPATH INJECTION - VULNERABLE                   ║
╚══════════════════════════════════════════════════════════╝

─────────────────────────────────────────────────────────
Test #1: "admin"
─────────────────────────────────────────────────────────
[DEBUG] Wykonywane zapytanie XPath: //user[username='admin']
[SUCCESS] Znaleziono użytkowników:
  User #1: admin | Password: secretAdmin123! | Role: administrator


─────────────────────────────────────────────────────────
Test #4: "admin' or '1'='1"
─────────────────────────────────────────────────────────
[DEBUG] Wykonywane zapytanie XPath: //user[username='admin' or '1'='1']
[SUCCESS] Znaleziono użytkowników:
  User #1: admin | Password: secretAdmin123! | Role: administrator
  User #2: john_doe | Password: johnPass456 | Role: user
  User #3: guest | Password: guest123 | Role: guest
  User #4: test-user | Password: testPass789 | Role: tester


─────────────────────────────────────────────────────────
Test #5: "' or 1=1 or ''='"
─────────────────────────────────────────────────────────
[DEBUG] Wykonywane zapytanie XPath: //user[username='' or 1=1 or ''='']
[SUCCESS] Znaleziono użytkowników:
  User #1: admin | Password: secretAdmin123! | Role: administrator
  User #2: john_doe | Password: johnPass456 | Role: user
  User #3: guest | Password: guest123 | Role: guest
  User #4: test-user | Password: testPass789 | Role: tester


─────────────────────────────────────────────────────────
Test #7: "admin' and substring(password,1,1)='s"
─────────────────────────────────────────────────────────
[DEBUG] Wykonywane zapytanie XPath: //user[username='admin' and substring(password,1,1)='s']
[SUCCESS] Znaleziono użytkowników:
  User #1: admin | Password: secretAdmin123! | Role: administrator
```

### XPATH INJECTION - Wersja zabezpieczona

```
╔══════════════════════════════════════════════════════════╗
║  TEST #4: XPATH INJECTION - FIXED                        ║
╚══════════════════════════════════════════════════════════╝

─────────────────────────────────────────────────────────
Test #1: "admin"
─────────────────────────────────────────────────────────
[DEBUG] Bezpieczne zapytanie XPath: //user[username=$username]
[DEBUG] Z parametrem username = 'admin'
[SUCCESS] Znaleziono użytkownika: admin | Rola: administrator

─────────────────────────────────────────────────────────
Test #4: "admin' or '1'='1"
─────────────────────────────────────────────────────────
[SECURITY WARNING] User: testUser | Input: admin' or '1'='1 | Reason: Nieprawidłowy format username
[BLOCKED] Nieprawidłowy format username

─────────────────────────────────────────────────────────
Test #5: "' or 1=1 or ''='"
─────────────────────────────────────────────────────────
[SECURITY WARNING] User: testUser | Input: ' or 1=1 or ''=' | Reason: Nieprawidłowy format username
[BLOCKED] Nieprawidłowy format username

─────────────────────────────────────────────────────────
Test #7: "admin' and substring(password,1,1)='s"
─────────────────────────────────────────────────────────
[SECURITY WARNING] User: testUser | Input: admin' and substring(password,1,1)='s | Reason: Nieprawidłowy format username
[BLOCKED] Nieprawidłowy format username
```

## Autor

Bartosz Mordarski
