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
     #_[react/view
        [react/popup-menu
         [react/popup-menu-trigger {:text "Popup test"}]
         [react/popup-menu-options
          [react/popup-menu-option {:text "First"}]
          [react/popup-menu-option {:text "Second"}]]]]
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

(views/defview buidl-text-input [field]
  (let [component               (reagent/current-component)
        set-container-height-fn #(reagent/set-state component {:container-height %})
        {:keys [container-height]} (reagent/state component)]
    [react/view {:style {:flex-direction    :row
                         :overflow          :hidden
                         :justify-content :center
                         :align-items :center
                         :align-content :center}}
     [react/text {:style {:font-size 15
                          :width 80}}
      (string/capitalize (str (name field) ":"))]
     [react/text-input {:placeholder (str "Type " (name field) "...")
                        :auto-focus             true
                        :multiline              true
                        :blur-on-submit         true
                        :style                  (styles/chat-text-input container-height)
                        :font                   :default
                        :on-content-size-change #(set-container-height-fn (.-height (.-contentSize (.-nativeEvent %))))
                        :on-change              (fn [e]
                                                  (let [native-event (.-nativeEvent e)
                                                        text         (.-text native-event)]
                                                    (re-frame/dispatch [:buidl/set-issue-input-field field text])))}]]))

(defn tag-view [tag]
  [react/touchable-highlight {:style {:border-radius 5
                                      :margin 2
                                      :width 100
                                      :height 40
                                      :background-color colors/blue-dark
                                      :justify-content :center
                                      :align-items :center
                                      :align-content :center
                                      :flex-direction :row}
                              :on-press #(re-frame/dispatch [:chat.ui/start-public-chat (str "status-buidl-test-tag-" tag)])}
   [react/text {:style {:font-size 10
                        :font-weight :bold
                        :color colors/white}} tag]])

(defn add-tag-view [tag]
  [react/touchable-highlight {:style {:border-radius 5
                                      :margin 2
                                      :width 100
                                      :height 40
                                      :background-color colors/blue-dark
                                      :justify-content :center
                                      :align-items :center
                                      :align-content :center
                                      :flex-direction :row}
                              :on-press #(re-frame/dispatch [:buidl/add-tag tag])}
   [react/text {:style {:font-size 10
                        :font-weight :bold
                        :color colors/white}} tag]])

(views/defview issue-view [{:keys [id title content tags]}]
  [react/view {:style {:flex-direction :row
                       :margin-horizontal 2
                       :margin-vertical 5
                       :padding 2
                       :border-radius 5
                       :background-color colors/white}}
   [react/touchable-highlight {:style {:flex 1}
                               :on-press #(re-frame/dispatch [:chat.ui/start-public-chat (str "status-buidl-test-issue-" id)])}
    [react/view
     [react/text {:style {:font-size 12
                          :font-weight :bold}}
      title]
     [react/text {:style {:font-size 10}}
      content]]]
   [react/view
    (doall
     (for [tag tags]
       ^{:key tag} [tag-view tag]))]])

(defn new-issue-title []
  [react/view {:style {:flex-direction :column}}
   [buidl-text-input :title]])

(defn new-issue-content []
  [react/view {:style {:flex-direction :column}}
   [buidl-text-input :content]])

(views/defview new-issue-tags []
  (views/letsubs [tags [:buidl.issue.ui/tags]
                  available-tags [:buidl.issue.ui/available-tags]]
    [react/view {:style {:flex-direction :column}}
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (doall
       (for [tag tags]
         ^{:key tag} [add-tag-view tag]))]
     [buidl-text-input :tag]
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (doall
       (for [tag available-tags]
         ^{:key tag} [add-tag-view tag]))]]))

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

(views/defview buidl-view []
  (views/letsubs [{:keys [input-text chat-id] :as current-chat} [:get-current-chat]
                  issues [:buidl/get-issues]
                  tags   [:buidl/get-tags]
                  {:keys [step]} [:buidl.ui/issue]]
    [react/view {:style {:flex             1
                         :flex-direction :column}}
     [toolbar-chat-view current-chat]
     [react/view {:style {:background-color colors/gray-lighter
                          :flex-direction :row
                          :flex-wrap :wrap}}
      (doall
       (for [tag  tags]
         ^{:key tag} [tag-view tag]))]
     [react/view {:style {:flex 1
                          :background-color colors/gray-lighter}}
      [react/scroll-view {:style {:overflow :hidden
                                  :flex-direction :column}}
       [react/view
        (doall
         (for [{:keys [id] :as issue}  issues]
           ^{:key id} [issue-view issue]))]]]
     [new-issue step]
     [new-issue-button step]]))
