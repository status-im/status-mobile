(ns status-im.ui.screens.help-center.views
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.list.item :as list.item]
    [status-im.ui.components.list.views :as list]
    [status-im.ui.components.react :as react]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]))

(def data
  [{:size :small
    :title (i18n/label :t/faq)
    :accessibility-label :faq-button
    :on-press
    #(.openURL ^js react/linking constants/faq)
    :chevron true}
   {:size :small
    :title (i18n/label :t/glossary)
    :accessibility-label :glossary-button
    :on-press
    #(re-frame/dispatch [:navigate-to :glossary])
    :chevron true}
   {:size :small
    :title (i18n/label :t/submit-bug)
    :accessibility-label :submit-bug-button
    :on-press
    #(re-frame/dispatch [:open-modal :bug-report])
    :chevron true}])

(defn help-center
  []
  [list/flat-list
   {:data      data
    :key-fn    (fn [_ i] (str i))
    :render-fn list.item/list-item}])
