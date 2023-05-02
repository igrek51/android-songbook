package igrek.songbook.cast

import igrek.songbook.util.capitalize

val animalNames: Set<String> = setOf(
    "Alligator",
    "Alpaca",
    "Anaconda",
    "Ant",
    "Antelope",
    "Armadillo",
    "Baboon",
    "Badger",
    "Barracuda",
    "Bat",
    "Bear",
    "Beaver",
    "Bee",
    "Beetle",
    "Bird",
    "Bison",
    "Buffalo",
    "Butterfly",
    "Camel",
    "Cat",
    "Caterpillar",
    "Catfish",
    "Cheetah",
    "Chicken",
    "Chimpanzee",
    "Chipmunk",
    "Cobra",
    "Condor",
    "Cougar",
    "Cow",
    "Coyote",
    "Crab",
    "Crane",
    "Crocodile",
    "Crow",
    "Deer",
    "Dinosaur",
    "Dog",
    "Dolphin",
    "Donkey",
    "Dove",
    "Dragonfly",
    "Duck",
    "Eagle",
    "Eel",
    "Elephant",
    "Falcon",
    "Ferret",
    "Fish",
    "Flamingo",
    "Flea",
    "Fly",
    "Fox",
    "Frog",
    "Giraffe",
    "Goat",
    "Goose",
    "Gopher",
    "Gorilla",
    "Hamster",
    "Hare",
    "Hawk",
    "Heron",
    "Hippopotamus",
    "Horse",
    "Hummingbird",
    "Husky",
    "Iguana",
    "Impala",
    "Kangaroo",
    "Lemur",
    "Leopard",
    "Lion",
    "Lizard",
    "Llama",
    "Lobster",
    "Lynx",
    "Monkey",
    "Moose",
    "Mosquito",
    "Moth",
    "Mouse",
    "Mule",
    "Octopus",
    "Opossum",
    "Orca",
    "Ostrich",
    "Otter",
    "Owl",
    "Oyster",
    "Panda",
    "Panther",
    "Parrot",
    "Peacock",
    "Pelican",
    "Penguin",
    "Pig",
    "Pigeon",
    "Porcupine",
    "Python",
    "Rabbit",
    "Raccoon",
    "Rat",
    "Rattlesnake",
    "Rhino",
    "Rooster",
    "Salmon",
    "Seal",
    "Sheep",
    "Skunk",
    "Sloth",
    "Snail",
    "Snake",
    "Spider",
    "Squirrel",
    "Swan",
    "Tiger",
    "Whale",
    "Wolf",
    "Wombat",
    "Zebra",
)

