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
   :datetime-ago                          "trước đây"
   :datetime-yesterday                    "hôm qua"
   :datetime-today                        "hôm nay"

   ;profile
   :profile                               "Hồ sơ"
   :message                               "Tin nhắn"
   :not-specified                         "Không được xác định"
   :public-key                            "Khóa công khai"
   :phone-number                          "Số điện thoại"
   :add-to-contacts                       "Thêm vào liên hệ"

   ;;make_photo
   :image-source-title                    "Ảnh đại diện"
   :image-source-make-photo               "Chụp"
   :image-source-gallery                  "Chọn từ gallery"

   ;sign-up
   :contacts-syncronized                  "Các liên hệ của bạn đã được đồng bộ hóa"
   :confirmation-code                     (str "Cảm ơn! Chúng tôi đã gửi cho bạn một tin nhắn văn bản với một xác nhận "
                                               "Vui lòng cung cấp mã đó để xác nhận số điện thoại của bạn")
   :incorrect-code                        (str "Xin lỗi mã không chính xác, vui lòng nhập lại")
   :phew-here-is-your-passphrase          "*Phù* điều đó thật khó khăn, đây là cụm mật khẩu của bạn, *hãy viết cụm này ra và giữ nó an toàn!* Bạn sẽ cần nó để khôi phục tài khoản của bạn."
   :here-is-your-passphrase               "Đây là cụm mật khẩu của bạn, *hãy viết cụm này ra và giữ nó an toàn!* Bạn sẽ cần nó để khôi phục tài khoản của bạn."
   :phone-number-required                 "Nhấp vào đây để nhập số điện thoại của bạn & tôi sẽ tìm kiếm bạn bè của bạn"
   :intro-status                          "Trò chuyện với tôi để thiết lập tài khoản của bạn và thay đổi các thiết lập của bạn!"
   :intro-message1                        "Chào mừng đến với trang Trạng thái\nNhấp vào tin nhắn này để thiết lập mật khẩu của bạn & bắt đầu!"
   :account-generation-message            "Cho tôi một giây, tôi phải làm vài thuật toán điên khùng để khởi tạo tài khoản của bạn!"

   ;chats
   :chats                                 "Trò chuyện"
   :new-group-chat                        "Cuộc trò chuyện theo nhóm mới"

   ;discover
   :discover                              "Khám phá"
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
   :contacts-group-new-chat               "Bắt đầu cuộc trò chuyện mới"
   :no-contacts                           "Chưa có liên hệ"
   :show-qr                               "Hiển thị QR"

   ;group-settings
   :remove                                "Xóa"
   :save                                  "Lưu"
   :clear-history                         "Xóa lịch sử"
   :chat-settings                         "Thiết lập trò chuyện"
   :edit                                  "Điều chỉnh"
   :add-members                           "Thêm Thành viên"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "đã nhận lời mời trò chuyện"
   :removed-from-chat                     "đã xóa bạn khỏi cuộc trò chuyện theo nhóm"
   :left                                  "đã rời đi"
   :invited                               "đã mời"
   :removed                               "đã xóa"
   :You                                   "Bạn"

   ;new-contact
   :add-new-contact                       "Thêm liên hệ mới"
   :scan-qr                               "Quét QR"
   :name                                  "Tên"
   :address-explication                   "Có lẽ ở đây nên có một vài nội dung để giải thích địa chỉ này là gì và phải tìm nó ở đâu"
   :contact-already-added                 "Liên hệ đã được thêm vào"
   :can-not-add-yourself                  "Bạn không thể tự thêm mình"
   :unknown-address                       "Địa chỉ không xác định"


   ;login
   :connect                               "Kết nối"
   :address                               "Địa chỉ"
   :password                              "Mật khẩu"
   :wrong-password                        "Mật khẩu sai"

   ;recover
   :passphrase                            "Cụm mật khẩu"
   :recover                               "Khôi phục"

   ;accounts
   :recover-access                        "Khôi phục quyền truy cập"

   ;wallet-qr-code
   :done                                  "Đã hoàn thành"
   :main-wallet                           "Ví chính"

   ;validation
   :invalid-phone                         "Số điện thoại không hợp lệ"
   :amount                                "Số tiền"
   ;transactions
   :status                                "Trạng thái"
   :recipient                             "Người nhận"

   ;:webview
   :web-view-error                        "Ối, lỗi"

   :confirm                               "Xác nhận"
   :phone-national                        "Quốc gia"
   :public-group-topic                    "Chủ đề"
   :debug-enabled                         "Máy chủ gỡ lỗi đã được khởi chạy! Bạn có thể thêm DApp bằng cách chạy *status-dev-cli scan* từ máy tính của mình"
   :new-public-group-chat                 "Tham gia cuộc trò chuyện công khai"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Hủy"
   :twelve-words-in-correct-order         "12 từ theo đúng thứ tự"
   :remove-from-contacts                  "Xóa khỏi danh bạ"
   :delete-chat                           "Xóa cuộc trò chuyện"
   :edit-chats                            "Chỉnh sửa cuộc trò chuyện"
   :sign-in                               "Đăng nhập"
   :create-new-account                    "Tạo tài khoản mới"
   :sign-in-to-status                     "Đăng nhập vào Trạng thái"
   :got-it                                "Đã hiểu"
   :move-to-internal-failure-message      "Chúng tôi cần di chuyển một số tập tin quan trọng từ bộ nhớ ngoài sang bộ nhớ trong. Để làm điều này, chúng tôi cần sự cho phép của bạn. Chúng tôi sẽ không sử dụng bộ nhớ ngoài trong các phiên bản trong tương lai."
   :edit-group                            "Chỉnh sửa nhóm"
   :delete-group                          "Xóa nhóm"
   :browsing-title                        "Duyệt"
   :reorder-groups                        "Sắp xếp lại các nhóm"
   :browsing-cancel                       "Hủy"
   :faucet-success                        "Đã nhận được yêu cầu Faucet"
   :choose-from-contacts                  "Chọn từ danh bạ"
   :new-group                             "Nhóm mới"
   :phone-e164                            "Quốc tế 1"
   :remove-from-group                     "Xóa khỏi nhóm"
   :search-contacts                       "Tìm kiếm danh bạ"
   :transaction                           "Giao dịch"
   :public-group-status                   "Công cộng"
   :leave-chat                            "Rời cuộc trò chuyện"
   :start-conversation                    "Bắt đầu cuộc trò chuyện"
   :topic-format                          "Định dạng sai [a-z0-9 \\ -] +"
   :enter-valid-public-key                "Vui lòng nhập khoá công khai hợp lệ hoặc quét mã QR"
   :faucet-error                          "Lỗi yêu cầu Faucet"
   :phone-significant                     "Đáng kể"
   :search-for                            "Tìm kiếm..."
   :sharing-copy-to-clipboard             "Sao chép vào bộ nhớ đệm"
   :phone-international                   "Quốc tế 2"
   :enter-address                         "Nhập địa chỉ"
   :send-transaction                      "Gửi giao dịch"
   :delete-contact                        "Xóa liên lạc"
   :mute-notifications                    "Tắt tiếng thông báo"

   :contact-s
                                          {:one   "liên hệ"
                                           :other "Danh bạ"}
   :next                                  "Tiếp theo"
   :from                                  "Từ"
   :search-chats                          "Tìm kiếm các cuộc trò chuyện"
   :in-contacts                           "Trong danh bạ"

   :sharing-share                         "Chia sẻ..."
   :type-a-message                        "Nhập một tin nhắn..."
   :type-a-command                        "Bắt đầu nhập một lệnh ..."
   :shake-your-phone                      "Tìm thấy lỗi hoặc có đề xuất? Chỉ cần ~lắc~ điện thoại của bạn!"
   :status-prompt                         "Tạo trạng thái để giúp mọi người biết về những điều bạn đang cung cấp. Bạn cũng có thể sử dụng #hashtags."
   :add-a-status                          "Thêm trạng thái ..."
   :error                                 "Lỗi"
   :edit-contacts                         "Chỉnh sửa danh bạ"
   :more                                  "thêm"
   :cancel                                "Hủy"
   :no-statuses-found                     "Không tìm thấy trạng thái nào"
   :browsing-open-in-web-browser          "Mở trong trình duyệt web"
   :delete-group-prompt                   "Điều này sẽ không ảnh hưởng đến danh bạ"
   :edit-profile                          "Chỉnh sửa hồ sơ"

   :empty-topic                           "Chủ đề trống"
   :to                                    "Đến"
   :data                                  "Dữ liệu"})