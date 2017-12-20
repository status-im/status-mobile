(ns status-im.translations.ne)

(def translations
  {
   ;;common
   :members-title                         "सदस्यहरु"
   :not-implemented                       "कार्यान्वयन भएको छैन"
   :chat-name                             "च्याट नाम"
   :notifications-title                   "सूचना र ध्वनि"
   :offline                               "अफलाइन"
   :search-for                            "॰॰॰ को लागि खोज"
   :cancel                                "रद्द गर्नुहोस्"
   :next                                  "अर्को"
   :type-a-message                        "सन्देश टाइप गरौं ॰॰॰"
   :type-a-command                        "आदेश टाइप गरौं ॰॰॰"
   :error                                 "त्रुटि"

   :camera-access-error                   "कृपया क्यामेरा उपयोग गर्न आवश्यक अनुमति प्रदान गर्न, सिस्टम सेटिङ्ग भित्र गएर स्टेटस > क्यमेरा चयन भएको स्थिति निश्चित गर्नुहोस् ।"
   :photos-access-error                   "कृपया फोटो उपयोग गर्न आवश्यक अनुमति प्रदान गर्न, सिस्टम सेटिङ्ग भित्र गएर स्टेटस > फोटो चयन भएको स्थिति निश्चित गर्नुहोस् ।"

   ;;drawer
   :switch-users                          "उपयोगकर्ता बदलौं"
   :current-network                       "चल्ती नेटवर्क"

   ;;chat
   :is-typing                             "ले टाइप गर्दै हुनुहुन्छ"
   :and-you                               "अनि तपाईं"
   :search-chat                           "च्याटमा खोजौं"
   :members                               {:one   "१ सदस्य"
                                           :other "{{count}} सदस्यहरु"
                                           :zero  "सदस्यहीन"}
   :members-active                        {:one   "१ सदस्य"
                                           :other "{{count}} सदस्यहरु"
                                           :zero  "सदस्यहीन"}
   :public-group-status                   "सार्वजनिक"
   :active-online                         "अनलाइन"
   :active-unknown                        "अज्ञात"
   :available                             "उपलब्ध"
   :no-messages                           "सन्देशहरु खाली"
   :suggestions-requests                  "अनुरोधहरु"
   :suggestions-commands                  "आदेशहरु"
   :faucet-success                        "धारा अनुरोध प्राप्त भएको छ"
   :faucet-error                          "धारा अनुरोध त्रुटि"

   ;;sync
   :sync-in-progress                      "एकनास बनाइंदै छ ॰॰॰"
   :sync-synced                           "एकनास छ"

   ;;messages
   :status-sending                        "पठाउंदै"
   :status-pending                        "बाँकी छ"
   :status-sent                           "पठाइसकेको"
   :status-seen-by-everyone               "सबैले हेरिसकेको"
   :status-seen                           "देखिसकेको"
   :status-delivered                      "बुझाइएको"
   :status-failed                         "असफल"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "सेकेन्ड"
                                           :other "सेकेन्ड"}
   :datetime-minute                       {:one   "मिनेट"
                                           :other "मिनेट"}
   :datetime-hour                         {:one   "घण्टा"
                                           :other "घण्टा"}
   :datetime-day                          {:one   "दिन"
                                           :other "दिन"}
   :datetime-ago                          "पहिले"
   :datetime-yesterday                    "हिजो"
   :datetime-today                        "आज"

   ;;profile
   :profile                               "प्रोफाइल"
   :edit-profile                          "प्रोफाइल सम्पादन"
   :message                               "सन्देश"
   :not-specified                         "नतोकिएको"
   :public-key                            "सार्वजनिक सांचो"
   :phone-number                          "फोन नम्बर"
   :update-status                         "तपाईंको स्टेटस अद्यतन गर्नुहोस् ॰॰॰"
   :add-a-status                          "नया स्टेटस जोड्नुहोस् ॰॰॰"
   :status-prompt                         "एउटा स्टेटस बनाउनुहोस् जसद्वारा तपांईले प्रस्ताव गर्नुभएको कुराहरु अरुहरुले थाहापाउन मद्दत पुगोस । तपांईले #ह्यास्ट्याग पनि प्रयोग गर्न सक्नुहुन्छ ।"
   :add-to-contacts                       "सम्पर्कहरुमा जोड्नुहोस्"
   :in-contacts                           "सम्पर्कहरुमा समावेस छ"
   :remove-from-contacts                  "सम्पर्कहरुबाट हटाउनुहोस्"
   :start-conversation                    "कुराकानी सुरु गर्नुहोस्"
   :send-transaction                      "कारोबार पठाउनुहोस्"

   ;;make_photo
   :image-source-title                    "प्रोफाइल तस्विर"
   :image-source-make-photo               "खिच्नुहोस"
   :image-source-gallery                  "ग्यालेरीबाट चयन गर्नुहोस्"

   ;;sharing
   :sharing-copy-to-clipboard             "क्लिप्बोर्डमा प्रतिलिपि राख्नुहोस्"
   :sharing-share                         "बांड्नुहोस् ॰॰॰"
   :sharing-cancel                        "रद्द गर्नुहोस्"

   :browsing-title                        "ब्राउज"
   :browsing-open-in-web-browser          "वेब ब्राउजरमा खोल्ने"
   :browsing-cancel                       "रद्द गर्नुहोस्"

   ;;sign-up
   :contacts-syncronized                  "तपाईंको सम्पर्कहरु एकनास भयो"
   :confirmation-code                     (str "धन्यवाद! हामीले तपाईंलाई पुष्टि कोड सहितको सन्देश पठाएका छौं । "
                                               "कृपया आफ्नो फोन नम्बर पुष्टि गर्न त्यो कोड प्रस्तुत गर्नुहोस् ।")
   :incorrect-code                        (str "माफ गर्नुहोस्, त्यो कोड गलत रहेछ, कृपया पुनः प्रविष्ट गर्नुहोस्")
   :phew-here-is-your-passphrase          "*उफऽऽऽ* त्यो कठिन थियो, तपाईंको गुप्तवाक्यांश प्रस्तुत छ, *यसलाई लेखेर सुरक्षित राख्नुहोस्* आफ्नो खाता पुनर्स्थापना गर्न यो अति आवश्यक छ ।"
   :here-is-your-passphrase               "तपाईंको गुप्तवाक्यांश प्रस्तुत छ, *यसलाई लेखेर सुरक्षित राख्नुहोस्* आफ्नो खाता पुनर्स्थापना गर्न यो अति आवश्यक छ ।"
   :phone-number-required                 "यहाँ ट्याप गरेर तपाईंको फोन नम्बर प्रविष्ट गर्नुहोस् अनि म तपाईंको साथीहरु भेट्टाइ दिन्छु ।"
   :shake-your-phone                      "त्रुटि फेलापार्नुभयो, अथवा सुझाव दिनु छ? बस फोन ~हल्लाउनुहोस्~!"
   :intro-status                          "तपाईंको खाता सेटअप गर्न र सेटिङ्ग बदल्नको लागि म सँग च्याट गर्नुहोस् ।"
   :intro-message1                        "स्टेटसमा तपाईंलाई स्वागत छ\nतपाईंको पास्वर्ड सेटअप र सुरुवात गर्नको लागि यो सन्देसमा ट्याप गर्नुहोस् ।"
   :account-generation-message            "एकै छिन पर्खिनुहोस्, तपाईंको खाता उत्पन्न गर्न केही कठिन हिसाब गर्दै छु!"
   :move-to-internal-failure-message      "हामीले केही महत्वपूर्ण फाइलहरु बाह्य बाट आन्तरिक भण्डारणमा सार्न आवश्यक छ । यस्को लागि हामीलाई तपाईंको अनुमति चाहिन्छ । भावी संस्करणहरुमा हामी बाह्य भण्डारण प्रयोगमा ल्याउदैनौं ।"
   :debug-enabled                         "त्रुटिसुधार सर्भर प्रवर्तित भयो! अब तपाईंले *status-dev-cli scan* कार्यान्वयन गरेर एकै नेटवर्कमा रहेको तपाईंको कम्प्युटरबाट सो सर्भर भेट्टाउन सक्नुहुन्छ ।"

   ;;phone types
   :phone-e164                            "अन्तराष्ट्रिय १"
   :phone-international                   "अन्तराष्ट्रिय २"
   :phone-national                        "राष्ट्रिय"
   :phone-significant                     "महत्वपूर्ण"

   ;;chats
   :chats                                 "च्याटहरु"
   :delete-chat                           "च्याट मेटाउने"
   :new-group-chat                        "नया सामूहिक च्याट"
   :new-public-group-chat                 "सार्वजनिक च्याटमा भाग लिने"
   :edit-chats                            "च्याटहरुको सम्पादन"
   :search-chats                          "च्याटहरुमा खोज"
   :empty-topic                           "खाली विषय"
   :topic-format                          "गलत ढाँचा [a-z0-9\\-]+"
   :public-group-topic                    "विषय"

   ;;discover
   :discover                              "भेट्टाउनुहोस्"
   :none                                  "बिना कुनै"
   :search-tags                           "खोज्ने ट्यागहरु यहाँ टाइप गर्नुहोस्"
   :popular-tags                          "लोकप्रिय ट्यागहरु"
   :recent                                "भर्खरको"
   :no-statuses-discovered                "कुनैपनि स्टेटस भेट्टाइएन"
   :no-statuses-found                     "कुनैपनि स्टेटस फेलापारिएन"

   ;;settings
   :settings                              "सेटिङ्गहरु"

   ;;contacts
   :contacts                              "सम्पर्कहरु"
   :new-contact                           "नया सम्पर्क"
   :delete-contact                        "सम्पर्क मेटाउने"
   :delete-contact-confirmation           "यो सम्पर्क तपाईंको सम्पर्कहरुबाट हटाइने छ"
   :remove-from-group                     "समूह बाट हटाउने"
   :edit-contacts                         "सम्पर्क सम्पादन"
   :search-contacts                       "सम्पर्कहरुमा खोज"
   :contacts-group-new-chat               "नया च्याट सुरु"
   :choose-from-contacts                  "सम्पर्कहरुबाट चयन गर्नुहोस्"
   :no-contacts                           "हाल कुनै सम्पर्कहरु छैनन्"
   :show-qr                               "QR देखाउने"
   :enter-address                         "ठेगान प्रविष्ट गर्ने"
   :more                                  "अरु"

   ;;group-settings
   :remove                                "हटाउनुहोस्"
   :save                                  "साँच्नुहोस्"
   :delete                                "मेटाउनुहोस्"
   :clear-history                         "इतिहास खाली गर्नुहोस्"
   :mute-notifications                    "सूचनाहरु मौन बनाउनुहोस्"
   :leave-chat                            "च्याटबाट बाहिरिनुहोस्"
   :chat-settings                         "च्याट सेटिङ्गहरु"
   :edit                                  "सम्पादन"
   :add-members                           "सदस्यहरु जोड्नुहोस्"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "नया समूह"
   :reorder-groups                        "समूह पुन: क्रमबद्ध गर्नुहोस्"
   :edit-group                            "समूह सम्पादन"
   :delete-group                          "समूह मेटाउनुहोस्"
   :delete-group-confirmation             "यो समूह तपाईंको समूहहरुबाट हटाइने छ । यसले सम्पर्कहरुमा असर गर्दैन ।"
   :delete-group-prompt                   "यसले सम्पर्कहरुमा असर गर्दैन ।"
   :contact-s                             {:one   "सम्पर्क"
                                           :other "सम्पर्कहरु"}

   ;;participants

   ;;protocol
   :received-invitation                   "च्याट निमन्त्रणा पाइयो"
   :removed-from-chat                     "तपाईंलाई सामूहिक च्याटबाट हटाइयो"
   :left                                  "बाहिरियो"
   :invited                               "आमन्त्रित"
   :removed                               "हटाइयो"
   :You                                   "तपाईं"

   ;;new-contact
   :add-new-contact                       "नया सम्पर्क जोड्नुहोस्"
   :scan-qr                               "QR स्क्यान गर्नुहोस्"
   :name                                  "नाम"
   :address-explication                   "सायद यहां ठेगाना बारे बयान गर्ने अनि त्यो कहाँ खोज्ने भन्ने कुराको कुनै पाठ हुनुपर्छ ।"
   :enter-valid-public-key                "कृपया एक मान्य सार्वजनिक सांचो प्रविष्ट गर्नुहोस् वा QR कोड स्क्यान गर्नुहोस् "
   :contact-already-added                 "यो सम्पर्क पहिल्यै जोडीसकिएको छ"
   :can-not-add-yourself                  "आफुले आफुलाइनै जोड्न मिल्दैन"
   :unknown-address                       "अज्ञात ठेगाना"

   ;;login
   :connect                               "कनेक्ट"
   :address                               "ठेगाना"
   :password                              "पासवर्ड"
   :sign-in-to-status                     "स्टेटसमा साइन-इन गर्नुहोस्"
   :sign-in                               "साइन-इन"
   :wrong-password                        "गलत पासवर्ड"

   ;;recover
   :passphrase                            "गुप्तवाक्यांश"
   :recover                               "पुनर्स्थापना"
   :twelve-words-in-correct-order         "१२ सब्दहरु सही क्रममा"

   ;;accounts
   :recover-access                        "पहुँच पुनर्स्थापना गर्नुहोस्"
   :create-new-account                    "नया खाता सिर्जना गर्नुहोस्"

   ;;wallet-qr-code
   :done                                  "सम्पन्न"
   :main-wallet                           "मुख्य वालेट"

   ;;validation
   :invalid-phone                         "नमिल्ने फोन नम्बर"
   :amount                                "रकम"

   ;;transactions
   :confirm                               "पुष्टि गर्नुहोस्"
   :transaction                           "कारोबार"
   :status                                "स्टेटस"
   :recipient                             "प्रापक"
   :to                                    "तर्फ"
   :from                                  "बाट"
   :data                                  "डाटा"
   :got-it                                "पाइयो"

   ;;webview
   :web-view-error                        "ओहोऽऽऽ, त्रुटि"})
