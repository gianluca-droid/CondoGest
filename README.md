# 🏢 CondoGest - Gestione Spese Condominiali

App Android nativa per la gestione completa delle spese, pagamenti e cedolini condominiali.

## ✨ Funzionalità

### 📊 Dashboard
- Panoramica finanziaria con totale spese, incassi e saldo
- Breakdown spese per categoria con barre percentuali
- Ultime spese e pagamenti

### 🏠 Unità & Condòmini
- Registro unità immobiliari (appartamenti, locali, box)
- Anagrafica condòmini con dati di contatto
- Tabelle millesimali per ripartizione

### 💰 Registrazione Spese
- 11 categorie predefinite (manutenzione, pulizia, ascensore, ecc.)
- Importo, descrizione, note e data
- Lista cronologica con filtri

### 💳 Pagamenti
- Registrazione pagamenti per unità
- 4 metodi: **Portale**, **Cedolino**, **Bonifico**, **Contanti**
- Filtro per metodo di pagamento
- Supporto sia per portali condominiali che cedolini cartacei

### 📄 Cedolini di Pagamento
- **Generazione automatica** per tutte le unità
- Ripartizione millesimale automatica
- Dettaglio voci di spesa per ogni cedolino
- Tracking stato: Emesso, Pagato, Scaduto, Parziale
- Funzione "Segna come pagato"

### 📈 Report & Statistiche
- Riepilogo finanziario completo
- Situazione per singolo condòmino (versato/dovuto/saldo)
- Dettaglio spese per categoria
- **Esportazione CSV** condivisibile

## 🛠️ Tecnologie

| Tecnologia | Utilizzo |
|---|---|
| **Kotlin** | Linguaggio principale |
| **Jetpack Compose** | UI declarativa |
| **Material 3** | Design system |
| **Room** | Database locale (SQLite) |
| **Navigation Compose** | Navigazione tra schermate |
| **Coroutines + Flow** | Programmazione asincrona reattiva |
| **ViewModel** | Gestione stato |
| **KSP** | Annotation processing (Room) |

## 📱 Requisiti

- Android 8.0 (API 26) o superiore
- Android Studio Hedgehog o superiore

## 🚀 Come iniziare

1. **Clona il repository**
   ```bash
   git clone https://github.com/TUOUSERNAME/CondoGest.git
   ```

2. **Apri in Android Studio**
   - File → Open → seleziona la cartella `CondoGest`

3. **Sincronizza Gradle**
   - Android Studio sincronizzerà automaticamente le dipendenze

4. **Esegui l'app**
   - Seleziona un emulatore o dispositivo fisico
   - Clicca Run (▶️)

## 📂 Struttura Progetto

```
app/src/main/java/com/condogest/app/
├── CondoGestApp.kt          # Application class
├── MainActivity.kt          # Activity principale con navigazione
├── data/
│   ├── model/Entities.kt    # Entità Room (Unit, Expense, Payment, Cedolino)
│   ├── dao/Daos.kt          # Data Access Objects
│   ├── database/AppDatabase.kt
│   ├── repository/CondoRepository.kt
│   └── SampleData.kt        # Dati demo italiani
├── ui/
│   ├── theme/               # Color, Type, Theme (Material 3 Dark)
│   ├── navigation/Screen.kt # Definizione schermate
│   ├── components/Components.kt  # Componenti riutilizzabili
│   └── screens/
│       ├── DashboardScreen.kt
│       ├── UnitsScreen.kt
│       ├── ExpensesScreen.kt
│       ├── PaymentsScreen.kt
│       ├── CedoliniScreen.kt
│       └── ReportsScreen.kt
└── viewmodel/CondoViewModel.kt
```

## 🎨 Design

- **Dark Theme** premium con palette ciano/viola
- **Material Design 3** con NavigationBar bottom
- Cards con sfondo scuro e accenti colorati
- Badge di stato colorati per cedolini e pagamenti
- Transizioni animate tra le schermate

## 📊 Dati Demo

Al primo avvio l'app viene precaricata con dati realistici:
- 8 unità immobiliari con condòmini italiani
- 13 spese degli ultimi 6 mesi
- 10 pagamenti con vari metodi
- 4 cedolini di esempio

## 📄 Licenza

MIT License - Vedi [LICENSE](LICENSE) per i dettagli.
