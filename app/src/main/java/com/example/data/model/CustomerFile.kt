package com.example.data.model

data class LegalAppointment(
    val sectionRef: String,
    val roleTitle: String,
    val personName: String,
    val telephone: String,
    val mobile: String,
    val email: String,
    val appointmentDocRef: String = "Refer to letter of appointment"
)

data class TracklessMachine(
    val make: String,
    val fleetNumber: String,
    val model: String,
    val description: String,
    val powerKw: String = ""
)

data class FireExtinguisherItem(
    val number: Int,
    val extinguisherCode: String,
    val typeAndSize: String,
    val location: String
)

data class HegEntry(
    val areaNumber: String,
    val areaDescription: String,
    val hegNumber: String,
    val description: String,
    val dmeCode: String,
    val dmeDescription: String
)

data class CustomerOperationalFile(
    val id: String,
    val documentNumber: String,
    val draftingDate: String,
    val revision: String,
    val nextRevisionDate: String,
    val customerName: String,
    val legalEntityName: String,
    val registeredTradeName: String,
    val companyRegistrationNumber: String,
    val vatRegistrationNumber: String,
    val controlGroupName: String,
    val controlGroupRegNumber: String,
    val controlGroupVatNumber: String,
    val registeredPhysicalAddress: String,
    val registeredPostalAddress: String,
    val controlGroupAddress: String,
    val siteName: String,
    val propertyDescription: String,
    val locationDescription: String,
    val gpsCoordinates: String,
    val miningRightNumber: String,
    val waterLicenseNumber: String,
    val commoditiesProduced: String,
    val operationDescription: String,
    val mechanisationLevel: String,
    val appointments: List<LegalAppointment>,
    val hegs: List<HegEntry>,
    val mobileMachines: List<TracklessMachine>,
    val fireExtinguishers: List<FireExtinguisherItem>,
    val emergencyHospitals: List<String>,
    val mandatoryCOPs: List<String>
)

object CustomerFileData {

