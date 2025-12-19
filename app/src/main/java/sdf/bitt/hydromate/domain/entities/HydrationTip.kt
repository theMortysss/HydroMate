package sdf.bitt.hydromate.domain.entities

/**
 * Категория совета по гидратации
 */
enum class TipCategory(
    val displayName: String,
    val icon: String,
    val color: String
) {
    BASICS(
        displayName = "Basics",
        icon = "💧",
        color = "#2196F3"
    ),
    CAFFEINE(
        displayName = "Caffeine",
        icon = "☕",
        color = "#6F4E37"
    ),
    ALCOHOL(
        displayName = "Alcohol",
        icon = "🍺",
        color = "#F44336"
    ),
    EXERCISE(
        displayName = "Exercise",
        icon = "🏃",
        color = "#4CAF50"
    ),
    HEALTH(
        displayName = "Health",
        icon = "❤️",
        color = "#E91E63"
    ),
    SCIENCE(
        displayName = "Science",
        icon = "🔬",
        color = "#9C27B0"
    ),
    MYTHS(
        displayName = "Myths",
        icon = "💭",
        color = "#FF9800"
    )
}

/**
 * Совет по гидратации
 */
data class HydrationTip(
    val id: String,
    val category: TipCategory,
    val title: String,
    val content: String,
    val actionableAdvice: String? = null,
    val source: String? = null,
    val isViewed: Boolean = false
) {
    companion object {
        /**
         * Все советы по гидратации на основе научных исследований
         */
        fun getAllTips(): List<HydrationTip> = listOf(
            // BASICS
            HydrationTip(
                id = "tip_basics_1",
                category = TipCategory.BASICS,
                title = "The 8 Glasses Myth",
                content = "The common advice to drink 8 glasses (64oz) of water daily lacks scientific basis. Your actual needs depend on body weight, activity level, climate, and diet. A better guideline is 30-35ml per kg of body weight.",
                actionableAdvice = "Use HydroMate's personalized calculator in Settings to find your optimal daily goal.",
                source = "Institute of Medicine, 2004"
            ),
            HydrationTip(
                id = "tip_basics_2",
                category = TipCategory.BASICS,
                title = "Listen to Your Thirst",
                content = "Thirst is a reliable indicator of hydration needs for most healthy adults. By the time you feel thirsty, you're already slightly dehydrated, but this mild dehydration is normal and not harmful.",
                actionableAdvice = "Don't force excessive water intake. Drink when thirsty and monitor your urine color.",
                source = "Negoianu & Goldfarb, 2008"
            ),
            HydrationTip(
                id = "tip_basics_3",
                category = TipCategory.BASICS,
                title = "Food Provides Hydration",
                content = "About 20% of your daily water intake comes from food. Fruits and vegetables like watermelon (92% water), cucumbers (95% water), and oranges (87% water) significantly contribute to hydration.",
                actionableAdvice = "Include water-rich foods in your diet to support overall hydration goals.",
                source = "Mayo Clinic, 2022"
            ),
            HydrationTip(
                id = "tip_basics_4",
                category = TipCategory.BASICS,
                title = "Urine Color Guide",
                content = "Pale yellow urine indicates good hydration. Dark yellow suggests you need more fluids. Clear urine means you might be overhydrated. Aim for pale yellow—it's the sweet spot.",
                actionableAdvice = "Check your urine color throughout the day as a simple hydration indicator.",
                source = "Armstrong et al., 2012"
            ),

            // CAFFEINE
            HydrationTip(
                id = "tip_caffeine_1",
                category = TipCategory.CAFFEINE,
                title = "Caffeine's Modest Effect",
                content = "Moderate caffeine intake (up to 400mg/day, about 4 cups of coffee) has only a mild diuretic effect. Regular caffeine consumers develop tolerance, reducing the diuretic impact further.",
                actionableAdvice = "Coffee and tea count toward your daily fluid intake, but balance with water.",
                source = "Killer et al., 2014"
            ),
            HydrationTip(
                id = "tip_caffeine_2",
                category = TipCategory.CAFFEINE,
                title = "Coffee Hydration Index",
                content = "Research shows coffee has a Beverage Hydration Index (BHI) of about 0.8-0.9, meaning it's still hydrating, just slightly less than water (BHI = 1.0). The water in coffee compensates for most of the diuretic effect.",
                actionableAdvice = "Don't skip your morning coffee out of hydration concerns—just don't rely on it exclusively.",
                source = "Maughan et al., 2016"
            ),
            HydrationTip(
                id = "tip_caffeine_3",
                category = TipCategory.CAFFEINE,
                title = "Green Tea Benefits",
                content = "Green tea contains less caffeine (25-30mg per cup) than coffee (95mg per cup) and provides antioxidants. It's an excellent middle ground for hydration with mild caffeine benefits.",
                actionableAdvice = "Try alternating between coffee and green tea for varied caffeine intake.",
                source = "Ruxton & Hart, 2011"
            ),

            // ALCOHOL
            HydrationTip(
                id = "tip_alcohol_1",
                category = TipCategory.ALCOHOL,
                title = "Alcohol's Dehydrating Effect",
                content = "Alcohol suppresses vasopressin (ADH), the hormone that helps kidneys retain water. This causes increased urination and net fluid loss. The effect is proportional to alcohol concentration and amount consumed.",
                actionableAdvice = "For every alcoholic drink, consume one glass of water to maintain hydration.",
                source = "Hobson & Maughan, 2010"
            ),
            HydrationTip(
                id = "tip_alcohol_2",
                category = TipCategory.ALCOHOL,
                title = "The 10:1 Rule",
                content = "For every gram of alcohol consumed, expect approximately 10ml of additional urine output. A standard drink (14g alcohol) results in about 140ml extra fluid loss beyond the drink's volume.",
                actionableAdvice = "Track both alcohol and water in HydroMate to see the real impact on your hydration.",
                source = "NIAAA, 2021"
            ),
            HydrationTip(
                id = "tip_alcohol_3",
                category = TipCategory.ALCOHOL,
                title = "Beer vs. Spirits",
                content = "Light beer (3-4% ABV) is less dehydrating than wine (12% ABV) or spirits (40% ABV). Higher alcohol concentration = greater dehydrating effect. Beer's water content partially offsets its alcohol.",
                actionableAdvice = "If drinking, choose lower alcohol content beverages and pace yourself with water.",
                source = "Shirreffs & Maughan, 1997"
            ),
            HydrationTip(
                id = "tip_alcohol_4",
                category = TipCategory.ALCOHOL,
                title = "Hangover Hydration",
                content = "Dehydration is only part of a hangover—alcohol also causes inflammation and disrupts sleep. However, rehydrating with water and electrolytes significantly improves recovery time.",
                actionableAdvice = "Before bed after drinking, consume 500-750ml of water. Continue hydrating the next day.",
                source = "Healthline Medical Review, 2023"
            ),

            // EXERCISE
            HydrationTip(
                id = "tip_exercise_1",
                category = TipCategory.EXERCISE,
                title = "Pre-Exercise Hydration",
                content = "Drink 400-600ml of water 2-3 hours before exercise, then 200-300ml 10-20 minutes before starting. This optimizes hydration without causing discomfort during activity.",
                actionableAdvice = "Set a pre-workout hydration goal in HydroMate to track timing.",
                source = "American College of Sports Medicine, 2007"
            ),
            HydrationTip(
                id = "tip_exercise_2",
                category = TipCategory.EXERCISE,
                title = "Sweat Rate Matters",
                content = "Average sweat rate is 0.5-2L per hour during exercise, varying by intensity, temperature, and individual factors. Athletes can lose 6-10% of body weight through sweat in prolonged activities.",
                actionableAdvice = "Weigh yourself before and after workouts to estimate fluid loss (1kg = 1L fluid).",
                source = "Sawka et al., 2007"
            ),
            HydrationTip(
                id = "tip_exercise_3",
                category = TipCategory.EXERCISE,
                title = "Electrolytes for Endurance",
                content = "For exercise lasting over 60-90 minutes, plain water isn't enough. You lose sodium, potassium, and other electrolytes through sweat, which need replacement to maintain performance and prevent hyponatremia.",
                actionableAdvice = "Use sports drinks or electrolyte supplements for prolonged, intense activities.",
                source = "Casa et al., 2000"
            ),

            // HEALTH
            HydrationTip(
                id = "tip_health_1",
                category = TipCategory.HEALTH,
                title = "Kidney Stone Prevention",
                content = "Adequate hydration (2.5-3L daily) significantly reduces kidney stone risk by diluting stone-forming minerals in urine. It's one of the most effective preventive measures.",
                actionableAdvice = "If you've had kidney stones, aim for at least 2.5L daily and monitor urine color.",
                source = "Fink et al., 2013"
            ),
            HydrationTip(
                id = "tip_health_2",
                category = TipCategory.HEALTH,
                title = "Cognitive Performance",
                content = "Even mild dehydration (1-2% body water loss) impairs cognitive function, mood, and concentration. Studies show improved alertness and reduced fatigue with proper hydration.",
                actionableAdvice = "Keep water accessible during mentally demanding tasks. Small, frequent sips work best.",
                source = "Popkin et al., 2010"
            ),
            HydrationTip(
                id = "tip_health_3",
                category = TipCategory.HEALTH,
                title = "Pregnancy & Breastfeeding",
                content = "Pregnant women need an extra 300ml daily (total ~2.3L). Breastfeeding mothers need 700-1000ml more (total ~3.1L) as breast milk is 87% water.",
                actionableAdvice = "Update your profile in Settings if pregnant or breastfeeding for accurate goals.",
                source = "Institute of Medicine, 2004"
            ),
            HydrationTip(
                id = "tip_health_4",
                category = TipCategory.HEALTH,
                title = "Skin Health Myth",
                content = "While severe dehydration affects skin, drinking extra water beyond adequate intake doesn't improve skin appearance or reduce wrinkles. Skin hydration depends more on topical care and overall health.",
                actionableAdvice = "Stay adequately hydrated for health, but don't expect cosmetic miracles from water alone.",
                source = "Palma et al., 2015"
            ),

            // SCIENCE
            HydrationTip(
                id = "tip_science_1",
                category = TipCategory.SCIENCE,
                title = "Water Balance Regulation",
                content = "Your body maintains water balance through kidneys, which can adjust urine concentration from 50ml to 1200ml per day based on intake. This homeostatic mechanism is remarkably efficient.",
                actionableAdvice = "Trust your body's regulation system—forcing excessive water can strain kidneys.",
                source = "Jequier & Constant, 2010"
            ),
            HydrationTip(
                id = "tip_science_2",
                category = TipCategory.SCIENCE,
                title = "Beverage Hydration Index",
                content = "The BHI measures how effectively different drinks hydrate compared to water (BHI=1.0). Milk has BHI~1.5 (best), oral rehydration solutions ~1.3, while alcohol has BHI~0.5-0.7 (worst).",
                actionableAdvice = "Vary your hydration sources. Milk, coconut water, and oral rehydration solutions are excellent.",
                source = "Maughan et al., 2016"
            ),
            HydrationTip(
                id = "tip_science_3",
                category = TipCategory.SCIENCE,
                title = "Hyponatremia Risk",
                content = "Overhydration dilutes blood sodium, causing hyponatremia—a serious condition. Marathon runners forcing excessive water intake are particularly at risk. Symptoms include nausea, confusion, and seizures.",
                actionableAdvice = "Don't overdo it. Drink to thirst, especially during endurance activities.",
                source = "Hew-Butler et al., 2017"
            ),
            HydrationTip(
                id = "tip_science_4",
                category = TipCategory.SCIENCE,
                title = "Cold vs. Room Temperature",
                content = "Cold water (5-10°C) is absorbed faster than warm water and helps cool body temperature during exercise. However, room temperature water is fine for normal hydration needs.",
                actionableAdvice = "For workouts, choose cold water. For daily hydration, temperature is personal preference.",
                source = "Casa et al., 2010"
            ),

            // MYTHS
            HydrationTip(
                id = "tip_myths_1",
                category = TipCategory.MYTHS,
                title = "The Color-Coded Myth",
                content = "Myth: Clear urine means perfect hydration. Reality: Pale yellow is ideal. Consistently clear urine suggests overhydration, which can dilute essential electrolytes.",
                actionableAdvice = "Aim for pale yellow, not clear. It's the healthy middle ground.",
                source = "Armstrong et al., 2012"
            ),
            HydrationTip(
                id = "tip_myths_2",
                category = TipCategory.MYTHS,
                title = "Coffee Doesn't Dehydrate",
                content = "Myth: Coffee dehydrates you. Reality: Coffee's water content compensates for caffeine's mild diuretic effect. Studies show habitual coffee drinkers maintain normal hydration status.",
                actionableAdvice = "Count your morning coffee toward daily fluid intake—science supports it.",
                source = "Killer et al., 2014"
            ),
            HydrationTip(
                id = "tip_myths_3",
                category = TipCategory.MYTHS,
                title = "Thirst Isn't Too Late",
                content = "Myth: If you're thirsty, you're already dehydrated. Reality: Thirst triggers at 1-2% body water loss, which is normal and not harmful. It's your body's natural hydration cue.",
                actionableAdvice = "Respond to thirst promptly, but don't fear it—it's working as designed.",
                source = "Negoianu & Goldfarb, 2008"
            ),
            HydrationTip(
                id = "tip_myths_4",
                category = TipCategory.MYTHS,
                title = "More Isn't Always Better",
                content = "Myth: The more water you drink, the healthier you are. Reality: Excessive water intake stresses kidneys and can cause hyponatremia. Adequate hydration, not maximum, is the goal.",
                actionableAdvice = "Follow HydroMate's personalized goal—it's calculated for optimal health, not maximum.",
                source = "Valtin, 2002"
            ),
            HydrationTip(
                id = "tip_myths_5",
                category = TipCategory.MYTHS,
                title = "Detox Water Fiction",
                content = "Myth: Special 'detox waters' cleanse your body. Reality: Your liver and kidneys handle detoxification naturally. Added fruits make water tastier but don't 'detoxify' beyond normal hydration benefits.",
                actionableAdvice = "Enjoy flavored water for taste, but don't expect magical health benefits.",
                source = "Klein & Kiat, 2015"
            )
        )

        /**
         * Получить советы по категории
         */
        fun getTipsByCategory(category: TipCategory): List<HydrationTip> {
            return getAllTips().filter { it.category == category }
        }

        /**
         * Получить случайный совет, который еще не просмотрен
         */
        fun getRandomUnviewedTip(viewedTipIds: Set<String>): HydrationTip? {
            val unviewed = getAllTips().filter { !viewedTipIds.contains(it.id) }
            return unviewed.randomOrNull()
        }
    }
}
