(ns status-im.contexts.chat.messenger.messages.scroll-to-bottom.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.chat.messenger.messages.scroll-to-bottom.style :as style]
    [utils.re-frame :as rf]
    [utils.worklets.chat.messenger.composer :as worklets]))

(defn button
  [chat-list-scroll-y]
  (let [{window-height :height}    (rn/get-window)
        scroll-down-button-opacity (worklets/scroll-down-button-opacity
                                    chat-list-scroll-y
                                    false
                                    window-height)]
    [rn/view {:style style/shell-button-container}
     [quo/floating-shell-button
      {:scroll-to-bottom {:on-press #(rf/dispatch [:chat.ui/scroll-to-bottom])}}
      style/scroll-to-bottom-button
      scroll-down-button-opacity]]))
