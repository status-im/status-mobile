(ns status-im.ui.screens.chat.components.reply
  (:require [quo.core :as quo]
            [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [quo.design-system.colors :as colors]
            [quo.components.animated.pressable :as pressable]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.chat.components.style :as styles]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [clojure.string :as string]))

(def ^:private reply-symbol "↪ ")

(defn format-author [contact-name]
  (if (or (= (aget contact-name 0) "@")
          ;; in case of replies
          (= (aget contact-name 1) "@"))
    (or (stateofus/username contact-name)
        (subs contact-name 0 81))
    contact-name))

(defn format-reply-author [from username current-public-key]
  (or (and (= from current-public-key)
           (str reply-symbol (i18n/label :t/You)))
      (format-author (str reply-symbol username))))

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

(defn reply-message [{:keys [from content]}]
  (let [contact-name       @(re-frame/subscribe [:contacts/contact-name-by-identity from])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])
        {:keys [image parsed-text]} content]
    [rn/view {:style (styles/reply-container false)}
     [rn/view {:style (styles/reply-content)}
      [quo/text {:weight          :medium
                 :number-of-lines 1
                 :style           {:line-height 18}
                 :size            :small}
       (format-reply-author from contact-name current-public-key)]
      (if image
        [react/image {:style  {:width            56
                               :height           56
                               :background-color :black
                               :margin-top       2
                               :border-radius    4}
                      :source {:uri image}}]
        [quo/text {:size            :small
                   :number-of-lines 1
                   :style           {:line-height 18}}
         (get-quoted-text-with-mentions parsed-text)])]
     [rn/view
      [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
                            :accessibility-label :cancel-message-reply}
       [icons/icon :main-icons/close-circle {:container-style (styles/close-button)
                                             :color           (:icon-01 @colors/theme)}]]]]))

(defn send-image [images]
  [rn/view {:style (styles/reply-container true)}
   [rn/scroll-view {:horizontal true
                    :style      (styles/reply-content)}
    (for [{:keys [uri]} (vals images)]
      ^{:key uri}
      [rn/image {:source {:uri uri}
                 :style  {:width         56
                          :height        56
                          :border-radius 4
                          :margin-right  4}}])]
   [rn/view
    [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/cancel-sending-image])
                          :accessibility-label :cancel-send-image}
     [icons/icon :main-icons/close-circle {:container-style (styles/close-button)
                                           :color           colors/white}]]]])
