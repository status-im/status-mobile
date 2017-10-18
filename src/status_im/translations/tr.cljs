(ns status-im.translations.tr)

(def translations
  {
   ;common
   :members-title                         "Üyeler"
   :not-implemented                       "Uygulanmadı!"
   :chat-name                             "Sohbet adı"
   :notifications-title                   "Bildirimler ve sesler"
   :offline                               "Çevrimdışı"

   ;drawer
   :invite-friends                        "Arkadaşlarınızı davet edin"
   :faq                                   "SSS"
   :switch-users                          "Kullanıcıları değiştir"

   ;chat
   :is-typing                             "yazıyor"
   :and-you                               "ve siz"
   :search-chat                           "Sohbette ara"
   :members                               {:one   "1 üye"
                                           :other "{{count}} üye"
                                           :zero  "üye yok"}
   :members-active                        {:one   "1 üye, 1 aktif"
                                           :other "{{count}} üye, {{count}} aktif"
                                           :zero  "üye yok"}
   :active-online                         "Çevrimiçi"
   :active-unknown                        "Bilinmiyor"
   :available                             "Uygun"
   :no-messages                           "Mesaj yok"
   :suggestions-requests                  "İstekler"
   :suggestions-commands                  "Komutlar"

   ;sync
   :sync-in-progress                      "Eşitleniyor..."
   :sync-synced                           "Eşitlendi"

   ;messages
   :status-sending                        "Gönderiliyor..."
   :status-pending                        "Beklemede"
   :status-sent                           "Gönderildi"
   :status-seen-by-everyone               "Herkes tarafından görüldü"
   :status-seen                           "Görüldü"
   :status-delivered                      "Teslim edildi"
   :status-failed                         "Başarısız"

   ;datetime
   :datetime-second                       {:one   "saniye"
                                           :other "saniye"}
   :datetime-minute                       {:one   "dakika"
                                           :other "dakika"}
   :datetime-hour                         {:one   "saat"
                                           :other "saat"}
   :datetime-day                          {:one   "gün"
                                           :other "gün"}
   :datetime-multiple                     "sn"
   :datetime-ago                          "önce"
   :datetime-yesterday                    "dün"
   :datetime-today                        "bugün"

   ;profile
   :profile                               "Profil"
   :report-user                           "KULLANICIYI ŞİKAYET ET"
   :message                               "Mesaj"
   :username                              "Kullanıcı Adı"
   :not-specified                         "Belirtilmemiş"
   :public-key                            "Ortak Anahtar"
   :phone-number                          "Telefon Numarası'"
   :email                                 "E-posta"
   :profile-no-status                     "Durum yok"
   :add-to-contacts                       "Kişi listesine ekle"
   :error-incorrect-name                  "Hatalı isim"
   :error-incorrect-email                 "Hatalı e-posta adresi"

   ;make_photo
   :image-source-title                    "Profil Fotoğrafı"
   :image-source-make-photo               "Çek"
   :image-source-gallery                  "Galeriden seç"
   :image-source-cancel                   "İptal"

   ;sign-up
   :contacts-syncronized                  "Kişi listeniz eşitlendi"
   :confirmation-code                     (str "Teşekkürler! Size onay kodunu içeren bir kısa mesaj "
                                               "gönderdim. Telefon numaranızı onaylamak için lütfen bu kodu girin")
   :incorrect-code                        (str "Üzgünüm kod hatalıydı, lütfen yeniden girin")
   :generate-passphrase                   (str "sizin için bir parola oluşturacağım, böylece başka bir cihazdan "
                                               "erişebilecek ya da girişinizi kurtarabileceksiniz")
   :phew-here-is-your-passphrase          "*Oh* bu oldukça zor oldu işte parolanız, *bu parolayı bir yere not ederek saklayın!* Hesabınızı kurtarmak için bu parolaya ihtiyacınız olacaktır."
   :here-is-your-passphrase               "İşte parolanız, *bu parolayı bir yere not ederek saklayın!* Hesabınızı kurtarmak için bu parolaya ihtiyacınız olacaktır."
   :written-down                          "Güvenli bir şekilde not ettiğinizden emin olun"
   :phone-number-required                 "Telefon numaranızı girmek için buraya dokunun, arkadaşlarınızı ben bulacağım"
   :intro-status                          "Hesabınızı kurmak ve ayarlarınızı değiştirmek için benimle sohbet edin!"
   :intro-message1                        "Status'e hoş geldiniz\nŞifrenizi oluşturmak ve hemen başlamak için bu mesaja dokunun!"
   :account-generation-message            "Bana birkaç saniye ayırın, hesabınızı oluşturmak için biraz matematik hesabı yapmam gerekecek!"

   ;chats
   :chats                                 "Sohbetler"
   :new-chat                              "Yeni sohbet"
   :new-group-chat                        "Yeni grup sohbeti"

   ;discover
   :discover                              "Keşfet"
   :none                                  "Hiçbiri"
   :search-tags                           "Aramak istediğiniz etiketleri buraya girin"
   :popular-tags                          "Popüler etiketler"
   :recent                                "Güncel"
   :no-statuses-discovered                "Herhangi bir durum keşfedilmedi"

   ;settings
   :settings                              "Ayarlar"

   ;contacts
   :contacts                              "Kişiler"
   :new-contact                           "Yeni Kişi"
   :show-all                              "TÜMÜNÜ GÖSTER"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Kişiler"
   :contacts-group-new-chat               "Yeni sohbet başlat"
   :no-contacts                           "Henüz herhangi bir kişi yok"
   :show-qr                               "Kare Kodu göster"

   ;group-settings
   :remove                                "Kaldır"
   :save                                  "Kaydet"
   :change-color                          "Rengi değiştir"
   :clear-history                         "Geçmişi temizle"
   :delete-and-leave                      "Sil ve ayrıl"
   :chat-settings                         "Sohbet ayarları"
   :edit                                  "Düzenle"
   :add-members                           "Üye ekle"
   :blue                                  "Mavi"
   :purple                                "Mor"
   :green                                 "Yeşil"
   :red                                   "Kırmızı"

   ;commands
   :money-command-description             "Para gönder"
   :location-command-description          "Konum gönder"
   :phone-command-description             "Telefon numarasını gönder"
   :phone-request-text                    "Telefon numarası iste"
   :confirmation-code-command-description "Onay kodunu gönder"
   :confirmation-code-request-text        "Onay kodu iste"
   :send-command-description              "Konum gönder"
   :request-command-description           "İstek gönder"
   :keypair-password-command-description  "Şifre gönder"
   :help-command-description              "Yardım"
   :request                               "İstek"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH alıcı: {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH gönderen: {{chat-name}}"

   ;new-group
   :group-chat-name                       "Sohbet adı"
   :empty-group-chat-name                 "Lütfen bir isim girin"
   :illegal-group-chat-name               "Lütfen başka bir isim seçin"

   ;participants
   :add-participants                      "Katılımcı Ekle"
   :remove-participants                   "Katılımcıları Sil"

   ;protocol
   :received-invitation                   "sohbet daveti alındı"
   :removed-from-chat                     "sizi grup sohbetinden sildi"
   :left                                  "ayrıldı"
   :invited                               "davet edildi"
   :removed                               "silindi"
   :You                                   "Siz"

   ;new-contact
   :add-new-contact                       "Yeni kişi ekle"
   :import-qr                             "İçe aktar"
   :scan-qr                               "Kare Kod tara"
   :name                                  "İsim"
   :whisper-identity                      "Whisper Kimliği"
   :address-explication                   "Burada belki de bir adresin ne olduğu ve bunu bulmak için nereye bakılmasıyla ilgili bazı metinler yer alabilir"
   :enter-valid-address                   "Lütfen geçerli bir adres girin ya da bir Kare Kod tarayın"
   :contact-already-added                 "Kişi zaten eklendi"
   :can-not-add-yourself                  "Kendinizi ekleyemezsiniz"
   :unknown-address                       "Bilinmeyen adres"


   ;login
   :connect                               "Bağlan"
   :address                               "Adres"
   :password                              "Şifre"
   :login                                 "Oturum Aç"
   :wrong-password                        "Hatalı Şifre"

   ;recover
   :recover-from-passphrase               "Parolayı kullanarak kurtar"
   :recover-explain                       "Erişimi kurtarmak için lütfen şifreniz için parolanızı girin"
   :passphrase                            "Parola"
   :recover                               "Kurtar"
   :enter-valid-passphrase                "Lütfen geçerli bir parola girin"
   :enter-valid-password                  "Lütfen geçerli bir şifre girin"

   ;accounts
   :recover-access                        "Erişimi kurtar"
   :add-account                           "Hesap ekle"

   ;wallet-qr-code
   :done                                  "Yapıldı"
   :main-wallet                           "Ana Cüzdan"

   ;validation
   :invalid-phone                         "Geçersiz telefon numarası"
   :amount                                "Miktar"
   :not-enough-eth                        (str "Yeterli ETH bakiyesi yok"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "İşlemi onayla"
                                           :other "{{count}} işlemi onayla"
                                           :zero  "İşlem yok"}
   :status                                "Durum"
   :pending-confirmation                  "Bekleyen onay"
   :recipient                             "Alıcı"
   :one-more-item                         "Bir öğe daha"
   :fee                                   "Ücret"
   :value                                 "Değer"

   ;:webview
   :web-view-error                        "hoppala, hata"

   :confirm                               "Onayla"
   :phone-national                        "Ulusal"
   :transactions-confirmed                {:one   "İşlem onaylandı"
                                           :other "{{count}} işlem onaylandı"
                                           :zero  "Hiçbir işlem onaylanmadı"}
   :public-group-topic                    "Başlık"
   :debug-enabled                         "Hata ayıklama sunucusu başlatıldı! Artık şimdi bilgisayarınızdan *status-dev-cli scan* çalıştırarak DApp'inizi ekleyebilirsiniz"
   :new-public-group-chat                 "Herkese açık sohbete katılın"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "İptal Et"
   :share-qr                              "QR Paylaş"
   :feedback                              "Geri bildirimde bulunmak ister misiniz?\nTelefonunuzu sallayın!"
   :twelve-words-in-correct-order         "Doğru sırayla 12 kelime"
   :remove-from-contacts                  "Kişi listesinden çıkarın"
   :delete-chat                           "Sohbeti silin"
   :edit-chats                            "Sohbetleri düzenleyin"
   :sign-in                               "Oturum açın"
   :create-new-account                    "Yeni hesap açın"
   :sign-in-to-status                     "Durumlar için oturum açın"
   :got-it                                "Anladım"
   :move-to-internal-failure-message      "Bazı önemli dosyaları harici depolamadan dahili depolamaya taşımamız gerekiyor. Bu nedenle, izniniz gerekli. Gelecek sürümlerde harici depolamayı kullanmayacağız."
   :edit-group                            "Grubu düzenleyin"
   :delete-group                          "Grubu silin"
   :browsing-title                        "Tarayın"
   :reorder-groups                        "Grupları yeniden sıralayın"
   :browsing-cancel                       "İptal Et"
   :faucet-success                        "Vana talebi alındı"
   :choose-from-contacts                  "Kişi listesinden seçin"
   :new-group                             "Yeni grup"
   :phone-e164                            "Uluslararası 1"
   :remove-from-group                     "Gruptan çıkarın"
   :search-contacts                       "Kişiler listesinde arayın"
   :transaction                           "İşlem"
   :public-group-status                   "Herkese açık"
   :leave-chat                            "Sohbetten çıkın"
   :start-conversation                    "Sohbete başlayın"
   :topic-format                          "Yanlış format [a-z0-9\\-]+"
   :enter-valid-public-key                "Lütfen geçerli bir ortak anahtar girin veya QR kodu tarayın"
   :faucet-error                          "Musluk talebi hatası"
   :phone-significant                     "Önemli"
   :search-for                            "Şunu ara..."
   :sharing-copy-to-clipboard             "Panoya kopyalayın"
   :phone-international                   "Uluslararası 2"
   :enter-address                         "Adresi girin"
   :send-transaction                      "İşlem gönderin"
   :delete-contact                        "Kişiyi silin"
   :mute-notifications                    "Bildirimleri kapat"

   :contact-s
                                          {:one   "kişi"
                                           :other "kişiler"}
   :group-name                            "Grup ismi"
   :next                                  "Sonraki"
   :from                                  "Şuradan:"
   :search-chats                          "Sohbetlerde arayın"
   :in-contacts                           "kişiler listesinde"

   :sharing-share                         "Paylaşın..."
   :type-a-message                        "Mesaj yazın..."
   :type-a-command                        "Komut girmeye başlayın..."
   :shake-your-phone                      "Hata mı buldunuz ya da öneriniz mi var? Sadece telefonunuzu ~sallamanız~ yeterli!"
   :status-prompt                         "Sunduğunuz şeyler hakkında insanlara yardımcı olması için bir durum oluşturun. #hashtag de kullanabilirsiniz."
   :add-a-status                          "Durum ekleyin..."
   :error                                 "Hata"
   :edit-contacts                         "Kişileri düzenleyin"
   :more                                  "dahası"
   :cancel                                "İptal Et"
   :no-statuses-found                     "Durum bulunamadı"
   :swow-qr                               "QR Göster"
   :browsing-open-in-web-browser          "Web tarayıcısında açın"
   :delete-group-prompt                   "Bu, kişi listesini etkilemeyecektir"
   :edit-profile                          "Profili düzenleyin"

   :enter-password-transactions
                                          {:one   "Şifrenizi girerek işlemi onaylayın"
                                           :other "Şifrenizi girerek işlemleri onaylayın"}
   :unsigned-transactions                 "İmzalanmamış işlemler"
   :empty-topic                           "Boş başlık"
   :to                                    "Gönderilen:"
   :group-members                         "Grup üyeleri"
   :estimated-fee                         "Tahmini ücret"
   :data                                  "Veri"})
