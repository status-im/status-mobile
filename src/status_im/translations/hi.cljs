(ns status-im.translations.hi)

(def translations
  {
   ;common
   :members-title                         "सदस्य"
   :not-implemented                       "!कार्यान्वित नहीं"
   :chat-name                             "चैट नाम"
   :notifications-title                   "सूचनाएं और आवाज"
   :offline                               "ऑफ़लाइन"

   ;drawer
   :invite-friends                        "दोस्तों को आमंत्रित करें"
   :faq                                   "अक्सर पूछे जाने वाले प्रश्न"
   :switch-users                          "उपयोगकर्ताओं को स्विच करें"

   ;chat
   :is-typing                             "टाइप कर रहा है"
   :and-you                               "और आप"
   :search-chat                           "चैट खोजें"
   :members                               {:one   "1 सदस्य"
                                           :other "{{count}} सदस्य"
                                           :zero  "कोई सदस्य नहीं"}
   :members-active                        {:one   "1 सदस्य, 1 सक्रिय"
                                           :other "{{count}} सदस्य, {{count}} सक्रिय"
                                           :zero  "कोई सदस्य नहीं"}
   :active-online                         "ऑनलाइन"
   :active-unknown                        "अनजान"
   :available                             "उपलब्ध"
   :no-messages                           "कोई संदेश नहीं"
   :suggestions-requests                  "अनुरोध"
   :suggestions-commands                  "कमांड"

   ;sync
   :sync-in-progress                      "सिंक किया जा रहा है..."
   :sync-synced                           "सिंक में"

   ;messages
   :status-sending                        "भेज रहे हैं"
   :status-pending                        "विचाराधीन"
   :status-sent                           "भेज दिया"
   :status-seen-by-everyone               "हर किसी ने देख लिया"
   :status-seen                           "देखा गया"
   :status-delivered                      "सौंप दिया"
   :status-failed                         "विफल रहा"

   ;datetime
   :datetime-second                       {:one   "सेकंड"
                                           :other "सेकंड"}
   :datetime-minute                       {:one   "मिनट"
                                           :other "मिनट"}
   :datetime-hour                         {:one   "घंटा"
                                           :other "घंटे"}
   :datetime-day                          {:one   "दिन"
                                           :other "दिन"}
   :datetime-multiple                     "एस"
   :datetime-ago                          "पहले"
   :datetime-yesterday                    "बीता हुआ कल"
   :datetime-today                        "आज"

   ;profile
   :profile                               "प्रोफाइल"
   :report-user                           "उपयोगकर्ता को रिपोर्ट करें"
   :message                               "संदेश"
   :username                              "उपयोगकर्ता नाम"
   :not-specified                         "निर्दिष्ट नहीं"
   :public-key                            "सार्वजनिक कुंजी"
   :phone-number                          "फ़ोन नंबर"
   :email                                 "ईमेल"
   :profile-no-status                     "कोई स्थिति नहीं"
   :add-to-contacts                       "संपर्क में जोड़ें"
   :error-incorrect-name                  "कृपया दूसरा नाम चयन करें"
   :error-incorrect-email                 "गलत ई-मेल"

   ;;make_photo
   :image-source-title                    "प्रोफ़ाइल छवि"
   :image-source-make-photo               "प्राप्त करें"
   :image-source-gallery                  "गैलरी से चयन करें"
   :image-source-cancel                   "रद्द करें"

   ;sign-up
   :contacts-syncronized                  "आपके संपर्कों को सिंक्रनाइज़ किया गया है"
   :confirmation-code                     (str "धन्यवाद! हमने आपको \"पुष्टि कोड\" के साथ एक टेक्स्ट संदेश भेजा है "
                                               "कृपया अपने फोन नंबर की पुष्टि करने के लिए वह कोड डालें।")
   :incorrect-code                        (str "क्षमा करें, कोड गलत था, कृपया फिर से डालें")
   :generate-passphrase                   (str "मैं आपके लिए एक पासफ्रेज जनरेट करूंगा ताकि आप अपना पासफ्रेज बहाल कर सकें "
                                               "अन्य डिवाइस से उपयोग या लॉगिन करें")
   :phew-here-is-your-passphrase          "*उफ़्फ़* यह बहुत मुश्किल था, यह रहा आपका पासफ्रेज, *इसे लिख लें और सुरक्षित रखें! * अपने खाते को ठीक करने के लिए आपको इसकी आवश्यकता होगी।"
   :here-is-your-passphrase               "यह रहा आपका पासफ्रेज, *इसे लिख लें और सुरक्षित रखें! * अपने खाते को ठीक करने के लिए आपको इसकी आवश्यकता होगी।"
   :written-down                          "पक्का करें कि आपने इसे सुरक्षित रूप से लिख लिया था"
   :phone-number-required                 "अपना फोन नंबर दर्ज करने के लिए यहां टैप करें और मैं आपके दोस्तों को ढूंढ निकालूंगा"
   :intro-status                          "अपना खाता सेट करने और अपनी सेटिंग्स बदलने के लिए मेरे साथ चैट करें!"
   :intro-message1                        "स्टेटस में आपका स्वागत है\n अपना पासवर्ड सेट करने और शुरुआत करने के लिए इस संदेश पर करें!"
   :account-generation-message            "मुझे एक सेकंड का समय दें, आपका खाता जनरेट करने के लिए मुझे कुछ क्रेजी गणित का प्रयोग करना होगा!"

   ;chats
   :chats                                 "चैट"
   :new-chat                              "नई चैट"
   :new-group-chat                        "नई ग्रुप चैट"

   ;discover
   :discover                              "खोज"
   :none                                  "कोई नहीं"
   :search-tags                           "अपने खोज टैग यहां टाइप करें"
   :popular-tags                          "लोकप्रिय टैग"
   :recent                                "नया"
   :no-statuses-discovered                "कोई स्टेटस नहीं मिला"

   ;settings
   :settings                              "सेटिंग्स"

   ;contacts
   :contacts                              "संपर्क"
   :new-contact                           "नया संपर्क"
   :show-all                              "सभी दिखाएं"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "लोग"
   :contacts-group-new-chat               "नई चैट शुरू करें"
   :no-contacts                           "अभी तक कोई संपर्क नहीं"
   :show-qr                               "QR दिखाएं"

   ;group-settings
   :remove                                "निकालें"
   :save                                  "सहेजें"
   :change-color                          "रंग बदलें"
   :clear-history                         "इतिहास साफ़ करें"
   :delete-and-leave                      "हटाएं और छोड़ें"
   :chat-settings                         "चैट सेटिंग्स"
   :edit                                  "संपादित करें"
   :add-members                           "सदस्य जोड़ें"
   :blue                                  "नीला"
   :purple                                "बैंगनी"
   :green                                 "हरा"
   :red                                   "लाल"

   ;commands
   :money-command-description             "पैसे भेजें"
   :location-command-description          "स्थान भेजें"
   :phone-command-description             "फोन नंबर भेजें"
   :phone-request-text                    "फ़ोन नंबर अनुरोध"
   :confirmation-code-command-description "पुष्टि कोड भेजें"
   :confirmation-code-request-text        "पुष्टि कोड अनुरोध"
   :send-command-description              "स्थान भेजें"
   :request-command-description           "अनुरोध भेजें"
   :keypair-password-command-description  ""
   :help-command-description              "मदद करें"
   :request                               "अनुरोध करें"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} {{chat-name}} को ETH"
   :chat-send-eth-from                    "{{amount}} {{chat-name}} से ETH"

   ;new-group
   :group-chat-name                       "चैट नाम"
   :empty-group-chat-name                 "कोई नाम दर्ज करें"
   :illegal-group-chat-name               "कृपया दूसरा नाम चयन करें"

   ;participants
   :add-participants                      "प्रतिभागियों को जोड़ें"
   :remove-participants                   "प्रतिभागियों को निकालें"

   ;protocol
   :received-invitation                   "चैट आमंत्रण प्राप्त हुआ"
   :removed-from-chat                     "आपको ग्रुप चैट से निकाल दिया"
   :left                                  "बाएं"
   :invited                               "आमंत्रित"
   :removed                               "निकाल दिया"
   :You                                   "आप"

   ;new-contact
   :add-new-contact                       "नया संपर्क जोड़ें"
   :import-qr                             "आयात करें"
   :scan-qr                               "QR स्कैन करें"
   :name                                  "नाम"
   :whisper-identity                      "पहचान बताएं"
   :address-explication                   "संभवतः यहाँ कुछ टेक्स्ट विवरण होना चाहिए कि पता क्या है और इसे कहाँ खोजा जाए"
   :enter-valid-address                   "कृपया एक वैध पता दर्ज करें या एक QR कोड स्कैन करें"
   :contact-already-added                 "संपर्क पहले से जोड़ लिया गया है"
   :can-not-add-yourself                  "आप अपने आपको नहीं जोड़ सकते"
   :unknown-address                       "अज्ञात पता"


   ;login
   :connect                               "कनेक्ट करें"
   :address                               "पता"
   :password                              "पासवर्ड"
   :login                                 "लॉगिन करें"
   :wrong-password                        "गलत पासवर्ड"

   ;recover
   :recover-from-passphrase               "पासफ्रेज से ठीक करें"
   :recover-explain                       "पहुंच को ठीक करने के लिए कृपया अपने पासवर्ड का पासफ्रेज दर्ज करें"
   :passphrase                            "पासफ्रेज"
   :recover                               "ठीक करें"
   :enter-valid-passphrase                "कृपया एक पासफ्रेज दर्ज करें"
   :enter-valid-password                  "कृपया पासवर्ड दर्ज करें"

   ;accounts
   :recover-access                        "पहुंच को ठीक करें"
   :add-account                           "खाता जोड़ें"

   ;wallet-qr-code
   :done                                  "पूरा हो गया"
   :main-wallet                           "मुख्य वॉलेट"

   ;validation
   :invalid-phone                         "अमान्य फोन नंबर"
   :amount                                "राशि"
   :not-enough-eth                        (str "बैलेंस पर पर्याप्त ETH नहीं "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "लेनदेन की पुष्टि करें"
                                           :other "{{count}} लेनदेनों की पुष्टि करें"
                                           :zero  "कोई लेनदेन नहीं"}
   :status                                "स्टेटस"
   :pending-confirmation                  "लंबित पुष्टि"
   :recipient                             "प्राप्तकर्ता"
   :one-more-item                         "एक अन्य आइटम"
   :fee                                   "शुल्क"
   :value                                 "मूल्य"

   ;:webview
   :web-view-error                        "ओह, त्रुटि"

   :confirm                               "पुष्टि करें"
   :phone-national                        "राष्ट्रीय"
   :transactions-confirmed                {:one   "लेनदेन की पुष्टि की गई"
                                           :other "{{count}} लेनदेनों की पुष्टि की गई"
                                           :zero  "किसी भी लेनदेन की पुष्टि नहीं हुई"}
   :public-group-topic                    "विषय"
   :debug-enabled                         "डीबग सर्वर लाँच कर दिया गया है! है। अब आप अपने कम्प्यूटर से *status-dev-cli scan* चलाकर अपने DApp को शामिल कर सकते हैं"
   :new-public-group-chat                 "सार्वजनिक चैट में शामिल हों"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "रद्द करें"
   :share-qr                              "QR साझा करें"
   :feedback                              "फीडबैक देना चाहते हैं?\nअपने फोन को हिलाएं!"
   :twelve-words-in-correct-order         "सही क्रम में 12 शब्द"
   :remove-from-contacts                  "संपर्कों से हटाएं"
   :delete-chat                           "चैट मिटाएं"
   :edit-chats                            "चैट्स का संपादन करें"
   :sign-in                               "साइन इन"
   :create-new-account                    "नया खाता बनाएं"
   :sign-in-to-status                     "स्टैटस में साइन इन करें"
   :got-it                                "समझ गया"
   :move-to-internal-failure-message      "हमें बाह्य से आंतरिक स्टोरेज में कुछ जरूरी फाइलें ले जानी हैं। ऐसा करने के लिए हमें आपकी अनुमति चाहिए। हम भावी संस्करणों में बाह्य स्टोरेज का उपयोग नहीं करने वाले हैं।"
   :edit-group                            "समूह का संपादन करें"
   :delete-group                          "समूह मिटाएं"
   :browsing-title                        "ब्राउज़ करें"
   :reorder-groups                        "समूहों को फिर से क्रम में रखें"
   :browsing-cancel                       "रद्द करें"
   :faucet-success                        "फॉसेट का अनुरोध प्राप्त हुआ है"
   :choose-from-contacts                  "संपर्कों में से चुनें"
   :new-group                             "नया समूह"
   :phone-e164                            "अंतरराष्ट्रीय 1"
   :remove-from-group                     "समूह से हटाएं"
   :search-contacts                       "संपर्क खोजें"
   :transaction                           "लेनदेन"
   :public-group-status                   "सार्वजनिक"
   :leave-chat                            "चैट छोड़ें"
   :start-conversation                    "बातचीत शुरू करें"
   :topic-format                          "गलत फॉर्मैट [a-z0-9\\-]+"
   :enter-valid-public-key                "कृपया वैध सार्वजनिक कुंजी प्रविष्ट करें या QR कोड स्कैन करें"
   :faucet-error                          "फॉसेट के अनुरोध की त्रुटि"
   :phone-significant                     "उल्लेखनीय"
   :search-for                            "... की खोज करें"
   :sharing-copy-to-clipboard             "क्लिपबोर्ड में कॉपी करें"
   :phone-international                   "अंतरराष्ट्रीय 2"
   :enter-address                         "पता प्रविष्ट करें"
   :send-transaction                      "लेनदेन भेजें"
   :delete-contact                        "संपर्क मिटाएं"
   :mute-notifications                    "अधिसूचनाएं म्यूट करें"


   :contact-s                             {:one   "संपर्क"
                                           :other "संपर्क"}
   :group-name                            "समूह का नाम"
   :next                                  "अगला"
   :from                                  "द्वारा"
   :search-chats                          "चैट्स खोजें"
   :in-contacts                           "संपर्कों में"

   :sharing-share                         "साझा करें..."
   :type-a-message                        "संदेश टाइप करें..."
   :type-a-command                        "आदेश टाइप करना शुरू करें..."
   :shake-your-phone                      "क्या कोई समस्या मिली या सुझाव देना है? बस अपने फोन को ~हिलाएं~!"
   :status-prompt                         "आप जिन चीजों की पेशकश कर रहे हैं उनके बारे में जानने में लोगों की मदद करने के लिए एक स्टैटस बनाएं। आप #हैशटैग्स का उपयोग भी कर सकते हैं।"
   :add-a-status                          "स्टैटस जोड़ें..."
   :error                                 "त्रुटि"
   :edit-contacts                         "संपर्कों का संपादन करें"
   :more                                  "अधिक"
   :cancel                                "रद्द करें"
   :no-statuses-found                     "कोई स्टैटस नहीं मिले"
   :swow-qr                               "QR दिखाएं"
   :browsing-open-in-web-browser          "वेब ब्राउज़र में खोलें"
   :delete-group-prompt                   "इससे संपर्क प्रभावित नहीं होंगे"
   :edit-profile                          "प्रोफाइल का संपादन करें"


   :enter-password-transactions           {:one   "अपना पासवर्ड प्रविष्ट करके लेनदेन की पुष्टि करें"
                                           :other "अपना पासवर्ड प्रविष्ट करके लेनदेनों की पुष्टि करें"}
   :unsigned-transactions                 "साइन न किए गए लेनदेन"
   :empty-topic                           "रिक्त विषय"
   :to                                    "प्रति"
   :group-members                         "समूह के सदस्य"
   :estimated-fee                         "संस्थान शुल्क"
   :data                                  "डेटा"})
