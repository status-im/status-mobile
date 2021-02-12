(ns status-im.ui.screens.help-center.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.topbar :as topbar]
            [status-im.constants :as constants]))

(def data
  [{:size                :small
    :title               (i18n/label :t/faq)
    :accessibility-label :faq-button
    :on-press
    #(.openURL ^js react/linking
               constants/faq)
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/glossary)
    :accessibility-label :glossary-button
    :on-press
    #(re-frame/dispatch [:navigate-to :glossary])
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/submit-bug)
    :accessibility-label :submit-bug-button
    :on-press
    #(re-frame/dispatch [:logging.ui/send-logs-pressed])
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/request-feature)
    :accessibility-label :request-a-feature-button
    :on-press
    #(re-frame/dispatch [:chat.ui/start-public-chat
                         "status"
                         {:navigation-reset? false}])
    :chevron             true}])

(defn help-center []
  [react/view {:flex 1 :background-color colors/white}
   [topbar/topbar {:title (i18n/label :t/need-help)}]
   [list/flat-list
    {:data      data
     :key-fn    (fn [_ i] (str i))
     :render-fn quo/list-item}]])
