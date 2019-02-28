(ns status-im.ui.components.icons.vector-icons
  (:require-macros [status-im.ui.components.svg :as components.svg])
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.styles :as styles]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.components.colors :as colors])
  (:refer-clojure :exclude [use]))

(when-not platform/desktop?
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
  (def circle (get-class "Circle")))

(def icons
  (if platform/desktop?
    {;; Main icons
     :main-icons/add               (js/require "./resources/icons/main/add.svg")
     :main-icons/add-contact       (js/require "./resources/icons/main/add_contact.svg")
     :main-icons/address           (js/require "./resources/icons/main/address.svg")
     :main-icons/advanced-settings (js/require "./resources/icons/main/advanced_settings.svg")
     :main-icons/arrow-left        (js/require "./resources/icons/main/arrow_left.svg")
     :main-icons/arrow-right       (js/require "./resources/icons/main/arrow_right.svg")
     :main-icons/arrow-up          (js/require "./resources/icons/main/arrow_up.svg")
     :main-icons/back              (js/require "./resources/icons/main/back.svg")
     :main-icons/backspace         (js/require "./resources/icons/main/backspace.svg")
     :main-icons/browser           (js/require "./resources/icons/main/browser.svg")
     :main-icons/browser-home      (js/require "./resources/icons/main/browser_home.svg")
     :main-icons/camera            (js/require "./resources/icons/main/camera.svg")
     :main-icons/cancel            (js/require "./resources/icons/main/cancel.svg")
     :main-icons/change            (js/require "./resources/icons/main/change.svg")
     :main-icons/check             (js/require "./resources/icons/main/check.svg")
     :main-icons/close             (js/require "./resources/icons/main/close.svg")
     :main-icons/commands          (js/require "./resources/icons/main/commands.svg")
     :main-icons/copy              (js/require "./resources/icons/main/copy.svg")
     :main-icons/dapp              (js/require "./resources/icons/main/dapp.svg")
     :main-icons/delete            (js/require "./resources/icons/main/delete.svg")
     :main-icons/desktop           (js/require "./resources/icons/main/desktop.svg")
     :main-icons/dropdown          (js/require "./resources/icons/main/dropdown.svg")
     :main-icons/dropdown-up       (js/require "./resources/icons/main/dropdown_up.svg")
     :main-icons/edit              (js/require "./resources/icons/main/edit.svg")
     :main-icons/filter            (js/require "./resources/icons/main/filter.svg")
     :main-icons/flash-active      (js/require "./resources/icons/main/flash_active.svg")
     :main-icons/flash-inactive    (js/require "./resources/icons/main/flash_inactive.svg")
     :main-icons/fleet             (js/require "./resources/icons/main/mailserver.svg")
     :main-icons/group-chat        (js/require "./resources/icons/main/group_chat.svg")
     :main-icons/help              (js/require "./resources/icons/main/help.svg")
     :main-icons/history           (js/require "./resources/icons/main/history.svg")
     :main-icons/home              (js/require "./resources/icons/main/home.svg")
     :main-icons/in-contacts       (js/require "./resources/icons/main/in_contacts.svg")
     :main-icons/info              (js/require "./resources/icons/main/info.svg")
     :main-icons/keycard           (js/require "./resources/icons/main/keycard.svg")
     :main-icons/language          (js/require "./resources/icons/main/language.svg")
     :main-icons/link              (js/require "./resources/icons/main/link.svg")
     :main-icons/log-level         (js/require "./resources/icons/main/mailserver.svg")
     :main-icons/logout            (js/require "./resources/icons/main/logout.svg")
     :main-icons/mailserver        (js/require "./resources/icons/main/mailserver.svg")
     :main-icons/make_admin        (js/require "./resources/icons/main/make_admin.svg")
     :main-icons/max               (js/require "./resources/icons/main/max.svg")
     :main-icons/message           (js/require "./resources/icons/main/message.svg")
     :main-icons/mobile            (js/require "./resources/icons/main/mobile.svg")
     :main-icons/more              (js/require "./resources/icons/main/more.svg")
     :main-icons/network           (js/require "./resources/icons/main/network.svg")
     :main-icons/next              (js/require "./resources/icons/main/next.svg")
     :main-icons/notification      (js/require "./resources/icons/main/notification.svg")
     :main-icons/paste             (js/require "./resources/icons/main/paste.svg")
     :main-icons/password          (js/require "./resources/icons/main/password.svg")
     :main-icons/photo             (js/require "./resources/icons/main/photo.svg")
     :main-icons/private-chat      (js/require "./resources/icons/main/private_chat.svg")
     :main-icons/profile           (js/require "./resources/icons/main/profile.svg")
     :main-icons/public-chat       (js/require "./resources/icons/main/public_chat.svg")
     :main-icons/qr                (js/require "./resources/icons/main/qr.svg")
     :main-icons/receive           (js/require "./resources/icons/main/receive.svg")
     :main-icons/refresh           (js/require "./resources/icons/main/refresh.svg")
     :main-icons/remove-contact    (js/require "./resources/icons/main/remove_contact.svg")
     :main-icons/reply             (js/require "./resources/icons/main/reply.svg")
     :main-icons/search            (js/require "./resources/icons/main/search.svg")
     :main-icons/security          (js/require "./resources/icons/main/security.svg")
     :main-icons/send              (js/require "./resources/icons/main/send.svg")
     :main-icons/settings          (js/require "./resources/icons/main/settings.svg")
     :main-icons/share             (js/require "./resources/icons/main/share.svg")
     :main-icons/stickers          (js/require "./resources/icons/main/stickers.svg")
     :main-icons/text              (js/require "./resources/icons/main/text.svg")
     :main-icons/tribute-to-talk   (js/require "./resources/icons/main/tribute_to_talk.svg")
     :main-icons/two-arrows        (js/require "./resources/icons/main/two_arrows.svg")
     :main-icons/user-profile      (js/require "./resources/icons/main/user_profile.svg")
     :main-icons/username          (js/require "./resources/icons/main/username.svg")
     :main-icons/wallet            (js/require "./resources/icons/main/wallet.svg")
     :main-icons/warning           (js/require "./resources/icons/main/warning.svg")
     :main-icons/wnode             (js/require "./resources/icons/main/mailserver.svg")
     ;; Tiny icons
     :tiny-icons/tiny-arrow-down   (js/require "./resources/icons/tiny/tiny_arrow_down.svg")
     :tiny-icons/tiny-check        (js/require "./resources/icons/tiny/tiny_check.svg")
     :tiny-icons/tiny-external     (js/require "./resources/icons/tiny/tiny_external.svg")
     :tiny-icons/tiny-group        (js/require "./resources/icons/tiny/tiny_group.svg")
     :tiny-icons/tiny-lock         (js/require "./resources/icons/tiny/tiny_lock.svg")
     :tiny-icons/tiny-lock-broken  (js/require "./resources/icons/tiny/tiny_lock_broken.svg")
     :tiny-icons/tiny-new-contact  (js/require "./resources/icons/tiny/tiny_new_contact.svg")
     :tiny-icons/tiny-pending      (js/require "./resources/icons/tiny/tiny_pending.svg")
     :tiny-icons/tiny-public       (js/require "./resources/icons/tiny/tiny_public.svg")
     :tiny-icons/tiny-reply        (js/require "./resources/icons/tiny/tiny_reply.svg")
     :tiny-icons/tiny-settings     (js/require "./resources/icons/tiny/tiny_settings.svg")
     :tiny-icons/tribute-to-talk   (js/require "./resources/icons/tiny/tribute_to_talk.svg")
     ;; Stickers icons
     :sticker-icons/stickers-big   (js/require "./resources/icons/stickers/stickers_big.svg")
     :sticker-icons/recent         (js/require "./resources/icons/stickers/recent.svg")
     ;; Old icons
     :icons/tooltip-triangle       (js/require "./resources/icons/tooltip-triangle.svg")
     :icons/logo                   (js/require "./resources/icons/logo.svg")
     :icons/indicator-big          (js/require "./resources/icons/indicator-big.svg")
     :icons/indicator-middle       (js/require "./resources/icons/indicator-middle.svg")
     :icons/indicator-small        (js/require "./resources/icons/indicator-small.svg")}

    ;; Mobile icons
    {;; Main icons
     :main-icons/add               (components.svg/slurp-svg "./resources/icons/main/add.svg")
     :main-icons/add-contact       (components.svg/slurp-svg "./resources/icons/main/add_contact.svg")
     :main-icons/address           (components.svg/slurp-svg "./resources/icons/main/address.svg")
     :main-icons/advanced-settings (components.svg/slurp-svg "./resources/icons/main/advanced_settings.svg")
     :main-icons/arrow-left        (components.svg/slurp-svg "./resources/icons/main/arrow_left.svg")
     :main-icons/arrow-right       (components.svg/slurp-svg "./resources/icons/main/arrow_right.svg")
     :main-icons/arrow-up          (components.svg/slurp-svg "./resources/icons/main/arrow_up.svg")
     :main-icons/back              (components.svg/slurp-svg "./resources/icons/main/back.svg")
     :main-icons/backspace         (components.svg/slurp-svg "./resources/icons/main/backspace.svg")
     :main-icons/browser           (components.svg/slurp-svg "./resources/icons/main/browser.svg")
     :main-icons/browser-home      (components.svg/slurp-svg "./resources/icons/main/browser_home.svg")
     :main-icons/camera            (components.svg/slurp-svg "./resources/icons/main/camera.svg")
     :main-icons/cancel            (components.svg/slurp-svg "./resources/icons/main/cancel.svg")
     :main-icons/change            (components.svg/slurp-svg "./resources/icons/main/change.svg")
     :main-icons/check             (components.svg/slurp-svg "./resources/icons/main/check.svg")
     :main-icons/close             (components.svg/slurp-svg "./resources/icons/main/close.svg")
     :main-icons/commands          (components.svg/slurp-svg "./resources/icons/main/commands.svg")
     :main-icons/copy              (components.svg/slurp-svg "./resources/icons/main/copy.svg")
     :main-icons/dapp              (components.svg/slurp-svg "./resources/icons/main/dapp.svg")
     :main-icons/delete            (components.svg/slurp-svg "./resources/icons/main/delete.svg")
     :main-icons/desktop           (components.svg/slurp-svg "./resources/icons/main/desktop.svg")
     :main-icons/dropdown          (components.svg/slurp-svg "./resources/icons/main/dropdown.svg")
     :main-icons/dropdown-up       (components.svg/slurp-svg "./resources/icons/main/dropdown_up.svg")
     :main-icons/edit              (components.svg/slurp-svg "./resources/icons/main/edit.svg")
     :main-icons/filter            (components.svg/slurp-svg "./resources/icons/main/filter.svg")
     :main-icons/flash-active      (components.svg/slurp-svg "./resources/icons/main/flash_active.svg")
     :main-icons/flash-inactive    (components.svg/slurp-svg "./resources/icons/main/flash_inactive.svg")
     :main-icons/fleet             (components.svg/slurp-svg "./resources/icons/main/mailserver.svg")
     :main-icons/group-chat        (components.svg/slurp-svg "./resources/icons/main/group_chat.svg")
     :main-icons/help              (components.svg/slurp-svg "./resources/icons/main/help.svg")
     :main-icons/history           (components.svg/slurp-svg "./resources/icons/main/history.svg")
     :main-icons/home              (components.svg/slurp-svg "./resources/icons/main/home.svg")
     :main-icons/in-contacts       (components.svg/slurp-svg "./resources/icons/main/in_contacts.svg")
     :main-icons/info              (components.svg/slurp-svg "./resources/icons/main/info.svg")
     :main-icons/keycard           (components.svg/slurp-svg "./resources/icons/main/keycard.svg")
     :main-icons/language          (components.svg/slurp-svg "./resources/icons/main/language.svg")
     :main-icons/link              (components.svg/slurp-svg "./resources/icons/main/link.svg")
     :main-icons/log-level         (components.svg/slurp-svg "./resources/icons/main/mailserver.svg")
     :main-icons/logout            (components.svg/slurp-svg "./resources/icons/main/logout.svg")
     :main-icons/mailserver        (components.svg/slurp-svg "./resources/icons/main/mailserver.svg")
     :main-icons/make-admin        (components.svg/slurp-svg "./resources/icons/main/make_admin.svg")
     :main-icons/max               (components.svg/slurp-svg "./resources/icons/main/max.svg")
     :main-icons/message           (components.svg/slurp-svg "./resources/icons/main/message.svg")
     :main-icons/mobile            (components.svg/slurp-svg "./resources/icons/main/mobile.svg")
     :main-icons/more              (components.svg/slurp-svg "./resources/icons/main/more.svg")
     :main-icons/network           (components.svg/slurp-svg "./resources/icons/main/network.svg")
     :main-icons/next              (components.svg/slurp-svg "./resources/icons/main/next.svg")
     :main-icons/notification      (components.svg/slurp-svg "./resources/icons/main/notification.svg")
     :main-icons/password          (components.svg/slurp-svg "./resources/icons/main/password.svg")
     :main-icons/paste             (components.svg/slurp-svg "./resources/icons/main/paste.svg")
     :main-icons/photo             (components.svg/slurp-svg "./resources/icons/main/photo.svg")
     :main-icons/private-chat      (components.svg/slurp-svg "./resources/icons/main/private_chat.svg")
     :main-icons/profile           (components.svg/slurp-svg "./resources/icons/main/profile.svg")
     :main-icons/public-chat       (components.svg/slurp-svg "./resources/icons/main/public_chat.svg")
     :main-icons/qr                (components.svg/slurp-svg "./resources/icons/main/qr.svg")
     :main-icons/receive           (components.svg/slurp-svg "./resources/icons/main/receive.svg")
     :main-icons/refresh           (components.svg/slurp-svg "./resources/icons/main/refresh.svg")
     :main-icons/remove_contact    (components.svg/slurp-svg "./resources/icons/main/remove_contact.svg")
     :main-icons/reply             (components.svg/slurp-svg "./resources/icons/main/reply.svg")
     :main-icons/search            (components.svg/slurp-svg "./resources/icons/main/search.svg")
     :main-icons/security          (components.svg/slurp-svg "./resources/icons/main/security.svg")
     :main-icons/send              (components.svg/slurp-svg "./resources/icons/main/send.svg")
     :main-icons/settings          (components.svg/slurp-svg "./resources/icons/main/settings.svg")
     :main-icons/share             (components.svg/slurp-svg "./resources/icons/main/share.svg")
     :main-icons/stickers          (components.svg/slurp-svg "./resources/icons/main/stickers.svg")
     :main-icons/text              (components.svg/slurp-svg "./resources/icons/main/text.svg")
     :main-icons/tribute-to-talk   (components.svg/slurp-svg "./resources/icons/main/tribute_to_talk.svg")
     :main-icons/two-arrows        (components.svg/slurp-svg "./resources/icons/main/two_arrows.svg")
     :main-icons/user-profile      (components.svg/slurp-svg "./resources/icons/main/user_profile.svg")
     :main-icons/username          (components.svg/slurp-svg "./resources/icons/main/username.svg")
     :main-icons/wallet            (components.svg/slurp-svg "./resources/icons/main/wallet.svg")
     :main-icons/warning           (components.svg/slurp-svg "./resources/icons/main/warning.svg")
     :main-icons/wnode             (components.svg/slurp-svg "./resources/icons/main/mailserver.svg")
     ;; Tiny icons
     :tiny-icons/tiny-arrow-down   (components.svg/slurp-svg "./resources/icons/tiny/tiny_arrow_down.svg")
     :tiny-icons/tiny-check        (components.svg/slurp-svg "./resources/icons/tiny/tiny_check.svg")
     :tiny-icons/tiny-external     (components.svg/slurp-svg "./resources/icons/tiny/tiny_external.svg")
     :tiny-icons/tiny-group        (components.svg/slurp-svg "./resources/icons/tiny/tiny_group.svg")
     :tiny-icons/tiny-lock         (components.svg/slurp-svg "./resources/icons/tiny/tiny_lock.svg")
     :tiny-icons/tiny-lock-broken  (components.svg/slurp-svg "./resources/icons/tiny/tiny_lock_broken.svg")
     :tiny-icons/tiny-new-contact  (components.svg/slurp-svg "./resources/icons/tiny/tiny_new_contact.svg")
     :tiny-icons/tiny-pending      (components.svg/slurp-svg "./resources/icons/tiny/tiny_pending.svg")
     :tiny-icons/tiny-public       (components.svg/slurp-svg "./resources/icons/tiny/tiny_public.svg")
     :tiny-icons/tiny-reply        (components.svg/slurp-svg "./resources/icons/tiny/tiny_reply.svg")
     :tiny-icons/tiny-settings     (components.svg/slurp-svg "./resources/icons/tiny/tiny_settings.svg")
     :tiny-icons/tribute-to-talk   (components.svg/slurp-svg "./resources/icons/tiny/tribute_to_talk.svg")
     ;; Stickers icons
     :stickers-icons/stickers-big  (components.svg/slurp-svg "./resources/icons/stickers/stickers_big.svg")
     :stickers-icons/recent        (components.svg/slurp-svg "./resources/icons/stickers/recent.svg")
     ;; Old icons
     :icons/tooltip-triangle       (components.svg/slurp-svg "./resources/icons/tooltip-triangle.svg")
     :icons/logo                   (components.svg/slurp-svg "./resources/icons/logo.svg")
     :icons/indicator-big          (components.svg/slurp-svg "./resources/icons/indicator-big.svg")
     :icons/indicator-middle       (components.svg/slurp-svg "./resources/icons/indicator-middle.svg")
     :icons/indicator-small        (components.svg/slurp-svg "./resources/icons/indicator-small.svg")}))

