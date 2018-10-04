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

(views/defview send-button [inp-ref input-text]
  (let [empty? (= "" input-text)]
    [react/touchable-highlight {:style    styles/send-button
                                :on-press (fn [e]
                                            (let [native-event (.-nativeEvent e)
                                                  text         (.-text native-event)]
                                              (re-frame/dispatch [:send-buidl-message {:tags #{(keyword @input-text)}
                                                                                       :issue 1
                                                                                       :github-issue 4212}]))
                                            (.clear @inp-ref)
                                            (.focus @inp-ref))}
     [react/view {:style (styles/send-icon empty?)}
      [icons/icon :icons/arrow-left {:style (styles/send-icon-arrow empty?)}]]]))

(views/defview buidl-text-input [chat-id]
  (views/letsubs [inp-ref (atom nil)
                  input-text (reagent/atom "")]
    {:should-component-update
     (fn [_ [_ old-chat-id] [_ new-chat-id]]
       ;; update component only when switch to another chat
       (not= old-chat-id new-chat-id))}
    (let [component               (reagent/current-component)
          set-container-height-fn #(reagent/set-state component {:container-height %})
          {:keys [container-height]} (reagent/state component)]
      [react/view {:style (styles/chat-box container-height)}
       [react/text-input {:placeholder            (i18n/label :t/type-a-message)
                          :auto-focus             true
                          :multiline              true
                          :blur-on-submit         true
                          :style                  (styles/chat-text-input container-height)
                          :font                   :default
                          :ref                    #(reset! inp-ref %)
                          :on-content-size-change #(set-container-height-fn (.-height (.-contentSize (.-nativeEvent %))))
                          :default-value          @input-text
                          :on-key-press           (fn [e]
                                                    (let [native-event (.-nativeEvent e)
                                                          key          (.-key native-event)
                                                          modifiers    (js->clj (.-modifiers native-event))
                                                          should-send  (and (= key "Enter") (not (contains? (set modifiers) "shift")))]
                                                      (when should-send
                                                        (re-frame/dispatch [:send-buidl-message {:tags #{(keyword @input-text)}
                                                                                                 :issue 1
                                                                                                 :github-issue 4212}])
                                                        (.clear @inp-ref)
                                                        (.focus @inp-ref))))
                          :on-change              (fn [e]
                                                    (let [native-event (.-nativeEvent e)
                                                          text         (.-text native-event)]
                                                      (reset! input-text text)))}]
       [send-button inp-ref input-text]])))

(defn tag-view [[tag value]]
  [react/view {:style {:border-radius 5
                       :padding 10
                       :margin 2
                       :height 200
                       :width 200
                       :background-color colors/blue-dark
                       :flex-direction :row
                       :flex 1}}
   [react/text {:style {:flex 1
                        :font-size 10
                        :font-weight :bold
                        :color colors/white}} tag]
   [react/text {:style {:flex 1
                        :font-size 8
                        :color colors/white
                        :text-align :right}} value]])

(views/defview buidl-view []
  (views/letsubs [{:keys [input-text chat-id] :as current-chat} [:get-current-chat]
                  messages [:buidl/get-messages]
                  tags [:buidl/get-tags]]
    [react/view {:style styles/chat-view}
     [toolbar-chat-view current-chat]
     #_[react/text  (pr-str messages)]
     [react/text (pr-str tags)]
     [react/view {:flex 1
                  :flex-direction :row
                  :flex-wrap :wrap}
      (doall
       (for [[index tag] (map-indexed vector tags)]
         ^{:key index} [tag-view tag]))]
     #_[buidl-text-input chat-id]]))
