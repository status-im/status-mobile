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
   :datetime-ago                          "iliyopita"
   :datetime-yesterday                    "jana"
   :datetime-today                        "leo"

   ;profile
   :profile                               "Profaili"
   :message                               "Ujumbe"
   :not-specified                         "Haijafafanuliwa"
   :public-key                            "Ufunguo wa Umma"
   :phone-number                          "Namba ya Simu"
   :add-to-contacts                       "Ongeza kwa mawasiliano"

   ;;make_photo
   :image-source-title                    "Picha ya profaili"
   :image-source-make-photo               "Chukua picha"
   :image-source-gallery                  "Chagua kutoka nyumba ya sanaa"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopiera"
   :sharing-share                         "Dela..."
   :sharing-cancel                        "Ghairi"

   ;sign-up
   :contacts-syncronized                  "Mawasiliano yako yamelandanishwa"
   :confirmation-code                     (str "Asante! Tumekutumia ujumbe mfupi na uthibitisho "
                                               "kificho. Tafadhali peana hicho kificho kuthibitisha namba yako ya simu")
   :incorrect-code                        (str "Samahani kificho hakikuwa sahihi, tafadhali ingiza tena")
   :phew-here-is-your-passphrase          "*Phew* hiyo ilikuwa ngumu, hapa ni kaulisiri yako, *iandike na uiweke salama!* Utaihitaji kwa ajili ya kufufua akaunti yako."
   :here-is-your-passphrase               "Hapa ni kaulisiri yako, *iandike na uiweke salama!* Utaihitaji kwa ajili ya kufufua akaunti yako."
   :phone-number-required                 "Bofya hapa kuingiza namba yako ya simu na nitapata marafiki zako"
   :intro-status                          "Ongea nami kuanzisha akaunti yako na kubadilisha mipangilio yako!"
   :intro-message1                        "Karibu kwa Hali na Ubofye ujumbe huu ili kuweka nenosiri lako na uanze!"
   :account-generation-message            "Nipe sekunde, naenda kufanya baadhi ya hisabati kutengeneza akaunti yako!"

   ;chats
   :chats                                 "Gumzo"
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
   :contacts-group-new-chat               "Anza gumzo mpya"
   :no-contacts                           "Bado hakuna mawasiliano"
   :show-qr                               "Onyesha QR"

   ;group-settings
   :remove                                "Ondoa"
   :save                                  "Hifadhi"
   :clear-history                         "Futa historia"
   :chat-settings                         "Mipangilio ya gumzo"
   :edit                                  "Hariri"
   :add-members                           "Ongeza wanachama"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "mwaliko wa gumzo ulipokelewa"
   :removed-from-chat                     "uliondolewa kwenye kikundi cha gumzo"
   :left                                  "uliondoka"
   :invited                               "ulialikwa"
   :removed                               "uliondolewa"
   :You                                   "Wewe"

   ;new-contact
   :add-new-contact                       "Ongeza mawasiliano mapya"
   :scan-qr                               "Piga picha QR"
   :name                                  "Jina"
   :address-explication                   "Labda hapa kunapaswa kuwa na baadhi ya maandishi kueleza anwani ni nini na ni wapi pa kuitafuta"
   :contact-already-added                 "Tayari mawasiliano yameongezwa"
   :can-not-add-yourself                  "Huwezi kujiongeza mwenyewe"
   :unknown-address                       "Anwani Haijulikani"


   ;login
   :connect                               "Unganisha"
   :address                               "Anwani"
   :password                              "Nenosiri"
   :wrong-password                        "Nenosiri sio halali"

   ;recover
   :passphrase                            "Kaulisiri"
   :recover                               "Okoa"

   ;accounts
   :recover-access                        "Okoa ufikiaji/Upatikanaji"

   ;wallet-qr-code
   :done                                  "Imefanyika"
   :main-wallet                           "Mkoba Mkuu"

   ;validation
   :invalid-phone                         "Namba ya simu ni batili"
   :amount                                "Kiasi"
   ;transactions
   :status                                "Hali"
   :recipient                             "Mpokeaji"

   ;:webview
   :web-view-error                        "nadhani, hitilafu"})