(defn- match-color [color]
  (cond
    (keyword? color)
    (case color
      :dark colors/black
      :gray colors/gray
      :blue colors/blue
      :active colors/blue
      :white colors/white
      :red colors/red
      colors/black)
    (string? color)
    color
    :else
    colors/black))

(defn desktop-icon
  [name {:keys [color container-style style accessibility-label width height]
         :or   {accessibility-label :icon}}]
  (let [default-viewbox {:width 24 :height 24}
        icon-style (if width
                     (assoc default-viewbox :width width :height height)
                     default-viewbox)
        color-style {:tint-color (match-color color)}]
    [react/view {:style               container-style
                 :accessibility-label accessibility-label}
     [react/image {:source (get icons name)
                   :style  (merge icon-style color-style style)}]]))

(defn mobile-icon
  [name {:keys [color container-style accessibility-label width height]
         :or   {accessibility-label :icon}}]
  ^{:key name}
  [react/animated-view {:style               container-style
                        :accessibility-label accessibility-label}
   (if-let [icon-fn (get icons name)]
     (let [icon-vec (icon-fn (match-color color))]
       (if width
         (update icon-vec 1 assoc :width width :height height)
         icon-vec))
     (throw (js/Error. (str "Unknown icon: " name))))])

(defn icon
  ([name] (icon name nil))
  ([name options]
   (let [icon-fn (if platform/desktop? desktop-icon mobile-icon)]
     [icon-fn name options])))

