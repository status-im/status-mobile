(ns status-im.ui.screens.desktop.main.buidl.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.vector-icons :as icons]
            [clojure.string :as string]
            [status-im.ui.screens.chat.styles.message.message :as message.style]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.utils.gfycat.core :as gfycat.core]
            [taoensso.timbre :as log]
            [reagent.core :as reagent]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as constants]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.datetime :as time]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.message.datemark :as message.datemark]
            [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.desktop.main.chat.styles :as styles]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.desktop.main.chat.events :as chat.events]))

(views/defview toolbar-chat-view [{:keys [chat-id color public-key public? group-chat]
                                   :as current-chat}]
  (views/letsubs [chat-name         [:get-current-chat-name]
                  {:keys [pending? whisper-identity photo-path]} [:get-current-chat-contact]]
    [react/view {:style styles/toolbar-chat-view}
     [react/view {:style {:flex-direction :row
                          :flex 1}}
      (if public?
        [react/view {:style (styles/topic-image color)}
         [react/text {:style styles/topic-text}
          (string/capitalize (second chat-name))]]
        [react/image {:style styles/chat-icon
                      :source {:uri photo-path}}])
      [react/view {:style (styles/chat-title-and-type pending?)}
       [react/text {:style styles/chat-title
                    :font  :medium}
        chat-name]
       (cond pending?
             [react/text {:style styles/add-contact-text
                          :on-press #(re-frame/dispatch [:add-contact whisper-identity])}
              (i18n/label :t/add-to-contacts)]
             public?
             [react/text {:style styles/public-chat-text}
              (i18n/label :t/public-chat)])]]
     [react/view
      (when (and (not group-chat) (not public?))
        [react/text {:style (styles/profile-actions-text colors/black)
                     :on-press #(re-frame/dispatch [:show-profile-desktop whisper-identity])}
         (i18n/label :t/view-profile)])
      [react/text {:style (styles/profile-actions-text colors/black)
                   :on-press #(re-frame/dispatch [:chat.ui/clear-history-pressed])}
       (i18n/label :t/clear-history)]
      [react/text {:style (styles/profile-actions-text colors/black)
                   :on-press #(re-frame/dispatch [:chat.ui/remove-chat-pressed chat-id])}
       (i18n/label :t/delete-chat)]]]))

(defn tag-view [tag {:keys [on-press]}]
  [react/touchable-highlight {:style {:border-radius 5
                                      :margin 2
                                      :padding 4
                                      :height 23
                                      :background-color (if (= tag "clear filter")
                                                          colors/red
                                                          colors/blue-dark)
                                      :justify-content :center
                                      :align-items :center
                                      :align-content :center}
                              :on-press on-press}
   [react/text {:style {:font-size 10
                        :color colors/white}
                :font :medium} tag]])

(views/defview issue-view [{:keys [id title content tags]}]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/start-public-chat (str "status-buidl-test-issue-" id)])}
   [react/view {:style {:flex-direction :column
                        :margin-horizontal 2
                        :margin-vertical 5
                        :padding 2
                        :border-radius 5
                        :background-color colors/white}}
    [react/view {:flex-direction :row
                 :style {:padding 4}}
     [react/text {:style {:font-size 12
                          :margin-right 10}
                  :font :medium}
      title]
     [react/text {:style {:font-size 12
                          :color colors/gray}}
      (str "#" id)]]
    [react/view {:flex-direction :row
                 :flex-wrap :wrap}
     [react/view {:style {:margin 2
                          :padding 4
                          :justify-content :center
                          :align-items :center
                          :align-content :center}}
      [react/text {:style {:font-size 10}}
       content]]
     (doall
      (for [tag tags]
        ^{:key tag} [tag-view tag {:on-press #(re-frame/dispatch [:chat.ui/start-public-chat (str "status-buidl-test-tag-" tag)])}]))]]])

(views/defview buidl-text-input [field]
  [react/view {:stlye {:flex 1
                       :flex-direction    :row}}
   [react/text {:style {:font-size 18
                        :text-align :center}
                :font :medium}
    (str "#" (string/capitalize (name field)))]
   [react/text-input {:placeholder "Type here..."
                      :auto-focus             true
                      :multiline              true
                      :blur-on-submit         true
                      :style {:color :black
                              :height            200
                              :margin-left       20
                              :margin-right      22}
                      :on-key-press           (fn [e]
                                                (let [native-event (.-nativeEvent e)
                                                      key          (.-key native-event)
                                                      modifiers    (js->clj (.-modifiers native-event))
                                                      should-send  (and (= key "Enter") (not (contains? (set modifiers) "shift")))]
                                                  (when should-send
                                                    (re-frame/dispatch [:buidl/next-step]))))
                      :on-change              (fn [e]
                                                (let [native-event (.-nativeEvent e)
                                                      text         (.-text native-event)]
                                                  (re-frame/dispatch [:buidl/set-issue-input-field field text])))}]])

(defn new-issue-title []
  [react/view {:style {:flex-direction :column
                       :margin 5
                       :border-radius 5
                       :background-color colors/white}}
   [buidl-text-input :title]])

(defn new-issue-content []
  [react/view {:style {:flex-direction :column
                       :margin 5
                       :border-radius 5
                       :background-color colors/white}}
   [buidl-text-input :content]])

(views/defview new-issue-tags []
  (views/letsubs [tags [:buidl.new-issue.ui/tags]
                  available-tags [:buidl.new-issue.ui/available-tags]]
    [react/view {:style {:flex-direction :column
                         :margin 5
                         :border-radius 5
                         :background-color colors/white}}
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (doall
       (for [tag tags]
         ^{:key tag} [tag-view tag {:on-press #(re-frame/dispatch [:buidl/add-tag tag])}]))]
     [buidl-text-input :tag]
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (doall
       (for [tag available-tags]
         ^{:key tag} [tag-view tag {:on-press #(re-frame/dispatch [:buidl/add-tag tag])}]))]]))

(views/defview new-issue [step]
  (case step
    :title [new-issue-title]
    :content [new-issue-content]
    :tags [new-issue-tags]
    nil))

(views/defview new-issue-button [step]
  (let [action (case step
                 nil #(re-frame/dispatch [:buidl/new-issue])
                 :tags #(re-frame/dispatch [:buidl/create-issue])
                 #(re-frame/dispatch [:buidl/next-step]))
        label (case step
                nil "New issue"
                :tags "Create issue"
                "Next step")]
    [react/touchable-highlight {:style {:height           34
                                        :width            34
                                        :justify-content  :center
                                        :align-items      :center
                                        :align-self       :center}
                                :on-press action}
     [react/view {:style {:border-radius 4
                          :background-color colors/blue-dark
                          :width 120
                          :height 30
                          :justify-content :center
                          :align-items :center
                          :align-content :center}}
      [react/text {:style {:color colors/white
                           :font-size 14}}
       label]]]))

(defn tag-filter-input [tag-filter tag-filter-ref]
  [react/text-input {:placeholder "Type here to filter tags..."
                     :auto-focus             true
                     :blur-on-submit         true
                     :style {:color :black
                             :height            30
                             :margin-left       20
                             :margin-right      22}
                     :ref #(reset! tag-filter-ref %)
                     :default-value tag-filter
                     :on-change (fn [e]
                                  (let [native-event (.-nativeEvent e)
                                        text         (.-text native-event)]
                                    (re-frame/dispatch [:buidl/tag-filter-changed text])))}])

(views/defview buidl-view []
  (views/letsubs [{:keys [input-text chat-id] :as current-chat} [:get-current-chat]
                  issues [:buidl/get-filtered-issues]
                  tag-filter   [:buidl/get-tag-filter]
                  tag-filter-ref (atom nil)
                  tags   [:buidl/get-filtered-tags]
                  {:keys [step]} [:buidl.ui/new-issue]]
    [react/view {:style {:flex             1
                         :flex-direction :column}}
     [toolbar-chat-view current-chat]
     [tag-filter-input tag-filter tag-filter-ref]
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (when (not-empty tag-filter)
        [tag-view "clear filter" {:on-press #(do (.clear @tag-filter-ref)
                                                 (.focus @tag-filter-ref)
                                                 (re-frame/dispatch [:buidl/tag-filter-changed ""]))}])
      (doall
       (for [tag  tags]
         ^{:key tag} [tag-view tag {:on-press #(do (.setNativeProps @tag-filter-ref #js {:text tag})
                                                   (re-frame/dispatch [:buidl/tag-filter-changed tag]))}]))]
     [react/view {:style {:flex 1
                          :padding-bottom 50
                          :background-color colors/gray-lighter}}
      [react/scroll-view {:style {:overflow :hidden
                                  :flex-direction :column}}
       [react/view {:style {:flex-direction :column}}
        (doall
         (for [{:keys [id] :as issue}  issues]
           ^{:key id} [issue-view issue]))]]]
     [react/view {:style {:flex-direction :column
                          :background-color colors/gray-lighter}}
      [new-issue step]
      [new-issue-button step]]]))
