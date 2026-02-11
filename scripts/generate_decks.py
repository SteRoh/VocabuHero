#!/usr/bin/env python3
"""Generate vocabulary deck CSVs: BASIC (1-1000), ADVANCED (1001-2000), EXPERT (2001-5000) for EN/IT/FR/ES to German.
Non-overlapping: each level contains only its own words."""
import os
import urllib.request
import ssl

HEADER = "Front,Back,Note"
YACLE_URL = "https://raw.githubusercontent.com/codogogo/xling-eval/master/bli_datasets/en-de/yacle.train.freq.5k.en-de.tsv"

# Italian-German: common pairs (most frequent / useful). Format: (italian, german)
EXTRA_IT_DE = [
    ("anno", "Jahr"), ("vita", "Leben"), ("mano", "Hand"), ("parte", "Teil"), ("occhio", "Auge"),
    ("giorno", "Tag"), ("mondo", "Welt"), ("notte", "Nacht"), ("testa", "Kopf"), ("ora", "Stunde"),
    ("cosa", "Sache"), ("casa", "Haus"), ("padre", "Vater"), ("madre", "Mutter"), ("figlio", "Sohn"),
    ("figlia", "Tochter"), ("fratello", "Bruder"), ("sorella", "Schwester"), ("marito", "Ehemann"),
    ("moglie", "Ehefrau"), ("nome", "Name"), ("numero", "Nummer"), ("anno", "Jahr"), ("tempo", "Zeit"),
    ("uomo", "Mann"), ("donna", "Frau"), ("bambino", "Kind"), ("famiglia", "Familie"), ("amico", "Freund"),
    ("lavoro", "Arbeit"), ("posto", "Platz"), ("città", "Stadt"), ("paese", "Land"), ("strada", "Straße"),
    ("porta", "Tür"), ("finestra", "Fenster"), ("tavolo", "Tisch"), ("sedia", "Stuhl"), ("letto", "Bett"),
    ("cucina", "Küche"), ("bagno", "Badezimmer"), ("scuola", "Schule"), ("chiesa", "Kirche"),
    ("ospedale", "Krankenhaus"), ("negozio", "Geschäft"), ("ufficio", "Büro"), ("macchina", "Auto"),
    ("treno", "Zug"), ("aereo", "Flugzeug"), ("nave", "Schiff"), ("barca", "Boot"),
    ("soldi", "Geld"), ("prezzo", "Preis"), ("costo", "Kosten"), ("pagare", "bezahlen"),
    ("comprare", "kaufen"), ("vendere", "verkaufen"), ("lavorare", "arbeiten"), ("studiare", "studieren"),
    ("giocare", "spielen"), ("leggere", "lesen"), ("scrivere", "schreiben"), ("parlare", "sprechen"),
    ("ascoltare", "hören"), ("vedere", "sehen"), ("sentire", "hören"), ("capire", "verstehen"),
    ("pensare", "denken"), ("credere", "glauben"), ("volere", "wollen"), ("potere", "können"),
    ("dovere", "müssen"), ("sapere", "wissen"), ("conoscere", "kennen"), ("ricordare", "sich erinnern"),
    ("dimenticare", "vergessen"), ("mangiare", "essen"), ("bere", "trinken"), ("dormire", "schlafen"),
    ("svegliarsi", "aufwachen"), ("alzarsi", "aufstehen"), ("sedersi", "sich setzen"), ("camminare", "gehen"),
    ("correre", "laufen"), ("nuotare", "schwimmen"), ("viaggiare", "reisen"), ("partire", "abfahren"),
    ("arrivare", "ankommen"), ("tornare", "zurückkehren"), ("entrare", "eintreten"), ("uscire", "hinausgehen"),
    ("aprire", "öffnen"), ("chiudere", "schließen"), ("prendere", "nehmen"), ("dare", "geben"),
    ("mettere", "stellen"), ("togliere", "entfernen"), ("tenere", "halten"), ("lasciare", "lassen"),
    ("chiamare", "rufen"), ("aspettare", "warten"), ("cercare", "suchen"), ("trovare", "finden"),
    ("perdere", "verlieren"), ("iniziare", "anfangen"), ("finire", "beenden"), ("continuare", "fortsetzen"),
    ("aiutare", "helfen"), ("chiedere", "fragen"), ("rispondere", "antworten"), ("dire", "sagen"),
    ("telefonare", "anrufen"), ("scrivere", "schreiben"), ("mandare", "schicken"), ("ricevere", "bekommen"),
    ("uno", "eins"), ("due", "zwei"), ("tre", "drei"), ("quattro", "vier"), ("cinque", "fünf"),
    ("sei", "sechs"), ("sette", "sieben"), ("otto", "acht"), ("nove", "neun"), ("dieci", "zehn"),
    ("cento", "hundert"), ("mille", "tausend"), ("primo", "erste"), ("secondo", "zweite"),
    ("lunedì", "Montag"), ("martedì", "Dienstag"), ("mercoledì", "Mittwoch"), ("giovedì", "Donnerstag"),
    ("venerdì", "Freitag"), ("sabato", "Samstag"), ("domenica", "Sonntag"),
    ("gennaio", "Januar"), ("febbraio", "Februar"), ("marzo", "März"), ("aprile", "April"),
    ("maggio", "Mai"), ("giugno", "Juni"), ("luglio", "Juli"), ("agosto", "August"),
    ("settembre", "September"), ("ottobre", "Oktober"), ("novembre", "November"), ("dicembre", "Dezember"),
    ("rosso", "rot"), ("blu", "blau"), ("verde", "grün"), ("giallo", "gelb"), ("bianco", "weiß"),
    ("nero", "schwarz"), ("grigio", "grau"), ("marrone", "braun"), ("arancione", "orange"),
    ("buono", "gut"), ("cattivo", "schlecht"), ("grande", "groß"), ("piccolo", "klein"),
    ("nuovo", "neu"), ("vecchio", "alt"), ("bello", "schön"), ("brutto", "hässlich"),
    ("facile", "einfach"), ("difficile", "schwer"), ("vero", "wahr"), ("falso", "falsch"),
    ("felice", "glücklich"), ("triste", "traurig"), ("caldo", "warm"), ("freddo", "kalt"),
    ("pieno", "voll"), ("vuoto", "leer"), ("aperto", "offen"), ("chiuso", "geschlossen"),
    ("pulito", "sauber"), ("sporco", "schmutzig"), ("forte", "stark"), ("debole", "schwach"),
    ("veloce", "schnell"), ("lento", "langsam"), ("alto", "hoch"), ("basso", "niedrig"),
    ("lungo", "lang"), ("corto", "kurz"), ("lontano", "weit"), ("vicino", "nah"),
    ("destra", "rechts"), ("sinistra", "links"), ("su", "oben"), ("giù", "unten"),
    ("dentro", "drinnen"), ("fuori", "draußen"), ("prima", "vorher"), ("dopo", "nachher"),
    ("sopra", "über"), ("sotto", "unter"), ("davanti", "vorn"), ("dietro", "hinten"),
    ("insieme", "zusammen"), ("solo", "allein"), ("ancora", "noch"), ("già", "schon"),
    ("sempre", "immer"), ("mai", "nie"), ("molto", "sehr"), ("poco", "wenig"),
    ("troppo", "zu viel"), ("abbastanza", "genug"), ("quasi", "fast"), ("forse", "vielleicht"),
    ("certamente", "sicherlich"), ("probabilmente", "wahrscheinlich"), ("perfetto", "perfekt"),
    ("importante", "wichtig"), ("possibile", "möglich"), ("impossibile", "unmöglich"),
    ("necessario", "nötig"), ("normale", "normal"), ("speciale", "besonders"),
    ("pronto", "bereit"), ("libero", "frei"), ("occupato", "beschäftigt"), ("malato", "krank"),
    ("sano", "gesund"), ("stanco", "müde"), ("riposato", "ausgeruht"), ("affamato", "hungrig"),
    ("assetato", "durstig"), ("ricco", "reich"), ("povero", "arm"), ("giovane", "jung"),
    ("anziano", "alt"), ("single", "ledig"), ("sposato", "verheiratet"), ("divorziato", "geschieden"),
    ("nato", "geboren"), ("morto", "tot"), ("vivo", "lebendig"), ("presente", "anwesend"),
    ("assente", "abwesend"), ("contento", "zufrieden"), ("preoccupato", "besorgt"),
    ("nervoso", "nervös"), ("tranquillo", "ruhig"), ("arrabbiato", "wütend"), ("sorpreso", "überrascht"),
    ("imbarazzato", "verlegen"), ("orgoglioso", "stolz"), ("deluso", "enttäuscht"),
    ("malattia", "Krankheit"), ("salute", "Gesundheit"), ("medicina", "Medizin"), ("dottore", "Arzt"),
    ("infermiere", "Krankenpfleger"), ("paziente", "Patient"), ("farmacia", "Apotheke"),
    ("cibo", "Essen"), ("colazione", "Frühstück"), ("pranzo", "Mittagessen"), ("cena", "Abendessen"),
    ("pane", "Brot"), ("carne", "Fleisch"), ("pesce", "Fisch"), ("frutta", "Obst"), ("verdura", "Gemüse"),
    ("latte", "Milch"), ("formaggio", "Käse"), ("uovo", "Ei"), ("zucchero", "Zucker"), ("sale", "Salz"),
    ("caffè", "Kaffee"), ("tè", "Tee"), ("vino", "Wein"), ("birra", "Bier"), ("acqua", "Wasser"),
    ("animali", "Tiere"), ("cane", "Hund"), ("gatto", "Katze"), ("uccello", "Vogel"), ("pesce", "Fisch"),
    ("natura", "Natur"), ("sole", "Sonne"), ("luna", "Mond"), ("stella", "Stern"), ("cielo", "Himmel"),
    ("terra", "Erde"), ("mare", "Meer"), ("fiume", "Fluss"), ("lago", "See"), ("montagna", "Berg"),
    ("bosco", "Wald"), ("albero", "Baum"), ("fiore", "Blume"), ("erba", "Gras"),
    ("clima", "Klima"), ("pioggia", "Regen"), ("neve", "Schnee"), ("vento", "Wind"), ("tempesta", "Sturm"),
    ("musica", "Musik"), ("canzone", "Lied"), ("film", "Film"), ("libro", "Buch"), ("arte", "Kunst"),
    ("sport", "Sport"), ("calcio", "Fußball"), ("tennis", "Tennis"), ("nuoto", "Schwimmen"),
    ("problema", "Problem"), ("soluzione", "Lösung"), ("domanda", "Frage"), ("risposta", "Antwort"),
    ("idea", "Idee"), ("piano", "Plan"), ("progetto", "Projekt"), ("decisione", "Entscheidung"),
    ("scelta", "Wahl"), ("possibilità", "Möglichkeit"), ("occasione", "Gelegenheit"),
    ("successo", "Erfolg"), ("fallimento", "Misserfolg"), ("errore", "Fehler"), ("fortuna", "Glück"),
    ("sventura", "Unglück"), ("amore", "Liebe"), ("odio", "Hass"), ("pace", "Frieden"), ("guerra", "Krieg"),
    ("libertà", "Freiheit"), ("giustizia", "Gerechtigkeit"), ("verità", "Wahrheit"), ("bugia", "Lüge"),
    ("diritto", "Recht"), ("dovere", "Pflicht"), ("responsabilità", "Verantwortung"),
]

