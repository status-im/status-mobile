(ns status-im.translations.pl)

(def translations
  {
   ;common
   :members-title                         "Użytkownicy"
   :not-implemented                       "nie wprowadzono"
   :chat-name                             "Nazwa czatu"
   :notifications-title                   "Powiadomienia i dźwięki"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Zaproś znajomych"
   :faq                                   "FAQ"
   :switch-users                          "Przełącz użytkowników"

   ;chat
   :is-typing                             "wpisuje tekst"
   :and-you                               "i ty"
   :search-chat                           "Przeszukaj czat"
   :members                               {:one   "1 użytkownik"
                                           :other "Użytkownicy: {{count}}"
                                           :zero  "brak użytkowników "}
   :members-active                        {:one   "1 użytkownik, 1 aktywny"
                                           :other "Użytkownicy:{{count}}, aktywni:{{count}}"
                                           :zero  "brak użytkowników"}
   :active-online                         "Online"
   :active-unknown                        "Nieznany"
   :available                             "Dostępny"
   :no-messages                           "Brak wiadomości"
   :suggestions-requests                  "Żądania"
   :suggestions-commands                  "Polecenia"

   ;sync
   :sync-in-progress                      "Synchronizacja..."
   :sync-synced                           "W synchronizacji"

   ;messages
   :status-sending                        "Wysyłanie"
   :status-pending                        "W oczekiwaniu"
   :status-sent                           "Wysłano"
   :status-seen-by-everyone               "Przeczytane przez wszystkich"
   :status-seen                           "Przeczytane"
   :status-delivered                      "Dostarczono"
   :status-failed                         "Niepowodzenie"

   ;datetime
   :datetime-second                       {:one   "sekunda"
                                           :other "sekund(y)"}
   :datetime-minute                       {:one   "minuta"
                                           :other "minut(y)"}
   :datetime-hour                         {:one   "godzina"
                                           :other "godziny(y)"}
   :datetime-day                          {:one   "dzień"
                                           :other "dni"}
   :datetime-multiple                     "s"
   :datetime-ago                          "temu"
   :datetime-yesterday                    "wczoraj"
   :datetime-today                        "dzisiaj"

   ;profile
   :profile                               "Profil"
   :report-user                           "ZGŁOŚ UŻYTOWNIKA"
   :message                               "Wiadomość"
   :username                              "Nazwa użytkownika"
   :not-specified                         "Nie określono"
   :public-key                            "Klucz publiczny"
   :phone-number                          "Numer telefonu "
   :email                                 "E-mail"
   :profile-no-status                     "Brak statusu"
   :add-to-contacts                       "Dodaj do kontaktów"
   :error-incorrect-name                  "Wybierz inną nazwę "
   :error-incorrect-email                 "Nieprawidłowy e-mail"

   ;;make_photo
   :image-source-title                    "Zdjęcie profilowe "
   :image-source-make-photo               "Przechwyć"
   :image-source-gallery                  "Wybierz z galerii "
   :image-source-cancel                   "Anuluj"

   ;sign-up
   :contacts-syncronized                  "Twoje kontakty zostały zsynchronizowane"
   :confirmation-code                     (str "Dziękujemy! Wysłaliśmy ci SMS-a z kodem"
                                               "potwierdzającym. Prosimy o podanie kodu w celu zweryfikowania swojego numeru telefonu")
   :incorrect-code                        (str "Przepraszamy, kod jest nieprawidłowy. Prosimy wprowadzić kod ponownie")
   :generate-passphrase                   (str "Wygenerujemy specjalne hasło, dzięki któremu będzie można przywrócić "
                                               "dostęp lub zalogować się z innego urządzenia")
   :phew-here-is-your-passphrase          "*Uff*, to nie było łatwe. Oto twoje specjalne hasło, *zapisz je i	 przechowuj w bezpiecznym miejscu!* Będzie ci potrzebne podczas procedury odzyskiwania konta."
   :here-is-your-passphrase               "Oto twoje specjalne hasło, *zapisz je i przechowuj w bezpiecznym miejscu!* Będzie ci potrzebne podczas procedury odzyskiwania konta."
   :written-down                          "Upewnij się, że je zapisałeś i przechowujesz w bezpiecznym miejscu"
   :phone-number-required                 "Dotknij tutaj, aby wprowadzić swój numer telefonu, a my znajdziemy twoich znajomych "
   :intro-status                          "Porozmawiaj ze mną na czacie, aby skonfigurować swoje konto i zmienić ustawienia!"
   :intro-message1                        "Witamy w sekcji Status\nWybierz tę wiadomość, aby ustawić hasło i rozpocząć!"
   :account-generation-message            "Daj mi chwilkę, muszę wykonać szalone obliczenia, żeby utworzyć dla Ciebie konto!"

   ;chats
   :chats                                 "Czaty"
   :new-chat                              "Nowy czat"
   :new-group-chat                        "Nowy czat grupowy"

   ;discover
   :discover                              "Odkryte"
   :none                                  "Brak"
   :search-tags                           "Tutaj wpisz swoje tagi wyszukiwania"
   :popular-tags                          "Popularne tagi"
   :recent                                "Najnowsze"
   :no-statuses-discovered                "Nie odkryto statusów"

   ;settings
   :settings                              "Ustawienia"

   ;contacts
   :contacts                              "Kontakty"
   :new-contact                           "Nowy kontakt"
   :show-all                              "POKAŻ WSZYSTKIE"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Ludzie"
   :contacts-group-new-chat               "Rozpocznij nowy czat"
   :no-contacts                           "W tej chwili brak kontaktów"
   :show-qr                               "Pokaż QR"

   ;group-settings
   :remove                                "Usuń"
   :save                                  "Zapisz"
   :change-color                          "Zmień kolor"
   :clear-history                         "Wyczyść historię"
   :delete-and-leave                      "Usuń i wyjdź"
   :chat-settings                         "Ustawienia czatu"
   :edit                                  "Edytuj"
   :add-members                           "Dodaj użytkowników"
   :blue                                  "Niebieski"
   :purple                                "Fioletowy"
   :green                                 "Zielony"
   :red                                   "Czerwony"

   ;commands
   :money-command-description             "Wyślij pieniądze"
   :location-command-description          "Wyślij  lokalizację"
   :phone-command-description             "Wyślij numer telefonu"
   :phone-request-text                    "Prośba o numer telefonu"
   :confirmation-code-command-description "Wyślij kod potwierdzający"
   :confirmation-code-request-text        "Prośba o kod potwierdzający"
   :send-command-description              "Wyślij lokalizację"
   :request-command-description           "Wyślij prośbę"
   :keypair-password-command-description  ""
   :help-command-description              "Pomoc"
   :request                               "Prośba"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH do {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH od {{chat-name}}"

   ;new-group
   :group-chat-name                       "Nazwa czatu"
   :empty-group-chat-name                 "Wprowadź nazwę"
   :illegal-group-chat-name               "Wybierz inną nazwę"

   ;participants
   :add-participants                      "Dodaj uczestników"
   :remove-participants                   "Usuń uczestników"

   ;protocol
   :received-invitation                   "otrzymano zaproszenie na czat"
   :removed-from-chat                     "usunięto z czatu grupowego"
   :left                                  "pozostało"
   :invited                               "zaproszeni"
   :removed                               "usunięci"
   :You                                   "Ty"

   ;new-contact
   :add-new-contact                       "Dodaj nowy kontakt"
   :import-qr                             "Importuj"
   :scan-qr                               "Skanuj QR"
   :name                                  "Nazwa"
   :whisper-identity                      "Sekretna tożsamość"
   :address-explication                   "Być może tutaj powinien znajdować się tekst wyjaśniający, czym jest adres i gdzie go szukać"
   :enter-valid-address                   "Wprowadź prawidłowy adres lub przeskanuj kod QR"
   :contact-already-added                 "Kontakt został już dodany"
   :can-not-add-yourself                  "Nie możesz dodać samego siebie"
   :unknown-address                       "Nieznany adres"


   ;login
   :connect                               "Połącz"
   :address                               "Adres"
   :password                              "Hasło"
   :login                                 "Login"
   :wrong-password                        "Nieprawidłowe hasło"

   ;recover
   :recover-from-passphrase               "Odzyskaj na podstawie hasła specjalnego"
   :recover-explain                       "Wprowadź hasło specjalne dla swojego hasła, aby odzyskać dostęp"
   :passphrase                            "Hasło specjalne"
   :recover                               "Odzyskaj"
   :enter-valid-passphrase                "Wprowadź hasło specjalne"
   :enter-valid-password                  "Wprowadź hasło"

   ;accounts
   :recover-access                        "Odzyskaj dostęp"
   :add-account                           "Dodaj konto "

   ;wallet-qr-code
   :done                                  "Zrobione"
   :main-wallet                           "Portfel główny"

   ;validation
   :invalid-phone                         "Nieprawidłowy numer telefonu"
   :amount                                "Kwota"
   :not-enough-eth                        (str "Brak ETH na koncie "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Potwierdź transakcję"
                                           :other "Potwierdź {{count}} transakcje(-i)"
                                           :zero  "Brak transakcji"}
   :status                                "Status"
   :pending-confirmation                  "Oczekuje na potwierdzenie"
   :recipient                             "Odbiorca"
   :one-more-item                         "Dodatkowy element"
   :fee                                   "Opłata"
   :value                                 "Wartość"

   ;:webview
   :web-view-error                        "oj, mamy błąd"

   :confirm                               "Potwierdź"
   :phone-national                        "Krajowy"
   :transactions-confirmed                {:one   "Potwierdzono transakcję"
                                           :other "Potwierdzone transakcje: {{count}}"
                                           :zero  "Brak potwierdzonych transakcji"}
   :public-group-topic                    "Temat"
   :debug-enabled                         "Uruchomiono serwer debugowania! Możesz teraz dodać program DApp, uruchamiając *status-dev-cli scan* na swoim komputerze"
   :new-public-group-chat                 "Dołącz do publicznego czatu"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Anuluj"
   :share-qr                              "Udostępnij QR"
   :feedback                              "Chcesz przesłać opinię?\nPotrząśnij telefonem!"
   :twelve-words-in-correct-order         "12 słów w prawidłowym porządku"
   :remove-from-contacts                  "Usuń z kontaktów"
   :delete-chat                           "Usuń czat"
   :edit-chats                            "Edytuj czaty"
   :sign-in                               "Zaloguj się"
   :create-new-account                    "Utwórz nowe konto"
   :sign-in-to-status                     "Zaloguj się do Statusu"
   :got-it                                "Rozumiem"
   :move-to-internal-failure-message      "Musimy przenieść ważne pliki z zewnętrznego na wewnętrzny nośnik danych. W tym celu potrzebujemy Twojego pozwolenia. W przyszłych wersjach zewnętrzne nośniki danych nie będą stosowane."
   :edit-group                            "Edytuj grupę"
   :delete-group                          "Usuń grupę"
   :browsing-title                        "Przeglądaj"
   :reorder-groups                        "Przestaw grupy"
   :browsing-cancel                       "Anuluj"
   :faucet-success                        "Odebrano prośbę dotyczącą kranu"
   :choose-from-contacts                  "Wybierz spośród kontaktów"
   :new-group                             "Nowa grupa"
   :phone-e164                            "Międzynarodowy 1"
   :remove-from-group                     "Usuń z grupy"
   :search-contacts                       "Przeszukaj kontakty"
   :transaction                           "Transakcja"
   :public-group-status                   "Publiczny"
   :leave-chat                            "Opuść czat"
   :start-conversation                    "Rozpocznij rozmowę"
   :topic-format                          "Nieprawidłowy format [a-z0-9\\-]+"
   :enter-valid-public-key                "Wprowadź prawidłowy klucz publiczny lub zeskanuj kod QR"
   :faucet-error                          "Błąd wysyłania prośby dotyczącej kranu"
   :phone-significant                     "Ważne"
   :search-for                            "Wyszukaj..."
   :sharing-copy-to-clipboard             "Skopiuj do schowka"
   :phone-international                   "Międzynarodowy 2"
   :enter-address                         "Wprowadź adres"
   :send-transaction                      "Wyślij transakcję"
   :delete-contact                        "Usuń kontakt"
   :mute-notifications                    "Wycisz powiadomienia"


   :contact-s                             {:one   "kontakt"
                                           :other "kontakty"}
   :group-name                            "Nazwa grupy"
   :next                                  "Dalej"
   :from                                  "Z"
   :search-chats                          "Przeszukaj czaty"
   :in-contacts                           "W kontaktach"

   :sharing-share                         "Udostępnij..."
   :type-a-message                        "Wpisz wiadomość..."
   :type-a-command                        "Zacznij wpisywać komendę..."
   :shake-your-phone                      "Znalazłeś błąd lub masz pytanie? Wystarczy ~potrząsnąć~ telefonem!"
   :status-prompt                         "Utwórz status, aby inni mogli dowiedzieć się, co oferujesz. Możesz również wykorzystać #hashtagi."
   :add-a-status                          "Dodaj status..."
   :error                                 "Błąd"
   :edit-contacts                         "Edytuj kontakty"
   :more                                  "więcej"
   :cancel                                "Anuluj"
   :no-statuses-found                     "Nie znaleziono statusów"
   :swow-qr                               "Pokaż QR"
   :browsing-open-in-web-browser          "Otwórz w przeglądarce"
   :delete-group-prompt                   "To nie będzie miało wpływu na kontakty"
   :edit-profile                          "Edytuj profil"


   :enter-password-transactions           {:one   "Potwierdź transakcję, wprowadzając hasło"
                                           :other "Potwierdź transakcje, wprowadzając hasło"}
   :unsigned-transactions                 "Nieautoryzowane transakcje"
   :empty-topic                           "Pusty temat"
   :to                                    "Do"
   :group-members                         "Członkowie grupy"
   :estimated-fee                         "Szacowana opłata"
   :data                                  "Dane"})