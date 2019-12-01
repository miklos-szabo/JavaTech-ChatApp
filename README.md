Java Technológia - Chat alkalmazás
=========================================
Használt technológiák, működés
------------------
Ez egy **JavaFX** alkalmazás, amivel két, egyszerre bejelentkezve levő felhasználó egymásnak üzeneteket tud küldeni, ez
**TCP** alapú  **kliens-szerver** architektúrával valósul meg.
A felhasználók adatai regisztráció után egy **SQLite** adatbázisba kerülnek.
A küldött üzenetek a **java.security, javax.crypto** csomagok segítségével titkosítva vannak.
A **Serializable** interface segítségével a chat history folyamatosan .txt fájlba mentődik, ahoonan be is lehet olvasni.
A felhasználói ablak adatai, például a mérete **Properties** segítségével .txt fájlba tárolódik, ahonnan a következő
indulás elején beolvassuk, így konzisztens felhasználói élményt tudunk nyűjtani.
A szerver a **Logger** osztállyal console-ra írja ki a kapcsolódásokat, üzenetek küldőit, fogadóit.
Minden kapcsolat localhost-on keresztül történik.

A szerver induláskor létrehoz egy ExecutorService-t, ahol a kapcsolódott klienseket tárolja, 
és figyeli a kapcsolódási kérelmeket. Ha kapcsolódott egy kliens, elküldi neki a használt titkosítás kulcsát, 
majd figyeli a bemenetetk, kezeli az üzenetet, válaszol.
Kliens oldalról üzeneteket JavaFx gombokkal küldünk, a kliens maga a szerver üzeneteit figyeli, kezeli.

Használat
------------
Legelőször a szerver alkalmazást kell elindítani. Ez egy egyszerű console app, ott lehet figyelni a log üzeneteket.

A kliens alkalmazás elindításakor a bejelentkező képernyőre kerülünk, ahonnan a register gombbal válthatunk 
a regisztrációs képernyőre, vagy bejelentkezhetünk. A regisztrációs képernyőről szintén visszaválthatunk bejelentkezésre.

Bejelentkezés után a chat képernyőre kerülünk, ahol ekkor csak a jelenleg bejelentkezett felhasználók láthatóak a
bal oldalon. Ha rákattintunk valamelyikre, akár magunkra is, megjelenik a Chat felület, ami az eddigi történetet
megjeleníti, itt lehet új üzeneteket küldeni. Kilépni az alkalmazás bezárásával lehet.