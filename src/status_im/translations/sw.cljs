(ns status-im.translations.sw)

(def translations
  {
   ;common
   :members-title                         "Wanachama"
   :not-implemented                       "!haijatekelezwa"
   :chat-name                             "Jina la gumzo"
   :notifications-title                   "Notisi na sauti"
   :offline                               "Nje ya mtandao"

   ;drawer
   :invite-friends                        "Karibisha marafiki"
   :faq                                   "Maswali Ya Mara kwa mara"
   :switch-users                          "Badili kwa watumiaji"

   ;chat
   :is-typing                             "anaandika"
   :and-you                               "na wewe"
   :search-chat                           "Tafuta gumzo"
   :members                               {:one   "Mwanachama 1"
                                           :other "{{count}} wanachama"
                                           :zero  "hakuna wanachama"}
   :members-active                        {:one   "Mwanachama 1, Aliyewajibika 1"
                                           :other "{{count}} wanachama, {{count}} waliowajibika"
                                           :zero  "hakuna wanachama"}
   :active-online                         "Mtandaoni"
   :active-unknown                        "Hawajulikani"
   :available                             "Wanapatikana"
   :no-messages                           "Hakuna ujumbe"
   :suggestions-requests                  "Maombi"
   :suggestions-commands                  "Amri"

   ;sync
   :sync-in-progress                      "Kulandanisha..."
   :sync-synced                           "Katika ulandanishaji"

   ;messages
   :status-sending                        "Kutuma"
   :status-pending                        "Inasubiri"
   :status-sent                           "Tuma"
   :status-seen-by-everyone               "Imeonekana na kila mtu"
   :status-seen                           "Imeonekana"
   :status-delivered                      "Imefikishwa"
   :status-failed                         "Imeshindwa"

   ;datetime
   :datetime-second                       {:one   "sekunde"
                                           :other "sekunde"}
   :datetime-minute                       {:one   "dakika"
                                           :other "dakika"}
   :datetime-hour                         {:one   "saa"
                                           :other "masaa"}
   :datetime-day                          {:one   "siku"
                                           :other "siku"}
   :datetime-multiple                     "s"
   :datetime-ago                          "iliyopita"
   :datetime-yesterday                    "jana"
   :datetime-today                        "leo"

   ;profile
   :profile                               "Profaili"
   :report-user                           "RIPOTI MTUMIAJI"
   :message                               "Ujumbe"
   :username                              "Jina la mtumiaji"
   :not-specified                         "Haijafafanuliwa"
   :public-key                            "Ufunguo wa Umma"
   :phone-number                          "Namba ya Simu"
   :email                                 "Barua pepe"
   :profile-no-status                     "Hakuna hadhi"
   :add-to-contacts                       "Ongeza kwa mawasiliano"
   :error-incorrect-name                  "Tafadhali chagua jina lingine"
   :error-incorrect-email                 "Barua pepe sio sahihi"

   ;;make_photo
   :image-source-title                    "Picha ya profaili"
   :image-source-make-photo               "Chukua picha"
   :image-source-gallery                  "Chagua kutoka nyumba ya sanaa"
   :image-source-cancel                   "Ghairi"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopiera"
   :sharing-share                         "Dela..."
   :sharing-cancel                        "Ghairi"

   ;sign-up
   :contacts-syncronized                  "Mawasiliano yako yamelandanishwa"
   :confirmation-code                     (str "Asante! Tumekutumia ujumbe mfupi na uthibitisho "
                                               "kificho. Tafadhali peana hicho kificho kuthibitisha namba yako ya simu")
   :incorrect-code                        (str "Samahani kificho hakikuwa sahihi, tafadhali ingiza tena")
   :generate-passphrase                   (str "Nitakutengenezea kaulisiri ili uweze kurejesha "
                                               "upatikanaji au kuingia kwa kutumia kifaa kingine")
   :phew-here-is-your-passphrase          "*Phew* hiyo ilikuwa ngumu, hapa ni kaulisiri yako, *iandike na uiweke salama!* Utaihitaji kwa ajili ya kufufua akaunti yako."
   :here-is-your-passphrase               "Hapa ni kaulisiri yako, *iandike na uiweke salama!* Utaihitaji kwa ajili ya kufufua akaunti yako."
   :written-down                          "Hakikisha umeiandika salama"
   :phone-number-required                 "Bofya hapa kuingiza namba yako ya simu na nitapata marafiki zako"
   :intro-status                          "Ongea nami kuanzisha akaunti yako na kubadilisha mipangilio yako!"
   :intro-message1                        "Karibu kwa Hali na Ubofye ujumbe huu ili kuweka nenosiri lako na uanze!"
   :account-generation-message            "Nipe sekunde, naenda kufanya baadhi ya hisabati kutengeneza akaunti yako!"

   ;chats
   :chats                                 "Gumzo"
   :new-chat                              "Gumzo mpya"
   :new-group-chat                        "Gumzo mpya ya kikundi"

   ;discover
   :discover                             "Ugunduzi"
   :none                                  "Hakuna"
   :search-tags                           "Andika vitambulisho vyako vya kutafuta hapa"
   :popular-tags                          "Vitambulisho maarufu"
   :recent                                "Hivi karibuni"
   :no-statuses-discovered                "Hakuna hali zimegundulika"

   ;settings
   :settings                              "Mipangilio"

   ;contacts
   :contacts                              "Mawasiliano"
   :new-contact                           "Mawasiliano mapya"
   :show-all                              "ONYESHA YOTE"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Watu"
   :contacts-group-new-chat               "Anza gumzo mpya"
   :no-contacts                           "Bado hakuna mawasiliano"
   :show-qr                               "Onyesha QR"

   ;group-settings
   :remove                                "Ondoa"
   :save                                  "Hifadhi"
   :change-color                          "Badilisha rangi"
   :clear-history                         "Futa historia"
   :delete-and-leave                      "Futa na uondoke"
   :chat-settings                         "Mipangilio ya gumzo"
   :edit                                  "Hariri"
   :add-members                           "Ongeza wanachama"
   :blue                                  "Bluu"
   :purple                                "Zambarau"
   :green                                 "Kijani"
   :red                                   "Nyekundu"

   ;commands
   :money-command-description             "Tuma pesa"
   :location-command-description          "Tuma eneo"
   :phone-command-description             "Tuma namba ya simu"
   :phone-request-text                    "Ombi la namba ya simu"
   :confirmation-code-command-description "Tuma kificho cha uthibitisho"
   :confirmation-code-request-text        "Ombi la kificho cha uthibitisho"
   :send-command-description              "Tuma eneo"
   :request-command-description           "Tuma ombi"
   :keypair-password-command-description  ""
   :help-command-description              "Msaada"
   :request                               "Ombi"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH kwa {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH kutoka {{chat-name}}"

   ;new-group
   :group-chat-name                       "Jina la gumzo"
   :empty-group-chat-name                 "Tafadhali ingiza jina"
   :illegal-group-chat-name               "Tafadhali chagua jina lingine"

   ;participants
   :add-participants                      "Ongeza Washirika"
   :remove-participants                   "Ondoa Washirika"

   ;protocol
   :received-invitation                   "mwaliko wa gumzo ulipokelewa"
   :removed-from-chat                     "uliondolewa kwenye kikundi cha gumzo"
   :left                                  "uliondoka"
   :invited                               "ulialikwa"
   :removed                               "uliondolewa"
   :You                                   "Wewe"

   ;new-contact
   :add-new-contact                       "Ongeza mawasiliano mapya"
   :import-qr                             "Agiza"
   :scan-qr                               "Piga picha QR"
   :name                                  "Jina"
   :whisper-identity                      "Utambulisho wa Tetesi"
   :address-explication                   "Labda hapa kunapaswa kuwa na baadhi ya maandishi kueleza anwani ni nini na ni wapi pa kuitafuta"
   :enter-valid-address                   "Tafadhali ingiza anwani sahihi au upige picha ya kificho cha QR"
   :contact-already-added                 "Tayari mawasiliano yameongezwa"
   :can-not-add-yourself                  "Huwezi kujiongeza mwenyewe"
   :unknown-address                       "Anwani Haijulikani"


   ;login
   :connect                               "Unganisha"
   :address                               "Anwani"
   :password                              "Nenosiri"
   :login                                 "Ingia"
   :wrong-password                        "Nenosiri sio halali"

   ;recover
   :recover-from-passphrase               "Okoa/fufua kutoka kaulisiri"
   :recover-explain                       "Tafadhali ingiza kaulisiri ili nenosiri lako liokoe upatikanaji"
   :passphrase                            "Kaulisiri"
   :recover                               "Okoa"
   :enter-valid-passphrase                "Tafadhali ingiza kaulisiri"
   :enter-valid-password                  "Tafadhali ingiza nenosiri"

   ;accounts
   :recover-access                        "Okoa ufikiaji/Upatikanaji"
   :add-account                           "Ongeza akaunti"

   ;wallet-qr-code
   :done                                  "Imefanyika"
   :main-wallet                           "Mkoba Mkuu"

   ;validation
   :invalid-phone                         "Namba ya simu ni batili"
   :amount                                "Kiasi"
   :not-enough-eth                        (str "ETH haitoshi kwenye salio "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Thibitisha mashirikiano"
                                           :other "Thibitisha {{count}} mashirikiano"
                                           :zero  "Hakuna mashirikiano"}
   :status                                "Hali"
   :pending-confirmation                  "Uthibitisho unasubiriwa"
   :recipient                             "Mpokeaji"
   :one-more-item                         "Bidhaa moja zaidi"
   :fee                                   "Ada"
   :value                                 "Thamani"

   ;:webview
   :web-view-error                        "nadhani, hitilafu"})
