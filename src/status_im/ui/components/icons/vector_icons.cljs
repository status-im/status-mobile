(ns status-im.ui.components.icons.vector-icons
  (:require-macros [status-im.utils.slurp :refer [slurp-svg]])
  (:require [reagent.core :as reagent]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]))


(def icons {:icons/chats               (js/require "./resources/icons/bottom/chats_gray.svg")
            :icons/chats-active        (js/require "./resources/icons/bottom/chats_active.svg")
            :icons/contacts            (js/require "./resources/icons/bottom/contacts_gray.svg")
            :icons/contacts-active     (js/require "./resources/icons/bottom/contacts_active.svg")
            :icons/discover            (js/require "./resources/icons/bottom/discover_gray.svg")
            :icons/discover-active     (js/require "./resources/icons/bottom/discover_active.svg")
            :icons/wallet              (js/require "./resources/icons/bottom/wallet_gray.svg")
            :icons/wallet-active       (js/require "./resources/icons/bottom/wallet_active.svg")
            :icons/speaker             (js/require "./resources/icons/speaker.svg")
            :icons/speaker-off         (js/require "./resources/icons/speaker_off.svg")
            :icons/transaction-history (js/require "./resources/icons/transaction_history.svg")
            :icons/add                 (js/require "./resources/icons/add.svg")
            :icons/add-wallet          (js/require "./resources/icons/add_wallet.svg")
            :icons/address             (js/require "./resources/icons/address.svg")
            :icons/arrow-left          (js/require "./resources/icons/arrow_left.svg")
            :icons/arrow-right         (js/require "./resources/icons/arrow_right.svg")
            :icons/flash-active        (js/require "./resources/icons/flash_active.svg")
            :icons/flash-inactive      (js/require "./resources/icons/flash_inactive.svg")
            :icons/attach              (js/require "./resources/icons/attach.svg")
            :icons/back                (js/require "./resources/icons/back.svg")
            :icons/browse              (js/require "./resources/icons/browse.svg")
            :icons/close               (js/require "./resources/icons/close.svg")
            :icons/copy-from           (js/require "./resources/icons/copy_from.svg")
            :icons/dots-horizontal     (js/require "./resources/icons/dots_horizontal.svg")
            :icons/dots-vertical       (js/require "./resources/icons/dots_vertical.svg")
            :icons/exclamation_mark    (js/require "./resources/icons/exclamation_mark.svg")
            :icons/filter              (js/require "./resources/icons/filter.svg")
            :icons/forward             (js/require "./resources/icons/forward.svg")
            :icons/fullscreen          (js/require "./resources/icons/fullscreen.svg")
            :icons/group-big           (js/require "./resources/icons/group_big.svg")
            :icons/group-chat          (js/require "./resources/icons/group_chat.svg")
            :icons/hamburger           (js/require "./resources/icons/hamburger.svg")
            :icons/hidden              (js/require "./resources/icons/hidden.svg")
            :icons/mic                 (js/require "./resources/icons/mic.svg")
            :icons/ok                  (js/require "./resources/icons/ok.svg")
            :icons/public              (js/require "./resources/icons/public.svg")
            :icons/public-chat         (js/require "./resources/icons/public_chat.svg")
            :icons/qr                  (js/require "./resources/icons/QR.svg")
            :icons/search              (js/require "./resources/icons/search.svg")
            :icons/smile               (js/require "./resources/icons/smile.svg")
            :icons/commands-list       (js/require "./resources/icons/commands_list.svg")
            :icons/dropdown-up         (js/require "./resources/icons/dropdown_up.svg")
            :icons/dropdown            (js/require "./resources/icons/dropdown.svg")
            :icons/grab                (js/require "./resources/icons/grab.svg")
            :icons/share               (js/require "./resources/icons/share.svg")
            :icons/tooltip-triangle    (js/require "./resources/icons/tooltip-triangle.svg")
            :icons/network             (js/require "./resources/icons/network.svg")})

(defn normalize-property-name [n]
  (if (= n :icons/options)
    (if platform/ios? :icons/dots-horizontal :icons/dots-vertical)
    n))

(def default-viewbox {:width 24 :height 24})

(defn icon
  ([name] (icon name nil))
  ([name {:keys [color container-style style accessibility-label]
          :or {accessibility-label :icon}}]
   (print "Icon")
   [react/view {:style               container-style
                :accessibility-label accessibility-label}
    [react/image {:source (get icons (normalize-property-name name))
                  :style  (merge default-viewbox style)}]]))
