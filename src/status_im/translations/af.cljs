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
   :invite-friends                        "Nooi vriende"
   :faq                                   "Vrae"
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
   :datetime-multiple                     "e" ; TODO probably wrong
   :datetime-ago                          "gelede"
   :datetime-yesterday                    "gister"
   :datetime-today                        "vandag"

   ;profile
   :profile                               "Profiel"
   :report-user                           "RAPPORTEER GEBRUIKER"
   :message                               "Boodskap"
   :username                              "Gebruikernaam"
   :not-specified                         "Nie gespesifiseer nie"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnommer"
   :email                                 "Epos"
   :profile-no-status                     "Geen status"
   :add-to-contacts                       "Voeg by kontakte"
   :error-incorrect-name                  "Kies asseblief 'n ander naam"
   :error-incorrect-email                 "Verkeerde epos"

   ;;make_photo
   :image-source-title                    "Profielbeeld"
   :image-source-make-photo               "Opname"
   :image-source-gallery                  "Kies uit galery"
   :image-source-cancel                   "Kanselleer"

   ;sign-up
   :contacts-syncronized                  "U kontakte is gesinchroniseer"
   :confirmation-code                     (str "Dankie! Ons het vir u 'n SMS gestuur met 'n bevestigings "
                                               "kode. Verskaf asseblief daardie kode om u telefoonnommer te bevestig")
   :incorrect-code                        (str "Jammer, die kode was nie korrek nie, voer asseblief weer in")
   :generate-passphrase                   (str "Ek sal 'n tydelike wagwoord vir jou skep sodat jy jou "
                                               "toegang kan herstel of van 'n ander toestel af kan aanteken")
   :phew-here-is-your-passphrase          "*Sjoe* dit was moeilik, hier is jou tydelike wagwoord, *skryf dit neer en hou dit veilig!* Jy sal dit nodig hê om jou rekening te herwin."
   :here-is-your-passphrase               "Hier is jou tydelike wagwoord, *skryf dit neer en hou dit veilig!* Jy sal dit nodig hê om jou rekening te herwin."
   :written-down                          "Maak seker dat jy dit veilig neergeskryf het"
   :phone-number-required                 "Tik hier om jou telefoonnommer in te voer & ek sal jou vriende opspoor"
   :intro-status                          "Gesels met my om jou rekening op te stel en jou stellings te verander!"
   :intro-message1                        "Welkom by Status\nTik tik hierdie boodskap om jou wagwoord te stel & laat ons begin!"
   :account-generation-message            "Net 'n oomblik, ek moet mal somme doen om jou rekening te skep!"

   ;chats
   :chats                                 "Geselsies"
   :new-chat                              "Nuwe geselsie"
   :new-group-chat                        "Nuwe groepgeselsie"

   ;discover
   :discover                             "Ontdekking"
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
   :show-all                              "WYS ALMAL"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mense"
   :contacts-group-new-chat               "Begin nuwe geselsie"
   :no-contacts                           "Nog geen kontakte nie"
   :show-qr                               "Wys QR"

   ;group-settings
   :remove                                "Verwyder"
   :save                                  "Stoor"
   :change-color                          "Verander kleur"
   :clear-history                         "Maak geskiedenis skoon"
   :delete-and-leave                      "Vee uit en gaan uit"
   :chat-settings                         "Geselsie-stellings"
   :edit                                  "Wysig"
   :add-members                           "Voeg lede by"
   :blue                                  "Blou"
   :purple                                "Pers"
   :green                                 "Groen"
   :red                                   "Rooi"

   ;commands
   :money-command-description             "Stuur geld"
   :location-command-description          "Stuur ligging"
   :phone-command-description             "Stuur telefoonnommer"
   :phone-request-text                    "Telefoonnommer-versoek"
   :confirmation-code-command-description "Stuur bevestigingskode"
   :confirmation-code-request-text        "Bevestigingskode-versoek"
   :send-command-description              "Stuur ligging"
   :request-command-description           "Stuur versoek"
   :keypair-password-command-description  ""
   :help-command-description              "Help"
   :request                               "Versoek"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH aan {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH van {{chat-name}}"
   :command-text-location                 "Ligging {{address}}"
   :command-text-browse                   "Webblaaier-blad: {{webpage}}"
   :command-text-send                     "Transaksie: {{amount}} ETH"
   :command-text-help                     "Help"

   ;new-group
   :group-chat-name                       "Bynaam"
   :empty-group-chat-name                 "Voer assseblief 'n naam in"
   :illegal-group-chat-name               "Kies asseblief 'n ander naam"

   ;participants
   :add-participants                      "Voeg deelnemers by"
   :remove-participants                   "Verwyder deelnemers"

   ;protocol
   :received-invitation                   "geselsie-uitnodiging ontvang"
   :removed-from-chat                     "het jou van groepsgeselsie verwyder"
   :left                                  "uitgegaan"
   :invited                               "uitgenooi"
   :removed                               "verwyder"
   :You                                   "Jou"

   ;new-contact
   :add-new-contact                       "Voeg nuwe kontak by"
   :import-qr                             "Voer in"
   :scan-qr                               "Skandeer QR"
   :name                                  "Naam"
   :whisper-identity                      "Whisper-identiteit"
   :address-explication                   "Miskien moet hier 'n bietjie teks wees wat verduidelik wat 'n adres is en waar om daarvoor te soek."
   :enter-valid-address                   "Voer asseblief 'n geldige adres in of skandeer 'n QR-kode"
   :contact-already-added                 "Die kontak is alreeds bygevoeg"
   :can-not-add-yourself                  "Jy kan nie jouself byvoeg nie"
   :unknown-address                       "Onbekende adres"


   ;login
   :connect                               "Konnekteer"
   :address                               "Adres"
   :password                              "Wagwoord"
   :login                                 "Teken aan"
   :wrong-password                        "Verkeerde wagwoord"

   ;recover
   :recover-from-passphrase               "Herstel van tydelike wagwoord"
   :recover-explain                       "Voer asseblief die tydelike wagwoord vir jou wagwoord in om toegang te herstel"
   :passphrase                            "Tydelike wagwoord"
   :recover                               "Herstel"
   :enter-valid-passphrase                "Voer asseblief 'n tydelike wagwoord in"
   :enter-valid-password                  "Voer asseblief 'n wagwoord in"

   ;accounts
   :recover-access                        "Herwin toegang"
   :add-account                           "Voeg rekening by"

   ;wallet-qr-code
   :done                                  "Gedoen"
   :main-wallet                           "Hoofbeursie"

   ;validation
   :invalid-phone                         "Ongeldige telefoonnommer"
   :amount                                "Bedrag"
   :not-enough-eth                        (str "Nie genoeg ETH in die rekening nie "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Bevestig transaksie"
                                           :other "Bevestig {{count}} transaksies"
                                           :zero  "Geen transaksies"}
   :status                                "Status"
   :pending-confirmation                  "Bevestiging hangende"
   :recipient                             "Ontvanger"
   :one-more-item                         "Nog een item"
   :fee                                   "Fooi"
   :value                                 "Waarde"

   ;:webview
   :web-view-error                        "oepsie, fout"})
