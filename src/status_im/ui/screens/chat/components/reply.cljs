(ns status-im.ui.screens.chat.components.reply
  (:require [clojure.string :as string]
            [quo.components.animated.pressable :as pressable]
            [quo.core :as quo]
            [quo.design-system.colors :as quo.colors]
            [quo.react :as quo.react]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.ethereum.stateofus :as stateofus]
            [i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.chat.components.style :as styles]))

(def ^:private reply-symbol "↪ ")

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
    (i18n/label :replying-to {:author author})))

(defn format-reply-author
  [from username current-public-key]
  (or (and (= from current-public-key)
           (str reply-symbol (i18n/label :t/You)))
      (str reply-symbol (format-author username))))

(defn get-quoted-text-with-mentions
  [parsed-text]
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

(defn reply-message
  [{:keys [from]}]
  (let [contact-name       @(re-frame/subscribe [:contacts/contact-name-by-identity from])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    [rn/view {:style {:flex-direction :row}}
     [rn/view {:style (styles/reply-content)}
      [quo/text
       {:weight          :medium
        :number-of-lines 1
        :style           {:line-height 18}}
       (format-reply-author from contact-name current-public-key)]]
     [rn/view
      [pressable/pressable
       {:on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
        :accessibility-label :cancel-message-reply}
       [icons/icon :main-icons/close-circle
        {:container-style (styles/close-button)
         :color           (:icon-02 @quo.colors/theme)}]]]]))

(defn send-image
  [images]
  [rn/view {:style (styles/reply-container-image)}
   [rn/scroll-view
    {:horizontal true
     :style      (styles/reply-content)}
    (for [{:keys [uri]} (vals images)]
      ^{:key uri}
      [rn/image
       {:source {:uri uri}
        :style  {:width         56
                 :height        56
                 :border-radius 4
                 :margin-right  4}}])]
   [rn/view
    [pressable/pressable
     {:on-press            #(re-frame/dispatch [:chat.ui/cancel-sending-image])
      :accessibility-label :cancel-send-image}
     [icons/icon :main-icons/close-circle
      {:container-style (styles/close-button)
       :color           quo.colors/white}]]]])

(defn focus-input-on-reply
  [reply had-reply text-input-ref]
  ;;when we show reply we focus input
  (when-not (= reply @had-reply)
    (reset! had-reply reply)
    (when reply
      ;; A setTimeout of 0 is necessary to ensure the statement is enqueued and will get executed ASAP.
      (js/setTimeout #(input-focus text-input-ref) 0))))

(defn reply-message-wrapper
  [reply]
  [rn/view
   {:style {:padding-horizontal 15
            :border-top-width   1
            :border-top-color   (:ui-01 @quo.colors/theme)
            :padding-vertical   8}}
   [reply-message reply]])

(defn reply-message-auto-focus-wrapper
  [text-input-ref]
  (let [had-reply (atom nil)]
    (fn []
      (let [reply @(re-frame/subscribe [:chats/reply-message])]
        (focus-input-on-reply reply had-reply text-input-ref)
        (when reply
          [reply-message-wrapper reply])))))
