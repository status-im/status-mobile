(ns status-im.translations.af)

(def translations
  {
   ;common
   :members-title                         "lede"
   :not-implemented                       "!nie geimplimenteer nie"
   :chat-name                             "Bynaam"
   :notifications-title                   "Kennisgewings en klanke"
   :offline                               "Aflyn"

   ;drawer
   :switch-users                          "Verander gebruikers"

   ;chat
   :is-typing                             "is besig om te tik"
   :and-you                               "en jy"
   :search-chat                           "Deursoek geselsies"
   :members                               {:one   "1 lid"
                                           :other "{{count}} lede"
                                           :zero  "geen lede"}
   :members-active                        {:one   "1 lid, 1 aktief"
                                           :other "{{count}} lede, {{count}} aktief"
                                           :zero  "geen lede"}
   :active-online                         "Aanlyn"
   :active-unknown                        "Onbekend"
   :available                             "Beskikbaar"
   :no-messages                           "Geen boodskappe"
   :suggestions-requests                  "Versoeke"
   :suggestions-commands                  "Opdragte"

   ;sync
   :sync-in-progress                      "Besig om te sinchroniseer..."
   :sync-synced                           "gesinchroniseer"

   ;messages
   :status-sending                        "Besig om te stuur"
   :status-pending                        "Hangende"
   :status-sent                           "Gestuur"
   :status-seen-by-everyone               "Deur almal gesien"
   :status-seen                           "Gesien"
   :status-delivered                      "Afgelewer"
   :status-failed                         "Gefaal"

   ;datetime
   :datetime-second                       {:one   "sekonde"
                                           :other "sekondes"}
   :datetime-minute                       {:one   "minuut"
                                           :other "minute"}
   :datetime-hour                         {:one   "uur"
                                           :other "ure"}
   :datetime-day                          {:one   "dag"
                                           :other "dae"}
   :datetime-ago                          "gelede"
   :datetime-yesterday                    "gister"
   :datetime-today                        "vandag"

   ;profile
   :profile                               "Profiel"
   :message                               "Boodskap"
   :not-specified                         "Nie gespesifiseer nie"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnommer"
   :add-to-contacts                       "Voeg by kontakte"

   ;;make_photo
   :image-source-title                    "Profielbeeld"
   :image-source-make-photo               "Opname"
   :image-source-gallery                  "Kies uit galery"

   ;sign-up
   :contacts-syncronized                  "U kontakte is gesinchroniseer"
   :confirmation-code                     (str "Dankie! Ons het vir u 'n SMS gestuur met 'n bevestigings "
                                               "kode. Verskaf asseblief daardie kode om u telefoonnommer te bevestig")
   :incorrect-code                        (str "Jammer, die kode was nie korrek nie, voer asseblief weer in")
   :phew-here-is-your-passphrase          "*Sjoe* dit was moeilik, hier is jou tydelike wagwoord, *skryf dit neer en hou dit veilig!* Jy sal dit nodig hê om jou rekening te herwin."
   :here-is-your-passphrase               "Hier is jou tydelike wagwoord, *skryf dit neer en hou dit veilig!* Jy sal dit nodig hê om jou rekening te herwin."
   :phone-number-required                 "Tik hier om jou telefoonnommer in te voer & ek sal jou vriende opspoor"
   :intro-status                          "Gesels met my om jou rekening op te stel en jou stellings te verander!"
   :intro-message1                        "Welkom by Status\nTik tik hierdie boodskap om jou wagwoord te stel & laat ons begin!"
   :account-generation-message            "Net 'n oomblik, ek moet mal somme doen om jou rekening te skep!"

   ;chats
   :chats                                 "Geselsies"
   :new-group-chat                        "Nuwe groepgeselsie"

   ;discover
   :discover                              "Ontdekking"
   :none                                  "Geen"
   :search-tags                           "Tik jou soek-oortjies hier in"
   :popular-tags                          "Gewilde oortjies"
   :recent                                "Onlangs"
   :no-statuses-discovered                "Geen statusse gevind nie"

   ;settings
   :settings                              "Stellings"

   ;contacts
   :contacts                              "Kontakte"
   :new-contact                           "Nuwe kontak"
   :contacts-group-new-chat               "Begin nuwe geselsie"
   :no-contacts                           "Nog geen kontakte nie"
   :show-qr                               "Wys QR"

   ;group-settings
   :remove                                "Verwyder"
   :save                                  "Stoor"
   :clear-history                         "Maak geskiedenis skoon"
   :chat-settings                         "Geselsie-stellings"
   :edit                                  "Wysig"
   :add-members                           "Voeg lede by"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "geselsie-uitnodiging ontvang"
   :removed-from-chat                     "het jou van groepsgeselsie verwyder"
   :left                                  "uitgegaan"
   :invited                               "uitgenooi"
   :removed                               "verwyder"
   :You                                   "Jou"

   ;new-contact
   :add-new-contact                       "Voeg nuwe kontak by"
   :scan-qr                               "Skandeer QR"
   :name                                  "Naam"
   :address-explication                   "Miskien moet hier 'n bietjie teks wees wat verduidelik wat 'n adres is en waar om daarvoor te soek."
   :contact-already-added                 "Die kontak is alreeds bygevoeg"
   :can-not-add-yourself                  "Jy kan nie jouself byvoeg nie"
   :unknown-address                       "Onbekende adres"


   ;login
   :connect                               "Konnekteer"
   :address                               "Adres"
   :password                              "Wagwoord"
   :wrong-password                        "Verkeerde wagwoord"

   ;recover
   :passphrase                            "Tydelike wagwoord"
   :recover                               "Herstel"

   ;accounts
   :recover-access                        "Herwin toegang"

   ;wallet-qr-code
   :done                                  "Gedoen"
   :main-wallet                           "Hoofbeursie"

   ;validation
   :invalid-phone                         "Ongeldige telefoonnommer"
   :amount                                "Bedrag"
   ;transactions
   :status                                "Status"
   :recipient                             "Ontvanger"

   ;:webview
   :web-view-error                        "oepsie, fout"

   :confirm                               "Bevestig"
   :phone-national                        "Nasionaal"
   :public-group-topic                    "Onderwerp"
   :debug-enabled                         "Ontfout-bediener is bekendgestel! U kan nou u DApp byvoeg deur *status-dev-cli scan* te hardloop vanaf u rekenaar"
   :new-public-group-chat                 "Sluit aan by openbare klets"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Kanselleer"
   :twelve-words-in-correct-order         "12 woorde in korrekte volgorde"
   :remove-from-contacts                  "Verwyder van kontakte"
   :delete-chat                           "Skrap klets"
   :edit-chats                            "Redigeer kletse"
   :sign-in                               "Teken aan"
   :create-new-account                    "Skep nuwe rekening"
   :sign-in-to-status                     "Teken aan op Status"
   :got-it                                "Het dit"
   :move-to-internal-failure-message      "Ons moet ‘n paar belangrike lêers skuif van eksterne na interne berging. Om dit te doen, het ons u toestemming nodig. Ons sal nie eksterne berging gebruik in toekomstige weergawes nie."
   :edit-group                            "Redigeer groep"
   :delete-group                          "Skrap groep"
   :browsing-title                        "Blaai"
   :reorder-groups                        "Herrangskik groepe"
   :browsing-cancel                       "Kanselleer"
   :faucet-success                        "Kraan versoek is ontvang"
   :choose-from-contacts                  "Kies uit kontakte"
   :new-group                             "Nuwe groep"
   :phone-e164                            "Internasionaal 1"
   :remove-from-group                     "Verwyder van groep"
   :search-contacts                       "Soek kontakte"
   :transaction                           "Transaksie"
   :public-group-status                   "Openbaar"
   :leave-chat                            "Verlaat klets"
   :start-conversation                    "Begin gesprek"
   :topic-format                          "Verkeerde formaat [a-z0-9\\-]+"
   :enter-valid-public-key                "Voer asseblief ‘n geldige openbare sleutel in of skandeer ‘n QR-kode in"
   :faucet-error                          "Kraan versoek fout"
   :phone-significant                     "Aansienlik"
   :search-for                            "Soek vir..."
   :sharing-copy-to-clipboard             "Kopieer na knipbord"
   :phone-international                   "Internasionaal 2"
   :enter-address                         "Voeg ‘n adres in"
   :send-transaction                      "Stuur transaksie"
   :delete-contact                        "Skrap kontak"
   :mute-notifications                    "Demp kennisgewings"


   :contact-s                             {:one   "kontak"
                                           :other "kontakte"}
   :next                                  "Volgende"
   :from                                  "Van"
   :search-chats                          "Soek kletse"
   :in-contacts                           "In kontakte"

   :sharing-share                         "Deel..."
   :type-a-message                        "Tik ‘n boodskap..."
   :type-a-command                        "Begin ‘n opdrag tik..."
   :shake-your-phone                      "Vind ‘n fout of het ‘n voorstel? Skud ~net~ u foon!"
   :status-prompt                         "Skep ‘n status om te help dat mense weet oor die goed wat u aanbied. U kan #hutsmerke ook gebruik."
   :add-a-status                          "Voeg ‘n status by..."
   :error                                 "Fout"
   :edit-contacts                         "Redigeer kontakte"
   :more                                  "meer"
   :cancel                                "Kanselleer"
   :no-statuses-found                     "Geen statusse gevind nie"
   :browsing-open-in-web-browser          "Maak oop in webblaaier"
   :delete-group-prompt                   "Dit sal nie kontakte affekteer nie"
   :edit-profile                          "Redigeer profiel"


   :empty-topic                           "Leë onderwerp"
   :to                                    "Aan"
   :data                                  "Data"})
