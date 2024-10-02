(ns status-im.contexts.chat.messenger.composer.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.chat.messenger.composer.actions.view :as actions]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.edit.view :as edit]
    [status-im.contexts.chat.messenger.composer.handlers :as handler]
    [status-im.contexts.chat.messenger.composer.images.view :as images]
    [status-im.contexts.chat.messenger.composer.link-preview.view :as link-preview]
    [status-im.contexts.chat.messenger.composer.mentions.view :as mentions]
    [status-im.contexts.chat.messenger.composer.reply.view :as reply]
    [status-im.contexts.chat.messenger.composer.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn input
  [_ _]
  (let [default-value (:input-text (rf/sub [:chats/current-chat-input]))]
    (fn [set-ref theme]
      [rn/text-input
       {:ref                      set-ref
        :on-change-text           handler/change-text
        :keyboard-appearance      theme
        :max-font-size-multiplier 1
        :multiline                true
        :placeholder              (i18n/label :t/type-something)
        :placeholder-text-color   (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
        :max-length               constants/max-text-size
        :accessibility-label      :chat-message-input
        :style                    (style/input-text theme)
        :default-value            default-value}])))

(defn view
  [layout-height]
  (let [theme     (quo.theme/use-theme)
        bottom    (safe-area/get-bottom)
        input-ref (rn/use-ref-atom nil)
        set-ref   (rn/use-callback (fn [value]
                                     (rf/dispatch [:chat/set-input-ref value])
                                     (reset! input-ref value)))]
    [rn/view {:style {:margin-bottom bottom}}
     [mentions/view layout-height]
     [quo/separator]
     [rn/view {:style {:padding-horizontal 20 :padding-top 20}}
      [:<>
       [reply/view input-ref]
       [edit/view input-ref]]
      [input set-ref theme]
      [:<>
       [link-preview/view]
       [images/images-list]]
      [actions/view input-ref]]]))
