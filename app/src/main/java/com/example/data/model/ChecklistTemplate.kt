package com.example.data.model

object ChecklistTemplate {

    val TEMPLATE_TITLES = listOf(
        "Monthly ablution facilities checklist *Male",
        "Monthly ablution facilities checklist *Female",
        "Monthly toilet and basin checklist *Male / Female"
    )

    fun createDefaultAblutionChecklist(): List<ChecklistItem> {
        val items = mutableListOf<ChecklistItem>()

        // SECTION A — BASINS
        val basins = listOf(
            "Are the basins clean?",
            "Are the soap dispensers filled with liquid soap?",
            "Do both water taps close properly?",
            "Is there a sign/poster above each basin illustrating the correct way to wash your hands properly?",
            "Are there any signs of moulds / yeast / mildew around or inside the basin?"
        )
        basins.forEachIndexed { index, q ->
            items.add(
                ChecklistItem(
                    id = "A_${index + 1}",
                    sectionCode = "A",
                    sectionTitle = "SECTION A — BASINS",
                    questionNumber = index + 1,
                    questionText = q
                )
            )
        }

        // SECTION B — SHOWERS
        val showers = listOf(
            "Are the showers clean?",
            "Do both water taps close properly?",
            "Are proper showerheads fitted on all the showers?",
            "Are the shower curtains / doors in good condition and close properly, providing the necessary privacy?",
            "Are there any signs of moulds / yeast / mildew on the shower floors and / or walls?"
        )
        showers.forEachIndexed { index, q ->
            items.add(
                ChecklistItem(
                    id = "B_${index + 1}",
                    sectionCode = "B",
                    sectionTitle = "SECTION B — SHOWERS",
                    questionNumber = index + 1,
                    questionText = q
                )
            )
        }

        // SECTION C — TOILETS
        val toilets = listOf(
            "Are the toilets clean and the floors dry?",
            "Are all toilet water bowls fitted with lids?",
            "Are all toilets fitted with sitting seats?",
            "Do all the toilets flush properly?",
            "Is there enough toilet paper in all the toilets?",
            "Do all the toilet doors close properly?",
            "Are there any signs of moulds / yeast / mildew?"
        )
        toilets.forEachIndexed { index, q ->
            items.add(
                ChecklistItem(
                    id = "C_${index + 1}",
                    sectionCode = "C",
                    sectionTitle = "SECTION C — TOILETS",
                    questionNumber = index + 1,
                    questionText = q
                )
            )
        }

        // SECTION D — URINALS
        val urinals = listOf(
            "Are all urinals clean?",
            "Do all the urinals flush properly?",
            "Are the floors around the urinals clean and dry?",
            "Are there any bad odours in the urinal area?",
            "Are there any signs of moulds / yeast / mildew?"
        )
        urinals.forEachIndexed { index, q ->
            items.add(
                ChecklistItem(
                    id = "D_${index + 1}",
                    sectionCode = "D",
                    sectionTitle = "SECTION D — URINALS",
                    questionNumber = index + 1,
                    questionText = q
                )
            )
        }

        // SECTION E — LOCKERS
        val lockers = listOf(
            "Are all employees issued with their own lockers?",
            "Are all lockers marked with their respective owner's names and / or numbers?",
            "Do the doors of the lockers close properly?",
            "Can the doors of the lockers lock?",
            "Is the locker area clean?"
        )
        lockers.forEachIndexed { index, q ->
            items.add(
                ChecklistItem(
                    id = "E_${index + 1}",
                    sectionCode = "E",
                    sectionTitle = "SECTION E — LOCKERS",
                    questionNumber = index + 1,
                    questionText = q
                )
            )
        }

        // SECTION F — GENERAL
        val general = listOf(
            "Is there a sign on the change house door indicating the gender?",
            "Are there signs / posters above every basin illustrating the correct way to wash your hands properly?",
            "Are there signs / posters at the showers educating employees about the dangers of fungal infections?",
            "Is there a rubbish bin inside the change house?",
            "Is there a bin liner in the rubbish bin?",
            "Is the change house deeply sanitised — state frequency under remarks column?",
            "Has a certificate been issued by the service provider who did the sanitising?",
            "Is there a cleaning schedule and register in the change house — state frequency under remarks column?",
            "Is the register signed off after cleaning and up to date?",
            "Is there a complaint / complement book in the change house?",
            "Is the change house clean and tidy?",
            "Is personal hygiene in the change house up to standard?",
            "Do the change house doors close automatically and properly?",
            "Are there any bad odours in the change house?",
            "Are employees trained on the importance of personal hygiene at work — state frequency under remarks column?",
            "Has a ventilation assessment been conducted in the facility — reference date and report number in the remarks column?",
            "Has an illumination assessment been conducted in the facility — reference date and report number in the remarks column?",
            "Review all complaints / complements in the complaint book and log in the remarks column — if none, state this fact.",
            "List the approximate total of employees using the facility in the remarks column.",
            "Provide full details of the deep-cleaning methods (procedure) and chemicals (with safety data sheets = SDS) used by the person / organisation conducting deep cleaning — log in the remarks column.",
            "Are windows in the facility of the frosted type to protect privacy in the facility — give full details in the remarks column.",
            "Obtain full list of chemicals used for daily / weekly / monthly cleaning, excluding deep cleaning, as well as copies of safety data sheets for these chemicals.",
            "Are the facilities disabled person friendly?"
        )
        general.forEachIndexed { index, q ->
            items.add(
                ChecklistItem(
                    id = "F_${index + 1}",
                    sectionCode = "F",
                    sectionTitle = "SECTION F — GENERAL",
                    questionNumber = index + 1,
                    questionText = q
                )
            )
        }

        return items
    }

