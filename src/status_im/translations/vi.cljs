(ns status-im.translations.vi)

(def translations
  {
   ;common
   :members-title                         "Các thành viên"
   :not-implemented                       "!không được thực hiện"
   :chat-name                             "Tên trò chuyện"
   :notifications-title                   "Thông báo và âm thanh"
   :offline                               "Không trực tuyến"

   ;drawer
   :invite-friends                        "Mời bạn bè "
   :faq                                   "FAQ"
   :switch-users                          "Đổi người dùng"

   ;chat
   :is-typing                             "đang gõ"
   :and-you                               "và bạn"
   :search-chat                           "Tìm kiếm trò chuyện"
   :members                               {:one   "1 thành viên"
                                           :other "{{count}} thành viên"
                                           :zero  "không thành viên"}
   :members-active                        {:one   "1 thành viên, 1 đang hoạt động"
                                           :other "{{count}} thành viên, {{count}} đang hoạt động"
                                           :zero  "không thành viên"}
   :active-online                         "Trực tuyến"
   :active-unknown                        "Không rõ"
   :available                             "Sẵn sàng"
   :no-messages                           "Không có tin nhắn"
   :suggestions-requests                  "Các yêu cầu"
   :suggestions-commands                  "Các lệnh"

   ;sync
   :sync-in-progress                      "Đang đồng bộ..."
   :sync-synced                           "Đồng bộ"

   ;messages
   :status-sending                        "Đang gửi"
   :status-pending                        "Chưa giải quyết"
   :status-sent                           "Đã gửi"
   :status-seen-by-everyone               "Được nhìn thấy bởi mọi người"
   :status-seen                           "Đã nhìn thấy "
   :status-delivered                      "Đã gửi"
   :status-failed                         "Đã thất bại"

   ;datetime
   :datetime-second                       {:one   "giây"
                                           :other "giây"}
   :datetime-minute                       {:one   "phút"
                                           :other "phút"}
   :datetime-hour                         {:one   "giờ"
                                           :other "giờ"}
   :datetime-day                          {:one   "ngày"
                                           :other "ngày"}
   :datetime-multiple                     "s"
   :datetime-ago                          "trước đây"
   :datetime-yesterday                    "hôm qua"
   :datetime-today                        "hôm nay"

   ;profile
   :profile                               "Hồ sơ"
   :report-user                           "BÁO CÁO NGƯỜI DÙNG"
   :message                               "Tin nhắn"
   :username                              "Tên người dùng"
   :not-specified                         "Không được xác định"
   :public-key                            "Khóa công khai"
   :phone-number                          "Số điện thoại"
   :email                                 "Email"
   :profile-no-status                     "Không có trạng thái"
   :add-to-contacts                       "Thêm vào liên hệ"
   :error-incorrect-name                  "Vui lòng chọn một tên khác"
   :error-incorrect-email                 "E-mail không chính xách"

   ;;make_photo
   :image-source-title                    "Ảnh đại diện"
   :image-source-make-photo               "Chụp"
   :image-source-gallery                  "Chọn từ gallery"
   :image-source-cancel                   "Hủy"

   ;sign-up
   :contacts-syncronized                  "Các liên hệ của bạn đã được đồng bộ hóa"
   :confirmation-code                     (str "Cảm ơn! Chúng tôi đã gửi cho bạn một tin nhắn văn bản với một xác nhận "
                                               "Vui lòng cung cấp mã đó để xác nhận số điện thoại của bạn")
   :incorrect-code                        (str "Xin lỗi mã không chính xác, vui lòng nhập lại")
   :generate-passphrase                   (str "Tôi sẽ tạo ra một cụm mật khẩu để cho bạn có thể khôi phục "
                                               "quyền truy cập của bạn hoặc đăng nhập từ một thiết bị khác")
   :phew-here-is-your-passphrase          "*Phù* điều đó thật khó khăn, đây là cụm mật khẩu của bạn, *hãy viết cụm này ra và giữ nó an toàn!* Bạn sẽ cần nó để khôi phục tài khoản của bạn."
   :here-is-your-passphrase               "Đây là cụm mật khẩu của bạn, *hãy viết cụm này ra và giữ nó an toàn!* Bạn sẽ cần nó để khôi phục tài khoản của bạn."
   :written-down                          "Hãy đảm bảo rằng bạn đã viết nó ra một cách bảo mật"
   :phone-number-required                 "Nhấp vào đây để nhập số điện thoại của bạn & tôi sẽ tìm kiếm bạn bè của bạn"
   :intro-status                          "Trò chuyện với tôi để thiết lập tài khoản của bạn và thay đổi các thiết lập của bạn!"
   :intro-message1                        "Chào mừng đến với trang Trạng thái\nNhấp vào tin nhắn này để thiết lập mật khẩu của bạn & bắt đầu!"
   :account-generation-message            "Cho tôi một giây, tôi phải làm vài thuật toán điên khùng để khởi tạo tài khoản của bạn!"

   ;chats
   :chats                                 "Trò chuyện"
   :new-chat                              "Cuộc trò chuyện mới"
   :new-group-chat                        "Cuộc trò chuyện theo nhóm mới"

   ;discover
   :discover                             "Khám phá"
   :none                                  "Không"
   :search-tags                           "Gõ các thẻ tìm kiếm của bạn tại đây"
   :popular-tags                          "Các thẻ phổ biến"
   :recent                                "Gần đây"
   :no-statuses-discovered                "Không có trạng thái được tìm thấy"

   ;settings
   :settings                              "Các thiết lập"

   ;contacts
   :contacts                              "Các liên hệ"
   :new-contact                           "Liên hệ mới"
   :show-all                              "HIỂN THỊ TOÀN BỘ"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Cá nhân"
   :contacts-group-new-chat               "Bắt đầu cuộc trò chuyện mới"
   :no-contacts                           "Chưa có liên hệ"
   :show-qr                               "Hiển thị QR"

   ;group-settings
   :remove                                "Xóa"
   :save                                  "Lưu"
   :change-color                          "Thay đổi màu sắc"
   :clear-history                         "Xóa lịch sử"
   :delete-and-leave                      "Xóa và rời đi"
   :chat-settings                         "Thiết lập trò chuyện"
   :edit                                  "Điều chỉnh"
   :add-members                           "Thêm Thành viên"
   :blue                                  "Màu xanh da trời"
   :purple                                "Màu tím"
   :green                                 "Màu xanh lá cây"
   :red                                   "Màu đỏ"

   ;commands
   :money-command-description             "Gửi tiền"
   :location-command-description          "Gửi vị trí"
   :phone-command-description             "Gửi số điện thoại"
   :phone-request-text                    "Yêu cầu số điện thoại"
   :confirmation-code-command-description "Gửi mã xác nhận"
   :confirmation-code-request-text        "Yêu cầu mã xác nhận"
   :send-command-description              "Gửi vị trí"
   :request-command-description           "Gửi yêu cầu"
   :keypair-password-command-description  ""
   :help-command-description              "Giúp đỡ"
   :request                               "Yêu cầu"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH đến {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH từ {{chat-name}}"
   :command-text-location                 "Vị trí: {{address}}"
   :command-text-browse                   "Trình duyệt trang web: {{webpage}}"
   :command-text-send                     "Giao dịch: {{amount}} ETH"
   :command-text-help                     "Giúp đỡ"

   ;new-group
   :group-chat-name                       "Tên trò chuyện"
   :empty-group-chat-name                 "Vui lòng nhập một tên"
   :illegal-group-chat-name               "Vui lòng chọn một tên khác"

   ;participants
   :add-participants                      "Thêm Người tham gia"
   :remove-participants                   "Xóa Người tham gia"

   ;protocol
   :received-invitation                   "đã nhận lời mời trò chuyện"
   :removed-from-chat                     "đã xóa bạn khỏi cuộc trò chuyện theo nhóm"
   :left                                  "đã rời đi"
   :invited                               "đã mời"
   :removed                               "đã xóa"
   :You                                   "Bạn"

   ;new-contact
   :add-new-contact                       "Thêm liên hệ mới"
   :import-qr                             "Nhập"
   :scan-qr                               "Quét QR"
   :name                                  "Tên"
   :whisper-identity                      "Danh tính Whisper"
   :address-explication                   "Có lẽ ở đây nên có một vài nội dung để giải thích địa chỉ này là gì và phải tìm nó ở đâu"
   :enter-valid-address                   "Vui lòng nhập một địa chỉ hợp lệ hoặc quét một mã QR"
   :contact-already-added                 "Liên hệ đã được thêm vào"
   :can-not-add-yourself                  "Bạn không thể tự thêm mình"
   :unknown-address                       "Địa chỉ không xác định"


   ;login
   :connect                               "Kết nối"
   :address                               "Địa chỉ"
   :password                              "Mật khẩu"
   :login                                 "Đăng nhập"
   :wrong-password                        "Mật khẩu sai"

   ;recover
   :recover-from-passphrase               "Khôi phục từ cụm mật khẩu"
   :recover-explain                       "Vui lòng nhập cụm mật khẩu cho mật khẩu của bạn để khôi phục quyền truy cập"
   :passphrase                            "Cụm mật khẩu"
   :recover                               "Khôi phục"
   :enter-valid-passphrase                "Vui lòng nhập một cụm mật khẩu"
   :enter-valid-password                  "Vui lòng nhập một mật khẩu"

   ;accounts
   :recover-access                        "Khôi phục quyền truy cập"
   :add-account                           "Thêm tài khoản"

   ;wallet-qr-code
   :done                                  "Đã hoàn thành"
   :main-wallet                           "Ví chính"

   ;validation
   :invalid-phone                         "Số điện thoại không hợp lệ"
   :amount                                "Số tiền"
   :not-enough-eth                        (str "Không đủ ETH trong số dư "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Xác nhận giao dịch"
                                           :other "Xác nhận {{count}} giao dịch"
                                           :zero  "Không có giao dịch"}
   :status                                "Trạng thái"
   :pending-confirmation                  "Chờ xác nhận"
   :recipient                             "Người nhận"
   :one-more-item                         "Thêm một mục nữa"
   :fee                                   "Phí"
   :value                                 "Giá trị"

   ;:webview
   :web-view-error                        "Ối, lỗi"})
