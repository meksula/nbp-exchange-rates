Mamy w aplikacji dostępne 2 endpointy zgodnie z dostarczoną specyfikacją.

1. Pobierz waluty z API NBP:
<br>
URL: `http://localhost:8080/api/v1/rates/EUR?effectiveDate=2026-06-08`
<br>
Metoda HTTP: `GET`


2. Zaktualizuj waluty, które są nie wpełni kompletne (out-to-date - ten przypadek opisany w specyfikacji ominąłem, ponieważ API NBP jest niemutowalne i zawsze zwróci dane aktualne,
które się nie zmienią). Metoda jest idempotentna, za każdym requestem dostaniemy ten sam wynik.
<br>
URL: `http://localhost:8080/api/v1/rates`
<br>
Metoda HTTP: `PUT`
<br>
Request body:
```
{
    "effectiveDate": "2026-06-04",
    "currencyCodes": ["EUR", "USD", "CAD"]
}
```


*Krótki opis co zostało zrobione*
<br>
W projekcie użyłem Java 17 wraz ze Sping Boot 2.7, request do NBP API jest realizowany za pomocą prostego RestTemplate.
Zastosowałem bibliotekę resiliance4j do merchanizmu retry z backoff'em. Oprócz tego dodałem lombok dla czytelności i ominięcia kodu boilerplate.
Zastanawiałem się nad uwzpólnieniem logiki procesowania kursu walutowego, która byłaby dzielona przez NpbRatesService oraz NbpRatesUpdateService.
Na koniec dodałem kilka użytecznych testów, które pozwalają na podstawową walidację kodu aplikacji.
Zastanawiałem się trochę nad tym czy weryfikację kursów walut przeprowadzić na warstwie DB czy aplikacji, zdecydowałem się to zrobić w kodzie aplikacji.