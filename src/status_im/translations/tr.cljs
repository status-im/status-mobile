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
   :datetime-ago                          "önce"
   :datetime-yesterday                    "dün"
   :datetime-today                        "bugün"

   ;profile
   :profile                               "Profil"
   :message                               "Mesaj"
   :not-specified                         "Belirtilmemiş"
   :public-key                            "Ortak Anahtar"
   :phone-number                          "Telefon Numarası'"
   :add-to-contacts                       "Kişi listesine ekle"

   ;make_photo
   :image-source-title                    "Profil Fotoğrafı"
   :image-source-make-photo               "Çek"
   :image-source-gallery                  "Galeriden seç"

   ;sign-up
   :contacts-syncronized                  "Kişi listeniz eşitlendi"
   :confirmation-code                     (str "Teşekkürler! Size onay kodunu içeren bir kısa mesaj "
                                               "gönderdim. Telefon numaranızı onaylamak için lütfen bu kodu girin")
   :incorrect-code                        (str "Üzgünüm kod hatalıydı, lütfen yeniden girin")
   :phew-here-is-your-passphrase          "*Oh* bu oldukça zor oldu işte parolanız, *bu parolayı bir yere not ederek saklayın!* Hesabınızı kurtarmak için bu parolaya ihtiyacınız olacaktır."
   :here-is-your-passphrase               "İşte parolanız, *bu parolayı bir yere not ederek saklayın!* Hesabınızı kurtarmak için bu parolaya ihtiyacınız olacaktır."
   :phone-number-required                 "Telefon numaranızı girmek için buraya dokunun, arkadaşlarınızı ben bulacağım"
   :intro-status                          "Hesabınızı kurmak ve ayarlarınızı değiştirmek için benimle sohbet edin!"
   :intro-message1                        "Status'e hoş geldiniz\nŞifrenizi oluşturmak ve hemen başlamak için bu mesaja dokunun!"
   :account-generation-message            "Bana birkaç saniye ayırın, hesabınızı oluşturmak için biraz matematik hesabı yapmam gerekecek!"

   ;chats
   :chats                                 "Sohbetler"
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
   :contacts-group-new-chat               "Yeni sohbet başlat"
   :no-contacts                           "Henüz herhangi bir kişi yok"
   :show-qr                               "Kare Kodu göster"

   ;group-settings
   :remove                                "Kaldır"
   :save                                  "Kaydet"
   :clear-history                         "Geçmişi temizle"
   :chat-settings                         "Sohbet ayarları"
   :edit                                  "Düzenle"
   :add-members                           "Üye ekle"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "sohbet daveti alındı"
   :removed-from-chat                     "sizi grup sohbetinden sildi"
   :left                                  "ayrıldı"
   :invited                               "davet edildi"
   :removed                               "silindi"
   :You                                   "Siz"

   ;new-contact
   :add-new-contact                       "Yeni kişi ekle"
   :scan-qr                               "Kare Kod tara"
   :name                                  "İsim"
   :address-explication                   "Burada belki de bir adresin ne olduğu ve bunu bulmak için nereye bakılmasıyla ilgili bazı metinler yer alabilir"
   :contact-already-added                 "Kişi zaten eklendi"
   :can-not-add-yourself                  "Kendinizi ekleyemezsiniz"
   :unknown-address                       "Bilinmeyen adres"


   ;login
   :connect                               "Bağlan"
   :address                               "Adres"
   :password                              "Şifre"
   :wrong-password                        "Hatalı Şifre"

   ;recover
   :passphrase                            "Parola"
   :recover                               "Kurtar"

   ;accounts
   :recover-access                        "Erişimi kurtar"

   ;wallet-qr-code
   :done                                  "Yapıldı"
   :main-wallet                           "Ana Cüzdan"

   ;validation
   :invalid-phone                         "Geçersiz telefon numarası"
   :amount                                "Miktar"
   ;transactions
   :status                                "Durum"
   :recipient                             "Alıcı"

   ;:webview
   :web-view-error                        "hoppala, hata"

   :confirm                               "Onayla"
   :phone-national                        "Ulusal"
   :public-group-topic                    "Başlık"
   :debug-enabled                         "Hata ayıklama sunucusu başlatıldı! Artık şimdi bilgisayarınızdan *status-dev-cli scan* çalıştırarak DApp'inizi ekleyebilirsiniz"
   :new-public-group-chat                 "Herkese açık sohbete katılın"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "İptal Et"
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
   :browsing-open-in-web-browser          "Web tarayıcısında açın"
   :delete-group-prompt                   "Bu, kişi listesini etkilemeyecektir"
   :edit-profile                          "Profili düzenleyin"

   :empty-topic                           "Boş başlık"
   :to                                    "Gönderilen:"
   :data                                  "Veri"})
