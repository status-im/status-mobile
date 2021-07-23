(ns status-im.ui.screens.chat.components.contact-request
  (:require [quo.core :as quo]
            [quo.react :as quo.react]
            [quo.react-native :as rn]
            [quo.design-system.colors :as quo.colors]
            [status-im.i18n.i18n :as i18n]
            [quo.components.animated.pressable :as pressable]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.chat.components.style :as styles]
            [re-frame.core :as re-frame]
            [clojure.string :as string]))

(def ^:private contact-request-symbol "↪ ")

(defn input-focus [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref) .focus))

(defn format-author [contact-name]
  (let [author (if (or (= (aget contact-name 0) "@")
                       ;; in case of replies
                       (= (aget contact-name 1) "@"))
                 (or (stateofus/username contact-name)
                     (subs contact-name 0 81))
                 contact-name)]
    (i18n/label :contact-requesting-to {:author author})))

(defn format-contact-request-author [from username current-public-key]
  (or (and (= from current-public-key)
           (str contact-request-symbol (i18n/label :t/You)))
      (str contact-request-symbol (format-author username))))

(defn get-quoted-text-with-mentions [parsed-text]
  (string/join
   (mapv (fn [{:keys [type literal children]}]
           (cond
             (= type "paragraph")
             (get-quoted-text-with-mentions children)

             (= type "mention")
             @(re-frame/subscribe [:contacts/contact-name-by-identity literal])

             (seq children)
             (get-quoted-text-with-mentions children)

             :else
             literal))
         parsed-text)))

(defn contact-request-message [{:keys [from]}]
  (let [contact-name       @(re-frame/subscribe [:contacts/contact-name-by-identity from])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    [rn/view {:style {:flex-direction :row}}
     [rn/view {:style (styles/contact-request-content)}
      [quo/button {:type     :secondary
                   :weight          :medium
                   :number-of-lines 1
                   :style           {:line-height 18}}
       "Cancel"]
      [quo/button {:type     :secondary
                   :weight          :medium
                   :after :main-icons/send
                   :style           {:line-height 18}}
       "Send request"]]]))

(defn focus-input-on-contact-request [contact-request had-contact-request text-input-ref]
  ;;when we show contact-request we focus input
  (when-not (= contact-request @had-contact-request)
    (reset! had-contact-request contact-request)
    (when contact-request
      (js/setTimeout #(input-focus text-input-ref) 250))))

(defn contact-request-message-wrapper [contact-request]
  [rn/view {:style {:padding-horizontal 15
                    :border-top-width 1
                    :border-top-color (:ui-01 @quo.colors/theme)
                    :padding-vertical 8}}
   [contact-request-message contact-request]])

(defn contact-request-message-auto-focus-wrapper [text-input-ref]
  (let [had-reply (atom nil)]
    (fn []
      (let [contact-request @(re-frame/subscribe [:chats/sending-contact-request])]
        (focus-input-on-contact-request contact-request had-reply text-input-ref)
        (when contact-request
          [contact-request-message-wrapper contact-request])))))