# English-German fallback (used if yacle download fails)
EN_DE = [
    ("the", "der/die/das"), ("be", "sein"), ("to", "zu"), ("of", "von"), ("and", "und"),
    ("a", "ein/eine"), ("in", "in"), ("that", "dass"), ("have", "haben"), ("I", "ich"),
    ("it", "es"), ("for", "für"), ("not", "nicht"), ("on", "auf"), ("with", "mit"),
    ("he", "er"), ("as", "als"), ("you", "du/Sie"), ("do", "tun"), ("at", "bei"),
    ("this", "dieser"), ("but", "aber"), ("his", "sein"), ("by", "von"), ("from", "von"),
    ("they", "sie"), ("we", "wir"), ("say", "sagen"), ("her", "ihr"), ("she", "sie"),
    ("or", "oder"), ("an", "ein"), ("will", "werden"), ("my", "mein"), ("one", "eins"),
    ("all", "alle"), ("would", "würde"), ("there", "dort"), ("their", "ihr"),
    ("what", "was"), ("so", "so"), ("up", "auf"), ("out", "aus"), ("if", "wenn"),
    ("about", "über"), ("who", "wer"), ("get", "bekommen"), ("which", "welche"),
    ("go", "gehen"), ("me", "mich"), ("when", "wann"), ("make", "machen"), ("can", "können"),
    ("like", "mögen"), ("time", "Zeit"), ("no", "nein"), ("just", "nur"), ("him", "ihn"),
    ("know", "wissen"), ("take", "nehmen"), ("people", "Leute"), ("into", "in"),
    ("year", "Jahr"), ("your", "dein/Ihr"), ("good", "gut"), ("some", "einige"),
    ("could", "könnte"), ("them", "sie"), ("see", "sehen"), ("other", "andere"),
    ("than", "als"), ("then", "dann"), ("now", "jetzt"), ("look", "schauen"),
    ("only", "nur"), ("come", "kommen"), ("its", "sein"), ("over", "über"),
    ("think", "denken"), ("also", "auch"), ("back", "zurück"), ("after", "nach"),
    ("use", "benutzen"), ("two", "zwei"), ("how", "wie"), ("our", "unser"),
    ("work", "Arbeit"), ("first", "erste"), ("well", "gut"), ("way", "Weg"),
    ("even", "sogar"), ("new", "neu"), ("want", "wollen"), ("because", "weil"),
    ("any", "irgendein"), ("these", "diese"), ("give", "geben"), ("day", "Tag"),
    ("most", "meiste"), ("us", "uns"), ("is", "ist"), ("are", "sind"), ("was", "war"),
    ("were", "waren"), ("been", "gewesen"), ("being", "seiend"), ("has", "hat"), ("had", "hatte"),
    ("does", "tut"), ("did", "tat"), ("life", "Leben"), ("man", "Mann"), ("woman", "Frau"),
    ("child", "Kind"), ("world", "Welt"), ("house", "Haus"), ("place", "Ort"), ("thing", "Ding"),
    ("hand", "Hand"), ("eye", "Auge"), ("head", "Kopf"), ("part", "Teil"), ("number", "Nummer"),
    ("name", "Name"), ("father", "Vater"), ("mother", "Mutter"), ("son", "Sohn"), ("daughter", "Tochter"),
    ("brother", "Bruder"), ("sister", "Schwester"), ("friend", "Freund"),
    ("water", "Wasser"), ("food", "Essen"), ("bread", "Brot"), ("meat", "Fleisch"),
    ("fish", "Fisch"), ("milk", "Milch"), ("coffee", "Kaffee"), ("tea", "Tee"),
    ("wine", "Wein"), ("beer", "Bier"), ("money", "Geld"), ("price", "Preis"),
    ("school", "Schule"), ("church", "Kirche"), ("shop", "Geschäft"), ("office", "Büro"),
    ("car", "Auto"), ("train", "Zug"), ("plane", "Flugzeug"), ("ship", "Schiff"),
    ("door", "Tür"), ("window", "Fenster"), ("table", "Tisch"), ("chair", "Stuhl"),
    ("bed", "Bett"), ("kitchen", "Küche"), ("room", "Zimmer"), ("street", "Straße"),
    ("city", "Stadt"), ("country", "Land"), ("book", "Buch"), ("letter", "Brief"),
    ("question", "Frage"), ("answer", "Antwort"), ("problem", "Problem"), ("idea", "Idee"),
    ("love", "Liebe"), ("war", "Krieg"), ("peace", "Frieden"), ("right", "Recht"),
    ("wrong", "falsch"), ("true", "wahr"), ("false", "falsch"), ("big", "groß"),
    ("small", "klein"), ("long", "lang"), ("short", "kurz"), ("high", "hoch"),
    ("low", "niedrig"), ("old", "alt"), ("young", "jung"), ("hot", "heiß"),
    ("cold", "kalt"), ("full", "voll"), ("empty", "leer"), ("open", "offen"),
    ("closed", "geschlossen"), ("easy", "einfach"), ("hard", "schwer"),
    ("happy", "glücklich"), ("sad", "traurig"), ("beautiful", "schön"), ("ugly", "hässlich"),
    ("strong", "stark"), ("weak", "schwach"), ("fast", "schnell"), ("slow", "langsam"),
    ("same", "gleich"), ("different", "anders"), ("possible", "möglich"),
    ("impossible", "unmöglich"), ("important", "wichtig"), ("necessary", "nötig"),
    ("ready", "bereit"), ("free", "frei"), ("busy", "beschäftigt"), ("sick", "krank"),
    ("healthy", "gesund"), ("tired", "müde"), ("hungry", "hungrig"), ("thirsty", "durstig"),
    ("rich", "reich"), ("poor", "arm"), ("present", "anwesend"), ("absent", "abwesend"),
    ("today", "heute"), ("tomorrow", "morgen"), ("yesterday", "gestern"),
    ("morning", "Morgen"), ("afternoon", "Nachmittag"), ("evening", "Abend"),
    ("night", "Nacht"), ("week", "Woche"), ("month", "Monat"),
    ("Monday", "Montag"), ("Tuesday", "Dienstag"), ("Wednesday", "Mittwoch"),
    ("Thursday", "Donnerstag"), ("Friday", "Freitag"), ("Saturday", "Samstag"),
    ("Sunday", "Sonntag"), ("January", "Januar"), ("February", "Februar"),
    ("March", "März"), ("April", "April"), ("May", "Mai"), ("June", "Juni"),
    ("July", "Juli"), ("August", "August"), ("September", "September"),
    ("October", "Oktober"), ("November", "November"), ("December", "Dezember"),
    ("red", "rot"), ("blue", "blau"), ("green", "grün"), ("yellow", "gelb"),
    ("white", "weiß"), ("black", "schwarz"), ("grey", "grau"), ("brown", "braun"),
    ("one", "eins"), ("two", "zwei"), ("three", "drei"), ("four", "vier"), ("five", "fünf"),
    ("six", "sechs"), ("seven", "sieben"), ("eight", "acht"), ("nine", "neun"), ("ten", "zehn"),
    ("hundred", "hundert"), ("thousand", "tausend"), ("first", "erste"), ("second", "zweite"),
    ("here", "hier"), ("where", "wo"), ("why", "warum"), ("how", "wie"), ("what", "was"), ("who", "wer"),
    ("yes", "ja"), ("no", "nein"), ("please", "bitte"), ("thanks", "danke"),
    ("sorry", "Entschuldigung"), ("hello", "hallo"), ("goodbye", "auf Wiedersehen"),
]

