(ns status-im.ui.screens.chat.components.contact-request
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as quo.colors]
            [quo.react :as quo.react]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.ethereum.stateofus :as stateofus]
            [utils.i18n :as i18n]
            [status-im.ui.screens.chat.components.style :as styles])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(def ^:private contact-request-symbol "↪ ")

(defn input-focus
  [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref)
          .focus))

(defn format-author
  [contact-name]
  (let [author (if (or (= (aget contact-name 0) "@")
                       ;; in case of replies
                       (= (aget contact-name 1) "@"))
                 (or (stateofus/username contact-name)
                     (subs contact-name 0 81))
                 contact-name)]
    (i18n/label :contact-requesting-to {:author author})))

(defn format-contact-request-author
  [from username current-public-key]
  (or (and (= from current-public-key)
           (str contact-request-symbol (i18n/label :t/You)))
      (str contact-request-symbol (format-author username))))

(defn get-quoted-text-with-mentions
  [parsed-text]
  (string/join
   (mapv (fn [{:keys [type literal children]}]
           (cond
             (= type "paragraph")
             (get-quoted-text-with-mentions children)

             (= type "mention")
             @(re-frame/subscribe [:messages/resolve-mention literal])

             (seq children)
             (get-quoted-text-with-mentions children)

             :else
             literal))
         parsed-text)))

(defn contact-request-message
  [their-public-key]
  (let [{:keys [input-text]} @(re-frame/subscribe [:chats/current-chat-input])]
    [rn/view {:style {:flex-direction :row}}
     [rn/view {:style (styles/contact-request-content)}
      [quo/button
       {:type            :secondary
        :weight          :medium
        :number-of-lines 1
        :style           {:line-height 18}
        :on-press        #(re-frame/dispatch [:chat.ui/cancel-contact-request])}
       (i18n/label :t/cancel)]
      [quo/button
       {:type     :secondary
        :disabled (string/blank? input-text)
        :weight   :medium
        :after    :main-icons/send
        :on-press #(re-frame/dispatch [:contacts/send-contact-request their-public-key input-text])
        :style    {:line-height 18}}
       (i18n/label :t/send-request)]]]))

(defn focus-input-on-contact-request
  [contact-request had-contact-request text-input-ref]
  ;;when we show contact-request we focus input
  (when-not (= contact-request @had-contact-request)
    (reset! had-contact-request contact-request)
    (when contact-request
      (js/setTimeout #(input-focus text-input-ref) 250))))

(defn contact-request-message-wrapper
  [contact-request]
  [rn/view
   {:style {:padding-horizontal 15
            :border-top-width   1
            :border-top-color   (:ui-01 @quo.colors/theme)
            :padding-vertical   8}}
   [contact-request-message contact-request]])

(defview contact-request-message-auto-focus-wrapper
  [text-input-ref]
  (letsubs [had-reply       (atom nil)
            contact-request @(re-frame/subscribe [:chats/sending-contact-request])]
    {:component-did-mount #(focus-input-on-contact-request contact-request had-reply text-input-ref)}
    (when contact-request
      [contact-request-message-wrapper contact-request])))
