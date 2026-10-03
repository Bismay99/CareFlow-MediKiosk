package com.example.data.model

enum class Language(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val welcomeText: String,
    val tagline: String,
    val consentText: String,
    val ttsPrompt: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        welcomeText = "Welcome to MediKiosk",
        tagline = "Smart Hospital Check-in & AI Triage Assistant",
        consentText = "I consent to sharing my symptoms and medical history for automated clinical triage with hospital staff.",
        ttsPrompt = "I consent to sharing my symptoms and medical history for automated clinical triage with hospital staff."
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिन्दी",
        welcomeText = "मेडीकियोस्क में आपका स्वागत है",
        tagline = "अस्पताल चेक-इन और एआई ट्राइएज सहायक",
        consentText = "मैं अस्पताल के कर्मचारियों के साथ ट्राइएज के लिए अपने लक्षण और चिकित्सा इतिहास साझा करने की सहमति देता/देती हूँ।",
        ttsPrompt = "मैं अस्पताल के कर्मचारियों के साथ ट्राइएज के लिए अपने लक्षण और चिकित्सा इतिहास साझा करने की सहमति देता हूँ।"
    ),
    BENGALI(
        code = "bn",
        displayName = "Bengali",
        nativeName = "বাংলা",
        welcomeText = "মেডিকিয়স্কে স্বাগতম",
        tagline = "স্মার্ট হাসপাতাল চেক-ইন এবং এআই ট্রায়াজ সহকারী",
        consentText = "আমি হাসপাতালের কর্মীদের সাথে ট্রায়াজের জন্য আমার লক্ষণ এবং চিকিৎসার ইতিহাস শেয়ার করতে সম্মতি দিচ্ছি।",
        ttsPrompt = "আমি হাসপাতালের কর্মীদের সাথে ট্রায়াজের জন্য আমার লক্ষণ শেয়ার করতে সম্মতি দিচ্ছি।"
    ),
    TAMIL(
        code = "ta",
        displayName = "Tamil",
        nativeName = "தமிழ்",
        welcomeText = "மெடிகியோஸ்க்கிற்கு வரவேற்கிறோம்",
        tagline = "மருத்துவமனை செக்-இன் மற்றும் ஏஐ ட்ரையேஜ் உதவியாளர்",
        consentText = "மருத்துவமனை ஊழியர்களுடன் எனது அறிகுறிகள் மற்றும் மருத்துவ வரலாற்றைப் பகிர்ந்து கொள்ள ஒப்புக்கொள்கிறேன்.",
        ttsPrompt = "மருத்துவமனை ஊழியர்களுடன் எனது அறிகுறிகளைப் பகிர்ந்து கொள்ள ஒப்புக்கொள்கிறேன்."
    ),
    TELUGU(
        code = "te",
        displayName = "Telugu",
        nativeName = "తెలుగు",
        welcomeText = "మెడికియోస్క్‌కి స్వాగతం",
        tagline = "ఆసుపత్రి చెక్-ఇన్ మరియు ఏఐ ట్రియాజ్ అసిస్టెంట్",
        consentText = "ఆసుపత్రి సిబ్బందితో నా లక్షణాలు మరియు వైద్య చరిత్రను పంచుకోవడానికి నేను అంగీకరిస్తున్నాను.",
        ttsPrompt = "ఆసుపత్రి సిబ్బందితో నా లక్షణాలు పంచుకోవడానికి అంగీకరిస్తున్నాను."
    ),
    MARATHI(
        code = "mr",
        displayName = "Marathi",
        nativeName = "मराठी",
        welcomeText = "मेडीकियोस्क मध्ये आपले स्वागत आहे",
        tagline = "रुग्णालय चेक-इन आणि एआय ट्रायज सहाय्यक",
        consentText = "मी रुग्णालयातील कर्मचाऱ्यांसोबत ट्राइएजसाठी माझी लक्षणे आणि वैद्यकीय माहिती शेअर करण्यास संमती देतो/देते.",
        ttsPrompt = "मी रुग्णालयातील कर्मचाऱ्यांसोबत माझी लक्षणे शेअर करण्यास संमती देतो."
    )
}
