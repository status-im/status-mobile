(ns status-im.ui.screens.help-center.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.topbar :as topbar]))

(def data
  [{:type                :small
    :title               :t/faq
    :accessibility-label :faq-button
    :on-press
    #(.openURL ^js react/linking
               (if platform/desktop?
                 "https://status.im/docs/FAQ-desktop.html"
                 "https://status.im/docs/FAQs.html"))
    :accessories         [:chevron]}
   {:type                :small
    :title               :t/glossary
    :accessibility-label :glossary-button
    :on-press
    #(re-frame/dispatch [:navigate-to :glossary])
    :accessories         [:chevron]}
   {:type                :small
    :title               :t/submit-bug
    :accessibility-label :submit-bug-button
    :on-press
    #(re-frame/dispatch [:logging.ui/send-logs-pressed])
    :accessories         [:chevron]}
   {:type                :small
    :title               :t/request-feature
    :accessibility-label :request-a-feature-button
    :on-press
    #(re-frame/dispatch [:chat.ui/start-public-chat
                         (if platform/desktop?
                           "status-desktop"
                           "status")
                         {:navigation-reset? false}])
    :accessories         [:chevron]}])

(defn help-center []
  [react/view {:flex 1 :background-color colors/white}
   [topbar/topbar {:title :t/need-help}]
   [list/flat-list
    {:data      data
     :key-fn    (fn [_ i] (str i))
     :render-fn list/flat-list-generic-render-fn}]])
