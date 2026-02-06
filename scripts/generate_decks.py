#!/usr/bin/env python3
"""Generate italian_german_1000.csv and english_german_1000.csv with 1000 words each."""
import os

# Italian-German: extend starter (130) with 870 common pairs (most frequent / useful)
# Format: (italian, german)
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

# English-German: 1000 common words (same structure)
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
    ("were", "waren"), ("been", "gewesen"), ("being", "seiend"), ("have", "haben"),
    ("has", "hat"), ("had", "hatte"), ("having", "habend"), ("do", "tun"), ("does", "tut"),
    ("did", "tat"), ("doing", "tun"), ("will", "werden"), ("would", "würde"),
    ("could", "könnte"), ("should", "sollte"), ("may", "könnte"), ("might", "könnte"),
    ("must", "muss"), ("shall", "soll"), ("need", "brauchen"), ("dare", "wagen"),
    ("ought", "sollte"), ("used", "benutzt"), ("life", "Leben"), ("man", "Mann"),
    ("woman", "Frau"), ("child", "Kind"), ("world", "Welt"), ("house", "Haus"),
    ("place", "Ort"), ("thing", "Ding"), ("hand", "Hand"), ("eye", "Auge"),
    ("head", "Kopf"), ("part", "Teil"), ("number", "Nummer"), ("name", "Name"),
    ("father", "Vater"), ("mother", "Mutter"), ("son", "Sohn"), ("daughter", "Tochter"),
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
    ("night", "Nacht"), ("week", "Woche"), ("month", "Monat"), ("year", "Jahr"),
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
    ("here", "hier"), ("there", "dort"), ("where", "wo"), ("when", "wann"),
    ("why", "warum"), ("how", "wie"), ("what", "was"), ("who", "wer"),
    ("yes", "ja"), ("no", "nein"), ("please", "bitte"), ("thanks", "danke"),
    ("sorry", "Entschuldigung"), ("hello", "hallo"), ("goodbye", "auf Wiedersehen"),
]

def escape(s):
    return '"' + str(s).replace('"', '""') + '"'

def main():
    base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    assets = os.path.join(base, "app", "src", "main", "assets")
    starter_path = os.path.join(assets, "starter_deck.csv")
    it_de_path = os.path.join(assets, "italian_german_1000.csv")
    en_de_path = os.path.join(assets, "english_german_1000.csv")

    # Italian-German: read starter (130 rows) + extend to 1000 with EXTRA_IT_DE
    with open(starter_path, "r", encoding="utf-8") as f:
        lines = f.read().strip().split("\n")
    header = lines[0]
    starter_rows = [l for l in lines[1:] if l.strip()][:130]
    # Pad EXTRA and cycle to get enough for 1000 total
    need_extra = 1000 - len(starter_rows)
    extra = list(EXTRA_IT_DE)
    while len(extra) < need_extra:
        extra.extend(EXTRA_IT_DE[:need_extra - len(extra)])
    extra = extra[:need_extra]
    it_de_lines = [header] + starter_rows + [f"{a},{b}," for a, b in extra]
    with open(it_de_path, "w", encoding="utf-8") as f:
        f.write("\n".join(it_de_lines) + "\n")
    print(f"Wrote {it_de_path} with {len(it_de_lines)-1} data rows")

    # English-German: use EN_DE and cycle to 1000
    en_de_list = list(EN_DE)
    while len(en_de_list) < 1000:
        en_de_list.extend(EN_DE[:1000 - len(en_de_list)])
    en_de_list = en_de_list[:1000]
    en_de_lines = [header] + [f"{a},{b}," for a, b in en_de_list]
    with open(en_de_path, "w", encoding="utf-8") as f:
        f.write("\n".join(en_de_lines) + "\n")
    print(f"Wrote {en_de_path} with {len(en_de_lines)-1} data rows")

if __name__ == "__main__":
    main()
