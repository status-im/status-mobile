(ns status-im.ui.components.icons.vector-icons
  (:require-macros [status-im.utils.slurp :as slurp])
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.react-native.js-dependencies :as js-dependencies])
  (:refer-clojure :exclude [use]))

(defn get-property [name]
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

(def icons {:icons/discover            (slurp/slurp-svg "./resources/icons/bottom/discover_gray.svg")
            :icons/contacts            (slurp/slurp-svg "./resources/icons/bottom/contacts_gray.svg")
            :icons/home                (slurp/slurp-svg "./resources/icons/bottom/home_gray.svg")
            :icons/home-active         (slurp/slurp-svg "./resources/icons/bottom/home_blue.svg")
            :icons/profile             (slurp/slurp-svg "./resources/icons/bottom/profile_gray.svg")
            :icons/profile-active      (slurp/slurp-svg "./resources/icons/bottom/profile_blue.svg")
            :icons/wallet              (slurp/slurp-svg "./resources/icons/bottom/wallet_gray.svg")
            :icons/wallet-active       (slurp/slurp-svg "./resources/icons/bottom/wallet_active.svg")
            :icons/speaker             (slurp/slurp-svg "./resources/icons/speaker.svg")
            :icons/speaker-off         (slurp/slurp-svg "./resources/icons/speaker_off.svg")
            :icons/transaction-history (slurp/slurp-svg "./resources/icons/transaction_history.svg")
            :icons/add                 (slurp/slurp-svg "./resources/icons/add.svg")
            :icons/add-contact         (slurp/slurp-svg "./resources/icons/add_contact.svg")
            :icons/add-wallet          (slurp/slurp-svg "./resources/icons/add_wallet.svg")
            :icons/address             (slurp/slurp-svg "./resources/icons/address.svg")
            :icons/arrow-left          (slurp/slurp-svg "./resources/icons/arrow_left.svg")
            :icons/arrow-right         (slurp/slurp-svg "./resources/icons/arrow_right.svg")
            :icons/flash-active        (slurp/slurp-svg "./resources/icons/flash_active.svg")
            :icons/flash-inactive      (slurp/slurp-svg "./resources/icons/flash_inactive.svg")
            :icons/attach              (slurp/slurp-svg "./resources/icons/attach.svg")
            :icons/browse              (slurp/slurp-svg "./resources/icons/browse.svg")
            :icons/close               (slurp/slurp-svg "./resources/icons/close.svg")
            :icons/copy-from           (slurp/slurp-svg "./resources/icons/copy_from.svg")
            :icons/delete              (slurp/slurp-svg "./resources/icons/delete.svg")
            :icons/dots-horizontal     (slurp/slurp-svg "./resources/icons/dots_horizontal.svg")
            :icons/dots-vertical       (slurp/slurp-svg "./resources/icons/dots_vertical.svg")
            :icons/exclamation_mark    (slurp/slurp-svg "./resources/icons/exclamation_mark.svg")
            :icons/filter              (slurp/slurp-svg "./resources/icons/filter.svg")
            :icons/fullscreen          (slurp/slurp-svg "./resources/icons/fullscreen.svg")
            :icons/group-big           (slurp/slurp-svg "./resources/icons/group_big.svg")
            :icons/group-chat          (slurp/slurp-svg "./resources/icons/group_chat.svg")
            :icons/chats               (slurp/slurp-svg "./resources/icons/chats.svg")
            :icons/hamburger           (slurp/slurp-svg "./resources/icons/hamburger.svg")
            :icons/hidden              (slurp/slurp-svg "./resources/icons/hidden.svg")
            :icons/in-contacts         (slurp/slurp-svg "./resources/icons/in_contacts.svg")
            :icons/mic                 (slurp/slurp-svg "./resources/icons/mic.svg")
            :icons/ok                  (slurp/slurp-svg "./resources/icons/ok.svg")
            :icons/public              (slurp/slurp-svg "./resources/icons/public.svg")
            :icons/public-chat         (slurp/slurp-svg "./resources/icons/public_chat.svg")
            :icons/qr                  (slurp/slurp-svg "./resources/icons/QR.svg")
            :icons/input-commands      (slurp/slurp-svg "./resources/icons/input_commands.svg")
            :icons/input-send          (slurp/slurp-svg "./resources/icons/input_send.svg")
            :icons/back                (slurp/slurp-svg "./resources/icons/back.svg")
            :icons/forward             (slurp/slurp-svg "./resources/icons/forward.svg")
            :icons/dropdown-up         (slurp/slurp-svg "./resources/icons/dropdown_up.svg")
            :icons/up                  (slurp/slurp-svg "./resources/icons/up.svg")
            :icons/down                (slurp/slurp-svg "./resources/icons/down.svg")
            :icons/grab                (slurp/slurp-svg "./resources/icons/grab.svg")
            :icons/share               (slurp/slurp-svg "./resources/icons/share.svg")
            :icons/tooltip-triangle    (slurp/slurp-svg "./resources/icons/tooltip-triangle.svg")
            :icons/open                (slurp/slurp-svg "./resources/icons/open.svg")
            :icons/network             (slurp/slurp-svg "./resources/icons/network.svg")
            :icons/wnode               (slurp/slurp-svg "./resources/icons/wnode.svg")
            :icons/refresh             (slurp/slurp-svg "./resources/icons/refresh.svg")
            :icons/newchat             (slurp/slurp-svg "./resources/icons/newchat.svg")
            :icons/logo                (slurp/slurp-svg "./resources/icons/logo.svg")})

(defn normalize-property-name [n]
  (if (= n :icons/options)
    (if platform/ios? :icons/dots-horizontal :icons/dots-vertical)
    n))

(defn icon
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
      (throw (js/Error. (str "Unknown icon: " name))))]))
