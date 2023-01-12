(ns status-im2.contexts.chat.messages.link-preview.view
  (:require
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

(defn link-preview [{:keys [content] :as message-data}]
  (let [ask-user? (rf/sub [:link-preview/link-preview-request-enabled])
        whitelist (rf/sub [:link-previews-whitelist])
        enabled-sites (rf/sub [:link-preview/enabled-sites])])
  (when (:links content)
    [rn/view
     [rn/text "Test"]]))