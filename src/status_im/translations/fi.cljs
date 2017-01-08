(ns status-im.translations.fi)

(def translations
  {
   ;common
   :members-title                         "Jäsenet"
   :not-implemented                       "!ei toteutettu"
   :chat-name                             "Keskustelun nimi"
   :notifications-title                   "Ilmoitukset ja äänet"
   :offline                               "Ei linjoilla"
   :search-for                            "Etsi..."
   :cancel                                "Keskeytä"

   ;drawer
   :invite-friends                        "Kutsu ystäviä"
   :faq                                   "UKK"
   :switch-users                          "Vaihda käyttäjää"
   :feedback                              "Palautetta?\nRavista puhelintasi!"

   ;chat
   :is-typing                             "kirjoittaa"
   :and-you                               "ja sinä"
   :search-chat                           "Etsi keskustelusta"
   :members                               {:one   "1 jäsen"
                                           :other "{{count}} jäsentä"
                                           :zero  "ei jäseniä"}
   :members-active                        {:one   "1 jäsen, 1 aktiivinen"
                                           :other "{{count}} jäsentä, {{count}} aktiivista"
                                           :zero  "ei jäseniä"}
   :active-online                         "Linjoilla"
   :active-unknown                        "Tuntematon"
   :available                             "Saatavilla"
   :no-messages                           "Ei viestejä"
   :suggestions-requests                  "Pyynnöt"
   :suggestions-commands                  "Komennot"
   :faucet-success                        "Faucet pyyntö vastaanotettu"
   :faucet-error                          "Faucet pyyntö virhe"

   ;sync
   :sync-in-progress                      "Synkronoidaan..."
   :sync-synced                           "Synkronointi valmis"

   ;messages
   :status-sending                        "Lähetetään"
   :status-pending                        "Lähetetään"
   :status-sent                           "Lähetetty"
   :status-seen-by-everyone               "Nähty (kaikki)"
   :status-seen                           "Nähty"
   :status-delivered                      "Toimitettu"
   :status-failed                         "Epäonnistui"

   ;datetime
   :datetime-second                       {:one   "sekuntti"
                                           :other "sekunttia"}
   :datetime-minute                       {:one   "minuutti"
                                           :other "minuuttia"}
   :datetime-hour                         {:one   "tunti"
                                           :other "tuntia"}
   :datetime-day                          {:one   "päivä"
                                           :other "päivää"}
   :datetime-multiple                     "s"
   :datetime-ago                          "sitten"
   :datetime-yesterday                    "eilen"
   :datetime-today                        "tänään"

   ;profile
   :profile                               "Profiili"
   :report-user                           "ILMIANNA KÄYTTÄJÄ"
   :message                               "Lähetä viesti"
   :username                              "Käyttäjänimi"
   :not-specified                         "Ei määritetty"
   :public-key                            "Julkinen avain"
   :phone-number                          "Puhelinnumero"
   :email                                 "Sähköposti"
   :profile-no-status                     "No status"
   :add-to-contacts                       "Lisää kontakteihin"
   :error-incorrect-name                  "Tarkista nimi"
   :error-incorrect-email                 "Tarkista sähköpostiosoite"

   ;;make_photo
   :image-source-title                    "Profiilikuva"
   :image-source-make-photo               "Ota kuva"
   :image-source-gallery                  "Valitse galleriasta"
   :image-source-cancel                   "Keskeytä"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopioi leikepöydälle"
   :sharing-share                         "Jaa..."
   :sharing-cancel                        "Keskeytä"

   ;sign-up
   :contacts-syncronized                  "Kontaktisi ovat synkronoitu"
   :confirmation-code                     (str "Kiitos! Lähetimme sinulle juuri tekstiviestin jossa on "
                                               "varmennuskoodi. Anna koodi vahvistaaksesi puhelinnumerosi.")
   :incorrect-code                        (str "Antamasi koodi ei ole oikein. Yritä uudestaan.")
   :generate-passphrase                   (str "Generoin sinulle salasanan, jolla voit palauttaa käyttöoikeutesi "
                                               "tai kirjautua toiselta laitteelta")
   :phew-here-is-your-passphrase          (str "*Huh* olipa haastavaa, tässä palautussalasanasi, *kirjoita se "
                                               "ylös ja pidä hyvässä tallessa!* Tarvitset sitä palauttaaksesi "
                                               " käyttöoikeutesi.")
   :written-down                          "Varmista että olet kirjoittanut palautussalasanasi ylös."
   :phone-number-required                 "Napauta tästä antaaksesi puhelinnumerosi, niin etsin ystäväsi"
   :shake-your-phone                      "Löysitkö bugin tai haluatko antaa palautetta? ~Ravista~ puhelintasi!"
   :intro-status                          "Keskustele kanssani niin autan sinua käytön kanssa! (englanniksi)"
   :intro-message1                        "Tervetuloa Status sovellukseen\nNapauta tätä viestiä asettaaksesi salasanasi!"
   :account-generation-message            "Pikku hetki, pitää laskea kovasti että saadaan tilisi generoitua!"

   ;chats
   :chats                                 "Keskustelut"
   :new-chat                              "Uusi keskustelu"
   :new-group-chat                        "Uusi ryhmäkeskustelu"

   ;discover
   :discover                              "Löydä"
   :none                                  "Ei mitään"
   :search-tags                           "Kirjoita hakusanasi tähän"
   :popular-tags                          "Suositut hakusanat"
   :recent                                "Viimeiset"
   :no-statuses-discovered                "No statuses discovered"
   :no-statuses-found                     "No statuses found"

   ;settings
   :settings                              "Asetukset"

   ;contacts
   :contacts                              "Kontaktit"
   :new-contact                           "Uusi kontakti"
   :remove-contact                        "Poita kontakti"
   :show-all                              "NÄYTÄ KAIKKI"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Ihmiset"
   :contacts-group-new-chat               "Aloita uusi keskustelu"
   :no-contacts                           "Ei kontakteja vielä"
   :show-qr                               "Näytä QR"
   :enter-address                         "Anna osoite"

   ;group-settings
   :remove                                "Poista"
   :save                                  "Tallenna"
   :change-color                          "Vaihda väriä"
   :clear-history                         "Tyhjennä historia"
   :delete-and-leave                      "Poista ja poistu"
   :chat-settings                         "Keskusteluasetukset"
   :edit                                  "Muokkaa"
   :add-members                           "Lisää jäseniä"
   :blue                                  "Sininen"
   :purple                                "Lila"
   :green                                 "Vihreä"
   :red                                   "Punainen"

   ;commands
   :money-command-description             "Lähetä rahaa"
   :location-command-description          "Lähetä sijainti"
   :phone-command-description             "Lähetä puhelinnumero"
   :phone-request-text                    "Puhelinnumeron kysely"
   :confirmation-code-command-description "Lähetä varmistuskoodi"
   :confirmation-code-request-text        "Varmistuskoodin kysely"
   :send-command-description              "Lähetä sijainti"
   :request-command-description           "Lähetä kysely"
   :keypair-password-command-description  ""
   :help-command-description              "Apua"
   :request                               "Kysely"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH keskusteluun {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH keskustelusta {{chat-name}}"
   :command-text-location                 "Sijainti: {{address}}"
   :command-text-browse                   "Selataan osoitetta: {{webpage}}"
   :command-text-send                     "Transaktio: {{amount}} ETH"
   :command-text-help                     "Apua"
   :command-text-faucet                   "Faucet: {{url}}"

   ;new-group
   :group-chat-name                       "Keskustelun nimi"
   :empty-group-chat-name                 "Anna nimi"
   :illegal-group-chat-name               "Anna uusi nimi"

   ;participants
   :add-participants                      "Lisää jäseniä"
   :remove-participants                   "Poista jäseniä"

   ;protocol
   :received-invitation                   "saatiin keskustelukutsu"
   :removed-from-chat                     "poisti sinut rymäkeskustelusta"
   :left                                  "poistui"
   :invited                               "kutsui"
   :removed                               "poisti"
   :You                                   "Sinä"

   ;new-contact
   :add-new-contact                       "Lisää uusi kontakti"
   :import-qr                             "Tuo"
   :scan-qr                               "Skannaa QR"
   :swow-qr                               "Näytä QR"
   :name                                  "Nimi"
   :whisper-identity                      "Whisper Identiteetti"
   :address-explication                   "Tässä pitäisi varmaan kuvata mikä on osoite ja mistä sellaisia löytää"
   :enter-valid-address                   "Anna oikean muotoinen osoite tai skannaa QR koodi"
   :enter-valid-public-key                "Anna oikean muotoinen julkinen avain tai skannaa QR koodi"
   :contact-already-added                 "Kontakti on jo lisätty"
   :can-not-add-yourself                  "Et voi lisätä itseäsi"
   :unknown-address                       "Tuntematon osoite"


   ;login
   :connect                               "Yhdistä"
   :address                               "Osoite"
   :password                              "Salasana"
   :login                                 "Kirjaudu"
   :wrong-password                        "Väärä salasana"

   ;recover
   :recover-from-passphrase               "Palauta tili palautussalasanasta"
   :recover-explain                       "Anna palautussalasana palauttaaksesi käyttöoikeuden."
   :passphrase                            "Palautussalasana"
   :recover                               "Palauta"
   :enter-valid-passphrase                "Anna palautussalasana"
   :enter-valid-password                  "Anna salasana"

   ;accounts
   :recover-access                        "Palauta käyttöoikeus"
   :add-account                           "Lisää tili"

   ;wallet-qr-code
   :done                                  "Valmis"
   :main-wallet                           "Lompakko"

   ;validation
   :invalid-phone                         "Virheellinen puhelinnumero"
   :amount                                "Määrä"
   :not-enough-eth                        (str "Ei riittävästi katetta "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Vahvista transaktio"
                                           :other "Vahvista {{count}} transaktiota"
                                           :zero  "Ei transaktioita"}
   :status                                "Tila"
   :pending-confirmation                  "Odottaa vahvistusta"
   :recipient                             "Vastaanottaja"
   :one-more-item                         "Yksi lisää"
   :fee                                   "Maksu"
   :value                                 "Arvo"

   ;:webview
   :web-view-error                        "hups, virhe"})