# French-German: common pairs. Format: (french, german)
FR_DE = [
    ("le", "der"), ("la", "die"), ("les", "die"), ("un", "ein"), ("une", "eine"), ("et", "und"),
    ("être", "sein"), ("avoir", "haben"), ("de", "von"), ("à", "zu"), ("en", "in"),
    ("que", "dass"), ("qui", "wer"), ("ce", "dieser"), ("dans", "in"), ("du", "von"),
    ("je", "ich"), ("il", "er"), ("elle", "sie"), ("on", "man"), ("nous", "wir"),
    ("vous", "Sie"), ("ils", "sie"), ("leur", "ihr"), ("pas", "nicht"), ("plus", "mehr"),
    ("tout", "alles"), ("aussi", "auch"), ("bien", "gut"), ("très", "sehr"), ("donc", "also"),
    ("comme", "wie"), ("mais", "aber"), ("ou", "oder"), ("si", "wenn"), ("par", "durch"),
    ("pour", "für"), ("avec", "mit"), ("sans", "ohne"), ("sur", "auf"), ("sous", "unter"),
    ("avant", "vorher"), ("après", "nach"), ("entre", "zwischen"), ("chez", "bei"),
    ("homme", "Mann"), ("femme", "Frau"), ("enfant", "Kind"), ("père", "Vater"), ("mère", "Mutter"),
    ("frère", "Bruder"), ("sœur", "Schwester"), ("ami", "Freund"), ("vie", "Leben"),
    ("jour", "Tag"), ("an", "Jahr"), ("temps", "Zeit"), ("heure", "Stunde"), ("nuit", "Nacht"),
    ("maison", "Haus"), ("ville", "Stadt"), ("pays", "Land"), ("monde", "Welt"),
    ("travail", "Arbeit"), ("argent", "Geld"), ("eau", "Wasser"), ("pain", "Brot"),
    ("café", "Kaffee"), ("lait", "Milch"), ("vin", "Wein"), ("bière", "Bier"),
    ("chose", "Ding"), ("part", "Teil"), ("nom", "Name"), ("numéro", "Nummer"),
    ("main", "Hand"), ("œil", "Auge"), ("tête", "Kopf"), ("porte", "Tür"),
    ("fenêtre", "Fenster"), ("table", "Tisch"), ("chaise", "Stuhl"), ("lit", "Bett"),
    ("cuisine", "Küche"), ("salle", "Zimmer"), ("rue", "Straße"), ("école", "Schule"),
    ("bureau", "Büro"), ("voiture", "Auto"), ("train", "Zug"), ("avion", "Flugzeug"),
    ("aller", "gehen"), ("venir", "kommen"), ("faire", "machen"), ("dire", "sagen"),
    ("voir", "sehen"), ("savoir", "wissen"), ("pouvoir", "können"), ("vouloir", "wollen"),
    ("prendre", "nehmen"), ("donner", "geben"), ("mettre", "stellen"), ("trouver", "finden"),
    ("grand", "groß"), ("petit", "klein"), ("nouveau", "neu"), ("bon", "gut"), ("mauvais", "schlecht"),
    ("beau", "schön"), ("vieux", "alt"), ("jeune", "jung"), ("long", "lang"), ("court", "kurz"),
    ("haut", "hoch"), ("bas", "niedrig"), ("fort", "stark"), ("faible", "schwach"),
    ("facile", "einfach"), ("difficile", "schwer"), ("possible", "möglich"),
    ("important", "wichtig"), ("rouge", "rot"), ("bleu", "blau"), ("vert", "grün"),
    ("jaune", "gelb"), ("blanc", "weiß"), ("noir", "schwarz"), ("gris", "grau"),
    ("un", "eins"), ("deux", "zwei"), ("trois", "drei"), ("quatre", "vier"), ("cinq", "fünf"),
    ("six", "sechs"), ("sept", "sieben"), ("huit", "acht"), ("neuf", "neun"), ("dix", "zehn"),
    ("lundi", "Montag"), ("mardi", "Dienstag"), ("mercredi", "Mittwoch"),
    ("janvier", "Januar"), ("février", "Februar"), ("mars", "März"), ("avril", "April"),
    ("mai", "Mai"), ("juin", "Juni"), ("juillet", "Juli"), ("août", "August"),
    ("septembre", "September"), ("octobre", "Oktober"), ("novembre", "November"), ("décembre", "Dezember"),
    ("merci", "danke"), ("s'il vous plaît", "bitte"), ("bonjour", "Guten Tag"),
    ("au revoir", "auf Wiedersehen"), ("oui", "ja"), ("non", "nein"),
]