    fun defaultDeepCleaningChemicals(): List<ChemicalEntry> = listOf(
        ChemicalEntry(chemicalName = "SuperCid Sanitiser Concentrate (Diversey)", sdsReturned = true, manufacturer = "Diversey SA", remarks = "Dilution 1:50 used for quarterly autoclave deep soak"),
        ChemicalEntry(chemicalName = "Chlor-Clean High-Level Biocide", sdsReturned = true, manufacturer = "Guest Medical", remarks = "Used for wall scrub and fungal decontamination")
    )

    fun defaultRoutineCleaningChemicals(): List<ChemicalEntry> = listOf(
        ChemicalEntry(chemicalName = "Multi-Surface Pine Disinfectant", sdsReturned = true, manufacturer = "CleanPro Industrial", remarks = "Daily floor wash"),
        ChemicalEntry(chemicalName = "ActiFoam Toilet Bowl Cleaner", sdsReturned = true, manufacturer = "SafeChem SA", remarks = "Twice daily bowl clean"),
        ChemicalEntry(chemicalName = "HandSan Antimicrobial Liquid Soap", sdsReturned = true, manufacturer = "Ecolab", remarks = "Soap dispensers replenishment")
    )

    fun defaultEnvironmentalReadings(): List<EnvironmentalReading> = listOf(
        EnvironmentalReading(
            periodName = "Start of shift",
            measurementTime = "06:30",
            wetBulbTemp = "21.4",
            dryBulbTemp = "24.8",
            globeTemp = "25.1",
            wbgtIndex = "22.5",
            relativeHumidity = "72.0",
            windSpeed = "0.2",
            generalConditions = "Cool morning, overcast, normal ventilation operating"
        ),
        EnvironmentalReading(
            periodName = "Middle of shift",
            measurementTime = "12:00",
            wetBulbTemp = "24.2",
            dryBulbTemp = "29.5",
            globeTemp = "30.4",
            wbgtIndex = "26.1",
            relativeHumidity = "65.5",
            windSpeed = "0.3",
            generalConditions = "Peak temperature, mechanical extraction fans active"
        ),
        EnvironmentalReading(
            periodName = "End of shift",
            measurementTime = "16:45",
            wetBulbTemp = "22.8",
            dryBulbTemp = "26.9",
            globeTemp = "27.5",
            wbgtIndex = "24.2",
            relativeHumidity = "69.0",
            windSpeed = "0.2",
            generalConditions = "Normal shift end, showers running at full complement"
        )
    )
}
