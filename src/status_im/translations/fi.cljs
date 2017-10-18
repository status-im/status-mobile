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
   :invite-friends                        "Kutsu ystäviä"
   :faq                                   "UKK"
   :switch-users                          "Vaihda käyttäjää"
   :feedback                              "Anna palautetta?\nRavista puhelintasi!"
   :view-all                              "Näytä kaikki"
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
   :datetime-multiple                     "s"
   :datetime-ago                          "sitten"
   :datetime-yesterday                    "eilen"
   :datetime-today                        "tänään"

   ;profile
   :profile                               "Profiili"
   :edit-profile                          "Muokkaa profiilia"
   :report-user                           "ILMIANNA KÄYTTÄJÄ"
   :message                               "Viesti"
   :username                              "Käyttäjätunnus"
   :not-specified                         "Ei määritelty"
   :public-key                            "Julkinen avain"
   :phone-number                          "Puhelinnumero"
   :email                                 "Sähköposti"
   :update-status                         "Päivitä tila..."
   :add-a-status                          "Lisää tila..."
   :status-prompt                         "Luo tila, jonka avulla ihmiset tietävät mitä asioita tarjoat. Voit myös käyttää #hashtag-merkintöjä."
   :add-to-contacts                       "Lisää kontakteihin"
   :in-contacts                           "Kontakteissa"
   :remove-from-contacts                  "Poista kontakteista"
   :start-conversation                    "Aloita keskustelu"
   :send-transaction                      "Lähetä tapahtuma"
   :share-qr                              "Jaa QR koodi"
   :error-incorrect-name                  "Ole hyvä ja valitse toinen nimi"
   :error-incorrect-email                 "Virheellinen sähköposti"

   ;;make_photo
   :image-source-title                    "Profiilikuva"
   :image-source-make-photo               "Kuvakaappaus"
   :image-source-gallery                  "Valitse galleriasta"
   :image-source-cancel                   "Peruuta"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopioi leikepöydälle"
   :sharing-share                         "Jaa..."
   :sharing-cancel                        "Peruuta"

   :browsing-title                        "Selaa"
   :browsing-browse                       "@selaa"
   :browsing-open-in-web-browser          "Avaa selaimessa"
   :browsing-cancel                       "Peruuta"

   ;sign-up
   :contacts-syncronized                  "Yhteystietosi ovat synkronoitu"
   :confirmation-code                     (str "Kiitos! Olemme lähettäneet sinulle tekstiviestin, jossa on vahvistus "
                                               "koodi. Ole hyvä ja anna koodi vahvistaaksesi puhelinnumerosi")
   :incorrect-code                        (str "Virheellinen koodi, ole hyvä ja yritä uudelleen")
   :generate-passphrase                   (str "Luon sinulle tunnuslauseen, jotta voit palauttaa sinun "
                                               "pääsysi tai kirjautua toisesta laitteesta")
   :phew-here-is-your-passphrase          "*Phew* se oli vaikeaa, tässä on tunnuslauseesi, *kirjoita tämä ylös ja pidä tallessa!* Tarvitset sitä palauttaaksesi tilisi."
   :here-is-your-passphrase               "Tässä on tunnuslauseesi, *kirjoita tämä ylös ja pidä tallessa!* Tarvitset sitä palauttaaksesi tilisi."
   :written-down                          "Varmista, että olet tarkasti kirjoittanut sen talteen"
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
   :new-chat                              "Uusi keskustelu"
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
   :show-all                              "NÄYTÄ KAIKKI"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Ihmiset"
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
   :change-color                          "Muuta väriä"
   :clear-history                         "Tyhjennä historia"
   :mute-notifications                    "Mykistä ilmoitukset"
   :leave-chat                            "Poistu keskustelusta"
   :delete-and-leave                      "Poista ja poistu"
   :chat-settings                         "Keskustelun asetukset"
   :edit                                  "Muokkaa"
   :add-members                           "Lisää käyttäjiä"
   :blue                                  "Sininen"
   :purple                                "Violetti"
   :green                                 "Vihreä"
   :red                                   "Punainen"

   ;commands
   :money-command-description             "Lähetä rahaa"
   :location-command-description          "Lähetä sijainti"
   :phone-command-description             "Lähetä puhelinnumero"
   :phone-request-text                    "Puhelinnumero pyyntö"
   :confirmation-code-command-description "Lähetä vahvistuskoodi"
   :confirmation-code-request-text        "Vahvistuskoodi pyyntö"
   :send-command-description              "Lähetä sijainti"
   :request-command-description           "Lähetä pyyntö"
   :keypair-password-command-description  ""
   :help-command-description              "Apua"
   :request                               "Pyyntö"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH käyttäjälle {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH käyttäjältä {{chat-name}}"

   ;new-group
   :group-chat-name                       "Keskustelun nimi"
   :empty-group-chat-name                 "Ole hyvä ja annan nimi"
   :illegal-group-chat-name               "Ole hyvä ja valitse toinen nimi"
   :new-group                             "Uusi ryhmä"
   :reorder-groups                        "Järjestele ryhmät uudelleen"
   :group-name                            "Ryhmän nimi"
   :edit-group                            "Muokkaa ryhmää"
   :delete-group                          "Poista ryhmä"
   :delete-group-confirmation             "Tämä ryhmä poistetaan ryhmistäsi. Tämä ei vaikuta yhteystietoihin"
   :delete-group-prompt                   "Tämä ei vaikuta yhteystietoihin"
   :group-members                         "Ryhmän jäsenet"
   :contact-s                             {:one   "yhteystieto"
                                           :other "yhteystietoa"}
   ;participants
   :add-participants                      "Lisää Osallistujia"
   :remove-participants                   "Poista Osallistujia"

   ;protocol
   :received-invitation                   "poista keskustelukutsu"
   :removed-from-chat                     "poista itsesi keskustelusta"
   :left                                  "poistui"
   :invited                               "kutsuttu"
   :removed                               "poistettu"
   :You                                   "Sinä"

   ;new-contact
   :add-new-contact                       "Lisää uusi yhteystieto"
   :import-qr                             "Tuo"
   :scan-qr                               "Skannaa QR"
   :swow-qr                               "Näytä QR"
   :name                                  "Nimi"
   :whisper-identity                      "Whisper Identiteetti"
   :address-explication                   "Ehkä tässä pitäisi olla jokin teksti, jossa selitetään, mikä osoite on ja mistä etsiä sitä"
   :enter-valid-address                   "Anna voimassaoleva osoite tai skannaa QR koodi"
   :enter-valid-public-key                "Anna voimassaoleva julkinen osoite tai skannaa QR koodi"
   :contact-already-added                 "Tämä yhteystieto on jo lisätty"
   :can-not-add-yourself                  "Et voi lisätä itseäsi"
   :unknown-address                       "Tuntematon osoite"


   ;login
   :connect                               "Yhdistä"
   :address                               "Osoite"
   :password                              "Salasana"
   :login                                 "Kirjaudu"
   :sign-in-to-status                     "Luo Status -tili"
   :sign-in                               "Luo tili"
   :wrong-password                        "Väärä salasana"

   ;recover
   :recover-from-passphrase               "Palauta tunnuslauseella"
   :recover-explain                       "Ole hyvä ja anna tunnuslause salasanan palauttamiseksi"
   :passphrase                            "Tunnuslause"
   :recover                               "Palauta"
   :enter-valid-passphrase                "Ole hyvä ja syötä tunnuslause"
   :enter-valid-password                  "Ole hyvä ja anna salasana"
   :twelve-words-in-correct-order         "12 sanaa oikeassa järjestyksessä"

   ;accounts
   :recover-access                        "Palauta käyttöoikeus"
   :add-account                           "Lisää tili"
   :create-new-account                    "Luo uusi tili"

   ;wallet-qr-code
   :done                                  "Valmis"
   :main-wallet                           "Ensisijainen Lompakko"

   ;validation
   :invalid-phone                         "Virheellinen puhelinnumero"
   :amount                                "Määrä"
   :not-enough-eth                        (str "Not enough ETH on balance "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Vahvista"
   :confirm-transactions                  {:one   "Vahvista tapahtuma"
                                           :other "Vahvista {{count}} tapahtumaa"
                                           :zero  "Ei tapahtumia"}
   :transactions-confirmed                {:one   "Tapahtuma vahvistettu"
                                           :other "{{count}} tapahtumaa vahvistettu"
                                           :zero  "Ei vahvistettuja tapahtumia"}
   :transaction                           "Tapahtuma"
   :unsigned-transactions                 "Allekirjoittamattomat tapahtumat"
   :no-unsigned-transactions              "Ei allekirjoitettuja tapahtumia"
   :enter-password-transactions           {:one   "Vahvista tapahtuma syöttämällä salasana"
                                           :other "Vahvista tapahtumat syöttämällä salasana"}
   :status                                "Tila"
   :pending-confirmation                  "Odottaa vahvistusta"
   :recipient                             "Vastaanottaja"
   :one-more-item                         "One more item"
   :fee                                   "Kulu"
   :estimated-fee                         "Arvioitu kulu"
   :value                                 "Arvo"
   :to                                    "Vastaanottajalle"
   :from                                  "Lähettäjältä"
   :data                                  "Tieto"
   :got-it                                "Vastaanotettu"
   :contract-creation                     "Sopimusten Luominen"

   ;:webview
   :web-view-error                        "oops, virhe"})