val adjectives: List<String> = listOf(
    "abundant",
    "accessible",
    "accommodative",
    "accomplished",
    "accurate",
    "achievable",
    "adaptable",
    "adaptive",
    "adequate",
    "adjustable",
    "admirable",
    "admiring",
    "adorable",
    "adored",
    "adoring",
    "adroit",
    "adulatory",
    "advanced",
    "advantageous",
    "adventuresome",
    "adventurous",
    "affable",
    "affectionate",
    "affirmative",
    "affluent",
    "affordable",
    "agile",
    "agreeable",
    "alluring",
    "altruistic",
    "amazed",
    "amazing",
    "ambitious",
    "amenable",
    "amiable",
    "amicable",
    "ample",
    "amusing",
    "angelic",
    "appealing",
    "appreciable",
    "appreciated",
    "appreciative",
    "appropriate",
    "ardent",
    "articulate",
    "artistic",
    "assuring",
    "astonished",
    "astonishing",
    "astounded",
    "astounding",
    "athletic",
    "attentive",
    "attractive",
    "audible",
    "auspicious",
    "authentic",
    "authoritative",
    "autonomous",
    "available",
    "avid",
    "awed",
    "awesome",
    "awestruck",
    "balanced",
    "beauteous",
    "beautiful",
    "believable",
    "beloved",
    "beneficent",
    "beneficial",
    "beneficiary",
    "benevolent",
    "best",
    "better",
    "blameless",
    "blissful",
    "blithe",
    "bonny",
    "booming",
    "boundless",
    "bountiful",
    "brainiest",
    "brainy",
    "brave",
    "breathtaking",
    "bright",
    "brighter",
    "brightest",
    "brilliant",
    "brisk",
    "brotherly",
    "bullish",
    "buoyant",
    "calm",
    "calming",
    "capable",
    "captivating",
    "carefree",
    "catchy",
    "celebrated",
    "celebratory",
    "champion",
    "charismatic",
    "charitable",
    "charming",
    "chaste",
    "cheaper",
    "cheapest",
    "cheerful",
    "cheery",
    "cherished",
    "chivalrous",
    "classic",
    "classy",
    "clean",
    "cleaner",
    "cleanest",
    "cleanly",
    "clear",
    "cleared",
    "clearer",
    "clever",
    "coherent",
    "cohesive",
    "colorful",
    "comely",
    "comfortable",
    "comforting",
    "comfy",
    "commendable",
    "commodious",
    "compact",
    "compassionate",
    "compatible",
    "competitive",
    "complementary",
    "compliant",
    "complimentary",
    "comprehensive",
    "conciliatory",
    "concise",
    "confident",
    "congenial",
    "congratulatory",
    "conscientious",
    "considerate",
    "consistent",
    "constructive",
    "consummate",
    "contrasty",
    "convenient",
    "convincing",
    "cool",
    "coolest",
    "cooperative",
    "correct",
    "courageous",
    "courteous",
    "courtly",
    "cozy",
    "creative",
    "credible",
    "crisp",
    "crisper",
    "cushy",
    "cute",
    "daring",
    "darling",
    "dashing",
    "dauntless",
    "dazzled",
    "dazzling",
    "decent",
    "decisive",
    "dedicated",
    "defeated",
    "deft",
    "delectable",
    "delicate",
    "delicious",
    "delighted",
    "delightful",
    "dependable",
    "deserving",
    "desirable",
    "desirous",
    "detachable",
    "devout",
    "dexterous",
    "dextrous",
    "dignified",
    "diligent",
    "diplomatic",
    "distinctive",
    "distinguished",
    "diversified",
    "divine",
    "dominated",
    "dumbfounded",
    "dumbfounding",
    "durable",
    "dynamic",
    "eager",
    "earnest",
    "eased",
    "easier",
    "easiest",
    "easy",
    "easygoing",
    "ebullient",
    "economical",
    "ecstatic",
    "educated",
    "effective",
    "effectual",
    "efficacious",
    "efficient",
    "effortless",
    "effusive",
    "elated",
    "elegant",
    "elite",
    "electric",
    "eloquent",
    "eminent",
    "enchanted",
    "enchanting",
    "encouraging",
    "endearing",
    "endorsed",
    "energetic",
    "engaging",
    "engrossing",
    "enhanced",
    "enjoyable",
    "enough",
    "enraptured",
    "enterprising",
    "entertaining",
    "enthralled",
    "enthusiastic",
    "enticing",
    "entranced",
    "entrancing",
    "enviable",
    "envious",
    "equitable",
    "erudite",
    "ethical",
    "euphoric",
    "evaluative",
    "eventful",
    "everlasting",
    "evocative",
    "exalted",
    "exalting",
    "exceeding",
    "excellent",
    "exceptional",
    "excited",
    "exciting",
    "exemplary",
    "exhilarating",
    "expansive",
    "exquisite",
    "extraordinary",
    "exuberant",
    "exultant",
    "fabulous",
    "fair",
    "faithful",
    "famed",
    "famous",
    "fancier",
    "fancy",
    "fantastic",
    "fascinating",
    "fashionable",
    "fast",
    "faster",
    "fastest",
    "faultless",
    "favorable",
    "favored",
    "favorite",
    "fearless",
    "feasible",
    "feisty",
    "felicitous",
    "fertile",
    "fervent",
    "fervid",
    "festive",
    "fiery",
    "fine",
    "finer",
    "finest",
    "firmer",
    "flashy",
    "flatter",
    "flattering",
    "flawless",
    "flexible",
    "flourishing",
    "fluent",
    "fond",
    "foolproof",
    "foremost",
    "formidable",
    "fortuitous",
    "fortunate",
    "fragrant",
    "free",
    "freed",
    "fresh",
    "fresher",
    "freshest",
    "friendly",
    "frugal",
    "fruitful",
    "funny",
    "futuristic",
    "gainful",
    "gallant",
    "galore",
    "generous",
    "genial",
    "gentle",
    "gentlest",
    "genuine",
    "gifted",
    "glad",
    "glamorous",
    "gleeful",
    "glimmering",
    "glistening",
    "glorious",
    "glowing",
    "godlike",
    "gold",
    "golden",
    "good",
    "goodly",
    "gorgeous",
    "graceful",
    "gracious",
    "grand",
    "grateful",
    "gratified",
    "gratifying",
    "great",
    "greatest",
    "groundbreaking",
    "guiltless",
    "gutsy",
    "halcyon",
    "hale",
    "hallowed",
    "handier",
    "handsome",
    "handy",
    "happier",
    "happy",
    "hardier",
    "hardy",
    "harmless",
    "harmonious",
    "healthful",
    "healthy",
    "heartening",
    "heartfelt",
    "heartwarming",
    "heavenly",
    "helpful",
    "heroic",
    "hilarious",
    "holy",
    "honest",
    "honorable",
    "honored",
    "hopeful",
    "hospitable",
    "hot",
    "hottest",
    "humane",
    "humble",
    "humorous",
    "humourous",
    "ideal",
    "idolized",
    "idyllic",
    "illuminating",
    "illustrious",
    "imaginative",
    "immaculate",
    "immense",
    "impartial",
    "impassioned",
    "impeccable",
    "important",
    "impressed",
    "impressive",
    "improved",
    "improving",
    "incredible",
    "indebted",
    "individualized",
    "indulgent",
    "industrious",
    "inestimable",
    "inexpensive",
    "infallible",
    "influential",
    "ingenious",
    "ingenuous",
    "innocuous",
    "innovative",
    "insightful",
    "insidious",
    "inspirational",
    "inspiring",
    "instructive",
    "instrumental",
    "integral",
    "integrated",
    "intelligent",
    "intelligible",
    "interesting",
    "intimate",
    "intricate",
    "intriguing",
    "intuitive",
    "invaluable",
    "inventive",
    "invigorating",
    "invincible",
    "inviolable",
    "inviolate",
    "invulnerable",
    "irreplaceable",
    "irreproachable",
    "irresistible",
    "jabbering",
    "jolly",
    "jovial",
    "joyful",
    "joyous",
    "jubilant",
    "judicious",
    "judgy",
    "keen",
    "kind",
    "knowledgeable",
    "laudable",
    "lavish",
    "lawful",
    "leading",
    "lean",
    "legendary",
    "lighter",
    "likable",
    "like",
    "liked",
    "lionhearted",
    "lively",
    "logical",
    "lovable",
    "loved",
    "lovely",
    "loving",
    "loyal",
    "lucid",
    "luckier",
    "luckiest",
    "lucky",
    "lucrative",
    "luminous",
    "lush",
    "lustrous",
    "luxuriant",
    "luxurious",
    "luxury",
    "lyrical",
    "magic",
    "magical",
    "magnanimous",
    "magnificent",
    "majestic",
    "manageable",
    "maneuverable",
    "marvellous",
    "marvelous",
    "masterful",
    "matchless",
    "mature",
    "meaningful",
    "memorable",
    "merciful",
    "merciless",
    "meritorious",
    "merry",
    "mesmerized",
    "mesmerizing",
    "meticulous",
    "mighty",
    "miraculous",
    "mischievous",
    "modern",
    "modest",
    "momentous",
    "monumental",
    "motivated",
    "navigable",
    "neat",
    "neatest",
    "nice",
    "nicest",
    "nifty",
    "nimble",
    "noble",
    "noiseless",
    "noteworthy",
    "nourishing",
    "observant",
    "obtainable",
    "optimal",
    "optimistic",
    "opulent",
    "orderly",
    "organized",
    "otic",
    "outstanding",
    "overjoyed",
    "painless",
    "palatial",
    "pampered",
    "panoramic",
    "paramount",
    "passionate",
    "patient",
    "patri",
    "peaceable",
    "peaceful",
    "peerless",
    "peppy",
    "perfect",
    "permissible",
    "personalized",
    "phenomenal",
    "picturesque",
    "playful",
    "pleasant",
    "pleased",
    "pleasing",
    "pleasurable",
    "plentiful",
    "plush",
    "poetic",
    "poignant",
    "poised",
    "polished",
    "polite",
    "popular",
    "portable",
    "posh",
    "positive",
    "powerful",
    "praiseworthy",
    "praising",
    "precious",
    "precise",
    "preeminent",
    "preferable",
    "premier",
    "prestigious",
    "pretty",
    "priceless",
    "principled",
    "privileged",
    "prize",
    "proactive",
    "prodigious",
    "productive",
    "proficient",
    "profound",
    "profuse",
    "progressive",
    "prolific",
    "prominent",
    "promised",
    "promising",
    "prompt",
    "proper",
    "propitious",
    "prosperous",
    "protective",
    "proud",
    "proven",
    "prudent",
    "punctual",
    "pure",
    "purposeful",
    "quaint",
    "qualified",
    "quick",
    "quiet",
    "radiant",
    "rapid",
    "rapt",
    "rapturous",
    "rational",
    "reachable",
    "readable",
    "ready",
    "realistic",
    "realizable",
    "reasonable",
    "reasoned",
    "receptive",
    "recommended",
    "redeeming",
    "refined",
    "reformed",
    "refreshed",
    "refreshing",
    "regal",
    "rejoicing",
    "relaxed",
    "reliable",
    "remarkable",
    "renewed",
    "renowned",
    "replaceable",
    "reputable",
    "resilient",
    "resolute",
    "resounding",
    "resourceful",
    "respectable",
    "respectful",
    "resplendent",
    "responsive",
    "restful",
    "restored",
    "retractable",
    "reverent",
    "revolutionary",
    "rewarding",
    "rich",
    "richer",
    "right",
    "righteous",
    "rightful",
    "robust",
    "romantic",
    "roomier",
    "roomy",
    "rosy",
    "safe",
    "saintly",
    "salutary",
    "sane",
    "satisfactory",
    "satisfied",
    "satisfying",
    "scenic",
    "seamless",
    "seasoned",
    "secure",
    "selective",
    "sensational",
    "sensible",
    "sensitive",
    "serene",
    "sharp",
    "sharper",
    "sharpest",
    "shiny",
    "significant",
    "silent",
    "simpler",
    "simplest",
    "simplified",
    "sincere",
    "skilled",
    "skillful",
    "sleek",
    "slick",
    "smart",
    "smarter",
    "smartest",
    "smiling",
    "smitten",
    "smooth",
    "smoother",
    "smoothest",
    "snappy",
    "snazzy",
    "sociable",
    "soft",
    "softer",
    "solicitous",
    "solid",
    "sophisticated",
    "soulful",
    "spacious",
    "sparkling",
    "spectacular",
    "speedy",
    "spellbinding",
    "spellbound",
    "spirited",
    "spiritual",
    "splendid",
    "spontaneous",
    "sporty",
    "spotless",
    "sprightly",
    "stable",
    "stainless",
    "stately",
    "statuesque",
    "staunch",
    "steadfast",
    "steadiest",
    "steady",
    "stellar",
    "stimulating",
    "stimulative",
    "straightforward",
    "streamlined",
    "striking",
    "strong",
    "stronger",
    "strongest",
    "stunned",
    "stunning",
    "stupendous",
    "sturdier",
    "sturdy",
    "stylish",
    "stylized",
    "suave",
    "sublime",
    "subsidized",
    "substantive",
    "succeeding",
    "successful",
    "sufficient",
    "suitable",
    "sumptuous",
    "super",
    "superb",
    "superior",
    "supple",
    "supported",
    "supporting",
    "supportive",
    "supreme",
    "surreal",
    "sustainable",
    "swank",
    "swankier",
    "swankiest",
    "swanky",
    "sweeping",
    "sweet",
    "sweetheart",
    "swift",
    "talented",
    "tantalizing",
    "tempting",
    "tenacious",
    "tender",
    "terrific",
    "thankful",
    "thinner",
    "thoughtful",
    "thrifty",
    "thrilled",
    "thrilling",
    "thriving",
    "tidy",
    "timely",
    "titillating",
    "tolerable",
    "top",
    "topnotch",
    "tops",
    "tough",
    "tougher",
    "toughest",
    "tranquil",
    "transparent",
    "trendy",
    "triumphal",
    "triumphant",
    "trusted",
    "trusting",
    "trustworthy",
    "trusty",
    "truthful",
    "twinkly",
    "unabashed",
    "unaffected",
    "unassailable",
    "unbeatable",
    "unbiased",
    "unbound",
    "uncomplicated",
    "unconditional",
    "undamaged",
    "undaunted",
    "understandable",
    "undisputable",
    "undisputed",
    "unencumbered",
    "unequivocal",
    "unfettered",
    "unforgettable",
    "unlimited",
    "unmatched",
    "unparalleled",
    "unquestionable",
    "unreal",
    "unrestricted",
    "unrivaled",
    "unselfish",
    "unwavering",
    "upbeat",
    "upscale",
    "usable",
    "useable",
    "useful",
    "valiant",
    "valuable",
    "verifiable",
    "veritable",
    "versatile",
    "vibrant",
    "victorious",
    "viewable",
    "vigilant",
    "virtuous",
    "visionary",
    "vivacious",
    "vivid",
    "warm",
    "warmer",
    "warmhearted",
    "wealthy",
    "welcome",
    "well",
    "wholesome",
    "wieldy",
    "willing",
    "winning",
    "wise",
    "witty",
    "won",
    "wonderful",
    "wondrous",
    "workable",
    "worth",
    "worthwhile",
    "worthy",
    "young",
    "youthful",
    "zealous",
    "zippy",
)

class AnimalNameFeeder {
    fun generateName(): String {
        val animal = animalNames.random().capitalize()
        val letter: Char = animal.lowercase()[0]
        val letterAdjectives = adjectives.filter { it[0] == letter }
        return if (letterAdjectives.isEmpty()){
            val randomNumber = (10..99).random()
            animal + randomNumber.toString()
        } else {
            val adjective = letterAdjectives.random().capitalize()
            "$adjective $animal"
        }
    }
}