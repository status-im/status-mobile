(ns status-im.ui.components.icons.vector-icons
  (:require-macros [status-im.ui.components.svg :as components.svg])
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.styles :as styles]
            [status-im.react-native.js-dependencies :as js-dependencies])
  (:refer-clojure :exclude [use]))

(def get-image
  (if platform/desktop?
    js/require
    components.svg/slurp-svg))

(if-not platform/desktop?
  (do (defn get-property [name]
            (object/get js-dependencies/svg name))

      (defn adapt-class [class]
            (when class
                  (reagent/adapt-react-class class)))

      (defn get-class [name]
            (adapt-class (get-property name)))

      (def svg (get-class "Svg"))
      (def g (get-class "G"))
      (def rect (get-class "Rect"))
      (def path (get-class "Path"))
      (def use (get-class "Use"))
      (def defs (get-class "Defs"))
      (def circle (get-class "Circle"))))

(def icons {:icons/discover            (get-image "./resources/icons/bottom/discover_gray.svg")
            :icons/contacts            (get-image "./resources/icons/bottom/contacts_gray.svg")
            :icons/home                (get-image "./resources/icons/bottom/home_gray.svg")
            :icons/home-active         (get-image "./resources/icons/bottom/home_blue.svg")
            :icons/profile             (get-image "./resources/icons/bottom/profile_gray.svg")
            :icons/profile-active      (get-image "./resources/icons/bottom/profile_blue.svg")
            :icons/wallet              (get-image "./resources/icons/bottom/wallet_gray.svg")
            :icons/wallet-active       (get-image "./resources/icons/bottom/wallet_active.svg")
            :icons/speaker             (get-image "./resources/icons/speaker.svg")
            :icons/speaker-off         (get-image "./resources/icons/speaker_off.svg")
            :icons/transaction-history (get-image "./resources/icons/transaction_history.svg")
            :icons/add                 (get-image "./resources/icons/add.svg")
            :icons/add-contact         (get-image "./resources/icons/add_contact.svg")
            :icons/add-wallet          (get-image "./resources/icons/add_wallet.svg")
            :icons/address             (get-image "./resources/icons/address.svg")
            :icons/arrow-left          (get-image "./resources/icons/arrow_left.svg")
            :icons/arrow-right         (get-image "./resources/icons/arrow_right.svg")
            :icons/flash-active        (get-image "./resources/icons/flash_active.svg")
            :icons/flash-inactive      (get-image "./resources/icons/flash_inactive.svg")
            :icons/attach              (get-image "./resources/icons/attach.svg")
            :icons/browse              (get-image "./resources/icons/browse.svg")
            :icons/close               (get-image "./resources/icons/close.svg")
            :icons/copy-from           (get-image "./resources/icons/copy_from.svg")
            :icons/delete              (get-image "./resources/icons/delete.svg")
            :icons/dots-horizontal     (get-image "./resources/icons/dots_horizontal.svg")
            :icons/dots-vertical       (get-image "./resources/icons/dots_vertical.svg")
            :icons/exclamation-mark    (get-image "./resources/icons/exclamation_mark.svg")
            :icons/filter              (get-image "./resources/icons/filter.svg")
            :icons/fullscreen          (get-image "./resources/icons/fullscreen.svg")
            :icons/group-big           (get-image "./resources/icons/group_big.svg")
            :icons/group-chat          (get-image "./resources/icons/group_chat.svg")
            :icons/chats               (get-image "./resources/icons/chats.svg")
            :icons/hamburger           (get-image "./resources/icons/hamburger.svg")
            :icons/hidden              (get-image "./resources/icons/hidden.svg")
            :icons/in-contacts         (get-image "./resources/icons/in_contacts.svg")
            :icons/lock                (get-image "./resources/icons/lock.svg")
            :icons/mic                 (get-image "./resources/icons/mic.svg")
            :icons/ok                  (get-image "./resources/icons/ok.svg")
            :icons/public              (get-image "./resources/icons/public.svg")
            :icons/public-chat         (get-image "./resources/icons/public_chat.svg")
            :icons/qr                  (get-image "./resources/icons/QR.svg")
            :icons/input-commands      (get-image "./resources/icons/input_commands.svg")
            :icons/input-send          (get-image "./resources/icons/input_send.svg")
            :icons/back                (get-image "./resources/icons/back.svg")
            :icons/forward             (get-image "./resources/icons/forward.svg")
            :icons/dropdown-up         (get-image "./resources/icons/dropdown_up.svg")
            :icons/up                  (get-image "./resources/icons/up.svg")
            :icons/down                (get-image "./resources/icons/down.svg")
            :icons/grab                (get-image "./resources/icons/grab.svg")
            :icons/share               (get-image "./resources/icons/share.svg")
            :icons/tooltip-triangle    (get-image "./resources/icons/tooltip-triangle.svg")
            :icons/open                (get-image "./resources/icons/open.svg")
            :icons/network             (get-image "./resources/icons/network.svg")
            :icons/wnode               (get-image "./resources/icons/wnode.svg")
            :icons/refresh             (get-image "./resources/icons/refresh.svg")
            :icons/newchat             (get-image "./resources/icons/newchat.svg")
            :icons/logo                (get-image "./resources/icons/logo.svg")
            :icons/camera              (get-image "./resources/icons/camera.svg")
            :icons/check               (get-image "./resources/icons/check.svg")
            :icons/dots                (get-image "./resources/icons/dots.svg")
            :icons/warning             (get-image "./resources/icons/warning.svg")})

(defn normalize-property-name [n]
      (if (= n :icons/options)
        (if platform/ios? :icons/dots-horizontal :icons/dots-vertical)
        n))

(if platform/desktop?
  (do (def default-viewbox {:width 24 :height 24})

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
                                  :style  (merge icon-style style)}]]))))
  (do (defn icon
            ([name] (icon name nil))
            ([name {:keys [color container-style style accessibility-label width height]
                    :or   {accessibility-label :icon}}]
              ^{:key name}
              [react/view {:style               container-style
                           :accessibility-label accessibility-label}
               (if-let [icon-fn (get icons (normalize-property-name name))]
                       (let [icon-vec (icon-fn
                                        (cond
                                          (keyword? color)
                                          (case color
                                                :dark styles/icon-dark-color
                                                :gray styles/icon-gray-color
                                                :blue styles/color-light-blue
                                                :active styles/color-blue4
                                                :white styles/color-white
                                                :red styles/icon-red-color
                                                styles/icon-dark-color)
                                          (string? color)
                                          color
                                          :else
                                          styles/icon-dark-color))]
                            (if width
                              (update icon-vec 1 assoc :width width :height height)
                              icon-vec))
                       (throw (js/Error. (str "Unknown icon: " name))))]))))
