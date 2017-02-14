(ns status-im.chat.views.new-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [status-im.components.react :refer [view
                                        scroll-view]]
    [status-im.chat.views.message-input :refer [plain-message-input-view]]
    [status-im.chat.constants :refer [input-height]]
    [status-im.utils.platform :refer [platform-specific]]
    [status-im.chat.styles.message :as st]))

(defn get-height [event]
  (.-height (.-layout (.-nativeEvent event))))

(defview chat-message-input-view []
  [margin [:input-margin]
   command? [:command?]
   response-height [:response-height]
   suggestions [:get-suggestions]
   message-input-height [:get-message-input-view-height]]
  (let [on-top? (or (and (seq suggestions) (not command?))
                    (not= response-height input-height))
        style   (get-in platform-specific [:component-styles :chat :new-message])]
    [view {:style     (merge (st/new-message-container margin on-top?) style)
           :on-layout (fn [event]
                        (let [height (get-height event)]
                          (when (not= height message-input-height)
                            (dispatch [:set-message-input-view-height height]))))}
     [plain-message-input-view]]))
