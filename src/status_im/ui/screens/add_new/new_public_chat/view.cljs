(ns status-im.ui.screens.add-new.new-public-chat.view
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.i18n-resources :as i18n-resources]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [status-im.ui.screens.add-new.new-public-chat.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(defn- start-chat [topic]
  (re-frame/dispatch [:chat.ui/start-public-chat topic {:navigation-reset? true}])
  (re-frame/dispatch [:set :public-group-topic nil]))

(defn- hash-icon []
  [quo/text {:color  :secondary
             :weight :medium
             :size   :x-large}
   "#"])

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
   [react/view {:padding-horizontal 4 :padding-vertical 8}
    [react/view {:border-color colors/gray-lighter :border-radius 36 :border-width 1 :padding-horizontal 8 :padding-vertical 5}
     [react/text {:style {:color colors/blue :typography :main-medium}} (str "#" topic)]]]])

(def lang-names {"zh" "chinese" "ja" "japanese" "ko" "korean" "ru" "russian" "es" "spanish" "fa" "farsi"})

(defn get-language-topic []
  (let [lang (subs (name i18n-resources/default-device-language) 0 2)
        lang-name (get lang-names lang)]
    (when-not (= lang "en")
      [(str "status-" (or lang-name lang))])))

(def default-public-chats
  (concat
   ["introductions" "chitchat" "status"]
   (get-language-topic)
   ["crypto" "tech" "music" "movies" "support"]))

(views/defview new-public-chat []
  (views/letsubs [topic [:public-group-topic]
                  error [:public-chat.new/topic-error-message]]
    [react/view {:style styles/group-container}
     [topbar/topbar {:title :t/new-public-group-chat :modal? true}]
     [react/scroll-view {:style {:flex 1}}
      [react/view {:padding-horizontal 16}
       [react/view {:align-items :center :padding-vertical 12}
        [react/image {:source (:new-chat-header resources/ui)
                      :style  {:width 160 :height 160}}]]
       [react/text {:style {:text-align :center :margin-bottom 32 :line-height 22}}
        (i18n/label :t/public-chat-description)]
       [chat-name-input topic error]]
      [react/view {:flex-direction :row :flex-wrap :wrap :margin-top 24 :padding-horizontal 12}
       (for [chat default-public-chats]
         (render-topic chat))]]]))
