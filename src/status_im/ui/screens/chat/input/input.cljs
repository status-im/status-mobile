(ns status-im.ui.screens.chat.input.input
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.styles.input.input :as style]
            [status-im.ui.screens.chat.styles.message.message :as message-style]
            [status-im.ui.screens.chat.input.send-button :as send-button]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.utils :as chat-utils]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.utils.platform :as platform]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.extensions.views :as extensions]))

(defview basic-text-input [{:keys [set-container-width-fn height single-line-input?]}]
  (letsubs [input-text           [:chats/current-chat-input-text]
            cooldown-enabled?    [:chats/cooldown-enabled?]]
    [react/text-input
     (merge
      {:ref                    #(when % (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-ref %}]))
       :accessibility-label    :chat-message-input
       :multiline              (not single-line-input?)
       :default-value          (or input-text "")
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :on-focus               #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused?    true
                                                                                :input-bottom-sheet nil
                                                                                :messages-focused? false}])
       :on-blur                #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused? false}])
       :on-submit-editing      #(when single-line-input?
                                  (re-frame/dispatch [:chat.ui/send-current-message]))
       :on-layout              #(set-container-width-fn (.-width (.-layout (.-nativeEvent %))))
       :on-change              #(re-frame/dispatch [:chat.ui/set-chat-input-text (.-text (.-nativeEvent %))])
       :on-selection-change    #(let [s (-> (.-nativeEvent %)
                                            (.-selection))
                                      end (.-end s)]
                                  (re-frame/dispatch [:chat.ui/set-chat-ui-props {:selection end}]))
       :style                  (style/input-view single-line-input?)
       :placeholder-text-color colors/gray
       :auto-capitalize        :sentences}
      (when cooldown-enabled?
        {:placeholder (i18n/label :cooldown/text-input-disabled)}))]))

(defview basic-text-input-desktop [{:keys [set-container-width-fn height single-line-input? set-text state-text]}]
  (letsubs [inp-ref       (atom nil)
            cooldown-enabled?    [:chats/cooldown-enabled?]]
    [react/text-input
     (merge
      {:ref                    #(when % (do
                                          (reset! inp-ref %)
                                          (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-ref %}])))
       :accessibility-label    :chat-message-input
       :multiline              (not single-line-input?)
       :default-value          @state-text
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :on-focus               #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused?    true
                                                                                :input-bottom-sheet nil
                                                                                :messages-focused? false}])
       :on-blur                #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused? false}])
       :submit-shortcut        {:key "Enter"}
       :on-submit-editing      #(do
                                  (.clear @inp-ref)
                                  (.focus @inp-ref)
                                  (re-frame/dispatch [:chat.ui/set-chat-input-text @state-text])
                                  (re-frame/dispatch [:chat.ui/send-current-message])
                                  (set-text ""))
       :on-layout              #(set-container-width-fn (.-width (.-layout (.-nativeEvent %))))
       :on-change              #(do
                                  (set-text (.-text (.-nativeEvent %))))
       :on-end-editing         #(re-frame/dispatch [:chat.ui/set-chat-input-text @state-text])
       :on-selection-change    #(let [s (-> (.-nativeEvent %)
                                            (.-selection))
                                      end (.-end s)]
                                  (re-frame/dispatch [:chat.ui/set-chat-ui-props {:selection end}]))
       :style                  (style/input-view single-line-input?)
       :placeholder-text-color colors/gray
       :auto-capitalize        :sentences}
      (when cooldown-enabled?
        {:placeholder (i18n/label :cooldown/text-input-disabled)}))]))

(defview invisible-input [{:keys [set-layout-width-fn value]}]
  (letsubs [input-text    [:chats/current-chat-input-text]]
    [react/text {:style     style/invisible-input-text
                 :on-layout #(let [w (-> (.-nativeEvent %)
                                         (.-layout)
                                         (.-width))]
                               (set-layout-width-fn w))}
     (or input-text "")]))

(defn get-options [type]
  (case (keyword type)
    :phone {:keyboard-type "phone-pad"}
    :password {:secure-text-entry true}
    :number {:keyboard-type "numeric"}
    nil))

