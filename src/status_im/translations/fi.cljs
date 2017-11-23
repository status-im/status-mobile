(ns status-im.translations.fi)

(def translations
  {
   ;common
   :members-title                         "Jäsenet"
   :not-implemented                       "!ei toteutettu"
   :chat-name                             "Keskustelun nimi"
   :notifications-title                   "Ilmoitukset ja äänet"
   :offline                               "Poissa"
   :search-for                            "Etsi..."
   :cancel                                "Peruuta"
   :next                                  "Seuraava"
   :type-a-message                        "Kirjoita viesti..."
   :type-a-command                        "Aloita komennon kirjoittaminen..."
   :error                                 "Virhe"

   :camera-access-error                   "Myöntääksesi vaadittu kameran käyttölupa, siirry järjestelmäasetuksiin ja varmista, että Status > Kamera on valittu."
   :photos-access-error                   "Myöntääksesi vaadittu kuvien käyttölupa, siirry järjestelmäasetuksiin ja varmista, että Status > Kuvat on valittu."

   ;drawer
   :switch-users                          "Vaihda käyttäjää"
   :current-network                       "Nykyinen verkko"

   ;chat
   :is-typing                             "kirjoittaa"
   :and-you                               "ja sinä"
   :search-chat                           "Etsi keskustelusta"
   :members                               {:one   "1 käyttäjä"
                                           :other "{{count}} käyttäjää"
                                           :zero  "ei käyttäjiä"}
   :members-active                        {:one   "1 käyttäjä"
                                           :other "{{count}} käyttäjää"
                                           :zero  "ei käyttäjiä"}
   :public-group-status                   "Julkinen"
   :active-online                         "Linjalla"
   :active-unknown                        "Ei tiedossa"
   :available                             "Saatavilla"
   :no-messages                           "Ei viestejä"
   :suggestions-requests                  "Pyynnöt"
   :suggestions-commands                  "Komennot"
   :faucet-success                        "Faucet pyyntö vastaanotettu"
   :faucet-error                          "Faucet pyyntö virhe"

   ;sync
   :sync-in-progress                      "Synkronoidaan..."
   :sync-synced                           "Synkronoitu"

   ;messages
   :status-sending                        "Lähetetään"
   :status-pending                        "Odottaa"
   :status-sent                           "Lähetetty"
   :status-seen-by-everyone               "Kaikki nähneet"
   :status-seen                           "Nähty"
   :status-delivered                      "Toimitettu"
   :status-failed                         "Epäonnistui"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "sekunti"
                                           :other "sekuntia"}
   :datetime-minute                       {:one   "minuutti"
                                           :other "minuuttia"}
   :datetime-hour                         {:one   "tunti"
                                           :other "tuntia"}
   :datetime-day                          {:one   "päivä"
                                           :other "päivää"}
   :datetime-ago                          "sitten"
   :datetime-yesterday                    "eilen"
   :datetime-today                        "tänään"

   ;profile
   :profile                               "Profiili"
   :edit-profile                          "Muokkaa profiilia"
   :message                               "Viesti"
   :not-specified                         "Ei määritelty"
   :public-key                            "Julkinen avain"
   :phone-number                          "Puhelinnumero"
   :update-status                         "Päivitä tila..."
   :add-a-status                          "Lisää tila..."
   :status-prompt                         "Luo tila, jonka avulla ihmiset tietävät mitä asioita tarjoat. Voit myös käyttää #hashtag-merkintöjä."
   :add-to-contacts                       "Lisää kontakteihin"
   :in-contacts                           "Kontakteissa"
   :remove-from-contacts                  "Poista kontakteista"
   :start-conversation                    "Aloita keskustelu"
   :send-transaction                      "Lähetä tapahtuma"

   ;;make_photo
   :image-source-title                    "Profiilikuva"
   :image-source-make-photo               "Kuvakaappaus"
   :image-source-gallery                  "Valitse galleriasta"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopioi leikepöydälle"
   :sharing-share                         "Jaa..."
   :sharing-cancel                        "Peruuta"

   :browsing-title                        "Selaa"
   :browsing-open-in-web-browser          "Avaa selaimessa"
   :browsing-cancel                       "Peruuta"

   ;sign-up
   :contacts-syncronized                  "Yhteystietosi ovat synkronoitu"
   :confirmation-code                     (str "Kiitos! Olemme lähettäneet sinulle tekstiviestin, jossa on vahvistus "
                                               "koodi. Ole hyvä ja anna koodi vahvistaaksesi puhelinnumerosi")
   :incorrect-code                        (str "Virheellinen koodi, ole hyvä ja yritä uudelleen")
   :phew-here-is-your-passphrase          "*Phew* se oli vaikeaa, tässä on tunnuslauseesi, *kirjoita tämä ylös ja pidä tallessa!* Tarvitset sitä palauttaaksesi tilisi."
   :here-is-your-passphrase               "Tässä on tunnuslauseesi, *kirjoita tämä ylös ja pidä tallessa!* Tarvitset sitä palauttaaksesi tilisi."
   :phone-number-required                 "Napauta tähän syöttääksesi puhelinnumerosi niin löydän ystäväsi"
   :shake-your-phone                      "Löysitkö vian tai onko sinulla ehdotus? Ravista puhelintasi!"
   :intro-status                          "Keskustele kanssani määrittääksesi tai muuttaaksesi tilisi asetuksia!"
   :intro-message1                        "Tervetuloa Statukseen\nNapauta tätä viestiä asettaaksesi salasanasi ja alkuun pääsemiseksi!"
   :account-generation-message            "Ole hyvä ja odota hetki, käynnistän kvanttitietokoneeni tilisi luomiseksi!"
   :move-to-internal-failure-message      "Meidän on siirrettävä joitain tärkeitä tiedostoja ulkoisesta sisäiseen tallennustilaan. Tätä varten tarvitsemme luvan. Emme tule käyttämään ulkoista tallennustilaa tulevissa versioissa."
   :debug-enabled                         "Debug-palvelin käynnistetty! Löytääksesi palvelimen suorita samaan verkkoon kuuluvalta tietokoneeltasi *status-dev-cli scan*."

   ;phone types
   :phone-e164                            "Kansainvälinen 1"
   :phone-international                   "Kansainvälinen 2"
   :phone-national                        "Kansallinen"
   :phone-significant                     "Merkittävä"

   ;chats
   :chats                                 "Keskustelut"
   :delete-chat                           "Poista keskustelu"
   :new-group-chat                        "Uusi ryhmäkeskustelu"
   :new-public-group-chat                 "Liity julkiseen keskusteluun"
   :edit-chats                            "Muokkaa keskusteluja"
   :search-chats                          "Etsi keskusteluja"
   :empty-topic                           "Tyhjä otsikko"
   :topic-format                          "Virheellinen muoto [a-z0-9\\-]+"
   :public-group-topic                    "Otsikko"

   ;discover
   :discover                              "Löydä"
   :none                                  "Tyhjä"
   :search-tags                           "Kirjoita hakutunnisteesi tähän"
   :popular-tags                          "Suositut tunnisteet"
   :recent                                "Uusimmat"
   :no-statuses-discovered                "Ei hakutuloksia"
   :no-statuses-found                     "Ei hakutuloksia"

   ;settings
   :settings                              "Asetukset"

   ;contacts
   :contacts                              "Yhteystiedot"
   :new-contact                           "Uusi yhteystieto"
   :delete-contact                        "Poista yhteystieto"
   :delete-contact-confirmation           "Tämä yhteystieto poistetaan yhteystiedoista"
   :remove-from-group                     "Poista ryhmästä"
   :edit-contacts                         "Muokkaa yhteystietoja"
   :search-contacts                       "Etsi yhteystietoja"
   :contacts-group-new-chat               "Aloita uusi keskustelu"
   :choose-from-contacts                  "Valitse yhteystiedoista"
   :no-contacts                           "No contacts yet"
   :show-qr                               "Näytä QR"
   :enter-address                         "Syötä osoite"
   :more                                  "lisää"

   ;group-settings
   :remove                                "Poista"
   :save                                  "Tallenna"
   :delete                                "Poista"
   :clear-history                         "Tyhjennä historia"
   :mute-notifications                    "Mykistä ilmoitukset"
   :leave-chat                            "Poistu keskustelusta"
   :chat-settings                         "Keskustelun asetukset"
   :edit                                  "Muokkaa"
   :add-members                           "Lisää käyttäjiä"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Uusi ryhmä"
   :reorder-groups                        "Järjestele ryhmät uudelleen"
   :edit-group                            "Muokkaa ryhmää"
   :delete-group                          "Poista ryhmä"
   :delete-group-confirmation             "Tämä ryhmä poistetaan ryhmistäsi. Tämä ei vaikuta yhteystietoihin"
   :delete-group-prompt                   "Tämä ei vaikuta yhteystietoihin"
   :contact-s                             {:one   "yhteystieto"
                                           :other "yhteystietoa"}
   ;participants

   ;protocol
   :received-invitation                   "poista keskustelukutsu"
   :removed-from-chat                     "poista itsesi keskustelusta"
   :left                                  "poistui"
   :invited                               "kutsuttu"
   :removed                               "poistettu"
   :You                                   "Sinä"

   ;new-contact
   :add-new-contact                       "Lisää uusi yhteystieto"
   :scan-qr                               "Skannaa QR"
   :name                                  "Nimi"
   :address-explication                   "Ehkä tässä pitäisi olla jokin teksti, jossa selitetään, mikä osoite on ja mistä etsiä sitä"
   :enter-valid-public-key                "Anna voimassaoleva julkinen osoite tai skannaa QR koodi"
   :contact-already-added                 "Tämä yhteystieto on jo lisätty"
   :can-not-add-yourself                  "Et voi lisätä itseäsi"
   :unknown-address                       "Tuntematon osoite"


   ;login
   :connect                               "Yhdistä"
   :address                               "Osoite"
   :password                              "Salasana"
   :sign-in-to-status                     "Luo Status -tili"
   :sign-in                               "Luo tili"
   :wrong-password                        "Väärä salasana"

   ;recover
   :passphrase                            "Tunnuslause"
   :recover                               "Palauta"
   :twelve-words-in-correct-order         "12 sanaa oikeassa järjestyksessä"

   ;accounts
   :recover-access                        "Palauta käyttöoikeus"
   :create-new-account                    "Luo uusi tili"

   ;wallet-qr-code
   :done                                  "Valmis"
   :main-wallet                           "Ensisijainen Lompakko"

   ;validation
   :invalid-phone                         "Virheellinen puhelinnumero"
   :amount                                "Määrä"
   ;transactions
   :confirm                               "Vahvista"
   :transaction                           "Tapahtuma"
   :status                                "Tila"
   :recipient                             "Vastaanottaja"
   :to                                    "Vastaanottajalle"
   :from                                  "Lähettäjältä"
   :data                                  "Tieto"
   :got-it                                "Vastaanotettu"

   ;:webview
   :web-view-error                        "oops, virhe"})
