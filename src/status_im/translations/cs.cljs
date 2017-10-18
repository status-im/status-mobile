(ns status-im.translations.cs)

(def translations
  {
   ;common
   :members-title                         "Členové"
   :not-implemented                       "!není implementováno"
   :chat-name                             "Název chatu"
   :notifications-title                   "Oznámení a zvuky"
   :offline                               "Offline"
   :search-for                            "Hledat..."
   :cancel                                "Storno"
   :next                                  "Další"
   :type-a-message                        "Napište zprávu..."
   :type-a-command                        "Začněte psát příkaz..."
   :error                                 "Chyba"

   :camera-access-error                   "Pro udělení potřebných oprávnění ke kameře přejděte do nastavení systému a ujistěte se, že je vybráno Status > Kamera."
   :photos-access-error                   "Pro udělení potřebných oprávnění k fotoaparátu přejděte do nastavení systému a ujistěte se, že je vybráno Status > Fotoaparát."

   ;drawer
   :invite-friends                        "Pozvat přátele"
   :faq                                   "Otázky a odpovědi"
   :switch-users                          "Přepnout uživatele"
   :feedback                              "Máte návrh?\nZatřeste telefonem!"
   :view-all                              "Zobrazit vše"
   :current-network                       "Aktuální síť"

   ;chat
   :is-typing                             "píše"
   :and-you                               "a ty"
   :search-chat                           "Hledat v chatu"
   :members                               {:one   "1 účastník"
                                           :other "{{count}} účastníci"
                                           :zero  "žádní účastníci"}
   :members-active                        {:one   "1 účastník"
                                           :other "{{count}} účastníci"
                                           :zero  "žádní účastníci"}
   :public-group-status                   "Veřejné"
   :active-online                         "Online"
   :active-unknown                        "Neznámý"
   :available                             "Dostupný"
   :no-messages                           "Žádné zprávy"
   :suggestions-requests                  "Požadavky"
   :suggestions-commands                  "Příkazy"
   :faucet-success                        "Požadavek na faucet byl přijat"
   :faucet-error                          "Chyba požadavku na faucet"

   ;sync
   :sync-in-progress                      "Synchronizuji..."
   :sync-synced                           "Synchronizováno"

   ;messages
   :status-sending                        "Odesílám"
   :status-pending                        "Čekám"
   :status-sent                           "Odesláno"
   :status-seen-by-everyone               "Přečteno všemi"
   :status-seen                           "Přečteno"
   :status-delivered                      "Doručeno"
   :status-failed                         "Selhalo"

   ;datetime
   :datetime-ago-format                   "{{ago}} {{number}} {{time-intervals}}"
   :datetime-second                       {:one   "sekunda"
                                           :other "sekund(y)"}
   :datetime-minute                       {:one   "minuta"
                                           :other "minut(y)"}
   :datetime-hour                         {:one   "hodina"
                                           :other "hodin(y)"}
   :datetime-day                          {:one   "den"
                                           :other "dny(ů)"}
   :datetime-multiple                     "s"
   :datetime-ago                          "uplynulo:"
   :datetime-yesterday                    "včera"
   :datetime-today                        "dnes"

   ;profile
   :profile                               "Profil"
   :edit-profile                          "Upravit profil"
   :report-user                           "NAHLÁSIT UŽIVATELE"
   :message                               "Zpráva"
   :username                              "Uživatel"
   :not-specified                         "Není zadáno"
   :public-key                            "Veřejný klíč"
   :phone-number                          "Telefonní číslo"
   :email                                 "E-mail"
   :update-status                         "Aktualizovat svůj status..."
   :add-a-status                          "Přidat status..."
   :status-prompt                         "Můžete vytvořit status, aby ostatní věděli, co nabízíte. Můžete používat i #hashtagy."
   :add-to-contacts                       "Přidat do kontaktů"
   :in-contacts                           "V kontaktech"
   :remove-from-contacts                  "Odstranit z kontaktů"
   :start-conversation                    "Zahájit konverzaci"
   :send-transaction                      "Odeslat transakci"
   :share-qr                              "Sdílet QR kód"
   :error-incorrect-name                  "Prosím vyberte jiné jméno"
   :error-incorrect-email                 "Nesprávný e-mail"

   ;;make_photo
   :image-source-title                    "Profilový obrázek"
   :image-source-make-photo               "Vyfotit"
   :image-source-gallery                  "Vybrat z galerie"
   :image-source-cancel                   "Storno"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopírovat do schránky"
   :sharing-share                         "Sdílet..."
   :sharing-cancel                        "Storno"

   :browsing-title                        "Prohlížet"
   :browsing-browse                       "@prohlížet"
   :browsing-open-in-web-browser          "Otevřít ve webovém prohlížeči"
   :browsing-cancel                       "Storno"

   ;sign-up
   :contacts-syncronized                  "Vaše kontakty byly synchronizovány"
   :confirmation-code                     (str "Díky! Poslali jsme Ti textovou zprávu s kódem pro potvrzení. "
                                               "Prosím potvrď své telefonní číslo zadáním tohoto kódu.")
   :incorrect-code                        (str "Tento kód není správný, prosím zkus to znovu.")
   :generate-passphrase                   (str "Teď vytvořím skupinu anglických slov, se kterou se"
                                               "dá obnovit přístup nebo přihlásit z jiného zařízení")
   :phew-here-is-your-passphrase          "*Uf*, to byla fuška, tady jsou bezpečnostní slova, *zapiš je a ulož na bezpečném místě!* Budou potřeba pro obnovení přístupu k účtu."
   :here-is-your-passphrase               "Tady jsou bezpečnostní slova, *zapiš je a ulož na bezpečném místě!* Budou potřeba pro obnovení přístupu k účtu."
   :written-down                          "Ujisti se, že je máš uložené na bezpečném místě."
   :phone-number-required                 "Klepnutím zde můžeš zadat své telefonní číslo a hledat přátele."
   :shake-your-phone                      "Našel jsi chybu nebo máš návrh? Prostě --zatřes-- telefonem!"
   :intro-status                          "Chat se mnou ti může pomoci nastavit ůčet a změnit další nastavení!"
   :intro-message1                       (str "Vítá vás Status\nKlepněte na tuto zprávu pro nastavení hesla a zahájení!")
   :account-generation-message            "Vteřinku, musím použít docela těžkou matiku na vygenerování účtu!"
   :move-to-internal-failure-message      "Potřebujeme přesunout některé důležité soubory z externího na interní úložiště. K tomu potřebujeme tvoje povolení. V dalších verzích už nebudeme externí úložiště používat."
   :debug-enabled                         "Server pro ladění byl spuštěn! Nyní můžeš spustit *status-dev-cli scan* pro nalezení serveru z počítače ve stejné síti."

   ;phone types
   :phone-e164                            "Mezinárodní 1"
   :phone-international                   "Mezinárodní 2"
   :phone-national                        "Národní"
   :phone-significant                     "Významný"

   ;chats
   :chats                                 "Chaty"
   :new-chat                              "Nový chat"
   :delete-chat                           "Smazat chat"
   :new-group-chat                        "Nový skupinový chat"
   :new-public-group-chat                 "Přidat se k veřejnému chatu"
   :edit-chats                            "Upravit chaty"
   :search-chats                          "Hledat v chatech"
   :empty-topic                           "Prázdné téma"
   :topic-format                          "Špatný formát [a-z0-9\\-]+"
   :public-group-topic                    "Téma"

   ;discover
   :discover                              "Objevit"
   :none                                  "Žádný"
   :search-tags                           "Napište tagy pro hledání"
   :popular-tags                          "Populární tagy"
   :recent                                "Poslední"
   :no-statuses-discovered                "Žádné statusy nebyly objeveny"
   :no-statuses-found                     "Žádné statusy nebyly nalezeny"

   ;settings
   :settings                              "Nastavení"

   ;contacts
   :contacts                              "Kontakty"
   :new-contact                          "Nový kontakt"
   :delete-contact                        "Smazat kontakt"
   :delete-contact-confirmation           "Tento kontakt bude smazán z tvých kontaktů"
   :remove-from-group                     "Odstranit ze skupiny"
   :edit-contacts                         "Upravit kontakty"
   :search-contacts                       "Hledat kontakty"
   :show-all                              "UKÁZAT VŠE"
   :contacts-group-dapps                  "ÐApky"
   :contacts-group-people                 "Lidé"
   :contacts-group-new-chat               "Zahájit nový chat"
   :choose-from-contacts                  "Vybrat z kontaktů"
   :no-contacts                           "Ještě tu nejsou žádné kontakty"
   :show-qr                               "Zobrazit QR kǒd"
   :enter-address                         "Zadejte adresu"
   :more                                  "více"

   ;group-settings
   :remove                                "Odstranit"
   :save                                  "Uložit"
   :delete                                "Smazat"
   :change-color                          "Změnit barvu"
   :clear-history                         "Vymazat historii"
   :mute-notifications                    "Vypnout oznámení"
   :leave-chat                            "Opustit chat"
   :delete-and-leave                      "Smazat a odejít"
   :chat-settings                         "Nastavení chatu"
   :edit                                  "Upravit"
   :add-members                           "Přidat členy"
   :blue                                  "Modrá"
   :purple                                "Purpurová"
   :green                                 "Zelená"
   :red                                   "Červená"

   ;commands
   :money-command-description             "Poslat peníze"
   :location-command-description          "Poslat umístění"
   :phone-command-description             "Poslat telefonní číslo"
   :phone-request-text                   "Požadavek na telefonní číslo"
   :confirmation-code-command-description "Poslat kód pro potvrzení"
   :confirmation-code-request-text        "Požadavek na kód pro potvrzení"
   :send-command-description              "Poslat umístění"
   :request-command-description           "Poslat požadavek"
   :keypair-password-command-description  ""
   :help-command-description              "Nápověda"
   :request                               "Požadavek"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH pro {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH od {{chat-name}}"

   ;new-group
   :group-chat-name                       "Název chatu"
   :empty-group-chat-name                 "Prosím zadejte název"
   :illegal-group-chat-name               "Prosím vyberte jiný název"
   :new-group                             "Nová skupina"
   :reorder-groups                        "Uspořádat skupiny"
   :group-name                            "Název skupiny"
   :edit-group                            "Upravit skupinu"
   :delete-group                          "Smazat skupinu"
   :delete-group-confirmation             "Tato skupina bude odstraněna z tvých skupin. Kontaktů se to nedotkne."
   :delete-group-prompt                   "Kontaktů se to nedotkne"
   :group-members                         "Členové skupiny"
   :contact-s                             {:one   "kontakt"
                                           :other "kontakty(ů)"}
   ;participants
   :add-participants                      "Přidat účastníky"
   :remove-participants                   "Odebrat účastníky"

   ;protocol
   :received-invitation                   "přijata pozvánka k chatu"
   :removed-from-chat                    "přijata pozvánka k skupinovému chatu"
   :left                                  "opuštěno"
   :invited                               "pozván"
   :removed                               "odstraněn"
   :You                                   "Ty"

   ;new-contact
   :add-new-contact                       "Přidat nový kontakt"
   :import-qr                             "Importovat"
   :scan-qr                               "Načíst QR kód"
   :swow-qr                               "Ukázat QR kód"
   :name                                  "Jméno"
   :whisper-identity                      "Identita na Whisperu"
   :address-explication                   "Identita na Whisperu je adresa blockchainové sítě Ethereum"
   :enter-valid-address                   "Prosím zadejte platnou adresu nebo načtěte QR kód"
   :enter-valid-public-key              "Prosím zadejte platný veřejný klíč nebo načtěte QR kód"
   :contact-already-added                 "Tento kontakt již byl přidán"
   :can-not-add-yourself                  "Nemůžeš přidat sebe"
   :unknown-address                       "Neznámá adresa"


   ;login
   :connect                             "Připojit"
   :address                               "Adresa"
   :password                              "Heslo"
   :login                                 "Přihlášení"
   :sign-in-to-status                     "Přihlásit se do Statusu"
   :sign-in                               "Přihlásit se"
   :wrong-password                        "Špatné heslo"

   ;recover
   :recover-from-passphrase               "Obnovit z bezpečnostních slov"
   :recover-explain                       "Pro obnovení přístupu prosím zadej bezpečnostní slova ke svému účtu"
   :passphrase                            "Bezpečnostní slova"
   :recover                               "Obnovit"
   :enter-valid-passphrase                "Prosím zadej bezpečnostní slova"
   :enter-valid-password                  "Prosím zadej heslo"
   :twelve-words-in-correct-order         "12 anglických slov ve správném pořadí"

   ;accounts
   :recover-access                        "Obnovit přístup"
   :add-account                           "Přidat účet"
   :create-new-account                    "Vytvořit nový účet"

   ;wallet-qr-code
   :done                                  "Hotovo"

   ;validation
   :invalid-phone                         "Neplatné telefonní číslo"
   :amount                                "Množství"
   :not-enough-eth                        (str "Nemáš na účtu dost ETH: "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Potvrdit"
   :confirm-transactions                  {:one   "Potvrdit transakci"
                                           :other "Potvrdit {{count}} transakce(í)"
                                           :zero  "Žádné transakce"}
   :transactions-confirmed                {:one   "Transakce potvrzena"
                                           :other "Potvrzeno transakcí: {{count}}"
                                           :zero  "Žádná transakce nebyla potvrzena"}
   :transaction                           "Transakce"
   :unsigned-transactions                 "Nepodepsané transakce"
   :no-unsigned-transactions              "Žádné nepodepsané transakce"
   :enter-password-transactions           {:one   "Potvrdit transakce zadáním hesla"
                                           :other "Potvrdit transakce zadáním hesla"}
   :status                                "Status"
   :pending-confirmation                  "Čekající na potvrzení"
   :recipient                             "Příjemce"
   :one-more-item                         "Jedna další položka"
   :fee                                   "Poplatek"
   :estimated-fee                         "Odhadovaný poplatek"
   :value                                 "Množství"
   :to                                    "Komu"
   :from                                  "Od"
   :data                                  "Data"
   :got-it                                "Mám to"
   :contract-creation                     "Vytvoření kontaktu"

   ;:webview
   :web-view-error                        "ups, chyba"
   ;;testfairy warning
   :testfairy-title                       "Varování!"
   :testfairy-message                     "Používáte aplikaci z nestabilního sestavení. Kvůli testování se v případě, že používáte Wi-Fi připojení, veškerá interakce nahrává (jako video a záznamy) a tyto informace může využít tým vývojářů pro řešení problémů. Uložená videa ani záznamy neobsahují Vaše hesla. Toto naahrávání se provádí jen v případě, že je aplikace nainstalována z nestabilního sestavení. Pokud je aplikace instalována z Obchodu Play nebo TestFlightu, nic se nenahrává."

   ;; wallet
   :wallet                                "Peněženka"
   :wallets                               "Peněženky"
   :your-wallets                          "Tvé peněženky"
   :main-wallet                           "Hlavní peněženka"
   :wallet-send                           "Poslat"
   :wallet-request                        "Požadavek"
   :wallet-exchange                       "Směnárna"
   :wallet-assets                         "Aktiva"
   :transactions                          "Transakce"
   :transactions-to                       "Komu"
   :transactions-sign                     "Podepsat"
   :transactions-sign-all                 "Podepsat vše"
   :transactions-sign-all-text            "Podepište transakci zadáním hesla.\nUjistěte se, že slova zobrazená výše odpovídají Vašim bezpečnostním slovům."
   :transactions-sign-input-placeholder   "Zadejte své heslo"
   :transactions-sign-all-done            "Hotovo"
   :transactions-delete                   "Smazat"
   :transactions-history                  "Historie"
   :transactions-unsigned                 "Nepodepsáno"
   :transactions-history-empty            "V historii nemáš žádné transakce"
   :transactions-unsigned-empty           "Nemáš žádné nepodepsané transakce"
   :transactions-filter-title             "Filtrovat historii"
   :transactions-filter-tokens            "Tokeny"
   :transactions-filter-type              "Typ"
   :transactions-filter-select-all        "Označit vše"})