(defview input-view [{:keys [single-line-input? set-text state-text]}]
  (let [component              (reagent/current-component)
        set-layout-width-fn    #(reagent/set-state component {:width %})
        set-container-width-fn #(reagent/set-state component {:container-width %})
        {:keys [width]} (reagent/state component)]
    [react/view {:style style/input-root}
     [react/animated-view {:style style/input-animated}
      [invisible-input {:set-layout-width-fn set-layout-width-fn}]
      (if platform/desktop?
        [basic-text-input-desktop {:set-container-width-fn set-container-width-fn
                                   :single-line-input?     single-line-input?
                                   :set-text               set-text
                                   :state-text             state-text}]
        [basic-text-input {:set-container-width-fn set-container-width-fn
                           :single-line-input?     single-line-input?}])]]))

(defview reply-message [from alias message-text]
  (letsubs [{:keys [ens-name]} [:contacts/contact-name-by-identity from]
            current-public-key [:multiaccount/public-key]]
    [react/scroll-view {:style style/reply-message-content}
     [react/view {:style style/reply-message-to-container}
      [vector-icons/tiny-icon :tiny-icons/tiny-reply {:container-style style/reply-icon
                                                      :accessibility-label :tiny-reply-icon
                                                      :width 20
                                                      :color colors/gray}]
      (chat-utils/format-reply-author from alias ens-name current-public-key style/reply-message-author)]
     [react/text {:style (assoc (message-style/style-message-text false) :font-size 14) :number-of-lines 3} message-text]]))

(defview reply-message-view []
  (letsubs [{:keys [content from alias] :as message} [:chats/reply-message]]
    (when message
      [react/view {:style style/reply-message-container}
       [react/view {:style style/reply-message}
        [photos/member-photo from]
        [reply-message from alias (:text content)]
        [react/touchable-highlight
         {:style               style/cancel-reply-highlight
          :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
          :accessibility-label :cancel-message-reply}
         [react/view {:style style/cancel-reply-container}
          [vector-icons/icon :main-icons/close {:container-style style/cancel-reply-icon
                                                :width 19
                                                :height 19
                                                :color           colors/white}]]]]])))

(defview container []
  (letsubs [margin               [:chats/input-margin]
            mainnet?             [:mainnet?]
            input-text           [:chats/current-chat-input-text]
            result-box           [:chats/current-chat-ui-prop :result-box]
            input-bottom-sheet   [:chats/current-chat-ui-prop :input-bottom-sheet]
            state-text           (reagent/atom "")]
    {:component-will-unmount #(when platform/desktop?
                                (re-frame/dispatch [:chat.ui/set-chat-input-text @state-text]))

     :component-did-mount    #(when-not (string/blank? input-text) (reset! state-text input-text))}
    (let [single-line-input? (:singleLineInput result-box)
          set-text           #(reset! state-text %)
          input-text-empty? (if platform/desktop?
                              (string/blank? state-text)
                              (string/blank? input-text))]
      [react/view {:style     (style/root margin)
                   :on-layout #(let [h (-> (.-nativeEvent %)
                                           (.-layout)
                                           (.-height))]
                                 (when (> h 0)
                                   (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-height h}])))}
       [reply-message-view]
       [react/view {:style style/input-container}
        [input-view {:single-line-input? single-line-input? :set-text set-text :state-text state-text}]
        (when (and input-text-empty? mainnet?)
          [stickers/button (= :stickers input-bottom-sheet)])
        (when (and input-text-empty?) ;;TODO show only for 1-1 chats?
          [extensions/button (= :extensions input-bottom-sheet)])
        (when-not input-text-empty?
          (if platform/desktop?
            [send-button/send-button-view {:input-text @state-text}
             #(do
                (re-frame/dispatch [:chat.ui/set-chat-input-text @state-text])
                (re-frame/dispatch [:chat.ui/send-current-message])
                (set-text ""))]
            [send-button/send-button-view {:input-text input-text}
             #(re-frame/dispatch [:chat.ui/send-current-message])]))]])))
