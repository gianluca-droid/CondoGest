package com.condogest.app.data

import com.condogest.app.data.model.*
import com.condogest.app.data.repository.CondoRepository
import java.util.*

object SampleData {
    suspend fun populateDatabase(repository: CondoRepository) {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // ─── Unità Immobiliari ───────────────────────────────────
        val units = listOf(
            CondoUnit(number = "1", floor = 0, type = "Locale", areaMq = 45.0, millesimi = 55.0, ownerName = "Mario Bianchi", ownerEmail = "mario.bianchi@email.it", ownerPhone = "333 1001001"),
            CondoUnit(number = "2", floor = 1, type = "Appartamento", areaMq = 85.0, millesimi = 125.0, ownerName = "Lucia Verdi", ownerEmail = "lucia.verdi@email.it", ownerPhone = "339 2002002"),
            CondoUnit(number = "3", floor = 1, type = "Appartamento", areaMq = 70.0, millesimi = 105.0, ownerName = "Giuseppe Russo", ownerEmail = "g.russo@email.it", ownerPhone = "347 3003003"),
            CondoUnit(number = "4", floor = 2, type = "Appartamento", areaMq = 95.0, millesimi = 140.0, ownerName = "Anna Ferrari", ownerEmail = "anna.ferrari@email.it", ownerPhone = "328 4004004"),
            CondoUnit(number = "5", floor = 2, type = "Appartamento", areaMq = 80.0, millesimi = 120.0, ownerName = "Paolo Esposito", ownerEmail = "p.esposito@email.it", ownerPhone = "366 5005005"),
            CondoUnit(number = "6", floor = 3, type = "Appartamento", areaMq = 110.0, millesimi = 160.0, ownerName = "Francesca Romano", ownerEmail = "f.romano@email.it", ownerPhone = "345 6006006"),
            CondoUnit(number = "7", floor = 3, type = "Appartamento", areaMq = 75.0, millesimi = 110.0, ownerName = "Marco Colombo", ownerEmail = "m.colombo@email.it", ownerPhone = "320 7007007"),
            CondoUnit(number = "8", floor = 4, type = "Appartamento", areaMq = 120.0, millesimi = 185.0, ownerName = "Sara Ricci", ownerEmail = "sara.ricci@email.it", ownerPhone = "351 8008008")
        )
        val unitIds = units.map { repository.insertUnit(it) }

        // ─── Spese (ultimi 6 mesi) ──────────────────────────────
        fun daysAgo(d: Int): Long { val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, -d); return c.timeInMillis }

        val expenses = listOf(
            Expense(date = daysAgo(150), category = "Pulizia", description = "Pulizia scale e androni - Gennaio", amount = 380.0, notes = "Fatt. 012/2026"),
            Expense(date = daysAgo(140), category = "Illuminazione", description = "Sostituzione lampade LED piano terra", amount = 185.0),
            Expense(date = daysAgo(120), category = "Manutenzione Ordinaria", description = "Riparazione portone ingresso", amount = 450.0, notes = "Fatt. 045/2026"),
            Expense(date = daysAgo(110), category = "Acqua", description = "Bolletta acqua I trimestre", amount = 1250.0),
            Expense(date = daysAgo(90), category = "Pulizia", description = "Pulizia scale e androni - Marzo", amount = 380.0, notes = "Fatt. 089/2026"),
            Expense(date = daysAgo(80), category = "Ascensore", description = "Manutenzione ordinaria ascensore", amount = 320.0),
            Expense(date = daysAgo(70), category = "Assicurazione", description = "Premio assicurazione annuale fabbricato", amount = 2800.0, notes = "Polizza n. 456789"),
            Expense(date = daysAgo(60), category = "Giardinaggio", description = "Manutenzione giardino condominiale", amount = 250.0),
            Expense(date = daysAgo(45), category = "Riscaldamento", description = "Manutenzione caldaia centralizzata", amount = 680.0),
            Expense(date = daysAgo(30), category = "Pulizia", description = "Pulizia scale e androni - Maggio", amount = 380.0),
            Expense(date = daysAgo(20), category = "Amministrazione", description = "Compenso amministratore II trimestre", amount = 1500.0),
            Expense(date = daysAgo(10), category = "Manutenzione Ordinaria", description = "Riparazione citofono int. 4 e 7", amount = 220.0),
            Expense(date = daysAgo(5), category = "Manutenzione Straordinaria", description = "Rifacimento impermeabilizzazione terrazzo", amount = 4500.0, notes = "Delibera assemblea 15/04")
        )
        expenses.forEach { repository.insertExpense(it) }

        // ─── Pagamenti ──────────────────────────────────────────
        val payments = listOf(
            Payment(unitId = unitIds[0], amount = 350.0, date = daysAgo(100), method = "Bonifico", reference = "BON-2026-001"),
            Payment(unitId = unitIds[1], amount = 520.0, date = daysAgo(95), method = "Portale", reference = "PRT-2026-012"),
            Payment(unitId = unitIds[2], amount = 430.0, date = daysAgo(90), method = "Cedolino", reference = "CED-2026-003"),
            Payment(unitId = unitIds[3], amount = 580.0, date = daysAgo(85), method = "Bonifico", reference = "BON-2026-004"),
            Payment(unitId = unitIds[4], amount = 490.0, date = daysAgo(80), method = "Portale", reference = "PRT-2026-015"),
            Payment(unitId = unitIds[5], amount = 650.0, date = daysAgo(60), method = "Contanti"),
            Payment(unitId = unitIds[1], amount = 520.0, date = daysAgo(35), method = "Portale", reference = "PRT-2026-028"),
            Payment(unitId = unitIds[3], amount = 580.0, date = daysAgo(25), method = "Bonifico", reference = "BON-2026-009"),
            Payment(unitId = unitIds[7], amount = 750.0, date = daysAgo(20), method = "Portale", reference = "PRT-2026-033"),
            Payment(unitId = unitIds[0], amount = 350.0, date = daysAgo(10), method = "Cedolino", reference = "CED-2026-011")
        )
        payments.forEach { repository.insertPayment(it) }

        // ─── Cedolini ───────────────────────────────────────────
        for (i in 0..3) {
            val items = listOf(
                CedolinoItem(cedolinoId = 0, description = "Pulizia scale", amount = 380.0 * units[i].millesimi / 1000),
                CedolinoItem(cedolinoId = 0, description = "Manutenzione ord.", amount = 450.0 * units[i].millesimi / 1000),
                CedolinoItem(cedolinoId = 0, description = "Acqua", amount = 1250.0 * units[i].millesimi / 1000),
                CedolinoItem(cedolinoId = 0, description = "Ascensore", amount = 320.0 * units[i].millesimi / 1000)
            )
            val total = items.sumOf { it.amount }
            val status = if (i < 2) "Pagato" else "Emesso"
            val cedolino = Cedolino(
                unitId = unitIds[i], period = "I Trimestre 2026",
                issueDate = daysAgo(100), dueDate = daysAgo(70),
                total = total, status = status,
                paidAmount = if (i < 2) total else 0.0,
                paidDate = if (i < 2) daysAgo(90) else null
            )
            repository.insertCedolinoWithItems(cedolino, items)
        }
    }
}
