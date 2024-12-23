(ns status-im.contexts.chat.messenger.messages.content.lightbox.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.lightbox.constants :as constants]
    [status-im.contexts.chat.messenger.messages.content.lightbox.style :as style]
    [status-im.contexts.chat.messenger.messages.content.text.view :as message-view]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn bottom-text-for-lightbox
  [_]
  (let [text-height (reagent/atom 0)]
    (fn [{:keys [content chat-id] :as _message}]
      (let [expandable-text? (> @text-height (* constants/line-height 2))]
        [message-view/render-parsed-text
         {:content        content
          :chat-id        chat-id
          :style-override (style/bottom-text expandable-text?)
          :on-layout      #(reset! text-height (oops/oget % "nativeEvent.layout.height"))}]))))

(defn drawer
  [images index]
  (let [{:keys [image]} (nth images index)
        uri             (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/save
        :accessibility-label :save-image
        :label               (i18n/label :t/save-image-library)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch
                                [:lightbox/save-image-to-gallery
                                 uri
                                 #(rf/dispatch [:toasts/upsert
                                                {:id   :random-id
                                                 :type :positive
                                                 :text (i18n/label :t/photo-saved)}])]))}]]]))
