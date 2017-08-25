(ns status-im.components.icons.vector-icons
  (:require-macros [status-im.utils.slurp :refer [slurp-svg]])
  (:require [reagent.core :as r]
            [status-im.utils.platform :refer [ios?]]
            [status-im.components.styles :as common]
            [status-im.components.react :as rn]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn get-property [name]
  (aget rn-dependencies/svg name))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-property name)))

(def svg (get-class "Svg"))
(def g (get-class "G"))
(def rect (get-class "Rect"))
(def path (get-class "Path"))
(def use-def (get-class "Use"))
(def defs (get-class "Defs"))

(def icons {:icons/chats               (slurp-svg "./resources/icons/bottom/chats_gray.svg")
            :icons/chats_active        (slurp-svg "./resources/icons/bottom/chats_active.svg")
            :icons/contacts            (slurp-svg "./resources/icons/bottom/contacts_gray.svg")
            :icons/contacts_active     (slurp-svg "./resources/icons/bottom/contacts_active.svg")
            :icons/discover            (slurp-svg "./resources/icons/bottom/discover_gray.svg")
            :icons/discover_active     (slurp-svg "./resources/icons/bottom/discover_active.svg")
            :icons/wallet              (slurp-svg "./resources/icons/bottom/wallet_gray.svg")
            :icons/wallet_active       (slurp-svg "./resources/icons/bottom/wallet_active.svg")
            :icons/speaker             (slurp-svg "./resources/icons/speaker.svg")
            :icons/speaker_off         (slurp-svg "./resources/icons/speaker_off.svg")
            :icons/transaction_history (slurp-svg "./resources/icons/transaction_history.svg")
            :icons/add                 (slurp-svg "./resources/icons/add.svg")
            :icons/add_wallet          (slurp-svg "./resources/icons/add_wallet.svg")
            :icons/address             (slurp-svg "./resources/icons/address.svg")
            :icons/arrow_left          (slurp-svg "./resources/icons/arrow_left.svg")
            :icons/arrow_right         (slurp-svg "./resources/icons/arrow_right.svg")
            :icons/attach              (slurp-svg "./resources/icons/attach.svg")
            :icons/back                (slurp-svg "./resources/icons/back.svg")
            :icons/browse              (slurp-svg "./resources/icons/browse.svg")
            :icons/close               (slurp-svg "./resources/icons/close.svg")
            :icons/dots_horizontal     (slurp-svg "./resources/icons/dots_horizontal.svg")
            :icons/dots_vertical       (slurp-svg "./resources/icons/dots_vertical.svg")
            :icons/forward             (slurp-svg "./resources/icons/forward.svg")
            :icons/fullscreen          (slurp-svg "./resources/icons/fullscreen.svg")
            :icons/group_big           (slurp-svg "./resources/icons/group_big.svg")
            :icons/group_chat          (slurp-svg "./resources/icons/group_chat.svg")
            :icons/hamburger           (slurp-svg "./resources/icons/hamburger.svg")
            :icons/hidden              (slurp-svg "./resources/icons/hidden.svg")
            :icons/mic                 (slurp-svg "./resources/icons/mic.svg")
            :icons/ok                  (slurp-svg "./resources/icons/ok.svg")
            :icons/public              (slurp-svg "./resources/icons/public.svg")
            :icons/public_chat         (slurp-svg "./resources/icons/public_chat.svg")
            :icons/qr                  (slurp-svg "./resources/icons/QR.svg")
            :icons/search              (slurp-svg "./resources/icons/search.svg")
            :icons/smile               (slurp-svg "./resources/icons/smile.svg")
            :icons/commands_list       (slurp-svg "./resources/icons/commands_list.svg")
            :icons/dropdown_up         (slurp-svg "./resources/icons/dropdown_up.svg")
            :icons/dropdown            (slurp-svg "./resources/icons/dropdown.svg")
            :icons/grab                (slurp-svg "./resources/icons/grab.svg")})

(defn check-name [n]
  (if (= n :icons/options)
    (if ios? :icons/dots_horizontal :icons/dots_vertical)
    n))

(defn icon [name & [opts]]
  (let [{:keys [color container-style style]} opts]
    [rn/view container-style
     (into []
       (concat
         [svg (merge {:width 24 :height 24 :viewBox "0 0 24 24"} style)]
         ((get icons (check-name name))
          (case color
            :dark common/icon-dark-color
            :gray common/icon-gray-color
            :blue common/color-light-blue
            :active common/color-blue4
            :white common/color-white
            :red common/icon-red-color
            common/icon-dark-color))))]))

(defn touchable-icon [n opts handler]
  [rn/touchable-highlight {:on-press handler}
   [rn/view
    [icon n opts]]])