    val AFRIMAT_FILE = CustomerOperationalFile(
        id = "cust_afrimat_kuipersbult",
        documentNumber = "WFH-AFR-KUI-OD-26-001",
        draftingDate = "29 September 2026",
        revision = "00",
        nextRevisionDate = "02 January 2027",
        customerName = "Afrimat CM North (Pty) Ltd",
        legalEntityName = "Afrimat CM North (Pty) Ltd",
        registeredTradeName = "Afrimat CM North (Pty) Ltd",
        companyRegistrationNumber = "2002 / 025895 / 07",
        vatRegistrationNumber = "4910205428",
        controlGroupName = "Afrimat Ltd",
        controlGroupRegNumber = "2006 / 022534 / 06",
        controlGroupVatNumber = "4200236356",
        registeredPhysicalAddress = "3 Sennett Street, Worcester, Western Cape, 0850",
        registeredPostalAddress = "P O Box 505, Worcester, Western Cape, 6849",
        controlGroupAddress = "Tyger Valley Office Park, Building No 2, Cnr Willie van Schoor & Old Oak Rd, Bellville 7530",
        siteName = "Kuipersbult Quarry & Ready-Mix Plant",
        propertyDescription = "Remainder of the farm Kuipersbult 511 LQ",
        locationDescription = "Lephalale, Magisterial Administrative District of Lephalale, Limpopo Province",
        gpsCoordinates = "-23.70760° S, 27.57939° E",
        miningRightNumber = "LP30/5/1/2/2/10023MR (Granted 12 Feb 2014, 30 Year Duration ending 11 Feb 2044)",
        waterLicenseNumber = "01/A42J/AGI/4378 (File 27/2/2/A942/4/1, 1,828 m³/a Abstraction, 53,000 m³/a Pit Water)",
        commoditiesProduced = "Various sized aggregate and ready-mix concrete",
        operationDescription = "Opencast quartzitic sandstone and conglomerate aggregate mine with a ready-mix batching plant. Drilling, blasting, crushing, screening, and delivery.",
        mechanisationLevel = "±80% mechanised with approximately 20% manual support",
        appointments = listOf(
            LegalAppointment(
                sectionRef = "Section 2A(1)",
                roleTitle = "Chief Executive Officer",
                personName = "Mr A J Van Heerden",
                telephone = "+27 (0)21 917 8840",
                mobile = "Head Office Direct",
                email = "andries@afrimat.co.za"
            ),
            LegalAppointment(
                sectionRef = "Section 2A(2) / 4(1)",
                roleTitle = "Employer Representative",
                personName = "Mr C Reyneke",
                telephone = "+27 (0)12 664 5649",
                mobile = "+27 (0)78 893 2725",
                email = "Casper.reyneke@afrimat.co.za"
            ),
            LegalAppointment(
                sectionRef = "Section 3(1)",
                roleTitle = "Mine Manager",
                personName = "Mr P K Lekgeu",
                telephone = "+27 (0)60 742 8208",
                mobile = "+27 (0)60 742 8208",
                email = "Peter.lekgeu@afrimat.co.za"
            ),
            LegalAppointment(
                sectionRef = "Section 12(1)",
                roleTitle = "Occupational Hygienist",
                personName = "Mr N B Letlape",
                telephone = "+27 (0)67 418 4168",
                mobile = "+27 (0)67 418 4168",
                email = "ChiefOH@workforcehealthsa.co.za"
            ),
            LegalAppointment(
                sectionRef = "Section 9(2)",
                roleTitle = "Occupational Hygienist Assistant",
                personName = "Mr N B Ngobese",
                telephone = "+27 (0)78 708 9415",
                mobile = "+27 (0)78 708 9415",
                email = "admin@workforcehealthsa.co.za",
                appointmentDocRef = "WFH-AFR-KUI-LA-OH-ASS-26-001"
            ),
            LegalAppointment(
                sectionRef = "Section 13(3)(a)(i)",
                roleTitle = "Occupational Medical Practitioner",
                personName = "Dr K O Sebopa",
                telephone = "+27 (0)82 925 1158",
                mobile = "+27 (0)82 925 1158",
                email = "drmasenyaolivia@gmail.com"
            ),
            LegalAppointment(
                sectionRef = "Schedule 4, Reg 2.13.2",
                roleTitle = "Competent Person – Mechanical",
                personName = "Mr H J Pienaar",
                telephone = "+27 (0)78 457 2443",
                mobile = "+27 (0)78 457 2443",
                email = "henniepienaarhh@gmail.com"
            ),
            LegalAppointment(
                sectionRef = "Schedule 4, Reg 2.13.2",
                roleTitle = "Competent Person – Electrical",
                personName = "Mr J M A Spangenberg",
                telephone = "+27 (0)73 527 1840",
                mobile = "+27 (0)73 527 1840",
                email = "ditshabaelectrical@gmail.com"
            ),
            LegalAppointment(
                sectionRef = "Schedule 4, Reg 2.17.1",
                roleTitle = "Safety Officer",
                personName = "Ms W O Mandoma",
                telephone = "+27 (0)78 226 6304",
                mobile = "+27 (0)78 226 6304",
                email = "Winnie.mandoma@afrimat.co.za"
            ),
            LegalAppointment(
                sectionRef = "Section 29(4), Reg 6.9",
                roleTitle = "Health and Safety Representative",
                personName = "Ms W G Jordaan",
                telephone = "+27 (0)76 224 9818",
                mobile = "+27 (0)76 224 9818",
                email = "lephalaleRMC2@afrimat.co.za"
            ),
            LegalAppointment(
                sectionRef = "Regulation 17",
                roleTitle = "Mine Surveyor",
                personName = "Mr G Brent-meek",
                telephone = "+27 (0)82 776 3607",
                mobile = "+27 (0)82 776 3607",
                email = "gbm@belanet.co.za"
            ),
            LegalAppointment(
                sectionRef = "Regulation 14.1",
                roleTitle = "Rock Engineer",
                personName = "Mr M Steinman",
                telephone = "+27 (0)82 378 9378",
                mobile = "+27 (0)82 378 9378",
                email = "steinmanngsp@yahoo.com"
            ),
            LegalAppointment(
                sectionRef = "Schedule 4, Reg 2.9.2",
                roleTitle = "Assistant to the Manager (Team Leader)",
                personName = "Mr M K Mphogo",
                telephone = "+27 (0)82 605 5779",
                mobile = "+27 (0)82 605 5779",
                email = "lephalalermc@afrimat.co.za"
            ),
            LegalAppointment(
                sectionRef = "Accounts",
                roleTitle = "Contact Official (Accounts)",
                personName = "Mr Reinhard Stander",
                telephone = "+27 (0)76 233 6399",
                mobile = "+27 (0)76 233 6399",
                email = "Reinhard.stander@afrimat.co.za"
            )
        ),
        hegs = listOf(
            HegEntry("1", "Kuipersbult Quarry - Opencast", "1", "Drilling", "30", "Roving plant"),
            HegEntry("1", "Kuipersbult Quarry - Opencast", "2", "Blasting", "07", "Opencast"),
            HegEntry("2", "Kuipersbult Quarry – Self-propelled mobile machines", "1", "Self-propelled mobile machines (excluding drill rigs)", "30", "Roving plant"),
            HegEntry("3", "Kuipersbult Quarry – Surface production", "1", "Crushing and screening plant/s", "20", "Crushing"),
            HegEntry("3", "Kuipersbult Quarry – Surface production", "2", "Management, admin and support services", "31", "Roving surface"),
            HegEntry("4", "Kuipersbult Quarry – Ready-mix plant", "1", "Batching / ready-mix plant", "29", "Final product"),
            HegEntry("5", "Kuipersbult Quarry – Engineering maintenance", "1", "Support services / surface workshops", "33", "Surface workshops")
        ),
        mobileMachines = listOf(
            TracklessMachine("Komatsu", "HF 063", "B30D", "Dump truck", "240 kW"),
            TracklessMachine("Bell", "HF 053", "B30", "Dump truck", "210 kW"),
            TracklessMachine("Bell", "NTSAD", "B45", "Dump truck", "320 kW"),
            TracklessMachine("Bell", "NTSADT", "B40", "Dump truck", "290 kW"),
            TracklessMachine("Hitachi", "FEL-01", "ZW220", "Front end loader", "145 kW"),
            TracklessMachine("Komatsu", "AMC 008", "WA008", "Front end loader", "155 kW"),
            TracklessMachine("Hyundai", "S 05", "220 LC95", "Excavator", "110 kW"),
            TracklessMachine("Volvo", "MPH 002", "EC380D", "Excavator", "215 kW"),
            TracklessMachine("Doosan", "CV 001", "DX300", "Excavator", "147 kW"),
            TracklessMachine("Bell", "HF 78", "380D", "Excavator", "208 kW"),
            TracklessMachine("Hino", "WT-04", "500 Series", "Water tanker / bowser", "190 kW")
        ),
        fireExtinguishers = listOf(
            FireExtinguisherItem(1, "VTM01", "Dry Chemical Powder 9KG", "Admin Office"),
            FireExtinguisherItem(2, "VTM02", "Dry Chemical Powder 9KG", "Geologist Office"),
            FireExtinguisherItem(3, "VTM03", "Dry Chemical Powder 9KG", "Water Truck"),
            FireExtinguisherItem(4, "VTM005", "Dry Chemical Powder 9KG", "Diesel Bowser"),
            FireExtinguisherItem(5, "VTM004", "Dry Chemical Powder 250KG", "Diesel Bowser Depot"),
            FireExtinguisherItem(6, "VTM003", "Dry Chemical Powder 250KG", "Diesel Bowser Depot"),
            FireExtinguisherItem(7, "VTM014", "Dry Chemical Powder 9KG", "SANY Excavator MAJ008"),
            FireExtinguisherItem(8, "VTM050", "Dry Chemical Powder 9KG", "Komatsu HF04"),
            FireExtinguisherItem(9, "VTM016", "Dry Chemical Powder 9KG", "Kobelco Excavator HF78"),
            FireExtinguisherItem(10, "VTM017", "Dry Chemical Powder 9KG", "Bell ADT HF67"),
            FireExtinguisherItem(11, "VTM22", "Dry Chemical Powder 9KG", "Bell ADT HF053"),
            FireExtinguisherItem(12, "VTM007", "Dry Chemical Powder 9KG", "AMC Tools Container"),
            FireExtinguisherItem(13, "AMC008", "Dry Chemical Powder 9KG", "AMC Boiler Maker Store"),
            FireExtinguisherItem(14, "AMC006", "Dry Chemical Powder 9KG", "Behind Store Room AMC006"),
            FireExtinguisherItem(15, "AMC015", "Dry Chemical Powder 9KG", "AMC Fatigue Room AMC015"),
            FireExtinguisherItem(16, "VTM014", "Dry Chemical Powder 9KG", "AMC Generator 002 AMC005"),
            FireExtinguisherItem(17, "AMC001", "Dry Chemical Powder 9KG", "AMC Generator Exit AMC001"),
            FireExtinguisherItem(18, "VTM024", "Dry Chemical Powder 9KG", "AMC Electrical DB Box AMC029")
        ),
        emergencyHospitals = listOf(
            "Lephalale Control Centre",
            "Marapong Private Hospital (Marapong Township)",
            "Mediclinic Lephalale",
            "Ellisras District Hospital",
            "Dr Seropa Surgery Lephalale",
            "Lephalale Fire & Disaster Management Centre",
            "Lephalale Police Station & Marapong Mobile Station"
        ),
        mandatoryCOPs = listOf(
            "MCOP: Safe use of conveyor belt installations for transportation of minerals & personnel",
            "MCOP: Occupational health programme on personal exposure to airborne pollutants",
            "MCOP: Occupational health programme for personal exposure to noise",
            "MCOP: Occupational health programme for thermal stress",
            "MCOP: Prevention, mitigation, and management of COVID-19 outbreak",
            "MCOP: Minimum standards of fitness to perform work on a mine",
            "MCOP: Management of medical incapacity due to ill-health and injury",
            "MCOP: Risk-based emergency medical care on a mine",
            "MCOP: Provision of PPE for women in mining",
            "MCOP: Prevention of fires at mines",
            "MCOP: Emergency preparedness and response",
            "MCOP: Trackless mobile machinery safety and collision management",
            "MCOP: Risk-based fatigue management at mines",
            "MCOP: Management of working in confined spaces",
            "MCOP: Quality assurance programme for occupational hygiene and ventilation measurements",
            "MCOP: Right to refuse dangerous work and leave a dangerous working place"
        )
    )
}
