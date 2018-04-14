(ns status-im.ui.components.icons.vector-icons
  (:require [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.styles :as styles]))

(def icons {:icons/discover            (js/require "./resources/icons/bottom/discover_gray.svg")
            :icons/contacts            (js/require "./resources/icons/bottom/contacts_gray.svg")
            :icons/home                (js/require "./resources/icons/bottom/home_gray.svg")
            :icons/home-active         (js/require "./resources/icons/bottom/home_blue.svg")
            :icons/profile             (js/require "./resources/icons/bottom/profile_gray.svg")
            :icons/profile-active      (js/require "./resources/icons/bottom/profile_blue.svg")
            :icons/wallet              (js/require "./resources/icons/bottom/wallet_gray.svg")
            :icons/wallet-active       (js/require "./resources/icons/bottom/wallet_active.svg")
            :icons/speaker             (js/require "./resources/icons/speaker.svg")
            :icons/speaker-off         (js/require "./resources/icons/speaker_off.svg")
            :icons/transaction-history (js/require "./resources/icons/transaction_history.svg")
            :icons/add                 (js/require "./resources/icons/add.svg")
            :icons/add-contact         (js/require "./resources/icons/add_contact.svg")
            :icons/add-wallet          (js/require "./resources/icons/add_wallet.svg")
            :icons/address             (js/require "./resources/icons/address.svg")
            :icons/arrow-left          (js/require "./resources/icons/arrow_left.svg")
            :icons/arrow-right         (js/require "./resources/icons/arrow_right.svg")
            :icons/flash-active        (js/require "./resources/icons/flash_active.svg")
            :icons/flash-inactive      (js/require "./resources/icons/flash_inactive.svg")
            :icons/attach              (js/require "./resources/icons/attach.svg")
            :icons/browse              (js/require "./resources/icons/browse.svg")
            :icons/close               (js/require "./resources/icons/close.svg")
            :icons/copy-from           (js/require "./resources/icons/copy_from.svg")
            :icons/delete              (js/require "./resources/icons/delete.svg")
            :icons/dots-horizontal     (js/require "./resources/icons/dots_horizontal.svg")
            :icons/dots-vertical       (js/require "./resources/icons/dots_vertical.svg")
            :icons/exclamation_mark    (js/require "./resources/icons/exclamation_mark.svg")
            :icons/filter              (js/require "./resources/icons/filter.svg")
            :icons/fullscreen          (js/require "./resources/icons/fullscreen.svg")
            :icons/group-big           (js/require "./resources/icons/group_big.svg")
            :icons/group-chat          (js/require "./resources/icons/group_chat.svg")
            :icons/chats               (js/require "./resources/icons/chats.svg")
            :icons/hamburger           (js/require "./resources/icons/hamburger.svg")
            :icons/hidden              (js/require "./resources/icons/hidden.svg")
            :icons/in-contacts         (js/require "./resources/icons/in_contacts.svg")
            :icons/mic                 (js/require "./resources/icons/mic.svg")
            :icons/ok                  (js/require "./resources/icons/ok.svg")
            :icons/public              (js/require "./resources/icons/public.svg")
            :icons/public-chat         (js/require "./resources/icons/public_chat.svg")
            :icons/qr                  (js/require "./resources/icons/QR.svg")
            :icons/input-commands      (js/require "./resources/icons/input_commands.svg")
            :icons/input-send          (js/require "./resources/icons/input_send.svg")
            :icons/back                (js/require "./resources/icons/back.svg")
            :icons/forward             (js/require "./resources/icons/forward.svg")
            :icons/dropdown-up         (js/require "./resources/icons/dropdown_up.svg")
            :icons/up                  (js/require "./resources/icons/up.svg")
            :icons/down                (js/require "./resources/icons/down.svg")
            :icons/grab                (js/require "./resources/icons/grab.svg")
            :icons/share               (js/require "./resources/icons/share.svg")
            :icons/tooltip-triangle    (js/require "./resources/icons/tooltip-triangle.svg")
            :icons/open                (js/require "./resources/icons/open.svg")
            :icons/network             (js/require "./resources/icons/network.svg")
            :icons/wnode               (js/require "./resources/icons/wnode.svg")
            :icons/refresh             (js/require "./resources/icons/refresh.svg")
            :icons/newchat             (js/require "./resources/icons/newchat.svg")
            :icons/logo                (js/require "./resources/icons/logo.svg")
            :icons/camera              (js/require "./resources/icons/camera.svg")})

(defn normalize-property-name [n]
  (if (= n :icons/options)
    (if platform/ios? :icons/dots-horizontal :icons/dots-vertical)
    n))

(def default-viewbox {:width 24 :height 24})

(defn icon
  ([name] (icon name nil))
  ([name {:keys [color container-style style accessibility-label width height]
          :or {accessibility-label :icon}}]
   (let [icon-style (if width
                      (assoc default-viewbox :width width :height height)
                      default-viewbox)]
     [react/view {:style               container-style
                  :accessibility-label accessibility-label}
      [react/image {:source (get icons (normalize-property-name name))
                    :style  (merge icon-style style)}]])))