# Spanish-German: common pairs. Format: (spanish, german)
ES_DE = [
    ("el", "der"), ("la", "die"), ("los", "die"), ("las", "die"), ("un", "ein"), ("una", "eine"),
    ("y", "und"), ("ser", "sein"), ("estar", "sein"), ("tener", "haben"), ("hacer", "machen"),
    ("de", "von"), ("a", "zu"), ("en", "in"), ("que", "dass"), ("con", "mit"), ("por", "durch"),
    ("para", "für"), ("sin", "ohne"), ("sobre", "über"), ("entre", "zwischen"),
    ("yo", "ich"), ("él", "er"), ("ella", "sie"), ("nosotros", "wir"), ("vosotros", "ihr"),
    ("ellos", "sie"), ("su", "sein"), ("tu", "dein"), ("mi", "mein"), ("no", "nicht"),
    ("más", "mehr"), ("muy", "sehr"), ("bien", "gut"), ("también", "auch"), ("como", "wie"),
    ("pero", "aber"), ("o", "oder"), ("si", "wenn"), ("hombre", "Mann"), ("mujer", "Frau"),
    ("niño", "Kind"), ("padre", "Vater"), ("madre", "Mutter"), ("hermano", "Bruder"),
    ("hermana", "Schwester"), ("amigo", "Freund"), ("vida", "Leben"), ("día", "Tag"),
    ("año", "Jahr"), ("tiempo", "Zeit"), ("hora", "Stunde"), ("noche", "Nacht"),
    ("casa", "Haus"), ("ciudad", "Stadt"), ("país", "Land"), ("mundo", "Welt"),
    ("trabajo", "Arbeit"), ("dinero", "Geld"), ("agua", "Wasser"), ("pan", "Brot"),
    ("café", "Kaffee"), ("leche", "Milch"), ("vino", "Wein"), ("cerveza", "Bier"),
    ("cosa", "Ding"), ("parte", "Teil"), ("nombre", "Name"), ("número", "Nummer"),
    ("mano", "Hand"), ("ojo", "Auge"), ("cabeza", "Kopf"), ("puerta", "Tür"),
    ("ventana", "Fenster"), ("mesa", "Tisch"), ("silla", "Stuhl"), ("cama", "Bett"),
    ("cocina", "Küche"), ("habitación", "Zimmer"), ("calle", "Straße"), ("escuela", "Schule"),
    ("oficina", "Büro"), ("coche", "Auto"), ("tren", "Zug"), ("avión", "Flugzeug"),
    ("ir", "gehen"), ("venir", "kommen"), ("decir", "sagen"), ("ver", "sehen"),
    ("saber", "wissen"), ("poder", "können"), ("querer", "wollen"),
    ("tomar", "nehmen"), ("dar", "geben"), ("poner", "stellen"), ("encontrar", "finden"),
    ("gran", "groß"), ("pequeño", "klein"), ("nuevo", "neu"), ("bueno", "gut"), ("malo", "schlecht"),
    ("bello", "schön"), ("viejo", "alt"), ("joven", "jung"), ("largo", "lang"), ("corto", "kurz"),
    ("alto", "hoch"), ("bajo", "niedrig"), ("fuerte", "stark"), ("débil", "schwach"),
    ("fácil", "einfach"), ("difícil", "schwer"), ("posible", "möglich"),
    ("importante", "wichtig"), ("rojo", "rot"), ("azul", "blau"), ("verde", "grün"),
    ("amarillo", "gelb"), ("blanco", "weiß"), ("negro", "schwarz"), ("gris", "grau"),
    ("uno", "eins"), ("dos", "zwei"), ("tres", "drei"), ("cuatro", "vier"), ("cinco", "fünf"),
    ("seis", "sechs"), ("siete", "sieben"), ("ocho", "acht"), ("nueve", "neun"), ("diez", "zehn"),
    ("lunes", "Montag"), ("martes", "Dienstag"), ("miércoles", "Mittwoch"),
    ("enero", "Januar"), ("febrero", "Februar"), ("marzo", "März"), ("abril", "April"),
    ("mayo", "Mai"), ("junio", "Juni"), ("julio", "Juli"), ("agosto", "August"),
    ("septiembre", "September"), ("octubre", "Oktober"), ("noviembre", "November"), ("diciembre", "Dezember"),
    ("gracias", "danke"), ("por favor", "bitte"), ("hola", "hallo"),
    ("adiós", "auf Wiedersehen"), ("sí", "ja"), ("no", "nein"),
]


