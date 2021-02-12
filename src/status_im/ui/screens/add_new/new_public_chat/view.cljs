(ns status-im.ui.screens.add-new.new-public-chat.view
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.i18n.i18n-resources :as i18n-resources]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.ui.components.topbar :as topbar]
            [status-im.add-new.db :as db]
            [status-im.chat.models :as chat.models]
            [status-im.ui.components.icons.icons :as icons])
  (:require-macros [status-im.utils.views :as views]))

(defn- start-chat [topic]
  (re-frame/dispatch [:chat.ui/start-public-chat topic {:navigation-reset? true}])
  (re-frame/dispatch [:set :public-group-topic nil]))

(defn- hash-icon []
  [icons/icon :main-icons/channel {:color colors/gray}])

(defn- chat-name-input [topic error]
  [quo/text-input
   {:on-change-text      #(re-frame/dispatch [:set :public-group-topic %])
    :on-submit-editing   #(when (db/valid-topic? topic) (start-chat topic))
    :auto-capitalize     :none
    :auto-focus          false
    :accessibility-label :chat-name-input
    :before              {:component [hash-icon]}
    ;; Set default-value as otherwise it will
    ;; be erased in global `onWillBlur` handler
    :default-value       topic
    :placeholder         "chat-name"
    :return-key-type     :go
    :auto-correct        false
    :error               error}])

(defn render-topic [topic]
  ^{:key topic}
  [react/touchable-highlight {:on-press            #(start-chat topic)
                              :accessibility-label :chat-item}
   [react/view {:padding-right 8 :padding-vertical 8}
    [react/view {:border-color colors/gray-lighter :border-radius 36 :border-width 1 :padding-horizontal 8 :padding-vertical 5}
     [react/text {:style {:color colors/blue :typography :main-medium}} (str "#" topic)]]]])

(def lang-names {"es" "status-espanol"
                 "pt" "statusbrasil"
                 "de" "status-german"
                 "fr" "status-french"
                 "it" "status-italiano"
                 "ru" "status-russian"
                 "zh" "status-chinese"
                 "ko" "status-korean"
                 "ja" "status-japanese"
                 "fa" "status-farsi"
                 "tr" "status-turkish"
                 "id" "indonesian"
                 "in" "indonesian"
                 "hi" "status-indian"
                 "fil" "status-filipino"
                 "nl" "status-dutch"})

(defn get-language-topic []
  (let [lang (subs (name i18n-resources/default-device-language) 0 2)
        lang3 (subs (name i18n-resources/default-device-language) 0 3)
        lang-name (or (get lang-names lang3) (get lang-names lang))]
    (when-not (= lang "en")
      (or lang-name (str "status-" lang)))))

(def section-featured "Featured")

(defn featured-public-chats []
  (let [lang-topic (get-language-topic)
        chats (some #(when (= section-featured (first %)) (second %)) (chat.models/chats))]
    (if lang-topic
      (conj chats lang-topic)
      chats)))

(views/defview new-public-chat []
  (views/letsubs [topic [:public-group-topic]
                  error [:public-chat.new/topic-error-message]]
    [react/view {:style {:flex 1}}
     [topbar/topbar {:title  (i18n/label :t/new-public-group-chat)
                     :modal? true}]
     [react/scroll-view {:style {:flex 1}}
      [react/view {:padding-horizontal 16}
       [react/view {:align-items :center :padding-vertical 8}
        [react/image {:source (:new-chat-header resources/ui)
                      :style  {:width 160 :height 160}}]]
       [react/text {:style {:text-align :center :margin-bottom 16 :line-height 22}}
        (i18n/label :t/public-chat-description)]
       [chat-name-input topic error]]
      [react/view {:margin-top 32}
       (for [[section chats] (chat.models/chats)]
         ^{:key section}
         [react/view
          [react/view {:margin-right               16 :padding-left            16 :padding-vertical 3
                       :border-bottom-width        1  :border-bottom-color     colors/gray-lighter
                       :border-top-width           1  :border-top-color        colors/gray-lighter
                       :border-right-width         1  :border-right-color      colors/gray-lighter
                       :border-bottom-right-radius 14 :border-top-right-radius 14}
           [quo/text {:weight :medium} section]]
          [react/view {:flex-direction :row :flex-wrap :wrap :margin-vertical 8 :padding-horizontal 16}
           (let [lang-topic (get-language-topic)
                 chats      (if (and (= section-featured section) lang-topic)
                              (conj chats lang-topic)
                              chats)]
             (for [chat chats]
               (render-topic chat)))]])]]]))
