(ns status-im.ui.screens.help-center.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im2.setup.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]))

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
    #(re-frame/dispatch [:open-modal :bug-report])
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/request-feature)
    :accessibility-label :request-a-feature-button
    :on-press
    #(re-frame/dispatch [:chat.ui/start-public-chat
                         "support"])
    :chevron             true}])

(defn help-center
  []
  [list/flat-list
   {:data      data
    :key-fn    (fn [_ i] (str i))
    :render-fn quo/list-item}])