def extend_to(pairs, n):
    """Extend list of (front, back) pairs to exactly n items by cycling."""
    result = list(pairs)
    while len(result) < n:
        result.extend(pairs[: n - len(result)])
    return result[:n]


def fetch_yacle_en_de():
    """Fetch yacle 5k EN-DE TSV and return list of (en, de) pairs. Returns None on failure."""
    try:
        ctx = ssl.create_default_context()
        with urllib.request.urlopen(YACLE_URL, context=ctx, timeout=15) as r:
            text = r.read().decode("utf-8")
        pairs = []
        for line in text.strip().split("\n"):
            line = line.strip()
            if not line:
                continue
            parts = line.split("\t", 1)
            if len(parts) == 2:
                pairs.append((parts[0].strip(), parts[1].strip()))
        return pairs if len(pairs) >= 5000 else None
    except Exception as e:
        print(f"Warning: Could not fetch yacle 5k ({e}). Using EN_DE fallback.")
        return None


def write_csv(path, pairs):
    """Write CSV with Front,Back,Note header and one row per pair."""
    lines = [HEADER] + [f"{a},{b}," for a, b in pairs]
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    print(f"Wrote {path} with {len(pairs)} rows")


def main():
    base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    assets = os.path.join(base, "app", "src", "main", "assets")
    os.makedirs(assets, exist_ok=True)

    # --- English-German (yacle 5k or EN_DE fallback) ---
    en_de_full = fetch_yacle_en_de()
    if en_de_full is None:
        en_de_full = extend_to(EN_DE, 5000)
    en_de_full = en_de_full[:5000]
    write_csv(os.path.join(assets, "english_german_basic.csv"), en_de_full[0:1000])
    write_csv(os.path.join(assets, "english_german_advanced.csv"), en_de_full[1000:2000])
    write_csv(os.path.join(assets, "english_german_expert.csv"), en_de_full[2000:5000])

    # --- Italian-German (EXTRA_IT_DE, optionally starter) ---
    it_full = extend_to(EXTRA_IT_DE, 5000)
    write_csv(os.path.join(assets, "italian_german_basic.csv"), it_full[0:1000])
    write_csv(os.path.join(assets, "italian_german_advanced.csv"), it_full[1000:2000])
    write_csv(os.path.join(assets, "italian_german_expert.csv"), it_full[2000:5000])

    # --- French-German ---
    fr_full = extend_to(FR_DE, 5000)
    write_csv(os.path.join(assets, "french_german_basic.csv"), fr_full[0:1000])
    write_csv(os.path.join(assets, "french_german_advanced.csv"), fr_full[1000:2000])
    write_csv(os.path.join(assets, "french_german_expert.csv"), fr_full[2000:5000])

    # --- Spanish-German ---
    es_full = extend_to(ES_DE, 5000)
    write_csv(os.path.join(assets, "spanish_german_basic.csv"), es_full[0:1000])
    write_csv(os.path.join(assets, "spanish_german_advanced.csv"), es_full[1000:2000])
    write_csv(os.path.join(assets, "spanish_german_expert.csv"), es_full[2000:5000])


if __name__ == "__main__":
    main()
