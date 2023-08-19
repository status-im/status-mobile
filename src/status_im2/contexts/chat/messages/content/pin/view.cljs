(ns status-im2.contexts.chat.messages.content.pin.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.chat.composer.reply.view :as reply]
            [status-im2.contexts.chat.messages.content.pin.style :as style]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

(defn pinned-by-view
  [pinned-by]
  (let [{:keys [public-key]} (rf/sub [:multiaccount/contact])
        contact-names        (rf/sub [:contacts/contact-two-names-by-identity pinned-by])
        author-name          (if (= pinned-by public-key)
                               (i18n/label :t/You)
                               (first contact-names))]
    [rn/view
     {:style               style/pin-indicator-container
      :accessibility-label :pinned-by}
     [quo/icon :i/pin
      {:color colors/primary-50
       :size  16}]
     [quo/text
      {:size   :label
       :weight :medium
       :style  style/pin-author-text}
      author-name]]))

(defn pinned-message
  [{:keys [from quoted-message timestamp-str]}]
  (let [display-name        (first (rf/sub [:contacts/contact-two-names-by-identity from]))
        customization-color (rf/sub [:profile/customization-color])]
    [quo/system-message
     {:type                :pinned
      :pinned-by           display-name
      :customization-color customization-color
      :child               [reply/quoted-message quoted-message false true]
      :timestamp           timestamp-str}]))
